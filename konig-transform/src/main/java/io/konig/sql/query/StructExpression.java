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


import io.konig.core.io.PrettyPrintWriter;

public class StructExpression extends BaseValueContainer
implements ItemExpression, ValueContainer {
	
	public StructExpression() {
		
	}
	
	@Override
	public void print(PrettyPrintWriter out) {
		boolean prettyPrint = out.isPrettyPrint();
		
		out.print("STRUCT(");
		out.pushIndent();
		String comma = "";
		for (QueryExpression field : getValues()) {
			out.print(comma);
			if (prettyPrint || comma.length()>0) {
				out.println();
			}
			out.indent();
			field.print(out);
			comma = ",";
		}
		if (prettyPrint) {
			out.print('\n');
		}
		out.popIndent();
		out.indent();
		out.print(')');
	}
	

}
