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


import java.util.HashMap;
import java.util.Map;

import io.konig.core.io.PrettyPrintWriter;

public class TimeUnit extends Expression {
	
	private static Map<String, TimeUnit> values = new HashMap<>();
	
	private String name;

	public static TimeUnit MINUTE = new TimeUnit("MINUTE");
	public static TimeUnit HOUR = new TimeUnit("HOUR");
	public static TimeUnit DAY = new TimeUnit("DAY");
	public static TimeUnit MONTH = new TimeUnit("MONTH");
	public static TimeUnit YEAR = new TimeUnit("YEAR");
	
	private TimeUnit(String name) {
		values.put(name, this);
		this.name = name;
	}
	
	public static TimeUnit forName(String name) {
		return values.get(name.toUpperCase());
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(name);
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		visitor.exit(this);
	}
	
	public String toString() {
		return name;
	}
	
	@Override
	public TimeUnit clone() {
		return this;
	}

}
