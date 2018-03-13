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
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

import io.konig.maven.DataServicesConfig;
import io.konig.maven.Exclude;
import io.konig.maven.FilterPart;
import io.konig.maven.GoogleCloudPlatformConfig;
import io.konig.maven.JavaCodeGeneratorConfig;
import io.konig.maven.WorkbookProcessor;

public class XmlSerializerTest {
	
	@Test
	public void testJava() {
		JavaCodeGeneratorConfig java = new JavaCodeGeneratorConfig();
		List<FilterPart> filter = new ArrayList<>();
		java.setFilter(filter);
		Exclude exclude = new Exclude();
		filter.add(exclude);
		Set<String> namespaces = new HashSet<>();
		exclude.setNamespaces(namespaces);
		namespaces.add("http://schema.org/");
		StringWriter buffer = new StringWriter();
		buffer.write("\n");
		XmlSerializer serializer = new XmlSerializer(buffer);
		serializer.setIndent(1);
		serializer.indent();
		
		
		serializer.write(java, "java");
		serializer.flush();
		String expected = "\n" + 
				"   <java>\n" + 
				"      <generateCanonicalJsonReaders>false</generateCanonicalJsonReaders>\n" + 
				"      <filter>\n" + 
				"         <exclude>\n" + 
				"            <namespaces>\n" + 
				"               <param>http://schema.org/</param>\n" + 
				"            </namespaces>\n" + 
				"         </exclude>\n" + 
				"      </filter>\n" + 
				"   </java>\n" + 
				"";
		String actual = buffer.toString().replace("\r", "");
		assertEquals(expected, actual);
	}
	
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

	@Test
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
			"      <enableBigQueryTransform>true</enableBigQueryTransform>\n" + 
			"      <enableMySqlTransform>true</enableMySqlTransform>\n" + 
			"   </googleCloudPlatform>\n" + 
			"";
		String actual = buffer.toString().replace("\r", "");
		
		assertEquals(expected, actual);
	}

}
