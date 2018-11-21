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

public class JsonPathName implements JsonPathKey {

	private String value;

	public JsonPathName(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.print(value);
	}

	@Override
	public boolean isIndex() {
		return false;
	}

	@Override
	public boolean isName() {
		return true;
	}

	@Override
	public boolean isWildCard() {
		return false;
	}

	@Override
	public JsonPathIndex asIndex() {
		return null;
	}

	@Override
	public JsonPathName asName() {
		return this;
	}

	@Override
	public JsonPathWildcard asWildcard() {
		return null;
	}
	
}
