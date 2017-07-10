package io.konig.transform.rule;

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


public class AlphabeticVariableNamer implements VariableNamer {

	private int count=0;
	
	
	@Override
	public String next() {
		count++;
		StringBuilder builder = new StringBuilder();
		int num = count;
		while (num > 0) {
			num--;
			int remainder = num % 26;
			char digit = (char) (remainder + 97);
			builder.append(digit);
			num = (num-remainder) / 26;
		}
		
		String value = builder.toString();
		
		if (value.length()>1) {
			char[] array = new char[value.length()];
			for (int i=0; i<value.length(); i++) {
				array[i] = value.charAt(array.length-1-i);
			}
			value = new String(array);
		}
		
		return value;
	}

}
