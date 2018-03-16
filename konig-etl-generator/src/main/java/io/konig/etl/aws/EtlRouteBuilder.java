package io.konig.etl.aws;

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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openrdf.model.impl.URIImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import io.konig.aws.datasource.AwsAurora;
import io.konig.aws.datasource.S3Bucket;
import io.konig.shacl.Shape;

public class EtlRouteBuilder {

	private Shape sourceShape;
	private Shape targetShape;
	private File outDir;
	private Document doc;

	public EtlRouteBuilder(File outDir) {
		this.outDir = outDir;
	}

	public EtlRouteBuilder(Shape sourceShape, Shape targetShape, File outDir) {
		this.sourceShape = sourceShape;
		this.targetShape = targetShape;
		this.outDir = outDir;
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

		Element from = doc.createElement("from");
		from.setAttribute("uri", "jetty://http://localhost:8888/myapp/etl/" + targetLocalName + "/fromS3");
		route.appendChild(from);

		S3Bucket bucket = sourceShape.findDataSource(S3Bucket.class);
		Element fromsqs = doc.createElement("from");
		fromsqs.setAttribute("uri",
				"aws-sqs://" + bucket.getNotificationConfiguration().getQueueConfiguration().getQueue().getResourceName()
						+ "?amazonSQSClient=#sqsClient&region=" + bucket.getRegion()
						+ "&defaultVisibilityTimeout=5000&deleteIfFiltered=false");

		AwsAurora targetTable = targetShape.findDataSource(AwsAurora.class);
		AwsAurora sourceTable = sourceShape.findDataSource(AwsAurora.class);

		route.appendChild(fromsqs);
		route.appendChild(addHeader("sourceTable", sourceTable.getTableReference().getAwsSchema() + "."
				+ sourceTable.getTableReference().getAwsTableName()));
		route.appendChild(addHeader("targetTable", targetTable.getTableReference().getAwsSchema() + "."
				+ targetTable.getTableReference().getAwsTableName()));
		route.appendChild(addHeader("bucketName", bucket.getBucketName()));
		route.appendChild(addHeader("dmlScript", targetLocalName));

		route.appendChild(addProcess("ref", "prepareToLoadStagingTable"));

		route.appendChild(addDataSourceRoute());

		route.appendChild(addProcess("ref", "prepareToLoadTargetTable"));

		route.appendChild(addDataSourceRoute());

		route.appendChild(addProcess("ref", "prepareToDeleteFromStagingTable"));

		route.appendChild(addDataSourceRoute());

		route.appendChild(addProcess("ref", "prepareToDeleteFromBucket"));

		Element toG = doc.createElement("from");
		toG.setAttribute("uri", "aws-s3://" + bucket.getBucketName()
				+ "?prefix=${header.fileName}&amazonS3Client=#s3Client&deleteAfterRead=true");
		route.appendChild(toG);

		route.appendChild(addProcess("ref", "prepareToExport"));

		route.appendChild(addDataSourceRoute());

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(outDir, "Route" + targetLocalName + ".xml"));
		transformer.transform(source, result);

		addConfig(targetTable);
		createDockerFile(targetLocalName, targetTable.getTableReference().getAwsSchema());
	}

	private void addConfig(AwsAurora targetTable) throws IOException {

		File file = new File(outDir, "camel-routes-config.properties");
		if (!file.exists()) {
			String jdbcUrl = "jdbc:mysql://${" + targetTable.getTableReference().getAwsAuroraHost() + "}/"
					+ targetTable.getTableReference().getAwsSchema()
					+ "?verifyServerCertificate=false&amp;useSSL=false";
			Properties properties = new Properties();
			properties.setProperty("camel.springboot.xmlRoutes", "true");
			properties.setProperty("camel.springboot.xmlRoutes", "classpath:camel-etl/*.xml");
			properties.setProperty("aws.rds.dbUrl", jdbcUrl);
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, "camel-routes-config");
			fileOut.close();
		}
	}

	private Element addHeader(String headerName, String headerValue) {
		Element header = doc.createElement("setHeader");
		header.setAttribute("headerName", headerName);

		Element headerConstant = doc.createElement("constant");
		headerConstant.setTextContent(headerValue);
		header.appendChild(headerConstant);
		return header;

	}

	private Element addProcess(String attrName, String attrValue) {
		Element process = doc.createElement("process");
		process.setAttribute(attrName, attrValue);
		return process;
	}

	private Element addDataSourceRoute() {
		Element to = doc.createElement("to");
		to.setAttribute("uri", "jdbc:mySqlDataSource");
		return to;
	}

	private void createDockerFile(String targetLocalName, String schemaName) throws FileNotFoundException {
		File dockerDir = new File(outDir.getParent(), "Docker");
		if (!dockerDir.exists()) {
			dockerDir.mkdirs();
		}
		File dockerFile = new File(new File(outDir.getParent(), "Docker"), targetLocalName);
		PrintWriter writer = new PrintWriter(dockerFile);
		writer.println("FROM konig-docker-aws-etl-base:latest");
		writer.println("COPY ../camel-etl/camel-routes-config.properties camel-etl/camel-routes-config.properties");
		writer.println("COPY ../camel-etl/Route" + targetLocalName + ".xml camel-etl/Route" + targetLocalName + ".xml");
		writer.println("COPY ../aurora/transform/" + schemaName + "_" + targetLocalName + ".sql " + schemaName + "_"
				+ targetLocalName + ".sql");
		writer.close();
	}

	public void createDockerComposeFile(Map<String, Object> services)
			throws IOException {
		DumperOptions options = new DumperOptions();
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setPrettyFlow(false);
		Yaml yaml = new Yaml(options);
		File dockerDir = new File(outDir.getParent(), "DockerCompose");

		if (!dockerDir.exists())
			dockerDir.mkdirs();
		File dockerComposeFile = new File(new File(outDir.getParent(), "DockerCompose"), "docker-compose.yml");
		FileWriter writer = new FileWriter(dockerComposeFile, true);
		writer.write("version: '2'");
		writer.write(System.lineSeparator());
		Map<String, Object> dockerComposeMap = new HashMap<>();
		dockerComposeMap.put("services", services);
		yaml.dump(dockerComposeMap, writer);
		writer.close();

	}
}
