package io.konig.transform.bigquery;

import io.konig.sql.query.BigQueryCommandLine;
import io.konig.sql.query.InsertStatement;
import io.konig.sql.query.UpdateExpression;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.ShapeRule;
import io.konig.transform.sql.factory.SqlFactory;

public class BigQueryCommandLineFactory {

	private SqlFactory sqlFactory;
	
	public BigQueryCommandLineFactory() {
		this(new SqlFactory());
	}
	
	public BigQueryCommandLineFactory(SqlFactory sqlFactory) {
		this.sqlFactory = sqlFactory;
	}
	
	public BigQueryCommandLine updateCommand(ShapeRule shapeRule) throws ShapeTransformException {
		BigQueryCommandLine cmd = null;
		
		UpdateExpression update = sqlFactory.updateExpression(shapeRule);
		if (update != null) {
			cmd = new BigQueryCommandLine();
			cmd.setDml(update);
			cmd.setUseLegacySql(false);
			cmd.setProjectId("{gcpProjectId}");
		}
		
		return cmd;
	}

	public BigQueryCommandLine insertCommand(ShapeRule shapeRule) throws ShapeTransformException {
		BigQueryCommandLine cmd = null;
		InsertStatement insert = sqlFactory.insertStatement(shapeRule);
		if (insert != null) {
		
			cmd = new BigQueryCommandLine();
			cmd.setProjectId("{gcpProjectId}");
			cmd.setDml(insert);
			cmd.setUseLegacySql(false);
		}
		return cmd;
	}

}
