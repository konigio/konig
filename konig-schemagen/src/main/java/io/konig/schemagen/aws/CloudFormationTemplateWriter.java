package io.konig.schemagen.aws;

/*
 * #%L
 * Konig Schema Generator
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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.aws.datasource.CloudFormationTemplate;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.SimplePojoFactory;
import io.konig.core.vocab.AWS;
import io.konig.core.vocab.GCP;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GoogleCloudSqlDatabase;
import io.konig.gcp.datasource.GoogleCloudSqlInstance;
import io.konig.gcp.datasource.GoogleCloudSqlSettings;
import io.konig.gcp.datasource.GoogleCloudSqlTableInfo;
import io.konig.gcp.io.GoogleCloudSqlJsonUtil;
import io.konig.maven.CloudSqlInfo;

public class CloudFormationTemplateWriter {

	public CloudFormationTemplateWriter() {
		
	}
	
	public void writeTemplates(File cloudFormationDir, Graph graph) throws IOException {
		
		List<Vertex> list = graph.v(AWS.CloudFormationTemplate).in(RDF.TYPE).toVertexList();
		if (!list.isEmpty()) {
			if (!cloudFormationDir.exists()) {
				cloudFormationDir.mkdirs();
			}
			for (Vertex v : list) {
				SimplePojoFactory pojoFactory = new SimplePojoFactory();
				CloudFormationTemplate cloudFormation = pojoFactory.create(v, CloudFormationTemplate.class);
				File yamlFile = new File(cloudFormationDir, cloudFormation.getStackName() + "_template.yml");
				try (PrintWriter out = new PrintWriter(yamlFile)) {
				    out.println(cloudFormation.getTemplate());
				}
			}
		}
		
	}

}
