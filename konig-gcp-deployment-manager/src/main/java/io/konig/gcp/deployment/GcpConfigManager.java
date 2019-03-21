package io.konig.gcp.deployment;

/*
 * #%L
 * Konig GCP Deployment Manager
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import io.konig.core.Graph;
import io.konig.datasource.DataSource;
import io.konig.datasource.DataSourceVisitor;
import io.konig.schemagen.gcp.BigQueryTableGenerator;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class GcpConfigManager {
	
	private static final String GS_PREFIX = "gs://";
	private DeploymentConfig config = new DeploymentConfig();
	private List<DataSourceVisitor> visitors = new ArrayList<>();
	private File globalConfigTemplate;
	
	
	
	public GcpConfigManager(BigQueryTableGenerator bigQueryTableGenerator, File globalConfigTemplate) {
		this.globalConfigTemplate = globalConfigTemplate;
		addVisitors(bigQueryTableGenerator);
	}
	
	private void addVisitors(BigQueryTableGenerator bigQueryTableGenerator) {
		
		visitors.add(new GoogleBigqueryTableVisitor(this, bigQueryTableGenerator));
		
	}
	
	public String bigqueryTableName(String datasetId, String tableId) {
		return "bigquery-" + datasetId + "-" + tableId;
	}
	
	public String storageBucketName(String bucketURI) {
		if (bucketURI.startsWith(GS_PREFIX)) {
			int end = bucketURI.indexOf('/', GS_PREFIX.length());
			if (end < 0) {
				end = bucketURI.length();
			}
			return bucketURI.substring(GS_PREFIX.length(), end);
		}
		throw new IllegalArgumentException("Invalid bucketURI: " + bucketURI);
	}
	
	public String storageBucketConfigName(String bucketName) {
		return "bucket-" + bucketName;
	}
	
	public URI bigqueryTableIri(String datasetId, String tableId) {
		return new URIImpl("https://www.googleapis.com/bigquery/v2/projects/${gcpProjectId}/datasets/" + 
				datasetId + "/tables/" + tableId);
	}
	
	public String datasetName(String datasetId) {
		return "dataset-" + datasetId;
	}
	
	public URI datasetIri(String datasetId) {
		return new URIImpl("https://www.googleapis.com/bigquery/v2/projects/${gcpProjectId}/datasets/" + datasetId);
	}

	public DeploymentConfig getConfig() {
		return config;
	}
	
	public void build(Graph graph, ShapeManager shapeManager) {
		for (Shape shape : shapeManager.listShapes()) {
			for (DataSource ds : shape.getShapeDataSource()) {
				for (DataSourceVisitor visitor : visitors) {
					visitor.visit(graph, shape, ds);
				}
			}
		}
	}
	
	public void write(Writer out) throws Exception {
		
		
		YAMLFactory yf = new YAMLFactory();
		ObjectMapper mapper = new ObjectMapper(yf);
		mapper.setSerializationInclusion(Include.NON_NULL);
		SequenceWriter sw = mapper.writerWithDefaultPrettyPrinter().writeValues(out);
		sw.write(config);
		out.write(System.lineSeparator());

		writeGlobalConfigTemplate(out);
	}
	
	private void writeGlobalConfigTemplate(Writer out) throws FileNotFoundException, IOException {
		if (globalConfigTemplate != null && globalConfigTemplate.exists()) {
			
			try (FileInputStream input = new FileInputStream(globalConfigTemplate)) {
				byte[] buffer = new byte[1024];
				int len = 0;
				while ( (len = input.read(buffer)) != -1) {
					String text = new String(buffer, 0, len);
					out.write(text);
				}
			}
		}
		
	}

	public void write(File file) throws Exception {
		file.getParentFile().mkdirs();
		try (FileWriter out = new FileWriter(file)) {
			write(out);
		}
		
	}

}
