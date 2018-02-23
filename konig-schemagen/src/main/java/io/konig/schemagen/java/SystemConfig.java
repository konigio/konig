package io.konig.schemagen.java;

/*
 * #%L
 * Konig Schema Generator Maven Plugin
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


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class SystemConfig {
	public static void init() throws IOException {

		String fileName = System.getenv("KONIG_DEPLOY_CONFIG");
		if (fileName == null) {
			fileName = System.getProperty("konig.deploy.config");
		}

		if (fileName == null) {
			throw new IOException("System config file not found. Please define either the "
					+ "'konig.deploy.config' system property, or " 
					+ "'KONIG_DEPLOY_CONFIG' environment variable");
		}
		InputStream in = null;
		try {
			Properties p = new Properties();
			in = new FileInputStream(new File(fileName));
			p.load(in);

			for (String name : p.stringPropertyNames()) {
				String value = p.getProperty(name);
				System.setProperty(name, value);
			}
			in.close();
		} catch (IOException ex) {
			throw new IOException("Unable to configure system property " + fileName);
		} 

	}
}
