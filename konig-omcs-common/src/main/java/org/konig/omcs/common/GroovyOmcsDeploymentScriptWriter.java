package org.konig.omcs.common;

/*
 * #%L
 * Konig GCP Deployment Maven Plugin
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


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;

import io.konig.maven.FileUtil;
import io.konig.maven.OracleManagedCloudConfig;

public class GroovyOmcsDeploymentScriptWriter {

	private String konigVersion;
	private OracleManagedCloudConfig oracleManagedCloud;
	private File scriptFile;
	private Writer out;
	private String indent = "   ";
	
	public GroovyOmcsDeploymentScriptWriter(OracleManagedCloudConfig oracleManagedCloud) {
		this.oracleManagedCloud = oracleManagedCloud;
		this.konigVersion = oracleManagedCloud.getKonigVersion();
		this.scriptFile = oracleManagedCloud.getOmcsScriptFile();
	}

	public void run() throws IOException {
		scriptFile.getParentFile().mkdirs();
		
		try (FileWriter writer = new FileWriter(scriptFile)) {
			out = writer;
			String grab = MessageFormat.format("@Grab(\"io.konig:konig-omcs-deploy-maven-plugin:{0}\")", konigVersion);
			println(grab);
			println();			
			println("import static io.konig.maven.OmcsResourceType.*;");
			println("import io.konig.maven.OmcsDeployment;");
			println();
			println("def deploymentPlan = {");
			printDatabaseCommands();
			printTableCommands();
			println("}");
			println("def scriptDir = new File(getClass().protectionDomain.codeSource.location.path).parent");
			println("deploymentPlan.delegate = new OmcsDeployment(scriptDir)");
			println("deploymentPlan()");
			
		}
		
	}

	private void printTableCommands() throws IOException {
		File schemaDir = oracleManagedCloud.getTables();
		if (schemaDir != null) {
			for (File file : schemaDir.listFiles()) {
				String path = FileUtil.relativePath(scriptFile, file);
				print(indent);
				print("create OracleTable from \"");
				print(path);
				print("\"");
				println(" println response ");
			}
		}
	}

	
	private void printDatabaseCommands() throws IOException {
		
		File databaseDir = oracleManagedCloud.getDatabases();
		if (databaseDir != null && databaseDir.isDirectory()) {
			for (File file : databaseDir.listFiles()) {
				String path = FileUtil.relativePath(scriptFile, file);
				print(indent);
				print("create OracleDatabase from \"");
				print(path);
				print("\"");
				println(" println response ");
			}
		}
		
	}
	
	private void print(String text) throws IOException {
		out.write(text);
	}

	private void println() throws IOException {
		out.write('\n');
	}
	private void println(String text) throws IOException {
		out.write(text);
		out.write('\n');
		
	}
	
}
