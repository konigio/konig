package io.konig.content.client;

import java.io.File;

public class ContentSystemClientTest {

	public static void main(String[] args) throws Exception {
		
		File baseDir = new File("src/test/resources/ContentSystemClientTest");
		String baseURL = "http://localhost:8080/content/";
		
		ContentPublisher publisher = new ContentPublisher();
		publisher.publish(baseDir, baseURL, "test", "1.0");

	}

}
