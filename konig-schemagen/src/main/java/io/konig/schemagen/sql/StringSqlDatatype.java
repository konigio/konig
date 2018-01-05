package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
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


import io.konig.core.io.PrettyPrintWriter;

public class StringSqlDatatype extends FacetedSqlDatatype {
	
	private int maxLength;


	public StringSqlDatatype(SqlDatatype datatype, Integer maxLength) {
		super(datatype);
		this.maxLength = maxLength;
	}



	public int getMaxLength() {
		return maxLength;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		super.print(out);
		out.print('(');
		out.print(maxLength);
		out.print(')');

	}

}