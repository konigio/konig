package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
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
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.FileIO.ReadableFile;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;

import io.konig.core.Context;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlJoinCondition;
import io.konig.core.showl.ShowlMapping;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlTemplatePropertyShape;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.IOUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.util.RewriteRule;
import io.konig.core.util.StringUtil;
import io.konig.core.util.ValueFormat.Element;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.shacl.PropertyConstraint;

public class BeamTransformGenerator {
	private static final List<RewriteRule> rewriteRuleList = new ArrayList<>();
	
	static {
		rewriteRuleList.add(new RewriteRule("(DoFn$", "("));
		rewriteRuleList.add(new RewriteRule("@DoFn$", "@"));
		rewriteRuleList.add(new RewriteRule(".DoFn$", ".DoFn."));
	}
	
	private String basePackage;
	private NamespaceManager nsManager;
	private JavaDatatypeMapper datatypeMapper;
	
	public BeamTransformGenerator(String basePackage, NamespaceManager nsManager) {
		this.basePackage = basePackage;
		this.nsManager = nsManager;
		datatypeMapper = new BasicJavaDatatypeMapper();
	}
	
	@SuppressWarnings("deprecation")
	public void generateAll(BeamTransformRequest request) throws BeamTransformGenerationException, IOException {

		
		
		List<File> childProjectList = new ArrayList<>();
		
		for (ShowlNodeShape node : request.getNodeList()) {
			// Consider refactoring so that we don't need to check that explicitDerivedFrom is not empty.
			// The list of nodes from the request should already be filtered!
			
			if (!node.getShape().getExplicitDerivedFrom().isEmpty()) {
				File projectDir = projectDir(request, node);
				childProjectList.add(projectDir);
				JCodeModel model = new JCodeModel();
				try {
					buildPom(request, projectDir);
				} catch (IOException e) {
					throw new BeamTransformGenerationException("Failed to generate pom.xml", e);
				}
				generateTransform(model, node);
				try {
					
					File javaDir = new File(projectDir, "src/main/java");
					javaDir.mkdirs();
					
					model.build(javaDir);
					rewrite(javaDir);
				} catch (IOException e) {
					throw new BeamTransformGenerationException("Failed to save Beam Transform code", e);
				}
			}
		}

		generateBeamParentPom(request, childProjectList);
		
		
	}
	
	private void generateBeamParentPom(BeamTransformRequest request, List<File> childProjectList) throws IOException {
		if (!childProjectList.isEmpty()) {
			File baseDir = request.getProjectDir();
			
			File pomFile = new File(baseDir, "pom.xml");
			VelocityEngine engine = new VelocityEngine();
			engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
			engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
			engine.init();
			
			VelocityContext context = new VelocityContext();
			context.put("groupId", request.getGroupId());
			context.put("artifactId", request.parentArtifactId());
			context.put("version", request.getVersion());
			context.put("childProjectList", childProjectList);
			
			Template template = engine.getTemplate("BeamTransformGenerator/parentPom.xml");
			
			try (FileWriter writer = new FileWriter(pomFile)) {
				template.merge(context, writer);
			}
		}
	}

	private File projectDir(BeamTransformRequest request, ShowlNodeShape node) throws BeamTransformGenerationException {
		URI shapeId = RdfUtil.uri(node.getId());
		if (shapeId == null) {
			fail("NodeShape must be identified by an IRI, but found {0}", node.getId().stringValue());
		}

		File projectDir = request.projectDir(shapeId);
		projectDir.mkdirs();
		return projectDir;
	}
	
