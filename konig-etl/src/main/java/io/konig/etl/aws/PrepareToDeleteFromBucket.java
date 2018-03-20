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
import org.apache.camel.component.aws.s3.S3Constants;

import io.konig.camel.aws.s3.S3Operations;


public class PrepareToDeleteFromBucket implements Processor {
	
    public void process(Exchange exchange) throws Exception {
    	exchange.getOut().setBody("delete");
    	String fileName = exchange.getIn().getHeader("fileName", String.class);
    	String bucketName = exchange.getIn().getHeader("bucketName", String.class);
        exchange.getIn().setHeader(S3Constants.KEY, fileName); 
        exchange.getIn().setHeader(S3Constants.BUCKET_NAME, bucketName);
        exchange.getIn().setHeader(S3Constants.S3_OPERATION, S3Operations.deleteObject);
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
    }
}