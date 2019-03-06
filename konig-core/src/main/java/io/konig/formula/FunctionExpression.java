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

import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;

public class FunctionExpression extends AbstractFormula implements BuiltInCall {
	public static final String SUM = "SUM";
	public static final String AVG = "AVG";
	public static final String COUNT = "COUNT";
	public static final String DAY = "DAY";
	public static final String MONTH = "MONTH";
	public static final String YEAR = "YEAR";
	public static final String TIME_INTERVAL = "TIME_INTERVAL";
	public static final String UNIX_TIME = "UNIX_TIME";
	public static final String CONCAT = "CONCAT";
	public static final String SUBSTR = "SUBSTR";
	public static final String STRPOS = "STRPOS";

	private String functionName;
	private List<Expression> argList = new ArrayList<>();
	private FunctionModel model;

	public FunctionExpression(FunctionModel model, Expression... arg) {

		this.functionName = model.getName();
		this.model = model;
		this.argList = new ArrayList<>();
		for (Expression e : arg) {
			argList.add(e);
		}
	}

	public FunctionExpression(FunctionModel model, List<Expression> argList) {
		this.functionName = model.getName();
		this.model = model;
		this.argList = argList;
	}

	public FunctionModel getModel() {
		return model;
	}

	public boolean isAggregation() {
		return functionName.equalsIgnoreCase(SUM) || functionName.equalsIgnoreCase(COUNT);
	}

	public static String getSum() {
		return SUM;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void addArg(Expression arg) {
		argList.add(arg);
	}

	public List<Expression> getArgList() {
		return argList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(functionName);
		out.print('(');
		String comma = "";
		for (Expression arg : argList) {
			out.print(comma);
			comma = ", ";
			out.print(arg);
		}
		out.print(')');

	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		for (Expression arg : argList) {
			arg.dispatch(visitor);
		}
		visitor.exit(this);
	}

}