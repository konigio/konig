package io.konig.transform.showl.sql;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.showl.NodeNamer;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlJoinCondition;
import io.konig.core.showl.ShowlMapping;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlSourceToSourceJoinCondition;
import io.konig.core.showl.ShowlTemplatePropertyShape;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.AndExpression;
import io.konig.sql.query.BooleanTerm;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.ValueExpression;

public class ShowlSqlTransform {
	private static final Logger logger = LoggerFactory.getLogger(ShowlSqlTransform.class);

	public ShowlSqlTransform() {
	}
	
	public InsertStatement createInsert(ShowlNodeShape targetNode, Class<? extends TableDataSource> datasourceType) throws ShowlSqlTransformException  {
		if (logger.isTraceEnabled()) {
			logger.trace("createInsert({})", targetNode.getPath());
		}
		Worker worker = new Worker();
		return worker.createInsert(targetNode, datasourceType);
	}

	private class Worker {
	
		private NodeNamer nodeNamer = new NodeNamer();
		private Class<? extends TableDataSource> datasourceType;
		
		public InsertStatement createInsert(ShowlNodeShape targetNode, Class<? extends TableDataSource> datasourceType) throws ShowlSqlTransformException  {
			this.datasourceType = datasourceType;
			
			TableNameExpression tableName = tableName(targetNode);
			List<ColumnExpression> columns = insertColumns(targetNode);
			SelectExpression selectQuery = selectInto(targetNode);
			InsertStatement insert = new InsertStatement(tableName, columns, selectQuery);
			
			return insert;
		}

		private SelectExpression selectInto(ShowlNodeShape targetNode) throws ShowlSqlTransformException {
			SelectExpression select = new SelectExpression();
			for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
				ValueExpression value = mappedValue(p);
				if (value != null) {
					select.add(value);
				}
			}
			addFrom(targetNode, select);
			return select;
		}

		private void addFrom(ShowlNodeShape targetNode, SelectExpression select) throws ShowlSqlTransformException {
			
			for (ShowlJoinCondition join : targetNode.getSelectedJoins()) {

				ShowlNodeShape focusNode = join.focusNode();
				TableItemExpression tableItem = tableAlias(focusNode);
				
				if (join instanceof ShowlSourceToSourceJoinCondition) {
					
					OnExpression on = onExpression(join);
					
					
					tableItem = new JoinExpression(tableItem, on);
				}
				select.getFrom().add(tableItem);
				
				
			}
			
		}

		private OnExpression onExpression(ShowlJoinCondition join) throws ShowlSqlTransformException {
			
			ShowlPropertyShape left = join.getLeft();
			ShowlPropertyShape right = join.getRight();
			
			if (left instanceof ShowlTemplatePropertyShape && right instanceof ShowlTemplatePropertyShape) {
				ShowlTemplatePropertyShape leftT = (ShowlTemplatePropertyShape) left;
				ShowlTemplatePropertyShape rightT = (ShowlTemplatePropertyShape) right;
				
				// We handle the special case where the templates are equal.
				// There are other special cases we should handle, such as the case
				// where the templates are the same except for variables that are equivalent
				// by mapping.  But that's too complicated for now.  
				
				if (leftT.getTemplate().equals(rightT.getTemplate())) {
					String leftTableAlias = nodeNamer.varname(left.getDeclaringShape());
					String rightTableAlias = nodeNamer.varname(right.getDeclaringShape());
					List<? extends ValueFormat.Element> elements = leftT.getTemplate().toList();
					BooleanTerm booleanTerm = null;
					AndExpression andExpression=null;
					
					for (ValueFormat.Element e : elements) {
						if (e.getType() == ElementType.VARIABLE) {
							String varName = e.getText();
							
							ColumnExpression leftCol = new ColumnExpression(leftTableAlias + "." + varName);
							ColumnExpression rightCol = new ColumnExpression(rightTableAlias + "." + varName);
							
							ComparisonPredicate compare = new ComparisonPredicate(
								ComparisonOperator.EQUALS, leftCol, rightCol);
							
							if (andExpression != null) {
								andExpression.add(compare);
							} else if (booleanTerm != null) {
								andExpression = new AndExpression();
								andExpression.add(booleanTerm);
								andExpression.add(compare);
								booleanTerm = andExpression;
							} else {
								booleanTerm = compare;
							}
							
						}
					}
					return new OnExpression(booleanTerm);
				} else {
					ValueExpression leftValue = templateValue(leftT);
					ValueExpression rightValue = templateValue(rightT);
					return new OnExpression(new ComparisonPredicate(
							ComparisonOperator.EQUALS,
							leftValue,
							rightValue));
				}
				
			}
			
			ComparisonPredicate compare = new ComparisonPredicate(
					ComparisonOperator.EQUALS,
					valueExpression(left),
					valueExpression(right));
				
			return new OnExpression(compare);
		}

