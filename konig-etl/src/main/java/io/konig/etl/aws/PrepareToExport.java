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
import org.codehaus.plexus.util.StringUtils;

public class PrepareToExport implements Processor {
	
    public void process(Exchange exchange) throws Exception {
    	String targetTable = exchange.getIn().getHeader("targetTable", String.class);
    	String bucketName = exchange.getIn().getHeader("targetBucketName", String.class);
    	String bucketRegion = exchange.getIn().getHeader("targetBucketRegion", String.class);
    	String fileName = exchange.getIn().getHeader("fileName", String.class); 
    	String modified = exchange.getIn().getHeader("modified", String.class); 
    	String envName = "";
    	if(System.getProperty("environmentName") != null) {
    		envName = System.getProperty("environmentName");
		}
    	bucketName = StringUtils.replaceOnce(bucketName,"${environmentName}",envName).toLowerCase();
        exchange.getOut().setBody("SELECT * FROM "+targetTable +" WHERE modified=TIMESTAMP('"+modified+"') INTO OUTFILE S3 's3-"+bucketRegion+"://"+bucketName+"/"+fileName+"' "
         		+ " FIELDS TERMINATED BY ','"
        		+ "   LINES TERMINATED BY '\n' OVERWRITE ON;");  
        
        
    }
}
