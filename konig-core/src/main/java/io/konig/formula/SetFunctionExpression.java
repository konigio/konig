package io.konig.formula;

/*
 * #%L
 * Konig Core
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


import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class SetFunctionExpression extends FunctionExpression {

	private boolean distinct;
	
	public SetFunctionExpression(String functionName, boolean distinct, List<Expression> argList) {
		super(functionName, argList);
		this.distinct = distinct;
	}
	

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(getFunctionName());
		out.print('(');
		if (distinct) {
			out.print("DISTINCT ");
		}
		String comma = "";
		for (Expression arg : getArgList()) {
			out.print(comma);
			comma = ", ";
			out.print(arg);
		}
		out.print(')');

	}
}
