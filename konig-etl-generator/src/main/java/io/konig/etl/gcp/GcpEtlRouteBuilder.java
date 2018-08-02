package io.konig.etl.gcp;

/*
 * #%L
 * konig-etl-generator
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
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openrdf.model.impl.URIImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.Shape;

public class GcpEtlRouteBuilder {

	private Shape sourceShape;
	private Shape targetShape;
	private File outDir;
	private Document doc;
	private File etlBaseDir;

	public File getEtlBaseDir() {
		return etlBaseDir;
	}

	public void setEtlBaseDir(File etlBaseDir) {
		this.etlBaseDir = etlBaseDir;
	}

	public GcpEtlRouteBuilder(File outDir) {
		this.outDir = outDir;
	}

	public GcpEtlRouteBuilder(Shape sourceShape, Shape targetShape, File outDir) {
		this.sourceShape = sourceShape;
		this.targetShape = targetShape;
		this.outDir = outDir;
	}

	public GcpEtlRouteBuilder() {
	}

	public void generate() throws ParserConfigurationException, TransformerException, IOException {
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		doc = docBuilder.newDocument();

		Element rootElement = doc.createElement("routes");
		doc.appendChild(rootElement);

		String targetLocalName = new URIImpl(targetShape.getId().stringValue()).getLocalName();

		rootElement.setAttribute("id", "Route" + targetLocalName);
		rootElement.setAttribute("xmlns", "http://camel.apache.org/schema/spring");

		Element route = doc.createElement("route");
		rootElement.appendChild(route);
		GoogleCloudStorageBucket googleStorageBucket = sourceShape.findDataSource(GoogleCloudStorageBucket.class);

		Element from = doc.createElement("from");
		from.setAttribute("uri",
				"google-pubsub://pearson-edw-core:" + googleStorageBucket.getNotificationInfo().get(0).getTopic());
		route.appendChild(from);
		route.appendChild(addProcess("ref", "prepareToLoadTargetTable"));
		Element toG = doc.createElement("to");
		GoogleBigQueryTable googleBigQueryTable = targetShape.findDataSource(GoogleBigQueryTable.class);

		toG.setAttribute("uri",
				"google-bigquery-insert:" + googleBigQueryTable.getTableReference().getProjectId() + ":"
						+ googleBigQueryTable.getTableReference().getDatasetId() + ":"
						+ googleBigQueryTable.getTableReference().getTableId());
		route.appendChild(toG);

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(outDir, "Route" + targetLocalName + ".xml"));
		transformer.transform(source, result);
	}


	private Element addProcess(String attrName, String attrValue) {
		Element process = doc.createElement("process");
		process.setAttribute(attrName, attrValue);
		return process;
	}
}
