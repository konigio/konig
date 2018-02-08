package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class CreateOracleTableStatement extends AbstractPrettyPrintable {

	private SqlTable table;

	public CreateOracleTableStatement(SqlTable table) {
		this.table = table;
	}
	

	public SqlTable getTable() {
		return table;
	}


	@Override
	public void print(PrettyPrintWriter out) {
		out.print("DECLARE");
		out.println(" v_count INTEGER;");
		out.println(" BEGIN");
		out.println(" SELECT count(1) INTO v_count FROM USER_TABLES WHERE table_name='"+table.getTableName().toUpperCase()+"';");
		out.println(" IF v_count = 0 THEN");
		out.println(" EXECUTE IMMEDIATE '");
		out.print("CREATE TABLE ");
		out.print(" ");
		table.print(out);
		out.print(" '");
		out.println(" END IF;");
		out.println(" END;");
		
		
	}
	

}

