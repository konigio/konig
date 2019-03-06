package io.konig.formula;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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

import io.konig.core.KonigException;

public class FunctionModel {
	
	public static final FunctionModel CONCAT = 
		new FunctionModel(FunctionExpression.CONCAT, KqlType.STRING)
		.param("stringValue", KqlType.STRING, true);

	public static final FunctionModel SUM = 
		new FunctionModel(FunctionExpression.SUM, KqlType.NUMBER)
		.param("expr", KqlType.NUMBER);

	public static final FunctionModel AVG = 
		new FunctionModel(FunctionExpression.AVG, KqlType.NUMBER)
		.param("expr", KqlType.NUMBER);

	public static final FunctionModel COUNT = 
		new FunctionModel(FunctionExpression.COUNT, KqlType.NUMBER)
		.param("expr", KqlType.NUMBER);

	public static final FunctionModel UNIX_TIME = 
		new FunctionModel(FunctionExpression.UNIX_TIME, KqlType.INTEGER)
		.param("temporalValue", KqlType.INSTANT);
	
	public static final FunctionModel DAY = 
			new DateTruncFunctionModel(FunctionExpression.DAY, KqlType.INSTANT)
			.param("timestamp", KqlType.INSTANT);
	
	public static final FunctionModel MONTH = 
			new DateTruncFunctionModel(FunctionExpression.MONTH, KqlType.INSTANT)
			.param("timestamp", KqlType.INSTANT);
	
	public static final FunctionModel YEAR = 
			new DateTruncFunctionModel(FunctionExpression.YEAR, KqlType.INSTANT)
			.param("timestamp", KqlType.INSTANT);

	public static final FunctionModel STRPOS = 
		new FunctionModel(FunctionExpression.STRPOS, KqlType.INTEGER)
		.param("sourceString", KqlType.STRING)
		.param("subString", KqlType.STRING);
	

	public static final FunctionModel SUBSTR = 
		new FunctionModel(FunctionExpression.SUBSTR, KqlType.STRING)
		.param("sourceString", KqlType.STRING)
		.param("start", KqlType.INTEGER)
		.param("length", KqlType.INTEGER);
	
	private static FunctionModel[] LIST = new FunctionModel[]{
		AVG,
		CONCAT,
		COUNT,
		DAY,
		MONTH,
		STRPOS,
		SUBSTR,
		SUM,
		UNIX_TIME,
		YEAR
	};
	
	public static FunctionModel fromName(String name) {
		for (FunctionModel m : LIST) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		throw new KonigException("Unknown function: " + name);
	}
	
	
	private String name;
	private KqlType returnType;
	private List<ParameterModel> parameters = new ArrayList<>();
	

	
	public FunctionModel(String name, KqlType returnType) {
		this.name = name;
		this.returnType = returnType;
	}

	public FunctionModel param(String name, KqlType type) {
		parameters.add(new ParameterModel(name, type));
		return this;
	}
	
	public FunctionModel param(String name, KqlType type, boolean ellipsis) {
		parameters.add(new ParameterModel(name, type, ellipsis));
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public List<ParameterModel> getParameters() {
		return parameters;
	}
	
	public KqlType getReturnType() {
		return returnType;
	}
	

}
