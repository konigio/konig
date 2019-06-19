package io.konig.core.showl.expression;

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

public class Indentation {
	
	private static Map<StringBuilder, Indentation> map = new HashMap<>();
	
	private int depth=0;
	private StringBuilder builder;

	
	public Indentation(StringBuilder builder) {
		this.builder = builder;
	}

	public static Indentation get(StringBuilder builder) {
		Indentation result = map.get(builder);
		if (result == null) {
			result = new Indentation(builder);
		}
		return result;
	}
	
	public void push() {
		depth++;
		if (depth == 1) {
			map.put(builder, this);
		}
	}
	
	public void pop() {
		 depth--;
		 if (depth == 0) {
			 map.remove(builder);
		 }
	}
	
	public void indent(StringBuilder builder) {
		for (int i=0; i<depth; i++) {
			builder.append("  ");
		}
	}

}
