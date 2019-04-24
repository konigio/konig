package io.konig.schemagen.env;

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
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;


public class EnvironmentGenerator {
	
	private File velocityLog;
	private Set<String> allowedSuffix = new HashSet<>();
	
	public EnvironmentGenerator(File velocityLog) {
		this.velocityLog = velocityLog;
		allowedSuffix.add(".yaml");
	}
	
	/**
	 * Generates environment-specific files from a set of environment-specific configurations.
	 * Here's a synopsis of the file-system layout:
	 * <pre>
	 *   {settingsDir}/env-{environmentName}.properties
	 *   {sourceDir}/blah/someResource.ext
	 *   {targetDir}/env/{environmentName}/blah/someResource.ext
	 * </pre>
	 * 
	 * The output file at
	 * <pre>
	 *    {targetDir}/env/{environmentName}/blah/someResource.ext
	 * </pre>
	 * 
	 * is formed by using the source file 
	 * <pre>
	 *   {sourceDir}/blah/someResource.ext
	 * </pre>
	 * 
	 * as a Velocity template and merging the properties from
	 * <pre>
	 *   {settingsDir}/env-{environmentName}.properties
	 * </pre>
	 * 
	 * into the template.
	 * 
	 * @param settingsDir A directory containing environment-specific configuration files with names of 
	 *                    the form "env-{environmentName}.properties", where {environmentName} is typically 
	 *                    one of "dev", "test", "stage", or "prod".
	 *                    
	 * @param sourceDir   The source directory for resource files that contain environment-specific variables.
	 * @param targetDir   The target directory under which the merged files will be placed.
	 *
	 */
	public void run(File settingsDir, File sourceDir, File targetDir) throws EnvironmentGenerationException, FileNotFoundException, IOException {
		if (settingsDir.isDirectory()) {
			Worker worker = new Worker();
			worker.run(settingsDir, sourceDir, targetDir);
		}
	}

	private class Worker {

		private VelocityContext context;
		private RuntimeServices runtime;
		
		private void buildContext(Properties properties) {
			context = new VelocityContext();
			for (Entry<Object,Object> e : properties.entrySet()) {
				String key = e.getKey().toString();
				Object value = e.getValue().toString();
				
				context.put(key, value);
			}
		}
		
		private void assemblyEnvironmentProperties(File settingsDir) throws FileNotFoundException, IOException {
			Map<String,Properties> map = new HashMap<>();
			boolean isDirty = false;
			for (File file : settingsDir.listFiles()) {
				String fileName = file.getName();
				if (fileName.endsWith(".properties")) {
					if (fileName.startsWith("env-")) {
						int dot = fileName.lastIndexOf('.');
						String envName = fileName.substring(4, dot);
						Properties p = properties(map, envName);
						try (FileReader reader = new FileReader(file)) {
							p.load(reader);
						}
					} else {
						Properties p = new Properties();
						try (FileReader reader = new FileReader(file)) {
							p.load(reader);
						}
						Enumeration<?> sequence = p.propertyNames();
						while (sequence.hasMoreElements()) {
							String propertyName = sequence.nextElement().toString();
							if (propertyName.startsWith("env.")) {
								int start = propertyName.indexOf('.');
								int end = propertyName.indexOf('.', start+1);
								if (end > start) {
									String value = p.getProperty(propertyName);
									String envName = propertyName.substring(start+1, end);
									propertyName = propertyName.substring(end+1);
									
									Properties env = properties(map, envName);
									env.setProperty(propertyName, value);
									isDirty = true;
								}
							}
						}
					}
				}
			}
			if (isDirty) {
				for (Entry<String,Properties> e : map.entrySet()) {
					String envName = e.getKey();
					Properties p = e.getValue();
					
					String fileName = "env-" + envName + ".properties";
					File file = new File(settingsDir, fileName);
					try (FileWriter writer = new FileWriter(file)) {
						p.store(writer, fileName);
					}
				}
			}
		}
		
		private Properties properties(Map<String, Properties> map, String envName) {
			Properties p = map.get(envName);
			if (p == null) {
				p = new Properties();
				map.put(envName, p);
			}
			return p;
		}

		private void run(File settingsDir, File sourceDir, File targetDir) throws EnvironmentGenerationException, FileNotFoundException, IOException {
			assemblyEnvironmentProperties(settingsDir);
			
			FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File file) {
					String name = file.getName();
					return name.startsWith("env-") && name.endsWith(".properties");
				}
			};

			runtime = new RuntimeInstance();
			if (velocityLog != null) {
				runtime.addProperty("runtime.log", velocityLog.getAbsolutePath());
			}
			for (File propertiesFile : settingsDir.listFiles(filter)) {
				String fileName = propertiesFile.getName();
				int dot = fileName.lastIndexOf('.');
				String envName = fileName.substring(4, dot);
				
				File outDir = new File(targetDir, envName);
				
				buildContext(propertiesFile);
				context.put("environmentName", envName);
				Environment env = new Environment(envName, outDir);
				
				generate(sourceDir, env);
			}
		}
		
		

		private void buildContext(File source) throws FileNotFoundException, IOException {
			Properties properties = new Properties();
			try (FileReader reader = new FileReader(source)) {
				properties.load(reader);
			}
			buildContext(properties);
			
		}

		private void generate(File source, Environment env) throws EnvironmentGenerationException {
			File outDir = env.getOutDir();
			try {
				merge(source, outDir);
			} catch (IOException | ParseException e) {
				throw new EnvironmentGenerationException("Failed to generate environment: " + env.getName(), e);
			}
			
		}
		
		private void merge(File source, File target) throws FileNotFoundException, IOException, ParseException {
			for (File sourceFile : source.listFiles()) {
				File targetFile = new File(target, sourceFile.getName());
				
				if (sourceFile.isDirectory()) {
					merge(sourceFile, targetFile);
				} else  if (accept(sourceFile)){
					File parent = targetFile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
					Template template = new Template();
					
					try (FileReader reader = new FileReader(sourceFile)) {
						try (FileWriter writer = new FileWriter(targetFile)) {

							template.setRuntimeServices(runtime);
							template.setData(runtime.parse(reader, targetFile.getName()));
							template.initDocument();
							template.merge(context, writer);
						}
					}

				}
				
			}
			
		}

		private boolean accept(File sourceFile) {
			String fileName = sourceFile.getName();
			int dot = fileName.lastIndexOf('.');
			if (dot > 0) {
				String suffix = fileName.substring(dot);
				return allowedSuffix.contains(suffix);
			}
			return false;
		}

		
	}

}
