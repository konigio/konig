package io.konig.etl.aws;

import org.apache.camel.spring.boot.CamelAutoConfiguration;
import org.springframework.beans.factory.annotation.Value;

/*
 * #%L
 * Konig ETL
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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSClient;

@SpringBootApplication
@ImportResource({"classpath:app-config.xml"})
@PropertySource("file:camel-routes-config.properties")
@EnableAutoConfiguration(exclude = { CamelAutoConfiguration.class })
public class CamelEtlRouteController {

	@Value("${aws.accessKey}")
	private String ACCESS_KEY;
	
	@Value("${aws.secretKey}")
	private String SECRET_KEY;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(CamelEtlRouteController.class, args);
	}

	@Bean
	AmazonSQSClient sqsClient() {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		return new AmazonSQSClient(credentials);
	}

	@Bean
	AmazonS3Client s3Client() {
		AWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
		return new AmazonS3Client(credentials);
	}
}
