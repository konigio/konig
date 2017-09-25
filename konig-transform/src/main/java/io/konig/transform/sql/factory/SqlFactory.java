package io.konig.transform.sql.factory;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.OwlReasoner;
import io.konig.core.Path;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.TurtleElements;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.CastSpecification;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.ComparisonOperator;
import io.konig.sql.query.ComparisonPredicate;
import io.konig.sql.query.FromExpression;
import io.konig.sql.query.FunctionExpression;
import io.konig.sql.query.GroupByClause;
import io.konig.sql.query.GroupingElement;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.JoinExpression;
import io.konig.sql.query.NullValueExpression;
import io.konig.sql.query.OnExpression;
import io.konig.sql.query.QueryExpression;
import io.konig.sql.query.Result;
import io.konig.sql.query.SearchCondition;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SignedNumericLiteral;
import io.konig.sql.query.SimpleCase;
import io.konig.sql.query.SimpleWhenClause;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.StructExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableItemExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.UpdateExpression;
import io.konig.sql.query.UpdateItem;
import io.konig.sql.query.ValueContainer;
import io.konig.sql.query.ValueExpression;
import io.konig.transform.factory.TransformBuildException;
import io.konig.transform.rule.BinaryBooleanExpression;
import io.konig.transform.rule.BooleanExpression;
import io.konig.transform.rule.ContainerPropertyRule;
import io.konig.transform.rule.CopyIdRule;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.FormulaPropertyRule;
import io.konig.transform.rule.FormulaIdRule;
import io.konig.transform.rule.IdRule;
import io.konig.transform.rule.IriTemplateIdRule;
import io.konig.transform.rule.JoinStatement;
import io.konig.transform.rule.LiteralPropertyRule;
import io.konig.transform.rule.MapValueTransform;
import io.konig.transform.rule.NullPropertyRule;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RenamePropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.rule.TransformBinaryOperator;
import io.konig.transform.rule.ValueTransform;
import io.konig.transform.sql.query.TableName;

public class SqlFactory {
	private String idColumnName = "id";
	public InsertStatement insertStatement(ShapeRule shapeRule) throws TransformBuildException {
		Worker worker = new Worker();
		return worker.insertStatement(shapeRule);
	}
	
	public UpdateExpression updateExpression(ShapeRule shapeRule) throws TransformBuildException {
		Worker worker = new Worker();
		return worker.updateExpression(shapeRule);
	}
	
	public SelectExpression selectExpression(ShapeRule shapeRule) throws TransformBuildException {
		Worker worker = new Worker();
		return worker.selectExpression(shapeRule);
	}
	
	private class Worker implements VariableTableMap {
		private SqlFormulaFactory formulaFactory = new SqlFormulaFactory();
		private OwlReasoner reasoner = new OwlReasoner(new MemoryGraph());
		private Map<String, TableItemExpression> tableItemMap = new HashMap<>();
		private boolean useAlias;
		

		private SelectExpression selectExpression(ShapeRule shapeRule) throws TransformBuildException {
			SelectExpression select = new SelectExpression();
			addDataChannels(select.getFrom(), shapeRule);
			addColumns(select, shapeRule);
			addGroupBy(select, shapeRule);
			
			return select;
		}

		public InsertStatement insertStatement(ShapeRule shapeRule) throws TransformBuildException {
			InsertStatement insert = null;
			BigQueryTableReference tableRef = bigQueryTableRef(shapeRule);
			if (tableRef != null) {
				TableName tableName = tableName(tableRef, null);
				List<ColumnExpression> columnList = columnList(shapeRule);
				SelectExpression select = selectExpression(shapeRule);
				insert = new InsertStatement(tableName.getExpression(), columnList, select);
			}
			return insert;
		}
		
		private List<ColumnExpression> columnList(ShapeRule shapeRule) {
			List<ColumnExpression> list = new ArrayList<>();
			IdRule idRule = shapeRule.getIdRule();
			
			if (idRule != null) {
				list.add(new ColumnExpression(idColumnName));
			}
			
			for (PropertyRule p : shapeRule.getPropertyRules()) {
				String columnName = p.getPredicate().getLocalName();
				list.add(new ColumnExpression(columnName));
			}
			return list;
		}

