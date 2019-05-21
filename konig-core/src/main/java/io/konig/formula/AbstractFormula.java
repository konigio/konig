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

import io.konig.core.io.PrettyPrintWriter;

public abstract class AbstractFormula implements Formula {

	@Override
	public String toString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter writer = new PrettyPrintWriter(buffer);
		print(writer);
		writer.close();
		
		return buffer.toString();
	}
	
	public String toSimpleString() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		out.setSuppressContext(true);
		print(out);
		out.flush();
		return buffer.toString();
	}
	
	public PrimaryExpression asPrimaryExpression() {
		return null;
	}
	
	@Override
	abstract public Formula clone();
	
	
	public BinaryRelationalExpression asBinaryRelationalExpression() {
		return null;
	}

}
