package io.konig.content.client;

/*
 * #%L
 * Konig Content System, Client Library
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


import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

public class GoogleStorageTest {

	public static void main(String[] args) throws Exception {
		
		
		try (FileInputStream credentialsStream = new FileInputStream("C:\\Users\\Greg\\auth\\gmcfall-data-catalog-client.json")) {

			String bucketId = "pearson-docs-catalog-bundle";

			Storage storage = StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(credentialsStream))
				.build().getService();
			
			Bucket bucket = storage.get(bucketId);
			
			Blob blob = bucket.create("foo/bar", "Hello, World!".getBytes(StandardCharsets.UTF_8), "text/plain");
		}
	
		 
	}

}