		private ValueExpression valueExpression(ShowlPropertyShape p) {
			
			return qualifiedColumn(p);
		}

		private TableAliasExpression tableAlias(ShowlNodeShape node) throws ShowlSqlTransformException {
			TableNameExpression tableName = tableName(node);
			String alias = nodeNamer.varname(node);
			
			return new TableAliasExpression(tableName, alias);
		}

		private ValueExpression mappedValue(ShowlDirectPropertyShape p) throws ShowlSqlTransformException {
			ShowlMapping m = p.getSelectedMapping();
			if (m == null) {
				return null;
			}
			ShowlPropertyShape other = m.findOther(p);
			if (other instanceof ShowlTemplatePropertyShape) {
				return templateValue((ShowlTemplatePropertyShape) other);
			}
			
			
			String tableAlias = nodeNamer.varname(other.getDeclaringShape());
			
			String sourceColumnName = other.getPredicate().getLocalName();
			String targetColumnName = p.getPredicate().getLocalName();
			ColumnExpression column = new ColumnExpression(tableAlias + "." + sourceColumnName);
			if (!targetColumnName.equals(sourceColumnName)) {
				return new AliasExpression(column, targetColumnName);
			}
			
			return column;
		}

		

		private ValueExpression templateValue(ShowlTemplatePropertyShape showlTemplate) throws ShowlSqlTransformException {
			ShowlNodeShape node = showlTemplate.getDeclaringShape();
			String tableAlias = nodeNamer.varname(node);
			IriTemplate template = showlTemplate.getTemplate();
			SqlFunctionExpression func = new SqlFunctionExpression(SqlFunctionExpression.CONCAT);

			for (ValueFormat.Element e : template.toList()) {
				switch (e.getType()) {
				case TEXT:
					func.addArg(new StringLiteralExpression(e.getText()));
					break;
					
				case VARIABLE:
					String fieldName = e.getText();
					String fullName = tableAlias + "." + fieldName;
					
					ColumnExpression column = new ColumnExpression(fullName);
					func.addArg(column);
					break;
					
				}
			}
			String targetName = showlTemplate.getPredicate().getLocalName();
			
			return new AliasExpression(func, targetName);
		}
		
		private ColumnExpression qualifiedColumn(ShowlPropertyShape p) {
			String tableName = nodeNamer.varname(p.getDeclaringShape());
			String columnName = tableName + "." + p.getPredicate().getLocalName();
			return new ColumnExpression(columnName);
			
		}

		private List<ColumnExpression> insertColumns(ShowlNodeShape targetNode) {
			List<ColumnExpression> list = new ArrayList<>();
			for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
				String columnName = p.getPredicate().getLocalName();
				list.add(new ColumnExpression(columnName));
			}
			return list;
		}

		private TableNameExpression tableName(ShowlNodeShape targetNode) throws ShowlSqlTransformException {
			for (DataSource ds : targetNode.getShape().getShapeDataSource()) {
				if (datasourceType.isInstance(ds)) {
					TableDataSource table = (TableDataSource) ds;
					String tableName = table.getQualifiedTableName();
					return new TableNameExpression(tableName);
				}
				
			}
			throw new ShowlSqlTransformException(
					"Datasource of type " + datasourceType.getSimpleName() + " not found in Shape ");
		}
	}
}
