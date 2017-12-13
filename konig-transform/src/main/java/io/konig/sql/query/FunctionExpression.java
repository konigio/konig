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


import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class FunctionExpression extends AbstractExpression implements NumericValueExpression, GroupingElement {
	
	public static final String ANY_VALUE = "ANY_VALUE";
	
	private String functionName;
	private List<QueryExpression> argList = new ArrayList<>();
	
	public FunctionExpression(String functionName) {
		this.functionName = functionName;
	}
	
	public FunctionExpression(String functionName, QueryExpression...arg) {
		this.functionName = functionName;
		for (QueryExpression e : arg) {
			addArg(e);
		}
	}
	
	public void addArg(QueryExpression arg) {
		argList.add(arg);
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.print(functionName);
		out.print('(');
		String comma = "";
		for (QueryExpression e : argList) {
			out.print(comma);
			e.print(out);
			comma = ", ";
		}
		
		out.print(')');
		
	}

	public String getFunctionName() {
		return functionName;
	}

	public List<QueryExpression> getArgList() {
		return argList;
	}

}
