package io.konig.sql.query;

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


import java.io.PrintWriter;

import io.konig.core.io.PrettyPrintWriter;

public class BigQueryCommandLine extends AbstractExpression {
	
	private String projectId;
	private String destinationTable;
	private boolean useLegacySql;
	private DmlExpression dml;
	
	

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getDestinationTable() {
		return destinationTable;
	}


	public void setDestinationTable(String destinationTable) {
		this.destinationTable = destinationTable;
	}

	public boolean isUseLegacySql() {
		return useLegacySql;
	}

	public void setUseLegacySql(boolean useLegacySql) {
		this.useLegacySql = useLegacySql;
	}

	public DmlExpression getDml() {
		return dml;
	}

	public void setDml(DmlExpression dml) {
		this.dml = dml;
	}

	public void print(PrintWriter out, String fileName) {
		
		out.print("bq query");
		if (projectId!= null) {
			out.print(" --project_id=");
			out.print(projectId);
		}
		if (destinationTable!=null) {
			out.print(" --destination_table=");
			out.print(destinationTable);
		}
		out.print(" --use_legacy_sql=");
		out.print(useLegacySql);
		out.print(" `cat ");
		out.print(fileName);
		out.print('`');
		out.print('\n');
		
	}
	
	@Override
	public void print(PrettyPrintWriter out) {
		boolean pretty = out.isPrettyPrint();
		out.setPrettyPrint(false);
		
		out.print("bq query");
		if (projectId!= null) {
			out.print(" --project_id=");
			out.print(projectId);
		}
		if (destinationTable!=null) {
			out.print(" --destination_table=");
			out.print(destinationTable);
		}
		out.print(" --use_legacy_sql=");
		out.print(useLegacySql);
		out.print(' ');
		out.print("$'");
		boolean escape = out.isEscapeSingleQuote();
		out.setEscapeSingleQuote(true);
		dml.print(out);
		out.setEscapeSingleQuote(escape);
		out.print("'");
		out.println();
		
		out.setPrettyPrint(pretty);
	}
	
	

	
}
