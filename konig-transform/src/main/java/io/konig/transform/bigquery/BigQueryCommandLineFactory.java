package io.konig.transform.bigquery;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.ColumnExpression;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.SelectExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;
import io.konig.transform.sql.query.TableName;

public class BigQueryCommandLineFactory {

	private String idColumnName = "id";
	private SqlFactory sqlFactory;
	
	public BigQueryCommandLineFactory() {
		this(new SqlFactory());
	}
	
	public BigQueryCommandLineFactory(SqlFactory sqlFactory) {
		this.sqlFactory = sqlFactory;
	}

	public String getIdColumnName() {
		return idColumnName;
	}

	public void setIdColumnName(String idColumnName) {
		this.idColumnName = idColumnName;
	}

	public BigQueryCommandLine insertCommand(ShapeRule shapeRule) throws ShapeTransformException {
		BigQueryCommandLine cmd = null;
		BigQueryTableReference tableRef = currentStateTableRef(shapeRule);
		if (tableRef != null) {
			TableName tableName = tableName(tableRef, null);
			List<ColumnExpression> columnList = columnList(shapeRule);
			SelectExpression select = sqlFactory.selectExpression(shapeRule);
			InsertStatement insert = new InsertStatement(tableName.getExpression(), columnList, select);
			cmd = new BigQueryCommandLine();
			cmd.setProjectId(tableRef.getProjectId());
			cmd.setDml(insert);
			cmd.setUseLegacySql(false);
		}
		return cmd;
	}
	
	private TableName tableName(BigQueryTableReference tableRef, String alias) {

		StringBuilder builder = new StringBuilder();
		builder.append(tableRef.getDatasetId());
		builder.append('.');
		builder.append(tableRef.getTableId());
		String tableName = builder.toString();
		return new TableName(tableName, alias);
	}

	private SelectExpression insertSelect(BigQueryTableReference tableRef, ShapeRule shapeRule) {
		
		return null;
	}

	private List<ColumnExpression> columnList(ShapeRule shapeRule) {
		List<ColumnExpression> list = new ArrayList<>();
		if (shapeRule.getTargetShape().getNodeKind()==NodeKind.IRI) {
			list.add(new ColumnExpression(idColumnName));
		}
		for (PropertyRule p : shapeRule.getPropertyRules()) {
			String columnName = p.getPredicate().getLocalName();
			list.add(new ColumnExpression(columnName));
		}
		return list;
	}

	private BigQueryTableReference currentStateTableRef(ShapeRule shapeRule) {
		Shape shape = shapeRule.getTargetShape();
		for (DataSource ds : shape.getShapeDataSource()) {
			if (ds.isA(Konig.GoogleBigQueryTable) && ds.isA(Konig.CurrentState)) {
				GoogleBigQueryTable bigQuery = (GoogleBigQueryTable) ds;
				return bigQuery.getTableReference();
			}
		}
		return null;
	}
}