		private void addGroupBy(SelectExpression select, ShapeRule shapeRule) throws TransformBuildException {
			
			GroupByClause clause = null;
			
			GroupingElement ge = shapeRule.getGroupingElement();
			if (ge != null) {
				clause = new GroupByClause(null);
				clause.add(ge);
			} else {
				for (PropertyRule p : shapeRule.getPropertyRules()) {
					if (p instanceof FormulaPropertyRule) {
						FormulaPropertyRule fpr = (FormulaPropertyRule) p;
						PropertyConstraint pc = fpr.getTargetProperty();
						if (Konig.dimension.equals(pc.getStereotype())) {
							if (clause == null) {
								clause = new GroupByClause(null);
							}
							QueryExpression qe = column(p);
							if (qe instanceof AliasExpression) {
								AliasExpression ae = (AliasExpression) qe;
								qe = ae.getExpression();
							}
							if (qe instanceof GroupingElement) {
								clause.add((GroupingElement)qe);
							} else {
								throw new TransformBuildException("Expected column to be a GroupingElement");
							}
						}
					}
				}
			}
			select.setGroupBy(clause);
			
			
		}
		
		private UpdateExpression updateExpression(ShapeRule shapeRule) throws TransformBuildException {
			
			UpdateExpression update = null;
			BigQueryTableReference tableRef = bigQueryTableRef(shapeRule);
			if (tableRef != null) {
				useAlias = true;
				update = new UpdateExpression();
				String fullName = fullTableName(tableRef);
				String targetTableAlias = shapeRule.getVariableNamer().next();
				TableNameExpression tableNameExpression = new TableNameExpression(fullName);
				TableItemExpression tableItem = new TableAliasExpression(tableNameExpression, targetTableAlias);
				
				update.setTable(tableItem);
				
				for (PropertyRule p : shapeRule.getPropertyRules()) {
					ColumnExpression left = new ColumnExpression(p.getPredicate().getLocalName());
					ValueExpression right = column(p, true);
					UpdateItem item = new UpdateItem(left, right);
					update.add(item);
				}
				
				addDataChannels(update.getFrom(), shapeRule);

				ValueExpression idRule = createIdRule(shapeRule);
				ValueExpression idColumn = column(targetTableAlias, idColumnName);
				
				SearchCondition search = new ComparisonPredicate(ComparisonOperator.EQUALS, idColumn, idRule);
				update.setWhere(search);
			}
			
			
			
			return update;
		}

		private ValueExpression column(String tableName, String columnName) {
			StringBuilder builder = new StringBuilder();
			builder.append(tableName);
			builder.append('.');
			builder.append(columnName);
			
			return new ColumnExpression(builder.toString());
		}

		private String fullTableName(BigQueryTableReference tableRef) {
			StringBuilder builder = new StringBuilder();
			builder.append(tableRef.getDatasetId());
			builder.append('.');
			builder.append(tableRef.getTableId());
			return builder.toString();
		}

		private BigQueryTableReference bigQueryTableRef(ShapeRule shapeRule) {
			Shape shape = shapeRule.getTargetShape();
			for (DataSource ds : shape.getShapeDataSource()) {
				if (ds.isA(Konig.GoogleBigQueryTable)) {
					GoogleBigQueryTable bigQuery = (GoogleBigQueryTable) ds;
					return bigQuery.getTableReference();
				}
			}
			return null;
		}
		
		private TableName tableName(BigQueryTableReference tableRef, String alias) {

			StringBuilder builder = new StringBuilder();
			builder.append(tableRef.getDatasetId());
			builder.append('.');
			builder.append(tableRef.getTableId());
			String tableName = builder.toString();
			return new TableName(tableName, alias);
		}

		private void addColumns(ValueContainer select, ShapeRule shapeRule) throws TransformBuildException {
			
			addIdColumn(select, shapeRule);
			
			List<PropertyRule> list = new ArrayList<>( shapeRule.getPropertyRules() );
			Collections.sort(list);
			
			for (PropertyRule p : list) {
				select.add(column(p));
			}
		}

		private void addIdColumn(ValueContainer select, ShapeRule shapeRule) throws TransformBuildException {
			ValueExpression e = createIdRule(shapeRule);
			if (e != null) {
				select.add(e);
			}
		}
		