	private void rewrite(File file) throws IOException {
		
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				rewrite(child);
			}
		} else {
			IOUtil.replaceAll(file, rewriteRuleList);
		}
		
	}

	private void buildPom(BeamTransformRequest request, File projectDir) throws IOException {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.init();
		
		VelocityContext context = new VelocityContext();
		context.put("groupId", request.getGroupId());
		context.put("artifactId", projectDir.getName());
		context.put("version", request.getVersion());
		
		Template template = engine.getTemplate("BeamTransformGenerator/pom.xml");
		File pomFile = new File(projectDir, "pom.xml");
		
		try (FileWriter writer = new FileWriter(pomFile)) {
			template.merge(context, writer);
		}
		
	}

	/**
	 * Generate the Java code for an Apache Beam transform from the data source to the specified target shape.
	 * @param model The code model in which the Java source code will be stored
	 * @param targetShape The target shape to be transformed.
	 * @throws BeamTransformGenerationException 
	 */
	public JDefinedClass generateTransform(JCodeModel model, ShowlNodeShape targetShape) throws BeamTransformGenerationException {
		Worker worker = new Worker(model, targetShape);
		
		return worker.generateTransform();
	}
	
	private class Worker {
		private JCodeModel model;
		private ShowlNodeShape targetShape;
		
		private JDefinedClass mainClass;
		private JDefinedClass readFileFnClass;
		private JDefinedClass options;
		private JDefinedClass toTargetFnClass;
		
		
		public Worker(JCodeModel model, ShowlNodeShape targetShape) {
			this.model = model;
			this.targetShape = targetShape;
		}

		
		private JDefinedClass generateTransform() throws BeamTransformGenerationException {
			
			try {
				declareReadFileFnClass();
				declareToTargetClass();
				declareMainClass();
				return mainClass;
			} catch (JClassAlreadyExistsException e) {
				throw new BeamTransformGenerationException("Failed to generate transform for ", e);
			}
		}
		
		private void declareToTargetClass() throws BeamTransformGenerationException, JClassAlreadyExistsException {
			
			ToTargetFnGenerator generator = new ToTargetFnGenerator();
			generator.generate();
		}


		void declareReadFileFnClass() throws JClassAlreadyExistsException, BeamTransformGenerationException {
			ReadFileFnGenerator generator = new ReadFileFnGenerator();
			generator.generate();
		}
		
		private class ToTargetFnGenerator {

			private void generate() throws BeamTransformGenerationException, JClassAlreadyExistsException {
				String prefix = namespacePrefix(targetShape.getId());
				String localName = RdfUtil.localName(targetShape.getId());
				String className = className(prefix, "To" + localName + "Fn");
				
				toTargetFnClass = model._class(className);

				// public class ReadFileFn extends DoFn<FileIO.ReadableFile, TableRow> {
				
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				AbstractJClass doFnClass = model.ref(DoFn.class).narrow(tableRowClass).narrow(tableRowClass);
				
				toTargetFnClass._extends(doFnClass);
				
				processElement();
				
			}

			private void processElement() throws BeamTransformGenerationException {
				
				// @ProcessElement
				// public void processElement(ProcessContext c) {
				
				JMethod method = 
						toTargetFnClass.method(JMod.PUBLIC, model.VOID, "processElement");
				
				method.annotate(model.directClass(ProcessElement.class.getName()));
				AbstractJClass processContextClass = model.directClass(ProcessContext.class.getName());
				JVar c = method.param(processContextClass, "c");
				
				//   try {
				//     TableRow inputRow = c.element();
				//     TableRow outputRow = new TableRow();
				
				JTryBlock tryBlock = method.body()._try();

				AbstractJClass tableRowClass = model.ref(TableRow.class);
				
				JVar inputRow = tryBlock.body().decl(tableRowClass, "inputRow", c.invoke("element"));
				JVar outputRow = tryBlock.body().decl(tableRowClass, "outputRow", tableRowClass._new());
				
				for (ShowlDirectPropertyShape p : targetShape.getProperties()) {
					transformProperty(tryBlock.body(), p, inputRow, outputRow);
				}
				
				//     if (!outputRow.isEmpty()) {
				//       c.output(outputRow);
				//     }
				
				tryBlock.body()
					._if(outputRow.invoke("isEmpty").not())
					._then().add(c.invoke("output").arg(outputRow));
				
				//   } catch (Throwable e) {
				//     e.printStackTrace();
				//   }
				AbstractJClass throwableClass = model.ref(Throwable.class);
				JCatchBlock catchBlock = tryBlock._catch(throwableClass);
				JVar oopsVar = catchBlock.param("oops");
				catchBlock.body().add(oopsVar.invoke("printStackTrace"));
				// }
				
			}

			private void transformProperty(JBlock body, ShowlDirectPropertyShape p, JVar inputRow, JVar outputRow) throws BeamTransformGenerationException {
				
				ShowlMapping mapping = p.getSelectedMapping();
				if (mapping == null) {
					fail("Mapping not found for property {0}");
				}
				
				ShowlPropertyShape other = mapping.findOther(p);
				
				if (other instanceof ShowlTemplatePropertyShape) {
					
					transformTemplateProperty(body, p, (ShowlTemplatePropertyShape)other, inputRow, outputRow);
					
				} else if (other instanceof ShowlDirectPropertyShape) {
					transformDirectProperty(body, p, (ShowlDirectPropertyShape)other, inputRow, outputRow);
					
				} else {
					other = other.getPeer();
					if (other instanceof ShowlDirectPropertyShape) {
						transformDirectProperty(body, p, (ShowlDirectPropertyShape) other, inputRow, outputRow);
					} else {
						fail("Failed to transform {0}", p.getPath());
					}
					
				} 
				
				
				
			}

			private void transformDirectProperty(JBlock body, ShowlDirectPropertyShape p, ShowlDirectPropertyShape other,
					JVar inputRow, JVar outputRow) {
				
				// Object $sourcePropertyName = inputRow.get("$sourcePropertyName");
				String sourcePropertyName = other.getPredicate().getLocalName();
				AbstractJClass objectClass = model.ref(Object.class);
				JVar sourcePropertyVar = body.decl(objectClass, sourcePropertyName, 
						inputRow.invoke("get").arg(JExpr.lit(sourcePropertyName)));
				
				// if ($sourcePropertyVar != null) {
				//   outputRow.set("$targetPropertyName", inputRow.get("$sourcePropertyName");
				// }

				String targetPropertyName = p.getPredicate().getLocalName();
				body._if(sourcePropertyVar.ne(JExpr._null()))._then().add(outputRow.invoke("set")
						.arg(JExpr.lit(targetPropertyName))
						.arg(sourcePropertyVar));
				
			}

			private void transformTemplateProperty(JBlock body, ShowlDirectPropertyShape p,
					ShowlTemplatePropertyShape other, JVar inputRow, JVar outputRow) throws BeamTransformGenerationException {
				
				// StringBuilder $builder = new StringBuilder();
				
				String targetPropertyName = p.getPredicate().getLocalName();
				
				AbstractJClass stringBuilderClass = model.ref(StringBuilder.class);
				String builderName = targetPropertyName + "Builder";
				
				JVar builder = body.decl(stringBuilderClass, builderName, stringBuilderClass._new());
			
				
				IriTemplate template = other.getTemplate();
				
				Context context = template.getContext();
				
				
				
				for (Element e : template.toList()) {
					switch (e.getType()) {
					case TEXT :
						// $builder.add("$e.getText()");
						body.add(builder.invoke("append").arg(JExpr.lit(e.getText())));
						break;
						
					case VARIABLE :
						// $builder.add(inputRow.get("$varName"));
						String simpleName = e.getText();
						URI predicate = new URIImpl(context.expandIRI(simpleName));
						ShowlDirectPropertyShape directProperty = directProperty(other.getDeclaringShape(), predicate);
						String varName = directProperty.getPredicate().getLocalName();
						body.add(builder.invoke("append").arg(inputRow.invoke("get").arg(varName)));
						break;
						
					}
				}
				
				// outputRow.set("$targetPropertyName", $builder.toString());
				
				body.add(outputRow.invoke("set").arg(JExpr.lit(targetPropertyName)).arg(builder.invoke("toString")));

				// TODO Auto-generated method stub
			}

			private ShowlDirectPropertyShape directProperty(ShowlNodeShape declaringShape, URI predicate) throws BeamTransformGenerationException {
				
				for (ShowlPropertyShape p : declaringShape.out(predicate)) {
					if (p instanceof ShowlDirectPropertyShape) {
						return (ShowlDirectPropertyShape) p;
					}
					ShowlPropertyShape peer = p.getPeer();
					if (peer instanceof ShowlDirectPropertyShape) {
						return (ShowlDirectPropertyShape) peer;
					}
				}
				fail("Direct property ''{0}'' not found in {1}", predicate.getLocalName(), declaringShape.getPath());
				return null;
			}
			
		}

		private class ReadFileFnGenerator {

			Map<Class<?>, JMethod> getterMap = new HashMap<>();

			private void generate() throws JClassAlreadyExistsException, BeamTransformGenerationException {
				
				// public class ReadFileFn extends DoFn<FileIO.ReadableFile, TableRow> {
				
				List<ShowlJoinCondition> joinList = targetShape.getSelectedJoins();
				
				if (joinList.size() != 1) {
					fail("Joins not supported yet.");
				}
				
				ShowlJoinCondition join = joinList.get(0);
				ShowlNodeShape otherNode = join.otherNode(targetShape);
				String sourceShapeName = RdfUtil.uri(otherNode.getId()).getLocalName();
				
				String simpleClassName = "Read" + sourceShapeName + "Fn";
				
				String nsPrefix = namespacePrefix(otherNode.getId());
				
				readFileFnClass = model._class(className(nsPrefix + "." + simpleClassName));
				
				
				AbstractJClass superClass =  model.directClass(DoFn.class.getName()).narrow(ReadableFile.class, TableRow.class);
			
				readFileFnClass._extends(superClass);
				
				processElement();
				
				
				
			}







			private void processElement() throws BeamTransformGenerationException {
				
				Set<ShowlPropertyShape> sourceProperties = sourceProperties();
				
				
				
				// @ProcessElement
				// public void processElement(ProcessContext c) {
				
				JMethod method = 
						readFileFnClass.method(JMod.PUBLIC, model.VOID, "processElement");
				method.annotate(model.directClass(ProcessElement.class.getName()));
				AbstractJClass processContextClass = model.directClass(ProcessContext.class.getName());
				
				JVar c = method.param(processContextClass, "c");
				
				//   try {
				//     FileIO.ReadableFile f = c.element();
				
				JBlock body = method.body();
				JTryBlock tryBlock = body._try();
				
				body = tryBlock.body();
				
				AbstractJClass readableFileType = model.ref(ReadableFile.class);
				JVar f = body.decl(readableFileType, "f").init(c.invoke("element"));
				
				//     ReadableByteChannel rbc = f.open();
				
				AbstractJClass readableByteChannelType = model.ref(ReadableByteChannel.class);
				JVar rbc = body.decl(readableByteChannelType, "rbc").init(f.invoke("open"));
				
				
				//     InputStream stream = Channels.newInputStream(rbc);
				AbstractJClass inputStreamType = model.ref(InputStream.class);
				AbstractJClass channelsClass = model.directClass(Channels.class.getName());
				
				JVar stream = body.decl(inputStreamType, "stream").init(
						channelsClass.staticInvoke("newInputStream").arg(rbc));
				
				//     try {

				JTryBlock innerTry = body._try();
				
				//       CSVParser csv = CSVParser.parse(stream, StandardCharsets.UTF_8, CSVFormat.RFC4180);

				JBlock innerBody = innerTry.body();
				
				AbstractJClass csvParserClass = model.ref(CSVParser.class);
				AbstractJClass standardCharsetsClass = model.ref(StandardCharsets.class);
				AbstractJClass csvFormatClass = model.ref(CSVFormat.class);
				
				JVar csv = innerBody.decl(csvParserClass, "csv").init(
						csvParserClass.staticInvoke("parse")
						.arg(stream)
						.arg(standardCharsetsClass.staticRef("UTF_8"))
						.arg(csvFormatClass.staticRef("RFC4180")));
				
				//       for(CSVRecord record : csv) {
				
				AbstractJClass csvRecordClass = model.ref(CSVRecord.class);
				JForEach forEachRecordLoop = innerBody.forEach(csvRecordClass, "record", csv);
				JVar record = forEachRecordLoop.var();
				JBlock forEachRecord = forEachRecordLoop.body();

				
				//         TableRow row = new TableRow();
				
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				JVar row = forEachRecord.decl(tableRowClass, "row").init(tableRowClass._new());
				

				for (ShowlPropertyShape sourceProperty : sourceProperties) {
					Class<?> datatype = javaDatatype(sourceProperty);
					AbstractJClass datatypeClass = model.ref(datatype);
					String fieldName = sourceProperty.getPredicate().getLocalName();
					JMethod getter = getterMap.get(datatype);
					
					//     $fieldName = ${getter}(record.get("${fieldName}"));
					JVar fieldVar = forEachRecord.decl(datatypeClass, fieldName, JExpr.invoke(getter).arg(record.invoke("get").arg(JExpr.lit(fieldName))));
					
					//     if ($fieldName != null) {
					//       row.set("$fieldName", $fieldName);
					forEachRecord
							._if(fieldVar.ne(JExpr._null()))
							._then().add(row.invoke("set").arg(JExpr.lit(fieldName)).arg(fieldVar));
					
					
					//     }
					
				}

				
				//         if (!row.isEmpty()) {
				//           c.output(row);
				//         }
				
				forEachRecord._if(row.invoke("isEmpty").not())._then().add(c.invoke("output").arg(row));
				
			
				
				
				//     } finally {
				//        reader.close();
				//     }

				innerTry._finally().add(stream.invoke("close"));
				
				//   } catch (Exception e) {
				//     e.printStackTrace();
				//   }
				
				AbstractJClass exceptionClass = model.directClass(Exception.class.getName());
				
				JCatchBlock catchBlock = tryBlock._catch(exceptionClass);
				JVar e = catchBlock.param("e");
				catchBlock.body().add(e.invoke("printStackTrace"));
				
				
						
				
				
			}



			private Set<ShowlPropertyShape> sourceProperties() throws BeamTransformGenerationException {
				List<ShowlJoinCondition> joinList = targetShape.getSelectedJoins();
				if (joinList.size() != 1) {
					fail("Cannot initialize wanted fields for {0}. Joins not supported yet.", targetShape.getPath());
				}
				ShowlJoinCondition join = joinList.get(0);
				Set<ShowlPropertyShape> set = targetShape.joinProperties(join);
				
				for (ShowlPropertyShape p : set) {
					
					declareDatatypeGetter(p);
					
					
				}
				return set;
			}

			private void declareDatatypeGetter(ShowlPropertyShape p) throws BeamTransformGenerationException {
				
				
				Class<?> javaClass = javaDatatype(p);
				
				if (!getterMap.containsKey(javaClass)) {
					String typeName = StringUtil.camelCase(javaClass.getSimpleName());
					
					String methodName = typeName + "Value";
					
					AbstractJClass returnType = model.ref(javaClass);
					
					

					AbstractJClass stringClass = model.ref(String.class);
					
					// $returnType ${returnType}Value(String stringValue) {
					JMethod method = readFileFnClass.method(JMod.PRIVATE, returnType, methodName);

					getterMap.put(javaClass, method);
					JVar stringValue = method.param(stringClass, "stringValue");
					
					//   if (stringValue != null) {
					
					JConditional if1 = method.body()
						._if(stringValue.ne(JExpr._null()));
					
					//     stringValue = stringValue.trim();
					if1._then().assign(stringValue, stringValue.invoke("trim"));
				
					//     if (stringValue.length() > 0) {
					
					JBlock block1 = if1._then()._if(stringValue.invoke("length").gt(JExpr.lit(0)))._then();
					
					if (javaClass.equals(String.class)) {
						     block1._return(stringValue);
					} else {
						// TODO: Handle other datatypes
						fail("Field type {0} not supported yet, for property {1}.", typeName, p.getPath());
					}
					
					//     }
					//   }
					// }
					
					method.body()._return(JExpr._null());
				}
				
				
			}



			private Class<?> javaDatatype(ShowlPropertyShape p) throws BeamTransformGenerationException {

				PropertyConstraint constraint = p.getPropertyConstraint();
				if (constraint == null) {
					ShowlPropertyShape peer = p.getPeer();
					if (peer != null) {
						constraint = peer.getPropertyConstraint();
					}
				}
				
				if (constraint == null) {
					fail("Property Constraint not found for {0}", p.getPath());
				}
				
				URI datatype = constraint.getDatatype();
				
				if (datatype == null) {
					fail("Expected a datatype property at {0}", p.getPath());
				}
				
				return datatypeMapper.javaDatatype(datatype);
			}

			
		}



		private void declareMainClass() throws BeamTransformGenerationException, JClassAlreadyExistsException {
			String mainClassName = mainClassName(targetShape);
			mainClass = model._class(mainClassName);
			declareOptionsClass();
			
			processMethod();
			mainMethod();
			
		}



		private void mainMethod() {
			
			// public static void main(String[] args) {
			
			JMethod method = mainClass.method(JMod.PUBLIC | JMod.STATIC, model.VOID, "main");
			JVar args = method.param(String[].class, "args");
			
			// Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
			
			AbstractJClass pipelineOptionsFactoryClass = model.ref(PipelineOptionsFactory.class);
			
			JVar optionsVar = method.body().decl(options, "options", 
					pipelineOptionsFactoryClass.staticInvoke("fromArgs").arg(args)
					.invoke("withValidation")
					.invoke("as").arg(options.staticRef("class")));
			
			// process(options);
			
			method.body().add(JExpr.invoke("process").arg(optionsVar));
			
			
		}


		private void declareOptionsClass() throws JClassAlreadyExistsException {

			// public interface Options extends PipelineOptions {
			
			options = mainClass._class(JMod.PUBLIC, "Options", EClassType.INTERFACE);
			options._extends(PipelineOptions.class);
			
			//   @Required
            //   String getSourceUri();

			JMethod getSourceUri = options.method(JMod.PUBLIC, String.class, "getSourceUri");
			getSourceUri.annotate(Required.class);

			//   void setSourceUri(String uri);
			
			options.method(JMod.PUBLIC, model.VOID, "setSourceUri").param(String.class, "uri");
			

			// }
		}



		private void processMethod() {
			
			// public static void process(Options options) {
			
			JMethod method = mainClass.method(JMod.PUBLIC | JMod.STATIC, model.VOID, "process");
			JVar optionsVar = method.param(options, "options");
			
			//   Pipeline p = Pipeline.create(options);
			
			AbstractJClass pipelineType = model.ref(Pipeline.class);
			
			JBlock body = method.body();
			JVar p = body.decl(pipelineType, "p", model.directClass(Pipeline.class.getName())
					.staticInvoke("create").arg(optionsVar));
			
			//   p.apply(FileIO.match().filepattern(options.getSourceUri()))
			
			AbstractJClass fileIoClass = model.directClass(FileIO.class.getName());
			JInvocation pipeline = p.invoke("apply").arg(
					fileIoClass
						.staticInvoke("match")
						.invoke("filepattern").arg(optionsVar.invoke("getSourceUri")));

			//     p.apply(FileIO.readMatches())
			
			pipeline = pipeline.invoke("apply").arg(fileIoClass.staticInvoke("readMatches"));
			
			//     p.apply("ReadFiles", ParDo.of(new $readFileFnClass()))
			
			AbstractJClass parDoClass = model.directClass(ParDo.class.getName());
			pipeline = pipeline.invoke("apply")
					.arg(JExpr.lit("ReadFiles"))
					.arg(parDoClass.staticInvoke("of")
							.arg(readFileFnClass._new()));
			
			//    p.apply("To${targetShapeName}", ParDo.of(new $toTargetFnClass()));
			String targetShapeName = RdfUtil.localName(targetShape.getId());
			String toTargetLabel = "To" + targetShapeName;
			pipeline = pipeline.invoke("apply")
					.arg(JExpr.lit(toTargetLabel))
					.arg(parDoClass.staticInvoke("of")
							.arg(toTargetFnClass._new()));
			
			//   p.apply("Write${targetShapeName}", BigQueryIO.writeTableRows()
			//        .to("$tableSpec").withCreateDisposition(CreateDisposition.CREATE_NEVER)
			//        .withWriteDisposition(WriteDisposition.WRITE_APPEND));
			
			String targetTableSpec = targetTableSpec();
			String writeLabel = "Write" + targetShapeName;
			AbstractJClass bigQueryIoClass = model.ref(BigQueryIO.class);
			AbstractJClass createDispositionClass = model.ref(CreateDisposition.class);
			AbstractJClass writeDispositionClass = model.ref(WriteDisposition.class);
			
			pipeline = pipeline.invoke("apply").arg(JExpr.lit(writeLabel))
					.arg(bigQueryIoClass.staticInvoke("writeTableRows")
							.invoke("to").arg(targetTableSpec)
							.invoke("withCreateDisposition").arg(createDispositionClass.staticRef("CREATE_NEVER"))
							.invoke("withWriteDisposition").arg(writeDispositionClass.staticRef("WRITE_APPEND")));
			
			

			body.add(pipeline);
			
		}

		private String targetTableSpec() {
			// For now we only support BigQuery
			for (DataSource ds : targetShape.getShape().getShapeDataSource()) {
				if (ds instanceof GoogleBigQueryTable) {
					GoogleBigQueryTable table = (GoogleBigQueryTable) ds;
					return table.getQualifiedTableName();
				}
			}
			return null;
		}


		private String mainClassName(ShowlNodeShape targetShape) throws BeamTransformGenerationException {
			
			URI shapeId = RdfUtil.uri(targetShape.getId());
			
			if (shapeId == null) {
				throw new BeamTransformGenerationException("Target Shape must be identified by an IRI");
			}
			
			Namespace ns = nsManager.findByName(shapeId.getNamespace());
			
			if (ns == null) {
				throw new BeamTransformGenerationException("Prefix not found for namespace: " + shapeId.getNamespace());
			}
			
			String prefix = ns.getPrefix();
			
			StringBuilder builder = new StringBuilder();
			builder.append(basePackage);
			builder.append('.');
			builder.append(prefix);
			builder.append('.');
			builder.append(shapeId.getLocalName());
			builder.append("Beam");
			
			return builder.toString();
		}
		
	}



	private BeamTransformGenerationException fail(String pattern, Object...args) throws BeamTransformGenerationException {
		throw new BeamTransformGenerationException(MessageFormat.format(pattern, args));
	}

	

	private String namespacePrefix(Resource id) throws BeamTransformGenerationException {
		if (id instanceof URI) {
			URI uri = (URI) id;
			Namespace ns = nsManager.findByName(uri.getNamespace());
			if (ns != null) {
				return ns.getPrefix();
			}
			fail("Prefix not found for namespace <{0}>", ns.getName());
		}
		fail("URI expected but id is a BNode");
		return null;
	}


	private String className(String simpleName) {
		return basePackage + "." + simpleName;
	}
	
	private String className(String namespacePrefix, String simpleName) throws BeamTransformGenerationException {
		return className(namespacePrefix + "." + simpleName);
	}
}
