package io.konig.transform.bigquery;

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
