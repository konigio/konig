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

public class NumericSqlDatatype extends FacetedSqlDatatype {

	private boolean unsigned;
	private Integer precision;
	private Integer scale;

	public NumericSqlDatatype(SqlDatatype datatype, boolean unsigned) {
		super(datatype);
		this.unsigned = unsigned;
	}
	
	public NumericSqlDatatype(SqlDatatype datatype, Integer precision) {
		super(datatype);
		this.precision = precision;
	}
	
	public NumericSqlDatatype(SqlDatatype datatype, Integer precision, Integer scale) {
		super(datatype);
		this.precision = precision;
		this.scale = scale;
	}
	
	public boolean isUnsigned() {
		return unsigned;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		super.print(out);
		if (unsigned && isNull(precision) && isNull(scale)) {
			out.print(" UNSIGNED");
		}
		if(!isNull(precision)) {
			if(precision > 0  && isNull(scale)) {
				out.print("(");
				out.print(precision);
				out.print(")");
			}
			if(precision > 0 && !isNull(scale) && scale >= 0) {
				out.print("(");
				out.print(precision);
				out.print(",");
				out.print(scale);
				out.print(")");
			}
		}
	}
	
	private boolean isNull(Integer value) {
		return value==null?true:false;
	}
}
