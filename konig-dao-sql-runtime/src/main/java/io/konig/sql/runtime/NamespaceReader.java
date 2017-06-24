package io.konig.sql.runtime;

/*
 * #%L
 * Konig DAO SQL Runtime
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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.konig.dao.core.DaoException;

public class NamespaceReader implements AutoCloseable {

	private BufferedReader reader;

	public NamespaceReader(Reader reader) {
		this.reader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
	}


	public Map<String,String> readNamespaces() throws DaoException {
		Map<String,String> map = new HashMap<>();
		try {
			
			Pattern pattern = Pattern.compile("@prefix\\s+([^: \t]+)\\s*:\\s*<([^>]+)>");
			String line;
			while ( (line = reader.readLine()) != null) {
				line = line.trim();
				if (line.length()==0) {
					continue;
				}
				
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String prefix = matcher.group(1);
					String namespaceURI = matcher.group(2);
					map.put(namespaceURI, prefix);
				}
			}
			
		} catch (IOException e) {
			throw new DaoException(e);
		}
		
		
		return map;
	}


	private void fail(String line) throws DaoException {
		throw new DaoException("Invalid namespace declaration: " + line);
		
	}


	@Override
	public void close() throws IOException {
		reader.close();
	}

}
