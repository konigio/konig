package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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


import static org.junit.Assert.*;

import java.io.File;
import java.io.StringWriter;

import org.junit.Ignore;
import org.junit.Test;

import io.konig.schemagen.maven.DataServicesConfig;
import io.konig.schemagen.maven.GoogleCloudPlatformConfig;
import io.konig.schemagen.maven.WorkbookProcessor;

public class XmlSerializerTest {
	
	@Test
	public void testWorkbook() {
		WorkbookProcessor workbook = new WorkbookProcessor();
		workbook.setWorkbookFile(new File("foo/bar.xlsx"));
		
		StringWriter buffer = new StringWriter();
		buffer.write("\n");
		XmlSerializer serializer = new XmlSerializer(buffer);
		serializer.setIndent(1);
		serializer.indent();
		
		
		serializer.write(workbook, "googleCloudPlatform");
		serializer.flush();
		
		String expected = "\n" + 
				"   <googleCloudPlatform>\n" + 
				"      <workbookFile>foo/bar.xlsx</workbookFile>\n" + 
				"      <inferRdfPropertyDefinitions>true</inferRdfPropertyDefinitions>\n" + 
				"      <failOnWarnings>false</failOnWarnings>\n" + 
				"      <failOnErrors>false</failOnErrors>\n" + 
				"   </googleCloudPlatform>\n" + 
				"";
		String actual = buffer.toString().replace("\r", "");
		assertEquals(expected, actual);
	}

	@Ignore
	public void testGoogleCloudPlatform() {
		
		DataServicesConfig dataServices = new DataServicesConfig();
		dataServices.setBasedir(new File("base/foo"));
		dataServices.setInfoFile(new File("config/info.yaml"));
		
		GoogleCloudPlatformConfig config = new GoogleCloudPlatformConfig();
		
		config.setBigQueryDatasetId("datasetId");
		config.setCredentials(new File("auth/credentials.json"));
		config.setDataServices(dataServices);
		
		StringWriter buffer = new StringWriter();
		buffer.write("\n");
		
		XmlSerializer serializer = new XmlSerializer(buffer);
		serializer.setIndent(1);
		serializer.indent();
		
		
		serializer.write(config, "googleCloudPlatform");
		serializer.flush();
		
		String expected = 
			"\n" + 
			"   <googleCloudPlatform>\n" + 
			"      <bigQueryDatasetId>datasetId</bigQueryDatasetId>\n" + 
			"      <credentials>auth/credentials.json</credentials>\n" + 
			"      <dataServices>\n" + 
			"         <basedir>base/foo</basedir>\n" + 
			"         <infoFile>config/info.yaml</infoFile>\n" + 
			"      </dataServices>\n" + 
			"   </googleCloudPlatform>\n" + 
			"";
		String actual = buffer.toString().replace("\r", "");
		
		assertEquals(expected, actual);
	}

}
