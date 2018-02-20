package io.konig.etl;

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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ETLControllerEndPoint {
	
	public Logger logger=LoggerFactory.getLogger(ETLControllerEndPoint.class);
	
    @RequestMapping("/etl")
    public @ResponseBody String runETLController(@RequestParam("inObjectId") String inObjectId, @RequestParam("outObjectId") String outObjectId) {
       logger.info("Input Object Id ::"+inObjectId+", Output Object Id :: "+outObjectId);   
       //TODO:: Return the ETLDatasource class based on a request param instead of MockETLDatasource-TBD
       ETLController etlController=new ETLController(new MockETLDatasource());
       String jsonFile="etlRequest.json";
       try {
		etlController.run(jsonFile, inObjectId, outObjectId);
       } catch (ETLException e) {
    	  logger.error("Exception occured in ETLControllerEndPoint. ",e);
    	  return "Error occured in ETL Process "+e.getMessage();
       }
       return "ETL Process completed";
    }

    public static void main(String[] args) {
        SpringApplication.run(ETLControllerEndPoint.class, args);
    }

}
