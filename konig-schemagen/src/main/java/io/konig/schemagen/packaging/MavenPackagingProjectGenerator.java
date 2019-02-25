package io.konig.schemagen.packaging;

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

public class MavenPackagingProjectGenerator {
	private static final String ASSEMBLY_LIST = "assemblyList";
	private static final String POM_TEMPLATE = "io/konig/schemagen/packaging/pom.xml";
	private static final String DESCRIPTOR_TEMPLATE = "io/konig/schemagen/packaging/dep.xml";
	private static final String ENV_PATH = "target/deploy/src/main/resources/env";
	private static final String POM_TARGET = "target/deploy/pom.xml";
	private static final String ASSEMBLY_FILE_PATTERN = "target/deploy/src/main/assembly/{0}-{1}.xml";
	private static final String ASSEMBLY_DESCRIPTOR_PATTERN = "$'{'project.basedir'}'/src/main/assembly/{0}-{1}.xml";
	private static final String ASSEMBLY_ID_PATTERN = "zip-{0}-{1}";
	private static final String ASSEMBLY_SOURCE_DIR_PATTERN = "$'{'project.basedir'}'/src/main/resources/env/{0}/{1}";
	

	public MavenPackagingProjectGenerator() {
	}
	

	
	public void generate(PackagingProjectRequest request) throws IOException, ParseException {
		RuntimeServices runtime = new RuntimeInstance();
		File velocityLogFile = request.getVelocityLogFile();
		if (velocityLogFile != null) {
			runtime.addProperty("runtime.log", velocityLogFile.getAbsolutePath());
		}
		VelocityContext context = buildContext(request);
	
		mergePom(runtime, context, request);
		mergeDescriptors(runtime, context);
		
		
	}

	private VelocityContext buildContext(PackagingProjectRequest request) {
		VelocityContext context = new VelocityContext();
		context.put("mavenProject", request.getMavenProject());
		context.put(ASSEMBLY_LIST, assemblyList(request));
		return null;
	}

	private List<AssemblyConfig> assemblyList(PackagingProjectRequest request) {
		List<AssemblyConfig> list = new ArrayList<>();
		File envRoot = new File(request.getBasedir(), ENV_PATH);
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
						
						AssemblyDescriptorConfig descriptor = new AssemblyDescriptorConfig(sourcePath, descriptorFile);

						
						list.add(new AssemblyConfig(pom, descriptor));
					}
				}
			}
		}
		return list;
	}

	private void mergePom(RuntimeServices runtime, VelocityContext context, PackagingProjectRequest request) throws IOException, ParseException {
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
		
	}

	private void mergeDescriptors(RuntimeServices runtime, VelocityContext context) throws IOException, ParseException {
		
		@SuppressWarnings("unchecked")
		List<AssemblyConfig> assemblyList = (List<AssemblyConfig>) context.get(ASSEMBLY_LIST);

		ClassLoader classLoader = getClass().getClassLoader();
		Template template = new Template();
		try (
			InputStream input = classLoader.getResourceAsStream(DESCRIPTOR_TEMPLATE); 
			InputStreamReader reader = new InputStreamReader(input)) {
			template.setRuntimeServices(runtime);
			template.setData(runtime.parse(reader, "dep.xml"));
		}
		for (AssemblyConfig assembly : assemblyList) {
		
			AssemblyDescriptorConfig descriptor = assembly.getDescriptor();
			File targetFile = descriptor.getDescriptorFile();
			String sourcePath = descriptor.getSourcePath();
			context.put("sourceDir", sourcePath);
			
				
			try (FileWriter writer = new FileWriter(targetFile)) {
				template.initDocument();
				template.merge(context, writer);
			}
		}
		
	}
	

}
