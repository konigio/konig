package io.konig.core.jsonpath;

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


import io.konig.core.io.PrettyPrintWriter;

public class JsonPathBracket implements JsonPathOperator {
	
	private JsonPathKey key;

	public JsonPathBracket(JsonPathKey key) {
		this.key = key;
	}


	public JsonPathKey getKey() {
		return key;
	}


	@Override
	public void print(PrettyPrintWriter out) {
		out.print('[');
		if (key instanceof JsonPathName) {
			out.print('\'');
			out.print(key);
			out.print('\'');
		} else {
			out.print(key);
		}
		out.print(']');

	}


	@Override
	public boolean isRoot() {
		return false;
	}


	@Override
	public boolean isField() {
		return false;
	}


	@Override
	public boolean isBracket() {
		return true;
	}


	@Override
	public JsonPathRoot asRoot() {
		return null;
	}


	@Override
	public JsonPathField asField() {
		return null;
	}


	@Override
	public JsonPathBracket asBracket() {
		return this;
	}

}
