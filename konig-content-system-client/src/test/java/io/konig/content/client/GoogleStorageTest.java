package io.konig.content.client;

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
