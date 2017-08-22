package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO Core
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


public class ChartUtil {
	private static final int UPPER = 1;
	private static final int LOWER = 2;

	public static String label(FieldInfo field) {
		String name = field.getName();
		StringBuilder builder = new StringBuilder();
		
		char c = Character.toUpperCase(name.charAt(0));
		builder.append(c);
		int priorCase = UPPER;
		for (int i=1; i<name.length(); i++) {
			c = name.charAt(i);
			int currentCase = Character.isUpperCase(c) ? UPPER : LOWER;
			if (currentCase != priorCase && currentCase==UPPER) {
				builder.append(' ');
			}
			builder.append(c);
			priorCase = currentCase;
		}
		
		return builder.toString();
	}
}
