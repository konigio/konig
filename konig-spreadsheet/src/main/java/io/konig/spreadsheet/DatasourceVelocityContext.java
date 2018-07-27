package io.konig.spreadsheet;

import java.util.HashMap;
import java.util.Map;

/*
 * #%L
 * Konig Spreadsheet
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


import java.util.Properties;

import org.apache.velocity.VelocityContext;

public class DatasourceVelocityContext extends VelocityContext {
	
	private Map<String,Object> parameters;
	private Properties defaultProperties;

	public DatasourceVelocityContext(Properties properties) {
		this.defaultProperties = properties;
		parameters = new HashMap<>();
	}

	public void clearParameters() {
		parameters.clear();
	}
	
	public void putParameter(String key, String value) {
		parameters.put(key, value);
	}

	public Object get(String key) {
		
		Object result = parameters.size()>0 ? parameters.get(key) : null;
		if (result == null) {
			result = super.get(key);
		}
		if (result == null && defaultProperties!=null) {
			result = defaultProperties.get(key);
		}
		return result;
	}

}