		private ValueExpression createIdRule(ShapeRule shapeRule) throws TransformBuildException {
			ValueExpression result = null;
			IdRule idRule = shapeRule.getIdRule();
			if (idRule != null) {
				if (idRule instanceof CopyIdRule) {
					CopyIdRule copyRule = (CopyIdRule) idRule;
					DataChannel channel = copyRule.getDataChannel();
					TableItemExpression tableItem = simpleTableItem(channel);
					String columnName = SqlUtil.columnName(tableItem, Konig.id);
					
					result = new ColumnExpression(columnName);
					
				} else if (idRule instanceof IriTemplateIdRule) {
					result = createIriTemplateValue(shapeRule, (IriTemplateIdRule) idRule);
					
				} else if (idRule instanceof FormulaIdRule) {
					result = createFormulaIdValue(shapeRule, (FormulaIdRule)idRule);
					
				} else {
					throw new TransformBuildException("Unsupported IdRule " + idRule.getClass().getName());
				}
			}
			return result;
		}

		private ValueExpression createFormulaIdValue(ShapeRule shapeRule, FormulaIdRule idRule) throws TransformBuildException {
			PropertyConstraint p = new PropertyConstraint(Konig.id);
			p.setFormula(shapeRule.getTargetShape().getIriFormula());
			p.setMaxCount(1);
			p.setValueClass(shapeRule.getTargetShape().getTargetClass());
			SqlFormulaExchange exchange = SqlFormulaExchange.builder()
					.withShape(shapeRule.getTargetShape())
					.withProperty(p)
					.withTableMap(this)
					.build();
			ValueExpression ve = formulaFactory.formula(exchange);
			
			GroupingElement ge = exchange.getGroupingElement();
			if (ge != null) {
				shapeRule.setGroupingElement(ge);
			}

			return new AliasExpression(ve, idColumnName);
		}

		private ValueExpression createIriTemplateValue(ShapeRule shapeRule, IriTemplateIdRule idRule) throws TransformBuildException {
			FunctionExpression func = new FunctionExpression("CONCAT");
			Shape shape = idRule.getDataChannel().getShape();
			IriTemplate template = shape.getIriTemplate();
			Context context = template.getContext();
			TableItemExpression tableItem = simpleTableItem(idRule.getDataChannel());
			
			for (Element e : template.toList()) {
				String text = e.getText();
				switch (e.getType()) {
				case TEXT :
					func.addArg(new StringLiteralExpression(text));
					break;
					
					
				case VARIABLE :
					URI iri = new URIImpl(context.expandIRI(text));
					String localName = iri.getLocalName();
					if (localName.length()==0) {
						func.addArg(new StringLiteralExpression(iri.stringValue()));
					} else {
						PropertyConstraint p = shape.getPropertyConstraint(iri);
						if (p==null) {
							StringBuilder msg = new StringBuilder();
							msg.append("Shape ");
							msg.append(TurtleElements.resource(shape.getId()));
							msg.append(" has invalid IRI template <");
							msg.append(template.getText());
							msg.append(">. Property not found: ");
							msg.append(text);
							throw new TransformBuildException(msg.toString());
						}
						URI datatype = p.getDatatype();
						if (XMLSchema.STRING.equals(datatype)) {
							func.addArg(SqlUtil.columnExpression(tableItem, iri));
						} else {
							CastSpecification cast = new CastSpecification(SqlUtil.columnExpression(tableItem, iri), "STRING");
							func.addArg(cast);
						}
					}
					break;
				}
			}
			
			return func;
		}


		private ValueExpression column(PropertyRule p) throws TransformBuildException {
			return column(p, false);
		}

