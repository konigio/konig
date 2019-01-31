package io.konig.transform.showl.sql;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.showl.NodeNamer;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlJoinCondition;
import io.konig.core.showl.ShowlMapping;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlTemplatePropertyShape;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;
import io.konig.sql.query.AliasExpression;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.SelectExpression;
import io.konig.sql.query.SqlFunctionExpression;
import io.konig.sql.query.StringLiteralExpression;
import io.konig.sql.query.TableAliasExpression;
import io.konig.sql.query.TableNameExpression;
import io.konig.sql.query.ValueExpression;

public class ShowlSqlTransform {

	public ShowlSqlTransform() {
	}
	
	public InsertStatement createInsert(ShowlNodeShape targetNode, Class<? extends TableDataSource> datasourceType) throws ShowlSqlTransformException  {
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
				select.add(value);
			}
			addFrom(targetNode, select);
			return select;
		}

		private void addFrom(ShowlNodeShape targetNode, SelectExpression select) throws ShowlSqlTransformException {
			for (ShowlJoinCondition join : targetNode.getSelectedJoins()) {
				
				ShowlNodeShape nodeShape = join.otherNode(targetNode);
				TableNameExpression tableName = tableName(nodeShape);
				TableAliasExpression alias = new TableAliasExpression(tableName, nodeNamer.varname(join));
				
				
				select.getFrom().add(alias);
			}
			
		}

		private ValueExpression mappedValue(ShowlDirectPropertyShape p) throws ShowlSqlTransformException {
			ShowlMapping m = p.getSelectedMapping();
			ShowlPropertyShape other = m.findOther(p);
			if (other instanceof ShowlTemplatePropertyShape) {
				return templateValue(m, (ShowlTemplatePropertyShape) other);
			}
			String tableAlias = nodeNamer.varname(m.getJoinCondition());
			
			String sourceColumnName = other.getPredicate().getLocalName();
			String targetColumnName = p.getPredicate().getLocalName();
			ColumnExpression column = new ColumnExpression(tableAlias + "." + sourceColumnName);
			if (!targetColumnName.equals(sourceColumnName)) {
				return new AliasExpression(column, targetColumnName);
			}
			
			return column;
		}

		private ValueExpression templateValue(ShowlMapping m, ShowlTemplatePropertyShape other) throws ShowlSqlTransformException {
			String tableAlias = nodeNamer.varname(m.getJoinCondition());
			IriTemplate template = other.getTemplate();
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
			String targetName = m.findOther(other).getPredicate().getLocalName();
			
			return new AliasExpression(func, targetName);
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
