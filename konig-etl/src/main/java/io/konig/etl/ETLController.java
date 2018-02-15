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


import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ETLController {
	
	public Logger logger=LoggerFactory.getLogger(ETLController.class);
	
	ETLDatasource etlDatasource;
	public ETLDatasource getEtlDatasource() {
		return etlDatasource;
	}
	public void setEtlDatasource(ETLDatasource etlDatasource) {
		this.etlDatasource = etlDatasource;
	}
	public ETLController(ETLDatasource etlDatasource){
		this.etlDatasource=etlDatasource;
	}
	public void run(String jsonFile,String inObjectId,String outObjectId) throws ETLException{
		logger.info("Entered ETLController run method");
		try (InputStream is=getClass().getClassLoader().getResourceAsStream(jsonFile)) {
			ObjectMapper objectMapper = new ObjectMapper();
			ETLRequest request=objectMapper.readValue(is, ETLRequest.class);	
			request.setDatasource(etlDatasource);
			request.setObjectId(inObjectId);
			ExportRequest exportRequest=request.getExportRequest();
			exportRequest.setObjectId(outObjectId);
			request.setExportRequest(exportRequest);
			ETLProcess process=new ETLProcess();
			process.run(request);
			
		} catch (IOException e) {
			throw new ETLException(e);
		}
		logger.info("Exiting ETLController run method");
	}
	
}
