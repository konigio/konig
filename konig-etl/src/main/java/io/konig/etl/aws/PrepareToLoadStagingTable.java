package io.konig.etl.aws;

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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PrepareToLoadStagingTable implements Processor {

	public void process(Exchange exchange) throws Exception {
		
		String body = exchange.getIn().getBody(String.class);
		if(body != null && !body.equals("")) {
			String sourceTable = exchange.getIn().getHeader("sourceTable", String.class);
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.configure(
					DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			JsonNode rootNode = objectMapper.readTree(body);
			JsonNode messageNode = rootNode.path("Message");
			S3EventNotification notification = objectMapper.readValue(messageNode.asText(), S3EventNotification.class);
			String bucketName = "";
			String fileName = "";
			for(S3EventNotificationRecord record : notification.getRecords()) {
				bucketName = record.getS3().getBucket().getName().toLowerCase();
				fileName = record.getS3().getObject().getKey();
				
				exchange.getOut().setBody(
						"LOAD DATA FROM S3 's3-"+record.getAwsRegion()+"://"+bucketName+"/"+fileName+"'"
								+ " INTO TABLE "+sourceTable+" FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n'; ");
				exchange.getOut().setHeaders(exchange.getIn().getHeaders());
				exchange.getOut().setHeader("fileName", fileName);
				exchange.getOut().setHeader("bucketName", bucketName);
			}
		}
		
	}
}
