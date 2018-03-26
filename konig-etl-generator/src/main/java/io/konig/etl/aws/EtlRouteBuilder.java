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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.impl.URIImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.Files;

import io.konig.aws.datasource.AwsAurora;
import io.konig.aws.datasource.S3Bucket;
import io.konig.maven.FileUtil;
import io.konig.maven.project.generator.MavenProjectConfig;
import io.konig.maven.project.generator.MavenProjectGeneratorException;
import io.konig.shacl.Shape;

public class EtlRouteBuilder {

	private Shape sourceShape;
	private Shape targetShape;
	private File outDir;
	private Document doc;
	private VelocityContext context;
	private VelocityEngine engine;

	private MavenProjectConfig config;
	public EtlRouteBuilder(File outDir) {
		this.outDir = outDir;
	}

	public EtlRouteBuilder(Shape sourceShape, Shape targetShape, File outDir) {
		this.sourceShape = sourceShape;
		this.targetShape = targetShape;
		this.outDir = outDir;
	}

	public EtlRouteBuilder() {
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

		S3Bucket sourceBucket = sourceShape.findDataSource(S3Bucket.class);
		Element fromsqs = doc.createElement("from");
		fromsqs.setAttribute("uri",
						+ "?amazonSQSClient=#sqsClient&region=" + bucket.getRegion()
				"aws-sqs://" + sourceBucket.getNotificationConfiguration().getQueueConfiguration().getQueue().getResourceName()
						+ "?amazonSQSClient=#sqsClient&region=" + sourceBucket.getRegion()

		AwsAurora targetTable = targetShape.findDataSource(AwsAurora.class);
		AwsAurora sourceTable = sourceShape.findDataSource(AwsAurora.class);
		S3Bucket targetBucket = targetShape.findDataSource(S3Bucket.class);
		
		route.appendChild(fromsqs);
		route.appendChild(addHeader("sourceTable", sourceTable.getTableReference().getAwsSchema() + "."
				+ sourceTable.getTableReference().getAwsTableName()));
		route.appendChild(addHeader("targetTable", targetTable.getTableReference().getAwsSchema() + "."
				+ targetTable.getTableReference().getAwsTableName()));
		route.appendChild(addHeader("sourceBucketName", sourceBucket.getBucketName()));
		route.appendChild(addHeader("targetBucketName", targetBucket.getBucketName()));
		route.appendChild(addHeader("targetBucketRegion", targetBucket.getRegion()));
		route.appendChild(addHeader("dmlScript", targetTable.getTableReference().getAwsSchema() + "_"+targetLocalName));

		route.appendChild(addProcess("ref", "prepareToLoadStagingTable"));

		route.appendChild(addDataSourceRoute());

		route.appendChild(addProcess("ref", "prepareToLoadTargetTable"));

		route.appendChild(addDataSourceRoute());

		route.appendChild(addProcess("ref", "prepareToDeleteFromStagingTable"));

		route.appendChild(addDataSourceRoute());

		route.appendChild(addProcess("ref", "prepareToDeleteFromBucket"));

		toG.setAttribute("uri", "konig-aws-s3://" + bucket.getBucketName()
				+ "?amazonS3Client=#s3Client");
		toG.setAttribute("uri", "konig-aws-s3://"+sourceBucket.getBucketName()+"?amazonS3Client=#s3Client&region=" + sourceBucket.getRegion());

		route.appendChild(addProcess("ref", "prepareToExport"));

		route.appendChild(addDataSourceRoute());

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(outDir, "Route" + targetLocalName + ".xml"));
		transformer.transform(source, result);

		addConfig(targetTable);
		createDockerFile(targetLocalName,targetTable.getTableReference().getAwsSchema());
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

	public void createDockerFile(String targetLocalName, String schemaName) throws IOException {
	System.out.println("inside creae docker file");
		if(config==null)
		{
			config=new MavenProjectConfig();	
		}
		//outDir=new File("C:/Users/604601/Documents/konig-examples/gcp/demo/demo-aws-model/target/aws/camel-etlRoute");
		
		config.setArtifactId("etl-"+targetLocalName.toLowerCase());
		config.setBaseDir(new File(outDir,"../../../../../ETLmS/ETL-"+targetLocalName));
		config.setGroupId("io.konig");
		config.setName("etl-"+targetLocalName.toLowerCase());
		config.setVersion("1.0.0");
		try {
			run();
		} catch (MavenProjectGeneratorException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File dockerDir = new File(new File(outDir,"../../../../../ETLmS/ETL-"+targetLocalName)+"/", "Docker");
		if (!dockerDir.exists()) {
			dockerDir.mkdirs();
		}
		File dockerFile = new File(dockerDir, "Dockerfile");
		PrintWriter writer = new PrintWriter(dockerFile);
		writer.println("FROM 220459826988.dkr.ecr.us-east-1.amazonaws.com/konig-docker-aws-etl-base:latest");
		if(new File(outDir,"camel-routes-config.properties").exists())
		{
			writer.println("ADD /camel-routes-config.properties ./camel-routes-config.properties");
			Files.copy(new File(outDir, "camel-routes-config.properties"), new File(dockerDir, "camel-routes-config.properties"));

		}
		if(new File(outDir,"Route"+targetLocalName+".xml").exists())
		{
			writer.println("ADD /Route"+targetLocalName+".xml ./Route"+targetLocalName+".xml");
			Files.copy(new File(outDir, "Route" + targetLocalName + ".xml"), new File(dockerDir, "Route" + targetLocalName + ".xml"));
		}
		if(new File(outDir,"../transform/"+schemaName+"_"+targetLocalName+".sql").exists())
		{
			writer.println("ADD /"+schemaName+"_"+targetLocalName+".sql ./"+schemaName + "_"+ targetLocalName +".sql");	
		}
		
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
	
	public void run() throws MavenProjectGeneratorException, IOException {
		clean();
		createVelocityEngine();
		createVelocityContext();
		merge();
	}
	

	private void clean() {
		FileUtil.delete(baseDir());
	}

	private void merge() throws IOException {

		baseDir().mkdirs();
		try (FileWriter out = new FileWriter(getTargetPom())) {
			Template template = engine.getTemplate("pom.xml");
			template.merge(context, out);
		}
		
	}
	private void createVelocityEngine() {

		File velocityLog = new File("target/logs/" + config.getArtifactId() + "-velocity.log");
		velocityLog.getParentFile().mkdirs();

		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		properties.put("runtime.log", velocityLog.getAbsolutePath());

		engine = new VelocityEngine(properties);

	}
	protected VelocityContext getContext() {
		return context;
	}

	protected VelocityContext createVelocityContext() {
		context = new VelocityContext();
		context.put("project", config);
		return context;
	}
	
	protected File baseDir() {
		return config.getBaseDir();
	}
	public File getTargetPom() {
		return new File(config.getBaseDir(), "pom.xml");
	}
	
}
