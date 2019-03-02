package io.konig.schemagen.packaging;

/*
 * #%L
 * Konig Schema Generator
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;

/**
 * Generates the Maven pom.xml and assembly descriptors.
 * 
 * The generated files use these patterns:
 * <pre>
 *   /target/deploy/pom.xml
 *   /target/deploy/src/assembly/{envName}-{resourceKind}.xml
 * </pre>
 * 
 * where resource kind is one of (gcp, aws)
 * and envName is a user-defined name for the environment -- typically one of (dev, test, stage, prod).
 * 
 * The assembly descriptors include FileSets of the form:
 * <pre>
 * 
 *    
 * 
 * </pre>
 * 
 * @author Greg McFall
 *
 */

public class MavenPackagingProjectGenerator {
	private static final String PACKAGE_ID = "packageId";
	private static final String ASSEMBLY_LIST = "assemblyList";
	private static final String FILE_SET_LIST = "fileSetList";
	private static final String POM_TEMPLATE = "io/konig/schemagen/packaging/pom.xml";
	private static final String DESCRIPTOR_TEMPLATE = "io/konig/schemagen/packaging/dep.xml";
	private static final String ENV_PATH = "target/deploy/src/main/resources/env";
	private static final String POM_TARGET = "target/deploy/pom.xml";
	private static final String ASSEMBLY_FILE_PATTERN = "target/deploy/src/assembly/{0}-{1}.xml";
	private static final String ASSEMBLY_DESCRIPTOR_PATTERN = "$'{'project.basedir'}'/src/assembly/{0}-{1}.xml";
	private static final String ASSEMBLY_ID_PATTERN = "zip-{0}-{1}";
	private static final String ASSEMBLY_SOURCE_DIR_PATTERN = "$'{'project.basedir'}'/src/main/resources/env/{0}/{1}";
	
	public MavenPackagingProjectGenerator() {
	}
	
	public File generate(PackagingProjectRequest request) throws IOException, ParseException {
		RuntimeServices runtime = new RuntimeInstance();
		File velocityLogFile = request.getVelocityLogFile();
		if (velocityLogFile != null) {
			runtime.addProperty("runtime.log", velocityLogFile.getAbsolutePath());
		}
		VelocityContext context = buildContext(request);
	
		File pomFile = mergePom(runtime, context, request);
		mergeDescriptors(runtime, context);
		
		return pomFile;
	}

	private VelocityContext buildContext(PackagingProjectRequest request) {
		VelocityContext context = new VelocityContext();
		context.put("mavenProject", request.getMavenProject());
		context.put(ASSEMBLY_LIST, assemblyList(request));
		return context;
	}

	private List<AssemblyConfig> assemblyList(PackagingProjectRequest request) {
		List<AssemblyConfig> list = new ArrayList<>();
		File envRoot = new File(request.getBasedir(), ENV_PATH);
		if (envRoot.isDirectory()) {
			for (File envDir : envRoot.listFiles() ) {
				if (envDir.isDirectory()) {
					String envName = envDir.getName();
					for (File resourceKind : envDir.listFiles()) {
						if (resourceKind.isDirectory()) {
							String resourceKindName = resourceKind.getName();
							
							String id = MessageFormat.format(ASSEMBLY_ID_PATTERN, envName, resourceKindName);
							String descriptorRef = MessageFormat.format(ASSEMBLY_DESCRIPTOR_PATTERN, envName, resourceKindName);
							
							AssemblyPomConfig pom = new AssemblyPomConfig(id, descriptorRef);
							
							String sourcePath = MessageFormat.format(ASSEMBLY_SOURCE_DIR_PATTERN, envName, resourceKindName);
							String descriptorPath = MessageFormat.format(ASSEMBLY_FILE_PATTERN, envName, resourceKindName);
							File descriptorFile = new File(request.getBasedir(), descriptorPath);
							
							String packageId = envName + "-" + resourceKindName;
							
							AssemblyDescriptorConfig descriptor = new AssemblyDescriptorConfig(packageId, sourcePath, descriptorFile);
							addFileSets(descriptor, request, resourceKindName, envName);
							
							list.add(new AssemblyConfig(pom, descriptor));
						}
					}
				}
			}
		}
		return list;
	}


	private void addFileSets(AssemblyDescriptorConfig descriptor, PackagingProjectRequest request,
			String resourceKindName, String envName) {
		for (MavenPackage pkg : request.getPackages()) {
			if (resourceKindName.equals(pkg.getKind().name())) {
				for (FileInclude include : pkg.getIncludes()) {
					String sourceDir = MessageFormat.format(include.getSourceDirPattern(), envName);
					String outputDirectory = include.getTargetPath();
					
					descriptor.addFileSet(new MavenFileSet(sourceDir, outputDirectory, include.getIncludeList()));
				}
				return;
			}
		}
		
	}

	private File mergePom(RuntimeServices runtime, VelocityContext context, PackagingProjectRequest request) throws IOException, ParseException {
		File targetFile = new File(request.getBasedir(), POM_TARGET);
		
		ClassLoader classLoader = getClass().getClassLoader();
		
		try (
			InputStream input = classLoader.getResourceAsStream(POM_TEMPLATE); 
			InputStreamReader reader = new InputStreamReader(input)) {
			
			try (FileWriter writer = new FileWriter(targetFile)) {
				Template template = new Template();
				template.setRuntimeServices(runtime);
				template.setData(runtime.parse(reader, "dep.xml"));
				template.initDocument();
				template.merge(context, writer);
			}
			
		}
		
		return targetFile;
		
	}

	private void mergeDescriptors(RuntimeServices runtime, VelocityContext context) throws IOException, ParseException {
		
		@SuppressWarnings("unchecked")
		List<AssemblyConfig> assemblyList = (List<AssemblyConfig>) context.get(ASSEMBLY_LIST);


		ClassLoader classLoader = getClass().getClassLoader();
		Template template = new Template();
		
		try (
			InputStream input = classLoader.getResourceAsStream(DESCRIPTOR_TEMPLATE); 
			InputStreamReader reader = new InputStreamReader(input)
		) {
			template.setRuntimeServices(runtime);
			template.setData(runtime.parse(reader, "dep.xml"));
			template.initDocument();
		}
		for (AssemblyConfig assembly : assemblyList) {
		
			AssemblyDescriptorConfig descriptor = assembly.getDescriptor();
			File targetFile = descriptor.getDescriptorFile();
			context.put(PACKAGE_ID, descriptor.getPackageId());
			context.put(FILE_SET_LIST, descriptor.getFileSetList());
			
			targetFile.getParentFile().mkdirs();
			try (FileWriter writer = new FileWriter(targetFile)) {
				template.merge(context, writer);
				writer.flush();
			}
		}
		
	}
	

}
