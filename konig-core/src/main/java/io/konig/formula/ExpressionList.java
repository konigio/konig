package io.konig.formula;

/*
 * #%L
 * Konig Core
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


import java.io.StringWriter;
import java.util.ArrayList;

import io.konig.core.io.PrettyPrintWriter;

public class ExpressionList extends ArrayList<Expression> implements Formula {
	private static final long serialVersionUID = 1L;

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print('(');
		String comma = "";
		for (Expression e : this) {
			out.print(comma);
			comma = " , ";
			e.print(out);
		}
		out.print(')');
	}
	
	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter writer = new PrettyPrintWriter(buffer);
		writer.setPrettyPrint(false);
		print(writer);
		writer.close();
		
		return buffer.toString();
	}

}
