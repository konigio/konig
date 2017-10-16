package io.konig.content.gae;

/*
 * #%L
 * Konig Content System, Google App Engine implementation
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

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.utils.SystemProperty;
import com.google.auth.oauth2.GoogleCredentials;

public class CredentialsFactory {

	private static final Logger logger = LoggerFactory.getLogger(CredentialsFactory.class);
	
	private static final String CREDENTIALS_PATH = "datacatalog/credentials.json";
	
	private static final CredentialsFactory INSTANCE = new CredentialsFactory();
	
	private GoogleCredentials credentials;
	
	public static CredentialsFactory instance() {
		return INSTANCE;
	}
	
	public CredentialsFactory() {
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {

			
			try (InputStream input = getClass().getClassLoader().getResourceAsStream(CREDENTIALS_PATH)) {
				if (input == null) {
					String msg = "Google Credentials must be added to the classpath at " + CREDENTIALS_PATH;
					logger.error(msg);
					throw new RuntimeException(msg);
				}
				credentials = GoogleCredentials.fromStream(input);
			} catch (IOException e) {
				logger.error("Failed to initialize Credentials Factory", e);
				throw new RuntimeException(e);
			}
		}
	}
	
	public GoogleCredentials getCredentials() {
		return credentials;
		
	}
}