		private ValueExpression column(PropertyRule p, boolean suppressRename) throws TransformBuildException {

			DataChannel channel = p.getDataChannel();
			TableItemExpression tableItem = simpleTableItem(channel);
			URI predicate = p.getPredicate();
			if (p instanceof ExactMatchPropertyRule) {

				return SqlUtil.columnExpression(tableItem, predicate);
			}
			
			if (p instanceof RenamePropertyRule) {
				RenamePropertyRule renameRule = (RenamePropertyRule) p;
				ValueTransform vt = renameRule.getValueTransform();
				if (vt instanceof MapValueTransform) {
					ValueExpression caseStatement = mapValues(renameRule, (MapValueTransform) vt);
					if (suppressRename) {
						return caseStatement;
					}
					return new AliasExpression(caseStatement, predicate.getLocalName());
				}
				URI sourcePredicate = renameRule.getSourceProperty().getPredicate();
				int pathIndex = renameRule.getPathIndex();
				Path path = renameRule.getSourceProperty().getEquivalentPath();
				if (pathIndex == path.asList().size()-1) {
					ValueExpression column = SqlUtil.columnExpression(tableItem, sourcePredicate);
					if (suppressRename) {
						return column;
					}
					return new AliasExpression(column, predicate.getLocalName());
				}
				throw new TransformBuildException("Unable to map Path");
			}
			if (p instanceof ContainerPropertyRule) {
				ContainerPropertyRule containerRule = (ContainerPropertyRule) p;
				StructExpression struct = new StructExpression();
				ShapeRule nested = containerRule.getNestedRule();
				addColumns(struct, nested);
				
				return new AliasExpression(struct, predicate.getLocalName());
			}
			
			if (p instanceof LiteralPropertyRule) {
				LiteralPropertyRule lpr = (LiteralPropertyRule) p;
				ValueExpression value = valueExpression(lpr.getValue());
				
				return new AliasExpression(value, predicate.getLocalName());
			}
			
			if (p instanceof NullPropertyRule) {
				return new AliasExpression(new NullValueExpression(), predicate.getLocalName());
			}
			
			if (p instanceof FormulaPropertyRule) {
				FormulaPropertyRule fr = (FormulaPropertyRule) p;
				
				SqlFormulaExchange exchange = SqlFormulaExchange.builder()
						.withProperty(fr.getSourceProperty())
						.withSourceTable(tableItem)
						.withTableMap(this)
						.build();
				
				ValueExpression ve = formulaFactory.formula(exchange);
				
				AliasExpression ae = new AliasExpression(ve, predicate.getLocalName());
				
				return ae;
			}
			
			throw new TransformBuildException("Unsupported PropertyRule: " + p.getClass().getName());
		}

		

		private ValueExpression mapValues(RenamePropertyRule p, MapValueTransform vt) throws TransformBuildException {
		
			DataChannel channel = p.getDataChannel();
			URI predicate = p.getSourceProperty().getPredicate();
			TableItemExpression tableItem = simpleTableItem(channel);
			
			ValueExpression caseOperand = SqlUtil.columnExpression(tableItem, predicate);
			List<SimpleWhenClause> whenClauseList = new ArrayList<>();
			
			// For now, we assume that URI values map to localName strings.
			
			for (Entry<Value,Value> e : vt.getValueMap().entrySet()) {
				Value key = e.getKey();
				Value value = e.getValue();
				
				ValueExpression whenOperand = valueExpression(key);
				Result result = mappedValue(value);
				
				SimpleWhenClause when = new SimpleWhenClause(whenOperand, result);
				whenClauseList.add(when);
			}
			
			Result elseClause = null;
			
			return new SimpleCase(caseOperand, whenClauseList, elseClause);
		}

		private Result mappedValue(Value value) throws TransformBuildException {
			if (value instanceof URI) {
				URI iri = (URI) value;
				return new StringLiteralExpression(iri.getLocalName());
			}
			if (value instanceof Literal) {
				return valueExpression(value);
			}
			throw new TransformBuildException("Cannot map value " + value.stringValue());
		}

		private ValueExpression valueExpression(Value value) throws TransformBuildException {
			if (value instanceof Literal) {
				Literal literal = (Literal) value;
				URI type = literal.getDatatype();
				if (type != null) {
					if (reasoner.isRealNumber(type)) {
						return new SignedNumericLiteral(new Double(literal.doubleValue()));
					}
					if (reasoner.isIntegerDatatype(type)) {
						return new SignedNumericLiteral(new Long(literal.longValue()));
					}
					return new StringLiteralExpression(literal.stringValue());
				}
				
			}
			throw new TransformBuildException("Cannot convert to ValueExpression: " + value.stringValue());
		}

