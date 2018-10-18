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

public class ForeignKeyConstraint extends AbstractPrettyPrintable {
	private String localColumnName;
	private String foreignTableName;
	private String foreignColumnName;
	
	

	public ForeignKeyConstraint(String localColumnName, String foreignTableName, String foreignColumnName) {
		this.localColumnName = localColumnName;
		this.foreignTableName = foreignTableName;
		this.foreignColumnName = foreignColumnName;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print("FOREIGN KEY (");
		out.print(SqlKeyword.quote(localColumnName));
		out.println(")");
		out.pushIndent();
		out.indent();
		out.print("REFERENCES ");
		out.print(SqlKeyword.quote(foreignTableName));
		out.print('(');
		out.print(SqlKeyword.quote(foreignColumnName));
		out.print(')');
		out.popIndent();
		
	}

}
