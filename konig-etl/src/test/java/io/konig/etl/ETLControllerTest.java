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


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ETLControllerTest {
	
	@Mock
	ETLDatasource etlDatasource;
	
	@Captor
	private ArgumentCaptor<ExportRequest> exportRequestCaptor;
	
	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testETLDatasource() throws Exception{
		ETLController etlController=new ETLController(etlDatasource);
		String jsonFile="etlRequest.json";
		String inObjectId="student.csv";
		String outObjectId="studentResult.csv";
		ETLRequest request=getETLRequest(jsonFile);		
		ExportRequest exReq=request.getExportRequest();
		
			etlController.run(jsonFile,inObjectId,outObjectId);
			verify(etlDatasource).createTmpTable(eq(request.getCreateSql()));
			verify(etlDatasource).loadTmpTable(eq(request.getTableName()), eq(request.getBucketName()), eq(inObjectId));
			verify(etlDatasource).transform(eq(request.getTransformSql()));
			verify(etlDatasource).export(exportRequestCaptor.capture());
			verify(etlDatasource).deleteTmpTable(eq(request.getTableName()));
			
			ExportRequest exportRequest=exportRequestCaptor.getValue();
			
			assertEquals(exReq.getBucketName(),exportRequest.getBucketName());
			assertEquals(exReq.getFileFormat(),exportRequest.getFileFormat());
			assertEquals(outObjectId,exportRequest.getObjectId());
			assertEquals(exReq.getSql(),exportRequest.getSql());
			assertEquals(exReq.getTableId(),exportRequest.getTableId());
			
		
	}

	private ETLRequest getETLRequest(String jsonFile) throws Exception {
		ETLRequest request =null;
		try (InputStream is=getClass().getClassLoader().getResourceAsStream(jsonFile)) {
			 ObjectMapper objectMapper = new ObjectMapper();
			 request=objectMapper.readValue(is, ETLRequest.class);
		} 		
		return request;
		
	}
	

}