		private void addDataChannels(FromExpression from, ShapeRule shapeRule) throws TransformBuildException {
			List<DataChannel> channelList = shapeRule.getAllChannels();
			useAlias = channelList.size()>1;
			TableItemExpression item = null;
			for (DataChannel channel : channelList) {
				item = toTableItemExpression(channel);
			}
			if (item == null) {
				throw new TransformBuildException("No source tables found");
			}
			from.add(item);
			
			
		}
		
		/**
		 * Get the "simple" TableItem associated with a channel.  
		 * A simple TableItem is either a TableNameExpression or a TableAliasExpression.
		 */
		private TableItemExpression simpleTableItem(DataChannel channel) throws TransformBuildException {
			TableItemExpression e = toTableItemExpression(channel);
			if (e instanceof JoinExpression) {
				JoinExpression join = (JoinExpression) e;
				e = join.getRightTable();
			}
			return e;
		}

		private TableItemExpression toTableItemExpression(DataChannel channel) throws TransformBuildException {
			if (channel==null) {
				return null;
			}
			TableItemExpression tableItem = tableItemMap.get(channel.getName());
			
			if (tableItem == null) {
				DataSource datasource = channel.getDatasource();
				if (datasource == null) {
					StringBuilder msg = new StringBuilder();
					msg.append("Cannot create table item for ");
					msg.append(TurtleElements.resource(channel.getShape().getId()));
					msg.append(". Datasource is not specified.");
					throw new TransformBuildException(msg.toString());
				}
				if (!(datasource instanceof TableDataSource)) {

					StringBuilder msg = new StringBuilder();
					msg.append("Cannot create table item for ");
					msg.append(TurtleElements.resource(channel.getShape().getId()));
					msg.append(". Specified datasource is not an instance of TableDataSource");
					throw new TransformBuildException(msg.toString());
				}
				TableDataSource tableSource = (TableDataSource) datasource;
				String tableName = tableSource.getTableIdentifier();
				tableItem = new TableNameExpression(tableName);
				if (useAlias) {
					tableItem = new TableAliasExpression(tableItem, channel.getName());
				}
				
				JoinStatement join = channel.getJoinStatement();
				if (join != null) {
					TableItemExpression left = simpleTableItem(join.getLeft());
					TableItemExpression right = tableItem;
					OnExpression joinSpecification = onExpression(left, right, join);
					tableItem = new JoinExpression(left, right, joinSpecification);
				}

				tableItemMap.put(channel.getName(), tableItem);
				String varName = channel.getVariableName();
				if (varName != null) {
					tableItemMap.put(varName, tableItem);
				}
			}
			
			
			return tableItem;
		}

		private OnExpression onExpression(TableItemExpression left, TableItemExpression right, JoinStatement join) throws TransformBuildException {
			SearchCondition searchCondition = null;
			BooleanExpression condition = join.getCondition();
			if (condition instanceof BinaryBooleanExpression) {
				BinaryBooleanExpression binary = (BinaryBooleanExpression) condition;
				
				URI leftPredicate = binary.getLeftPredicate();
				URI rightPredicate = binary.getRightPredicate();
				
				ValueExpression leftValue = SqlUtil.columnExpression(left, leftPredicate);
				ValueExpression rightValue = SqlUtil.columnExpression(right, rightPredicate);
				ComparisonOperator comparisonOperator = comparisonOperator(binary.getOperator());
				
				
				searchCondition = new ComparisonPredicate(comparisonOperator, leftValue, rightValue);
				
				
			} else {
				throw new TransformBuildException("Unsupported BooleanExpression: " + condition.getClass().getSimpleName() );
			}
			
			
			return new OnExpression(searchCondition);
		}

		

		private ComparisonOperator comparisonOperator(TransformBinaryOperator operator) throws TransformBuildException {
			switch (operator) {
			case EQUAL :
				return ComparisonOperator.EQUALS;
				
			case NOT_EQUAL :
				return ComparisonOperator.NOT_EQUALS;
			}
			
			throw new TransformBuildException("Unsupported binary operator: " + operator);
		}

		@Override
		public TableItemExpression tableForVariable(String varName) {
			return tableItemMap.get(varName);
		}

	}
	
	

}
