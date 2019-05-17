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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.FileIO.ReadableFile;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JEnumConstant;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JStringLiteral;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;

import io.konig.core.Context;
import io.konig.core.Edge;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlClass;
import io.konig.core.showl.ShowlDataSource;
import io.konig.core.showl.ShowlDerivedPropertyExpression;
import io.konig.core.showl.ShowlDerivedPropertyShape;
import io.konig.core.showl.ShowlDirectPropertyExpression;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEnumPropertyExpression;
import io.konig.core.showl.ShowlEqualStatement;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlFunctionExpression;
import io.konig.core.showl.ShowlIriReferenceExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlStatement;
import io.konig.core.showl.ShowlStaticPropertyShape;
import io.konig.core.showl.ShowlTemplatePropertyShape;
import io.konig.core.showl.StaticDataSource;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.IOUtil;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.util.RewriteRule;
import io.konig.core.util.StringUtil;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.datasource.DataSource;
import io.konig.formula.Expression;
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;
import io.konig.formula.IriTemplateExpression;
import io.konig.formula.LiteralFormula;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class BeamTransformGenerator {
	private static final Logger logger = LoggerFactory.getLogger(BeamTransformGenerator.class);
	private static final List<RewriteRule> rewriteRuleList = new ArrayList<>();
	
	static {
		rewriteRuleList.add(new RewriteRule("(DoFn$", "("));
		rewriteRuleList.add(new RewriteRule("@DoFn$", "@"));
		rewriteRuleList.add(new RewriteRule(".DoFn$", ".DoFn."));
	}
	
	private String basePackage;
	private NamespaceManager nsManager;
	private JavaDatatypeMapper datatypeMapper;
	private OwlReasoner reasoner;
	
	public BeamTransformGenerator(String basePackage, OwlReasoner reasoner) {
		this.basePackage = basePackage;
		this.reasoner = reasoner;
		this.nsManager = reasoner.getGraph().getNamespaceManager();
		datatypeMapper = new BasicJavaDatatypeMapper();
	}
	
	@SuppressWarnings("deprecation")
	public void generateAll(BeamTransformRequest request) throws BeamTransformGenerationException, IOException {

		
		
		List<File> childProjectList = new ArrayList<>();
		
		for (ShowlNodeShape node : request.getNodeList()) {
			// Consider refactoring so that we don't need to check that explicitDerivedFrom is not empty.
			// The list of nodes from the request should already be filtered!
			
//			if (!node.getShape().getExplicitDerivedFrom().isEmpty()) {
				File projectDir = projectDir(request, node);
				childProjectList.add(projectDir);
				JCodeModel model = new JCodeModel();
				
				try {
					buildPom(request, projectDir, node);
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
//			}
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

	private void buildPom(BeamTransformRequest request, File projectDir, ShowlNodeShape node) throws IOException, BeamTransformGenerationException {
		VelocityEngine engine = new VelocityEngine();
		engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		engine.init();
		
		VelocityContext context = new VelocityContext();
		context.put("groupId", request.getGroupId());
		context.put("artifactId", projectDir.getName());
		context.put("version", request.getVersion());
		context.put("projectName", projectDir.getName());
		context.put("batchEtlBucketIri", batchEtlBucketIri(node));
		
		Worker w = new Worker(null,null);
		String mainClassName = w.mainClassName(node);		
		context.put("mainClass", mainClassName);		
		
		for (DataSource ds : node.getShape().getShapeDataSource()) {
			if (ds instanceof GoogleBigQueryTable) {
				GoogleBigQueryTable table = (GoogleBigQueryTable) ds;
				StringBuilder builder = new StringBuilder();
				builder.append(table.getTableReference().getDatasetId());
				builder.append('-');
				builder.append(table.getTableReference().getTableId());
				builder.append('-');
				builder.append("BatchPipeline");
				context.put("templateName", builder.toString());
			}
		}
		
		Template template = engine.getTemplate("BeamTransformGenerator/pom.xml");
		File pomFile = new File(projectDir, "pom.xml");
		
		try (FileWriter writer = new FileWriter(pomFile)) {
			template.merge(context, writer);
		}
		
	}
	
	
	private String batchEtlBucketIri(ShowlNodeShape node) throws BeamTransformGenerationException {
		for (ShowlChannel channel : node.getChannels()) {
			ShowlDataSource ds = channel.getSourceNode().getShapeDataSource();
			
			if (ds == null) {
				for (DataSource s : channel.getSourceNode().getShape().getShapeDataSource()) {
					String result = bucketBaseIri(s.getId());
					if (result != null) {
						return result;
					}
				}
			} else {
				String result = bucketBaseIri(ds.getDataSource().getId());
				if (result != null) {
					return result;
				}
			}
		}
		fail("Could not detect batchEtlBucketIri for {0}", node.getPath());
		return null;
	}

	private String bucketBaseIri(Resource id) {
		if (id != null) {
			String value = id.stringValue();
			if (value.startsWith("gs://")) {
				int end = value.indexOf('/', 5);
				if (end > 0) {
					return value.substring(0,  end);
				}
				return value;
			}
		}
		return null;
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
		private ShowlNodeShape targetNode;
		
		private JDefinedClass mainClass;
		private Map<URI,SourceInfo> sourceInfoMap = new LinkedHashMap<>();
//		private Map<URI,JDefinedClass> readFileFnMap = new HashMap<>();
//		private JDefinedClass readFileFnClass;
		private JDefinedClass optionsClass;
		private JDefinedClass toTargetFnClass;
		
		private JDefinedClass iriClass;
		
		private Map<URI,Map<URI, RdfProperty>> enumClassProperties = new HashMap<>();
		
		
		public Worker(JCodeModel model, ShowlNodeShape targetShape) {
			this.model = model;
			this.targetNode = targetShape;
		}


		private boolean singleSource() {
			return targetNode.getChannels().size()==1;
		}

		private void assertTrue(boolean isTrue, String pattern, Object...args) throws BeamTransformGenerationException {
			if (!isTrue) {
				fail(pattern, args);
			}
			
		}
		
		/**
		 * Get the Java Class for the datatype of a given property.
		 * @param p
		 * @return The Java Class for the datatype of the property 'p', or null if 'p' is an ObjectProperty.
		 */
		private Class<?> javaDatatype(ShowlPropertyShape p) throws BeamTransformGenerationException {

			Class<?> result = tryJavaDatatype(p);
			if (result == null) {
				fail("Java datatype not found for {0}", p.getPath());
			}
			return result;
		}
		
		private Class<?> javaType(ShowlPropertyShape p) throws BeamTransformGenerationException {
			Class<?> type = tryJavaDatatype(p);
			if (type == null) {
				
				if (Konig.id.equals(p.getPredicate())) {
					return String.class;
				}
				PropertyConstraint constraint = p.getPropertyConstraint();
				if (constraint != null && constraint.getNodeKind()==NodeKind.IRI && constraint.getShape()==null) {
					// IRI reference
					
					return String.class;
				}
				
				
				fail("Failed to determine Java type of {0}", p.getPath());
			}
			
			return type;
			
		}
		
		private Class<?> tryJavaDatatype(ShowlPropertyShape p) {

			PropertyConstraint constraint = p.getPropertyConstraint();
			if (constraint == null) {
				ShowlPropertyShape peer = p.getPeer();
				if (peer != null) {
					constraint = peer.getPropertyConstraint();
				}
			}
			
			if (constraint == null) {
				return null;
			}
			
			URI datatype = constraint.getDatatype();
			
			if (datatype == null) {
				return null;
			}
			
			return datatypeMapper.javaDatatype(datatype);
		}
		
		private JDefinedClass generateTransform() throws BeamTransformGenerationException {
			
			try {
				declareEnumClasses();
				declareReadFileFnClass();
				declareToTargetClass();
				declareMainClass();
				return mainClass;
			} catch (JClassAlreadyExistsException e) {
				throw new BeamTransformGenerationException("Failed to generate transform for ", e);
			}
		}
		
		private void declareEnumClasses() throws BeamTransformGenerationException {
			
			Set<ShowlClass> enumClasses = new HashSet<>();
			addEnumClasses(enumClasses, targetNode);
			
			if (!enumClasses.isEmpty()) {
				declareIriClass();
			}
			
			for (ShowlClass enumClass : enumClasses) {
				declareEnumClass(enumClass.getId());
			}
			
		}


		private void declareIriClass() throws BeamTransformGenerationException {
			String iriClassName = iriClassName();
			try {
				iriClass = model._class(iriClassName);
				AbstractJClass stringClass = model.ref(String.class);
				
				JFieldVar namespace = iriClass.field(JMod.PRIVATE, stringClass, "namespace");
				JFieldVar localName = iriClass.field(JMod.PRIVATE, stringClass, "localName");
				
				JMethod ctor = iriClass.constructor(JMod.PUBLIC);
				JVar namespaceParam = ctor.param(stringClass, "namespace");
				JVar localNameParam = ctor.param(stringClass, "localName");
			
				ctor.body().assign(JExpr._this().ref(namespace), namespaceParam);
				ctor.body().assign(JExpr._this().ref(localName), localNameParam);
				
				iriClass.method(JMod.PUBLIC, stringClass, "getNamespace").body()._return(namespace);
				iriClass.method(JMod.PUBLIC, stringClass, "getLocalName").body()._return(localName);
				
				iriClass.method(JMod.PUBLIC, stringClass, "stringValue").body()._return(namespace.plus(localName));
				
				iriClass.method(JMod.PUBLIC, stringClass, "toString").body()._return(JExpr.invoke("stringValue"));
				
				
			} catch (JClassAlreadyExistsException e) {
				throw new BeamTransformGenerationException("Failed to declare IRI class", e);
			}
			
			
		}


		private String iriClassName() {
			StringBuilder builder = new StringBuilder();
			builder.append(basePackage);
			builder.append(".rdf.IRI");
			return builder.toString();
		}


		private void declareEnumClass(URI owlClass) throws BeamTransformGenerationException {
			
			if (enumClassProperties.containsKey(owlClass)) {
				return;
			}
			
			String enumClassName = enumClassName(owlClass);
			
			
			try {
				// public class $enumClassName {
				JDefinedClass enumClass = model._class(enumClassName, EClassType.ENUM);
				
				List<Vertex> individuals = reasoner.getGraph().getVertex(owlClass).asTraversal().in(RDF.TYPE).toVertexList();

				Map<URI, RdfProperty> propertyMap = enumProperties(individuals);
				
				Map<URI, JFieldVar> enumIndex = enumIndex(enumClass, propertyMap);
				
				enumClassProperties.put(owlClass, propertyMap);
				
				JBlock staticInit = enumClass.init();
				for (Vertex individual : individuals) {
					enumMember(enumIndex, enumClass, staticInit, individual);
				}
				
				for (Map.Entry<URI, JFieldVar> entry : enumIndex.entrySet()) {
					URI property = entry.getKey();
					JFieldVar field = entry.getValue();
					
					String methodName = "findBy" + StringUtil.capitalize(property.getLocalName());
					// For now we assume that inverse functional properties have a String datatype.
					
					RdfProperty rdf = propertyMap.get(property);
					
					AbstractJClass propertyType = model.ref(datatypeMapper.javaDatatype(rdf.getRange()));
					
					JMethod method = enumClass.method(JMod.STATIC | JMod.PUBLIC , enumClass, methodName);
					JVar param = method.param(propertyType, property.getLocalName());
					
					method.body()._return(field.invoke("get").arg(param));
							 
				}
				
				//   private IRI id;
				
				JFieldVar idField = enumClass.field(JMod.PRIVATE, iriClass, "id");
				AbstractJClass stringClass = model.ref(String.class);
				
				//   public $enumClassName id(String namespace, String localName) {
				//     id = new IRI(namespace, localName);
				//     return this;
				//   }
				
				JMethod idMethod = enumClass.method(JMod.PRIVATE, enumClass, "id");
				JVar namespaceParam = idMethod.param(stringClass, "namespace");
				JVar localNameParam = idMethod.param(stringClass, "localName");
				
				idMethod.body().assign(idField, iriClass._new().arg(namespaceParam).arg(localNameParam));
				idMethod.body()._return(JExpr._this());
				
				
				//  public IRI getId() {
				//    return id;
				//  }
				
				enumClass.method(JMod.PUBLIC, iriClass, "getId").body()._return(idField);
				
				
				for (RdfProperty rdfProperty : propertyMap.values()) {
					URI propertyId = rdfProperty.getId();
					URI range = rdfProperty.getRange();
					
					
					
					Class<?> fieldClass = datatypeMapper.javaDatatype(range);
					
					AbstractJClass datatypeClass = fieldClass==null ? 
							model.directClass(enumClassName(range)) : 
							model.ref(fieldClass);
							
					String fieldName = propertyId.getLocalName();
					
					
					//  private $datatypeClass $fieldName;
					
					JFieldVar field = enumClass.field(JMod.PRIVATE, datatypeClass, fieldName);
					
					//  public $enumClassName $fieldName($datatypeClass $fieldName) {
					//    this.$fieldName = $fieldName;
					//    return this;
					//  }
					
					JMethod setter = enumClass.method(JMod.PRIVATE, enumClass, fieldName);
					JVar param = setter.param(datatypeClass, fieldName);
					setter.body().assign(JExpr._this().ref(field), param);
					setter.body()._return(JExpr._this());
					
					JMethod getter = enumClass.method(JMod.PUBLIC, datatypeClass, "get" + StringUtil.capitalize(fieldName));
					getter.body()._return(field);
			
				}
				
				
			} catch (JClassAlreadyExistsException e) {
				throw new BeamTransformGenerationException("Failed to declare enum " + owlClass.stringValue(), e);
			}
		}

		private Map<URI, JFieldVar> enumIndex(JDefinedClass enumClass, Map<URI, RdfProperty> propertyMap) {
			Map<URI, JFieldVar> map = new HashMap<>();
			for (RdfProperty property : propertyMap.values()) {

				URI propertyId = property.getId();
				JFieldVar mapField = null;
				if (reasoner.isInverseFunctionalProperty(propertyId)) {
					Class<?> datatypeJavaClass = datatypeMapper.javaDatatype(property.getRange());
					AbstractJClass datatypeClass = model.ref(datatypeJavaClass);
					String fieldName = propertyId.getLocalName();
					AbstractJClass mapClass = model.ref(Map.class).narrow(datatypeClass, enumClass);
					AbstractJClass hashMapClass = model.ref(HashMap.class).narrow(datatypeClass, enumClass);
					mapField = enumClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, mapClass, fieldName + "Map", hashMapClass._new());
					
					map.put(propertyId, mapField);
				}
			}
			return map;
		}


		private void enumMember(Map<URI, JFieldVar> enumIndex, JDefinedClass enumClass, JBlock staticInit, Vertex individual) throws BeamTransformGenerationException {
			String fieldName = enumMemberName(RdfUtil.uri(individual.getId()));
			
			// Suppose we are building the following enumeration...
			
			// public enum GenderType {
			//   Male,
			//   Female;
			//   ..
			// }
			
			// Then 'Male' and 'Female' are the enum constants.
			
			// The next line will create the enum constant, or get the existing constant if it was already created.
			
			
			JEnumConstant constant = enumClass.enumConstant(fieldName);
			
			// We build static initializers for each member of the enumeration, and we build
			// a map for for inverse functional properties.  For example, if genderCode is
			// inverse functional, we would have...
			
			//  static {
			//    Male.id("http://schema.org/", "Male").name("Male").genderCode("M");
			//    genderCodeMap.put("M", Male);
			//
			//    Female.id("http://schema.org/", "Female").name("Female").genderCode("F");
			//    genderCodeMap.put("M", Female);
			//  }
			
			URI individualId = RdfUtil.uri(individual.getId());
			
			JInvocation invoke = constant.invoke("id")
					.arg(JExpr.lit(individualId.getNamespace()))
					.arg(JExpr.lit(individualId.getLocalName()));
			 
			outerLoop : for (Edge edge : individual.outEdgeSet()) {
				URI predicate = edge.getPredicate();
				if (RDF.TYPE.equals(predicate)) {
					continue;
				}
				
				Value object = edge.getObject();
				
				if (object instanceof URI) {
					
					URI objectId = (URI) object;
					if (reasoner.isEnumerationMember(objectId)) {
						
						
						Set<URI> objectTypeSet = reasoner.getGraph().v(objectId).out(RDF.TYPE).toUriSet();
						for (URI objectType : objectTypeSet) {
							if (Schema.Enumeration.equals(objectType)) {
								continue;
							}
							if (reasoner.isEnumerationClass(objectType)) {
								declareEnumClass(objectType);
								
								String objectClassName = enumClassName(objectType);
								AbstractJClass objectClass = model.directClass(objectClassName);
								
								// Male.subject(Category.Demographics);
								
								invoke = invoke.invoke(predicate.getLocalName()).arg(
										objectClass.staticRef(enumMemberName(objectId)));
								
								// For now, we do not support IRI references to be inverse functional properties suitable
								// for indexing.
								
								continue outerLoop;
							}
						}
					}
					
					
				}
				
				Literal literal = null;
				if (object instanceof Literal) {
					literal = (Literal) object;
				}
				
				if (literal == null) {
					fail("Cannot build enum member {0}.  Object Property not suported: {1}", 
						RdfUtil.compactId(individual.getId(), nsManager),
						RdfUtil.compactId(predicate, nsManager));
				}
				
				
				JStringLiteral litValue = JExpr.lit(object.stringValue());
				invoke = invoke.invoke(predicate.getLocalName()).arg(litValue);
				
				JFieldVar mapField = enumIndex.get(predicate);
				if (mapField != null) {
					staticInit.add(mapField.invoke("put").arg(litValue).arg(constant));
				}
				
					
			}

			staticInit.add(invoke);
			
			
		}


		private Map<URI, RdfProperty> enumProperties(List<Vertex> individuals) throws BeamTransformGenerationException {
			Map<URI, RdfProperty> map = new HashMap<>();
			outerLoop : for (Vertex v : individuals) {
				for (Edge e : v.outEdgeSet()) {
					URI predicate = e.getPredicate();
					if (RDF.TYPE.equals(predicate) || map.containsKey(predicate)) {
						continue;
					}
					Value object = e.getObject();
					if (object instanceof Literal) {
						Literal literal = (Literal) object;
						map.put(predicate, new RdfProperty(predicate, literal.getDatatype()));
					} else if (object instanceof URI) {
						URI objectId = (URI) object;
						
						Set<URI> typeSet = reasoner.getGraph().v(objectId).out(RDF.TYPE).toUriSet();
						for (URI typeId : typeSet) {
							if (typeId.equals(Schema.Enumeration)) {
								continue;
							}
							if (reasoner.isEnumerationClass(typeId)) {
								map.put(predicate, new RdfProperty(predicate, typeId));
								continue outerLoop;
							}
						}
						
						fail("Object property {0} not supported on individual {1}", 
								RdfUtil.compactId(predicate, nsManager),
								RdfUtil.compactId(objectId, nsManager));
					}
				}
			}
			return map;
		}

		private String enumMemberName(URI individualId) {
			
			String localName = individualId.getLocalName();
			localName = localName.replace("%20", "_");
			localName = localName.replace("%", "x");
			
			return localName;
		}

		private String enumClassName(URI enumClass) throws BeamTransformGenerationException {
			StringBuilder builder = new StringBuilder();
			builder.append(basePackage);
			builder.append('.');
			
			Namespace ns = nsManager.findByName(enumClass.getNamespace());
			if (ns == null) {
				fail("Prefix not found for namespace: {0}", enumClass.getNamespace());
			}
			builder.append(ns.getPrefix());
			builder.append('.');
			builder.append(enumClass.getLocalName());
			
			return builder.toString();
		}


		private void addEnumClasses(Set<ShowlClass> enumClasses, ShowlNodeShape node) {
			for (ShowlDirectPropertyShape p : node.getProperties()) {
				
				ShowlExpression e = p.getSelectedExpression();
				if (e instanceof ShowlEnumPropertyExpression) {
					ShowlPropertyShape q = ((ShowlEnumPropertyExpression) e).getSourceProperty();
					enumClasses.add(q.getDeclaringShape().getOwlClass());
				}
				if (p.getValueShape() != null) {
					addEnumClasses(enumClasses, p.getValueShape());
				}
			}
			
		}


		private void declareToTargetClass() throws BeamTransformGenerationException, JClassAlreadyExistsException {
			if (singleSource()) {
				ToTargetFnGenerator generator = new ToTargetFnGenerator();
				generator.generate();
			}
		}


		void declareReadFileFnClass() throws JClassAlreadyExistsException, BeamTransformGenerationException {
			
			if (singleSource()) {
			
				ShowlChannel channel = targetNode.getChannels().get(0);
				ShowlNodeShape sourceNode = channel.getSourceNode();
				SourceInfo sourceInfo = new SourceInfo(channel);
				sourceInfoMap.put(RdfUtil.uri(sourceNode.getId()), sourceInfo);
				ReadFileFnGenerator generator = new ReadFileFnGenerator(sourceInfo);
				generator.generate();
			} else {
				declareFileToKvFn();
			}
		}
		
		private void declareFileToKvFn() throws BeamTransformGenerationException, JClassAlreadyExistsException {
			
			for (ShowlChannel channel : targetNode.getChannels()) {
				
				ShowlStatement joinStatement = channel.getJoinStatement();
				
				if (joinStatement == null) {
					continue;
				}
		
				ShowlPropertyShape leftKey = leftKey(joinStatement);
				ShowlPropertyShape rightKey = rightKey(joinStatement);
				
				
				generateFileToKvFn(leftKey, channel(leftKey, channel));
				generateFileToKvFn(rightKey, channel(rightKey, channel));
				
				
			}
			
		}
		
	
		

		private ShowlChannel channel(ShowlPropertyShape key, ShowlChannel channel) throws BeamTransformGenerationException {
			ShowlNodeShape sourceNode = key.getDeclaringShape();
			if (sourceNode == channel.getSourceNode()) {
				return channel;
			}
			for (ShowlChannel c : targetNode.getChannels()) {
				if (sourceNode == c.getSourceNode()) {
					return c;
				}
			}
			
			throw new BeamTransformGenerationException("Channel not found for " + key.getPath());
		}


		private void generateFileToKvFn(ShowlPropertyShape keyProperty, ShowlChannel channel) throws BeamTransformGenerationException, JClassAlreadyExistsException {
			
			ShowlNodeShape node = channel.getSourceNode();
			if (isEnumNode(node)) {
				return;
			}
			
			if (logger.isTraceEnabled()) {
				logger.trace("generateFileToKvFn({})", keyProperty.getPath());
			}
			
			URI sourceNodeId = RdfUtil.uri(channel.getSourceNode().getId());
			SourceInfo info = sourceInfoMap.get(sourceNodeId);
			if (info == null) {
				info = new SourceInfo(channel);
				sourceInfoMap.put(sourceNodeId, info);
			}
			

			FileToKvFnGenerator generator = new FileToKvFnGenerator(info, keyProperty);
			generator.generate();
			
		}

		private boolean isEnumNode(ShowlNodeShape node) {
		
			return node.getShapeDataSource()!=null && node.getShapeDataSource().getDataSource() instanceof StaticDataSource;
		}


		private ShowlPropertyShape rightKey(ShowlStatement joinStatement) throws BeamTransformGenerationException {
			if (joinStatement instanceof ShowlEqualStatement) {
				ShowlExpression e = ((ShowlEqualStatement) joinStatement).getRight();
				if (e instanceof ShowlDirectPropertyExpression || e instanceof ShowlEnumPropertyExpression) {
					return ((ShowlPropertyExpression) e).getSourceProperty();
				}
			}
			throw new BeamTransformGenerationException("Failed to get rightKey from " + joinStatement.toString());
		}


		private ShowlPropertyShape leftKey(ShowlStatement joinStatement) throws BeamTransformGenerationException {

			if (joinStatement instanceof ShowlEqualStatement) {
				ShowlExpression e = ((ShowlEqualStatement) joinStatement).getLeft();
				if (e instanceof ShowlDirectPropertyExpression) {
					return ((ShowlDirectPropertyExpression) e).getSourceProperty();
				}
			}
			throw new BeamTransformGenerationException("Failed to get leftKey from " + joinStatement.toString());
		}




		/**
		 * Generates a DoFn that converts ReadableFile instances to KV<String, TableRow> instances.
		 * @author Greg McFall
		 *
		 */
		private class FileToKvFnGenerator extends BaseReadFnGenerator {
			
			private ShowlPropertyShape keyProperty;
			private JVar keyPropertyVar;


			public FileToKvFnGenerator(SourceInfo sourceInfo, ShowlPropertyShape keyProperty) throws BeamTransformGenerationException {
				super(sourceInfo);
				if (keyProperty == null) {
					fail("keyProperty is null for source node {0}", sourceInfo.getFocusNode().getPath());
				}
				this.keyProperty = keyProperty;
				if (logger.isTraceEnabled()) {
					logger.trace("new FileToKvFnGenerator({}, keyProperty={})", 
							sourceInfo.getFocusNode().getPath(), keyProperty.getPath());
				}
			}

			private void generate() throws BeamTransformGenerationException, JClassAlreadyExistsException {
				
				
				// public class Read${shapeName}Fn extends DoFn<ReadableFile, KV<String,TableRow>> {
				URI shapeId = RdfUtil.uri(sourceInfo.getFocusNode().getId());
				String shapeName = RdfUtil.shortShapeName(shapeId);

				AbstractJClass stringClass = model.ref(String.class);
				AbstractJClass readableFileClass = model.ref(ReadableFile.class);
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(tableRowClass);
				AbstractJClass doFnClass = model.ref(DoFn.class).narrow(readableFileClass).narrow(kvClass);
				
				
				String simpleClassName = "Read" + shapeName + "Fn";
				String className = className(namespacePrefix(shapeId), simpleClassName);
		
				logger.trace("generating class {}", className);
				
				thisClass = model._class(className)._extends(doFnClass);
				
				sourceInfo.setReadFileFn(thisClass);
				
				
				processElement();
			}

			@Override
			protected void registerSourceField(ShowlPropertyShape sourceProperty, JVar fieldVar) {
				if (logger.isTraceEnabled()) {
					logger.trace("FileToKvFnGenerator.registerSourceField({})", sourceProperty.getPath() );
				}
				if (sourceProperty == keyProperty) {
					keyPropertyVar = fieldVar;
				}
				
			}
			
			/**
			 * @param outputBlock The block to which output statements will be added
			 * @param c The Context entity that receives the output
			 * @param row The TableRow instance supplied as output
			 * @throws BeamTransformGenerationException 
			 */
			@Override
			protected void deliverOutput(JBlock outputBlock, JVar c, JVar row) throws BeamTransformGenerationException {
				
				if (keyPropertyVar == null) {
					fail("keyProperty {0} not found for {1}", keyProperty.getPredicate().getLocalName(), sourceInfo.getFocusNode().getPath());
				}
		
				// c.output(KV.of($keyPropertyVar.toString(), row));

				AbstractJClass kvClass = model.ref(KV.class);
				
				outputBlock.add(c.invoke("output").arg(kvClass.staticInvoke("of").arg(keyPropertyVar.invoke("toString")).arg(row)));
				
			}

		}
		
		private abstract class BaseTargetFnGenerator {

			protected JDefinedClass thisClass;
			private JMethod concatMethod = null;
			private JMethod requiredMethod = null;
			
			protected void transformProperty(JBlock body, ShowlDirectPropertyShape p, JVar inputRow, JVar outputRow, JVar enumObject) throws BeamTransformGenerationException {
				
				
				ShowlExpression e = p.getSelectedExpression();
				if (e == null) {
					fail("Mapping not found for property {0}", p.getPath());
				}
				
				if (e instanceof ShowlDirectPropertyExpression) {
					ShowlDirectPropertyShape other = ((ShowlDirectPropertyExpression) e).getSourceProperty();
					transformDirectProperty(body, p, other, inputRow, outputRow);
					
				} else if (e instanceof ShowlFunctionExpression) {
					transformFunction(body, p, (ShowlFunctionExpression)e, inputRow, outputRow);
				} else if (e instanceof ShowlEnumPropertyExpression) {
					transformEnumProperty(body, p, (ShowlEnumPropertyExpression)e, inputRow, outputRow, enumObject);
				} else if (p.getValueShape() != null) {
					transformObjectProperty(body, p, inputRow, outputRow);
					
				} else if (e instanceof ShowlDerivedPropertyExpression) {
					transformDerivedProperty(body, p, (ShowlDerivedPropertyExpression) e, inputRow, outputRow);
					
				} else {
					fail("At {0}, expression not supported: {1}", p.getPath(), e.displayValue());
				}
				

				// TODO: Finish reimplementation here!!!
				
				
//				ShowlMapping mapping = p.getSelectedMapping();
//				if (mapping == null) {
//					fail("Mapping not found for property {0}", p.getPath());
//				}
//				
//				ShowlPropertyShape other = mapping.findOther(p);
//				
//				if (p.getValueShape() != null) {
//					transformObjectProperty(body, p, inputRow, outputRow);
//					
//				} else if (other instanceof ShowlStaticPropertyShape) {
//					transformStaticProperty(body, p, (ShowlStaticPropertyShape)other, inputRow, outputRow, enumObject);
//					
//				} else if (other instanceof ShowlTemplatePropertyShape) {
//					
//					transformTemplateProperty(body, p, (ShowlTemplatePropertyShape)other, inputRow, outputRow);
//					
//				} else if (other instanceof ShowlDirectPropertyShape) {
//					transformDirectProperty(body, p, (ShowlDirectPropertyShape)other, inputRow, outputRow);
//					
//				} else {
//					other = other.getPeer();
//					if (other instanceof ShowlDirectPropertyShape) {
//						transformDirectProperty(body, p, (ShowlDirectPropertyShape) other, inputRow, outputRow);
//					} else {
//						fail("Failed to transform {0}", p.getPath());
//					}
//					
//				} 
				
				
				
			}

			private void transformDerivedProperty(JBlock body, ShowlDirectPropertyShape p,
					ShowlDerivedPropertyExpression e, JVar inputRow, JVar outputRow) throws BeamTransformGenerationException {
				
				PropertyConstraint constraint = e.getSourceProperty().getPropertyConstraint();
				if (constraint == null) {
					fail("At {0}, failed to transform derived property {1}: PropertyConstraint is null ", 
							p.getPath(), e.getSourceProperty().getPath());
				}
				
				QuantifiedExpression formula = constraint.getFormula();
				if (formula == null) {

					fail("At {0}, failed to transform derived property {1}: PropertyConstraint does not define a formula", 
							p.getPath(), e.getSourceProperty().getPath());
				}
				
				PrimaryExpression primary = formula.asPrimaryExpression();
				if (primary instanceof IriTemplateExpression) {
					transformIriTemplateExpression(body, p, e.getSourceProperty(), (IriTemplateExpression) primary, inputRow, outputRow);
				
				} else {

					fail("At {0}, failed to transform derived property {1}: Formula not supported {2}", 
							p.getPath(), e.getSourceProperty().getPath(), formula.toSimpleString());
				}
				
			}

			private void transformIriTemplateExpression(JBlock body, ShowlDirectPropertyShape p,
					ShowlDerivedPropertyShape other, IriTemplateExpression primary, JVar inputRow,
					JVar outputRow) throws BeamTransformGenerationException {
				
				IriTemplate template = primary.getTemplate();
				
				// StringBuilder $builder = new StringBuilder();
				
				String targetPropertyName = p.getPredicate().getLocalName();
				
				AbstractJClass stringBuilderClass = model.ref(StringBuilder.class);
				String builderName = targetPropertyName + "Builder";
				
				JVar builder = body.decl(stringBuilderClass, builderName, stringBuilderClass._new());
			
				
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

				
			}

			private void transformEnumProperty(JBlock body, ShowlDirectPropertyShape p, ShowlEnumPropertyExpression e,
					JVar inputRow, JVar outputRow, JVar enumObject) {
				
				URI predicate = p.getPredicate();
				String fieldName = predicate.getLocalName();
				
				String getterName = "get" + StringUtil.capitalize(fieldName);
				
				AbstractJClass fieldType = model.ref(Object.class);
				
				
				// Object $fieldName = enumObject.get("$fieldName");
				JVar field = body.decl(fieldType, fieldName, enumObject.invoke(getterName));
				
				// if ($field != null) {
				//  outputRow.set("$fieldName", $field);
				// }
				
				body._if(field.ne(JExpr._null()))._then().add(outputRow.invoke("set").arg(JExpr.lit(fieldName)).arg(field));
				
				
			}

			private void transformFunction(JBlock body, ShowlDirectPropertyShape p, ShowlFunctionExpression e, JVar inputRow, JVar outputRow) throws BeamTransformGenerationException {
				
				FunctionExpression function = e.getFunction();
				if (function.getModel() == FunctionModel.CONCAT) {
					transformConcat(body, p, function, inputRow, outputRow);
				}
				
			}

			private void transformConcat(JBlock body, ShowlDirectPropertyShape p, FunctionExpression function, JVar inputRow, JVar outputRow) throws BeamTransformGenerationException {
				declareRequiredMethod();
				JMethod concatMethod = declareConcatMethod();
				
				AbstractJClass objectClass = model.ref(Object.class);
				
				JInvocation concatInvoke = JExpr.invoke(concatMethod);
				
				// Object $targetProperty = concat(...)
				
				JVar targetProperty = body.decl(objectClass, p.getPredicate().getLocalName());
				
				/*  Use a handler to declare the arguments of the concat method.
				 *  The arguments will be either a string literal, or an expression of the form:
				 *  
				 *     required(inputRow, "$fieldName")
				 *     
				 */
			
				TableRowExpressionHandler handler = new TableRowExpressionHandler(inputRow);
				for (Expression arg : function.getArgList()) {
					IJExpression e = handler.javaExpression(arg);
					concatInvoke.arg(e);
				}

				targetProperty.init(concatInvoke);
				
				// outputRow.set("$targetPropertyName", $targetProperty);
				
				body.add(
					outputRow.invoke("set").arg(JExpr.lit(p.getPredicate().getLocalName())).arg(targetProperty)
				);
				
			}

			private JMethod declareRequiredMethod() {
				if (requiredMethod == null) {
					// private Object required(TableRow row, String fieldName) throws RuntimeException {
					AbstractJClass objectClass = model.ref(Object.class);
					AbstractJClass tableRowClass = model.ref(TableRow.class);
					AbstractJClass stringClass = model.ref(String.class);
					
					requiredMethod = thisClass.method(JMod.PRIVATE, objectClass, "required");
					JVar row = requiredMethod.param(tableRowClass, "row");
					JVar fieldName = requiredMethod.param(stringClass, "fieldName");
					
					//  Object value = row.get(fieldName);
					JVar value = requiredMethod.body().decl(objectClass, "value", row.invoke("get").arg(fieldName));
					
					//  if (value == null) {
					//    throw new RuntimeException("Field " + fieldName + " must not be null.");
					//  }
					
					AbstractJClass runtimeExceptionClass = model.ref(RuntimeException.class);
					requiredMethod.body()._if(value.eq(JExpr._null()))._then()
						._throw(runtimeExceptionClass._new().arg(
								JExpr.lit("Field ").plus(fieldName).plus(JExpr.lit(" must not be null."))));
					
					//  return value;
					requiredMethod.body()._return(value);
					
				    // }
				}
				return requiredMethod;
			}

			private JMethod declareConcatMethod() {
				if (concatMethod == null) {
					// String concat(Object...args) {
					AbstractJClass objectClass = model.ref(Object.class);
					AbstractJClass stringClass = model.ref(String.class);
					concatMethod = thisClass.method(JMod.PRIVATE, stringClass, "concat");
					JVar args = concatMethod.varParam(objectClass, "args");
					
					JBlock body = concatMethod.body();
					
					//  StringBuilder builder = new StringBuilder();
					AbstractJClass stringBuilderClass = model.ref(StringBuilder.class);
					JVar builder = body.decl(stringBuilderClass, "builder", stringBuilderClass._new());
					
					//  for (Object value : args) {
					JForEach forEach = body.forEach(objectClass, "value", args);
					
					//    builder.append(value.toString());
					
					forEach.body().add(builder.invoke("append").arg(forEach.var().invoke("toString")));
					
					body._return(builder.invoke("toString"));
				}
				return concatMethod;
			}

			protected void transformStaticProperty(JBlock body, ShowlDirectPropertyShape p,
					ShowlStaticPropertyShape other, JVar inputRow, JVar outputRow, JVar enumObject) throws BeamTransformGenerationException {
				
				
				URI predicate = p.getPredicate();
				String fieldName = predicate.getLocalName();
				
				String getterName = "get" + StringUtil.capitalize(fieldName);
				
				AbstractJClass fieldType = model.ref(Object.class);
				
				
				// Object $fieldName = enumObject.get("$fieldName");
				JVar field = body.decl(fieldType, fieldName, enumObject.invoke(getterName));
				
				// if ($field != null) {
				//  outputRow.set("$fieldName", $field);
				// }
				
				body._if(field.ne(JExpr._null()))._then().add(outputRow.invoke("set").arg(JExpr.lit(fieldName)).arg(field));
				
				
				
			}

			protected void transformObjectProperty(JBlock body, ShowlDirectPropertyShape p, JVar inputRow,
					JVar outputRow) throws BeamTransformGenerationException {
				
				ShowlNodeShape valueShape = p.getValueShape();
				

				ShowlPropertyShape enumSourceKey = valueShape.enumSourceKey(reasoner);
				if (enumSourceKey != null) {
					transformEnumObject(body, p, enumSourceKey);
					return;
				}
				
				ShowlIriReferenceExpression iriRef = iriRef(p);
				if (iriRef != null) {
					transformHardCodedEnumObject(body, p, iriRef);
					return;
				}
				
				
				String targetFieldName = p.getPredicate().getLocalName();
				AbstractJClass tableRowClass = model.ref(TableRow.class);

				
				
				// TableRow $targetFieldName = new TableRow();
				
				JVar fieldRow = body.decl(tableRowClass, targetFieldName, tableRowClass._new());
				
				
				for (ShowlDirectPropertyShape direct : valueShape.getProperties()) {
					transformProperty(body, direct, inputRow, fieldRow, null);
					
				}
				
				
			}

			


			private void transformHardCodedEnumObject(JBlock body, ShowlDirectPropertyShape p,
					ShowlIriReferenceExpression iriRef) throws BeamTransformGenerationException {
				
ShowlNodeShape valueShape = p.getValueShape();
				
				URI targetProperty = p.getPredicate();
				
				String targetFieldName = targetProperty.getLocalName();
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				
				String enumTransformMethodName = "set" + StringUtil.capitalize(targetFieldName);
				
				JMethod method = toTargetFnClass.method(JMod.PRIVATE, model.VOID, enumTransformMethodName);
				
				
				JVar outputRowParam = method.param(tableRowClass, "outputRow");
				

				JVar enumObject = hardCodedEnumObject(method.body(), iriRef, valueShape);
				
				
				
				// TableRow $targetFieldName = new TableRow();
				
				JVar fieldRow = method.body().decl(tableRowClass, targetFieldName + "Row", tableRowClass._new());
				
				for (ShowlDirectPropertyShape direct : valueShape.getProperties()) {
					transformProperty(method.body(), direct, null, fieldRow, enumObject);
					
				}
				//     if (!$fieldRow.isEmpty()) {
		        //       outputRow.set("$targetFieldName", $targetFieldName);
		        //     }
				method.body()._if(fieldRow.invoke("isEmpty").not())._then()
					.add(outputRowParam.invoke("set").arg(JExpr.lit(targetFieldName)).arg(fieldRow));
				
				
				
				body.add(JExpr.invoke(method).arg(outputRowParam));
				
			}

			private JVar hardCodedEnumObject(JBlock block, ShowlIriReferenceExpression iriRef, ShowlNodeShape valueShape) throws BeamTransformGenerationException {

				
			
				String individualLocalName = iriRef.getIriValue().getLocalName();
				
				String enumClassName = enumClassName(valueShape.getOwlClass().getId());
				AbstractJClass enumClass = model.directClass(enumClassName);
				JFieldRef fieldRef = enumClass.staticRef(individualLocalName);
				URI property = valueShape.getAccessor().getPredicate();
				
				
				
				String varName = property.getLocalName();
				return block.decl(enumClass, varName, fieldRef);
			}

			private ShowlIriReferenceExpression iriRef(ShowlDirectPropertyShape p) {
				if (p.getSelectedExpression() instanceof ShowlIriReferenceExpression) {
					return (ShowlIriReferenceExpression) p.getSelectedExpression();
				}
				return null;
			}

			protected void transformEnumObject(JBlock body, ShowlDirectPropertyShape p, 
					ShowlPropertyShape enumSourceKey) throws BeamTransformGenerationException {
				

				ShowlNodeShape valueShape = p.getValueShape();
				
				URI targetProperty = p.getPredicate();
				
				String targetFieldName = targetProperty.getLocalName();
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				
				String enumTransformMethodName = "transform" + StringUtil.capitalize(targetFieldName);
				
				JMethod method = toTargetFnClass.method(JMod.PRIVATE, model.VOID, enumTransformMethodName);
				
				JVar inputRowParam = method.param(tableRowClass, "inputRow");
				
				JVar outputRowParam = method.param(tableRowClass, "outputRow");
				

				JVar enumObject = enumObject(method.body(), enumSourceKey, valueShape, inputRowParam);
				
				
				
				// TableRow $targetFieldName = new TableRow();
				
				JVar fieldRow = method.body().decl(tableRowClass, targetFieldName + "Row", tableRowClass._new());
				
				for (ShowlDirectPropertyShape direct : valueShape.getProperties()) {
					transformProperty(method.body(), direct, inputRowParam, fieldRow, enumObject);
					
				}
				//     if (!$fieldRow.isEmpty()) {
		        //       outputRow.set("$targetFieldName", $targetFieldName);
		        //     }
				method.body()._if(fieldRow.invoke("isEmpty").not())._then()
					.add(outputRowParam.invoke("set").arg(JExpr.lit(targetFieldName)).arg(fieldRow));
				
				
				
				body.add(JExpr.invoke(method).arg(inputRowParam).arg(outputRowParam));
				
			}

			protected JVar enumObject(JBlock block, ShowlPropertyShape enumSourceKey, ShowlNodeShape valueShape, JVar inputRow) throws BeamTransformGenerationException {

				
				String enumSourceKeyName = enumSourceKeyName(enumSourceKey, valueShape);
				
			
				String enumClassName = enumClassName(valueShape.getOwlClass().getId());
				AbstractJClass enumClass = model.directClass(enumClassName);
				URI property = valueShape.getAccessor().getPredicate();
				
				String findMethodName = "findBy" + StringUtil.capitalize(enumSourceKeyName);
				JInvocation arg = inputRow.invoke("get").arg(JExpr.lit(enumSourceKey.getPredicate().getLocalName())).invoke("toString");
				String varName = property.getLocalName();
				return block.decl(enumClass, varName, enumClass.staticInvoke(findMethodName).arg(arg));
			}

			protected String enumSourceKeyName(ShowlPropertyShape enumSourceKey, ShowlNodeShape valueShape) throws BeamTransformGenerationException {
				URI enumClassId = valueShape.getOwlClass().getId();
				Map<URI,RdfProperty> propertyMap = enumClassProperties.get(enumClassId);
				
				if (propertyMap.containsKey(enumSourceKey.getPredicate())) {
					return enumSourceKey.getPredicate().getLocalName();
				}
				
				ShowlPropertyShape peer = enumSourceKey.getPeer();
				if (peer != null && propertyMap.containsKey(peer.getPredicate())) {
					return peer.getPredicate().getLocalName();
				}
				throw new BeamTransformGenerationException("Failed to get enumSourceKeyName for " + enumSourceKey.getPath());
			}

			protected void transformDirectProperty(JBlock body, ShowlDirectPropertyShape p, ShowlDirectPropertyShape other,
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

			protected void transformTemplateProperty(JBlock body, ShowlDirectPropertyShape p,
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

			
			}

			protected ShowlDirectPropertyShape directProperty(ShowlNodeShape declaringShape, URI predicate) throws BeamTransformGenerationException {
				
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

		private class ToTargetFnGenerator extends BaseTargetFnGenerator {

			private void generate() throws BeamTransformGenerationException, JClassAlreadyExistsException {
				String prefix = namespacePrefix(targetNode.getId());
				String localName = RdfUtil.localName(targetNode.getId());
				String className = className(prefix, "To" + localName + "Fn");
				
				toTargetFnClass = thisClass = model._class(className);

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
				
				for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
					transformProperty(tryBlock.body(), p, inputRow, outputRow, null);
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

			
		}
		
		private class MergeFnGenerator extends BaseTargetFnGenerator {
			private GroupInfo groupInfo;

			private MergeFnGenerator(GroupInfo groupInfo) {
				this.groupInfo = groupInfo;
			}
			
			private void generate() throws BeamTransformGenerationException {
				
				// public $mergeFnClassName extends DoFn<KV<String, CoGbkResult>, String> {
				
				AbstractJClass stringClass = model.ref(String.class);
				AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
				AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				AbstractJClass processContextClass = model.ref(ProcessContext.class);
				AbstractJClass throwableClass = model.ref(Throwable.class);
				
				
				AbstractJClass doFnClass = model.ref(DoFn.class).narrow(kvClass).narrow(tableRowClass);
				String className = mainPackage() + "." + groupInfo.mergeClassName();
				
				
				try {
					JDefinedClass mergeFnClass = thisClass = model._class(className)._extends(doFnClass);
					groupInfo.setMergeFnClass(mergeFnClass);
					
					// @ProcessElement
					// public void processElement(ProcessContext c) {
					
					JMethod method = mergeFnClass.method(JMod.PUBLIC, model.VOID, "processElement");
					method.annotate(ProcessElement.class);
					JVar c = method.param(processContextClass, "c");
					
					//   try {
					JTryBlock tryBlock = method.body()._try();
					
				
					
					//   KV<String, CoGbkResult> e = c.element();
					
					JVar e = tryBlock.body().decl(coGbkResultClass, "e", c.invoke("element"));
					
					//   TableRow outRow = new TableRow();
					
					JVar outputRow = tryBlock.body().decl(tableRowClass, "outputRow", tableRowClass._new());
					
					for (SourceInfo sourceInfo : groupInfo.getSourceList()) {
						String methodName = "process" + RdfUtil.shortShapeName(sourceInfo.getFocusNode().getId());
						tryBlock.body().add(JExpr.invoke(methodName).arg(e).arg(outputRow));
						processSource(sourceInfo, methodName, kvClass, tableRowClass, throwableClass);
					}
					
					//   if (!outputRow.isEmpty()) {
					//     c.output(outputRow);
					//   }
					
					tryBlock.body()._if(outputRow.invoke("isEmpty").not())._then().add(c.invoke("output").arg(e));
					
					JCatchBlock catchBlock = tryBlock._catch(throwableClass);
					JVar oops = catchBlock.param("oops");
					catchBlock.body().add(oops.invoke("printStackTrace"));
					
					
				} catch (JClassAlreadyExistsException e) {
					throw new BeamTransformGenerationException("Failed to create MergeFn class " + className, e);
				}
				
			}

			private void processSource(
					SourceInfo sourceInfo, 
					String methodName, 
					AbstractJClass kvClass, 
					AbstractJClass tableRowClass, AbstractJClass throwableClass
			) throws BeamTransformGenerationException {
				JDefinedClass mergeFnClass = groupInfo.getMergeFnClass();
				
				AbstractJClass iterableClass = model.ref(Iterable.class).narrow(tableRowClass);
				
				//  private void process$sourceShapeName(KV<String, CoGbkResult> e, TableRow outputRow) {
				
				JMethod method = mergeFnClass.method(JMod.PRIVATE, model.VOID, methodName);
				method._throws(throwableClass);
				JVar e = method.param(kvClass, "e");
				JVar outputRow = method.param(tableRowClass, "outputRow");
				
				//   Iterable<TableRow> inputRowList = e.getValue().getAll($tagVar);
				
				JVar inputRowList = method.body().decl(
						iterableClass, "inputRowList", e.invoke("getValue").invoke("getAll").arg(
								mainClass.staticRef(sourceInfo.getTupleTag())));
				
				//   for(TableRow inputRow : inputRowList) {
				
				JForEach forEach = method.body().forEach(tableRowClass, "inputRow", inputRowList);
				
				JVar inputRow = forEach.var();
				
				for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
					
					ShowlExpression expression = p.getSelectedExpression();
					if (expression == null) {
						fail("Mapping not found for property {0}", p.getPath());
					}
					
					ShowlPropertyShape other = null;
					if (expression instanceof ShowlPropertyExpression) {
						other = ((ShowlPropertyExpression) expression).getSourceProperty();
					} else {
						fail("For property {0}, unsupported expression: {1}", p.getPath(), expression.displayValue());
					}
					
					if (other.getDeclaringShape() == sourceInfo.getFocusNode()) {
						transformProperty(forEach.body(), p, inputRow, outputRow, null);
					}
				}
				
			}
			
		}
		private class TableRowExpressionHandler implements ExpressionHandler {
			
			private JVar inputRow;
			
			public TableRowExpressionHandler(JVar inputRow) {
				this.inputRow = inputRow;
			}



			@Override
			public IJExpression javaExpression(Expression e) throws BeamTransformGenerationException {
				PrimaryExpression primary = e.asPrimaryExpression();
				
				if (primary instanceof LiteralFormula) {
					Literal literal = ((LiteralFormula) primary).getLiteral();
					if (literal.getDatatype().equals(XMLSchema.STRING)) {
						return JExpr.lit(literal.stringValue());
					} else {
						fail("Typed literal not supported in expression: {0}", e.toSimpleString());
					}
				}
				
				if (primary instanceof PathTerm) {
					PathTerm term = (PathTerm) primary;
					URI iri = term.getIri();
					
					
					return JExpr.invoke("required").arg(inputRow).arg(JExpr.lit(iri.getLocalName()));
				}

				fail("Unsupported expression: {0}", e.toSimpleString());
				return null;
			}
			
		}
		
		private abstract class BaseReadFnGenerator {

			protected SourceInfo sourceInfo;

			protected Map<Class<?>, JMethod> getterMap = new HashMap<>();
			protected JDefinedClass thisClass;
			private JFieldVar patternField = null;
			
			public BaseReadFnGenerator(SourceInfo sourceInfo) {
				this.sourceInfo = sourceInfo;
			}
			
			
			protected void processElement() throws BeamTransformGenerationException {
				
				List<ShowlPropertyShape> sourceProperties = sourceProperties();
				
				
				// @ProcessElement
				// public void processElement(ProcessContext c) {
				
				JMethod method = 
						thisClass.method(JMod.PUBLIC, model.VOID, "processElement");
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
				
				//       CSVParser csv = CSVParser.parse(stream, StandardCharsets.UTF_8, CSVFormat.RFC4180.withFirstRecordAsHeader().withSkipHeaderRecord());

				JBlock innerBody = innerTry.body();
				
				AbstractJClass csvParserClass = model.ref(CSVParser.class);
				AbstractJClass standardCharsetsClass = model.ref(StandardCharsets.class);
				AbstractJClass csvFormatClass = model.ref(CSVFormat.class);
				
				JVar csv = innerBody.decl(csvParserClass, "csv").init(
						csvParserClass.staticInvoke("parse")
						.arg(stream)
						.arg(standardCharsetsClass.staticRef("UTF_8"))
						.arg(csvFormatClass.staticRef("RFC4180").invoke("withFirstRecordAsHeader").invoke("withSkipHeaderRecord")));
				//       for(CSVRecord record : csv) {
				
				AbstractJClass csvRecordClass = model.ref(CSVRecord.class);
				JForEach forEachRecordLoop = innerBody.forEach(csvRecordClass, "record", csv);
				JVar record = forEachRecordLoop.var();
				JBlock forEachRecord = forEachRecordLoop.body();

				
				//         TableRow row = new TableRow();
				
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				JVar row = forEachRecord.decl(tableRowClass, "row").init(tableRowClass._new());
				

				for (ShowlPropertyShape sourceProperty : sourceProperties) {
					Class<?> datatype = javaType(sourceProperty);
					
					AbstractJClass datatypeClass = datatype==GregorianCalendar.class ? model.ref(Long.class) : model.ref(datatype);
					String fieldName = sourceProperty.getPredicate().getLocalName();
					JMethod getter = getterMap.get(datatype);
					if (getter == null) {
						fail("Getter not found for {0}", datatype.getSimpleName());
					}
					
					//     $fieldName = ${getter}(record.get("${fieldName}"));
					JVar fieldVar = forEachRecord.decl(datatypeClass, fieldName, 
							JExpr.invoke(getter)
							.arg(record.invoke("get")
							.arg(JExpr.lit(fieldName))));
					
					registerSourceField(sourceProperty, fieldVar);
					
					//     if ($fieldName != null) {
					//       row.set("$fieldName", $fieldName);
					forEachRecord
							._if(fieldVar.ne(JExpr._null()))
							._then().add(row.invoke("set").arg(JExpr.lit(fieldName)).arg(fieldVar));
					
					
					//     }
					
				}

				
				//         if (!row.isEmpty()) {
				//           $outputStatements
				//         }
				
				
				JBlock outputBlock = forEachRecord._if(row.invoke("isEmpty").not())._then();
				
				deliverOutput(outputBlock, c, row);
				
			
				
				
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


			protected void registerSourceField(ShowlPropertyShape sourceProperty, JVar fieldVar) {
				// Do nothing by default
				
				// subclasses may override
				
			}

			abstract protected void deliverOutput(JBlock outputBlock, JVar c, JVar row) throws BeamTransformGenerationException;

			protected List<ShowlPropertyShape> sourceProperties() throws BeamTransformGenerationException {
				

				List<ShowlPropertyShape> list = targetNode.selectedPropertiesOf(sourceInfo.getFocusNode());
				
				for (ShowlPropertyShape p : list) {
				
					declareDatatypeGetter(p);
					
					
				}
				
				
				return list;
			}

			


			protected JFieldVar patternField() {
				if (patternField == null) {
					
					// private static final DATE_PATTERN = Pattern.compile("(\\d+-\\d+-\\d+)(.*)");
					
					AbstractJClass patternClass = model.ref(Pattern.class);
					patternField = thisClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC , patternClass, 
							"DATE_PATTERN", 
							patternClass.staticInvoke("compile").arg(
									JExpr.lit("(\\d+-\\d+-\\d+)(.*)")));
				}
				
				return patternField;
				
			}

			protected void declareDatatypeGetter(ShowlPropertyShape p) throws BeamTransformGenerationException {
				
				if (p.getValueShape() != null) {
					Set<ShowlPropertyShape> set = p.getValueShape().allOutwardProperties();
					for (ShowlPropertyShape q : set) {
						declareDatatypeGetter(q);
					}
					return;
				}
				
				Class<?> javaClass = javaType(p);
				if (javaClass == null) {
					return;
				}
				
				if (!getterMap.containsKey(javaClass)) {
					String typeName = javaClass==GregorianCalendar.class ? "temporal" : StringUtil.camelCase(javaClass.getSimpleName());
					
					String methodName = typeName + "Value";

					AbstractJClass stringClass = model.ref(String.class);
					AbstractJClass returnType = 
							javaClass == GregorianCalendar.class ? model.ref(Long.class) : 
							javaClass == Integer.class ? model.ref(Long.class) :
							model.ref(javaClass);
					
					

					
					// $returnType ${returnType}Value(String stringValue) {
					JMethod method = thisClass.method(JMod.PRIVATE, returnType, methodName);

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
						     
					} else if (javaClass==Boolean.class) {
						block1._return(JExpr.lit("true").invoke("equalsIgnoreCase").arg(stringValue));
						
					} else if (javaClass == Long.class || javaClass == Integer.class) {
						AbstractJClass longClass = model.ref(Long.class);
						block1._return(longClass._new().arg(stringValue));
						
					} else if (javaClass == Double.class || javaClass==double.class) {
						AbstractJClass doubleClass = model.ref(Double.class);
						block1._return(doubleClass._new().arg(stringValue));
						
					} else if (javaClass == GregorianCalendar.class) {
						
						// if (stringValue.contains("T")) {
						//   if (stringValue.contains("/") {
						//     return Instant.from(ZonedDateTime.parse(stringValue).toEpochMilli();
						//   } else if (stringValue.contains("Z") {
						//     return Instant.parse(stringValue).toEpochMilli();
						//   } else {
					    //     return Instant.from(OffsetDateTime.parse(stringValue)).toEpochMilli();
						//   }
						//  } 
						//  Matcher matcher = DATE_PATTERN.matcher(stringValue);
						//  if (matcher.matches()) {
						//    String datePart = matcher.group(1);
						//    String zoneOffset = matcher.group(2);
						//    if (zoneOffset.length() == 0 || zoneOffset.equals("Z")) {
						//      zoneOffset = "+00:00";
						//    } 
						//    stringValue = datePart + "T00:00:00.000" + zoneOffset;
						//    return Instant.from(OffsetDateTime.parse(stringValue)).toEpochMilli();
						//  }
						
						JFieldVar datePattern = patternField();
						
						AbstractJClass instantClass = model.ref(Instant.class);
						AbstractJClass offsetDateTimeClass = model.ref(OffsetDateTime.class);
						AbstractJClass matcherClass = model.ref(Matcher.class);
						AbstractJClass zonedDateTimeClass = model.ref(ZonedDateTime.class);
						JConditional outerIf = block1._if(stringValue.invoke("contains").arg(JExpr.lit("T")));
						
						JConditional innerIf = outerIf._then()._if(stringValue.invoke("contains").arg(JExpr.lit("/")));
						
						innerIf._then()._return(instantClass.staticInvoke("from").arg(
								zonedDateTimeClass.staticInvoke("parse").arg(stringValue)).invoke("toEpochMilli"));
						
						innerIf._elseif(stringValue.invoke("contains").arg("Z"))._then()._return(
								instantClass.staticInvoke("parse").arg(stringValue).invoke("toEpochMilli"));
						
						innerIf._else()._return(instantClass.staticInvoke("from").arg(
								offsetDateTimeClass.staticInvoke("parse").arg(stringValue)).invoke("toEpochMilli"));
						
						JVar matcher = block1.decl(matcherClass, "matcher", 
								datePattern.invoke("matcher").arg(stringValue));
						
						JConditional ifMatches = block1._if(matcher.invoke("matches"));
						
						JBlock ifMatchesBlock = ifMatches._then();
						
						JVar datePart = ifMatchesBlock.decl(stringClass, "datePart", 
								matcher.invoke("group").arg(JExpr.lit(1)));
						
						JVar zoneOffset = ifMatchesBlock.decl(stringClass, "zoneOffset", 
								matcher.invoke("group").arg(JExpr.lit(2)));
						
						ifMatchesBlock._if(zoneOffset.invoke("length").eq(JExpr.lit(0))
								.cor(zoneOffset.invoke("equals").arg(JExpr.lit("Z"))))._then().add(
										JExpr.assign(stringValue, datePart.plus("T00:00:00.000").plus(zoneOffset)));
						
						ifMatchesBlock._return(instantClass.staticInvoke("from").arg(
								offsetDateTimeClass.staticInvoke("parse").arg(stringValue)).invoke("toEpochMilli"));
					
						
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
			
		}

		private class ReadFileFnGenerator extends BaseReadFnGenerator {

			
			
			
			public ReadFileFnGenerator(SourceInfo sourceInfo) {
				super(sourceInfo);
			}

			private void generate() throws JClassAlreadyExistsException, BeamTransformGenerationException {
				
				// public class ReadFileFn extends DoFn<FileIO.ReadableFile, TableRow> {
				
				
				
				
				ShowlNodeShape sourceNode = sourceInfo.getFocusNode();
				String sourceShapeName = RdfUtil.uri(sourceNode.getId()).getLocalName();
				
				String simpleClassName = "Read" + sourceShapeName + "Fn";
				
				String nsPrefix = namespacePrefix(sourceNode.getId());
				
			
				thisClass = model._class(className(nsPrefix + "." + simpleClassName));
				
				sourceInfo.setReadFileFn(thisClass);
				
				
				
				
				AbstractJClass superClass =  model.directClass(DoFn.class.getName()).narrow(ReadableFile.class, TableRow.class);
			
				thisClass._extends(superClass);
				
				processElement();
			}


			/**
			 * @param outputBlock The block to which output statements will be added
			 * @param c The Context entity that receives the output
			 * @param row The TableRow instance supplied as output
			 */
			@Override
			protected void deliverOutput(JBlock outputBlock, JVar c, JVar row) {
				outputBlock.add(c.invoke("output").arg(row));
				
			}
			
		}



		private void declareMainClass() throws BeamTransformGenerationException, JClassAlreadyExistsException {
			String mainClassName = mainClassName(targetNode);
			mainClass = model._class(mainClassName);
			declareOptionsClass();
			
			sourceUriMethod();
			processMethod();
			mainMethod();
			
		}



		private void sourceUriMethod() throws BeamTransformGenerationException {
			
			if (singleSource()) {
				singleSourceUriMethod();
			} else {
				multipleSourceUriMethod();
			}
		}


		private void multipleSourceUriMethod() throws BeamTransformGenerationException {
			
			// private String sourceUri(String pattern, Options options) {

			AbstractJClass stringClass = model.ref(String.class);
			JMethod method = mainClass.method(JMod.PRIVATE | JMod.STATIC, stringClass, "sourceURI");
			JVar pattern = method.param(stringClass, "pattern");
			JVar options = method.param(optionsClass, "options");
			
			//   return pattern.replace("${environmentName}", options.getEnvironment()) + "/*";
			
			
			/*
			 * We assume that all datasources have the same variable name for the environment name.
			 * 
			 * Scan all datasources and confirm this assumption; throw an exception if 
			 * the assumption is not true.
			 */
			
			String varName = null;

			for (ShowlChannel channel : targetNode.getChannels()) {
			

				String datasourceId = channel.getSourceNode().getShapeDataSource().getDataSource().getId().stringValue();
				
				int varStart = datasourceId.lastIndexOf('$');
				int varEnd = datasourceId.indexOf('}', varStart)+1;
				String varName2 = datasourceId.substring(varStart, varEnd);
				if (varName == null) {
					varName = varName2;
				} else if (!varName.equals(varName2)) {
					String msg = MessageFormat.format("Conflicting variables for environment data sources for {0}", targetNode.getPath());
					throw new BeamTransformGenerationException(msg);
				}
			}

			if (varName == null) {
				throw new BeamTransformGenerationException("Environment name variable not found for target " + targetNode.getPath());
			}
			

			
			JStringLiteral wildcard = JExpr.lit("/*");
			
			method.body()._return(pattern.invoke("replace")
					.arg(JExpr.lit(varName))
					.arg(options.invoke("getEnvironment")).plus(wildcard));
			
			// }
			
		}



		private void singleSourceUriMethod() {

			// private String sourceURI(Options options) {
			AbstractJClass stringClass = model.ref(String.class);
			JMethod method = mainClass.method(JMod.PRIVATE | JMod.STATIC, stringClass, "sourceURI");
			JVar options = method.param(optionsClass, "options");
			
			// String envName = options.getEnvironment();
			
			JVar envName = method.body().decl(stringClass, "envName", options.invoke("getEnvironment"));

			
			//  return "$bucketId".replace("${environmentName}", envName);

			ShowlNodeShape sourceNode = targetNode.getChannels().get(0).getSourceNode();
			
	
			
			String datasourceId = sourceNode.getShapeDataSource().getDataSource().getId().stringValue();
			

			int varStart = datasourceId.lastIndexOf('$');
			int varEnd = datasourceId.indexOf('}', varStart)+1;
			String varName = datasourceId.substring(varStart, varEnd);
			
			
			JStringLiteral pattern = JExpr.lit(datasourceId);
			
			JStringLiteral wildcard = JExpr.lit("/*");
						
			method.body()._return(pattern.invoke("replace")
					.arg(JExpr.lit(varName))
					.arg(options.invoke("getEnvironment")).plus(wildcard));
			
			
			
			// }
			
		}



		private void mainMethod() {
			
			// public static void main(String[] args) {
			
			JMethod method = mainClass.method(JMod.PUBLIC | JMod.STATIC, model.VOID, "main");
			JVar args = method.param(String[].class, "args");
			
			// Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
			
			AbstractJClass pipelineOptionsFactoryClass = model.ref(PipelineOptionsFactory.class);
			
			JVar optionsVar = method.body().decl(optionsClass, "options", 
					pipelineOptionsFactoryClass.staticInvoke("fromArgs").arg(args)
					.invoke("withValidation")
					.invoke("as").arg(optionsClass.staticRef("class")));
			
			// process(options);
			
			method.body().add(JExpr.invoke("process").arg(optionsVar));
			
			
		}


		private void declareOptionsClass() throws JClassAlreadyExistsException {

			// public interface Options extends PipelineOptions {
			
			optionsClass = mainClass._class(JMod.PUBLIC, "Options", EClassType.INTERFACE);
			optionsClass._extends(PipelineOptions.class);
			
			//   @Required
			//   @Description("The name of the environment; typically one of (dev, test, stage, prod)")
            //   String getEnvironment();

			JMethod getEnvironment = optionsClass.method(JMod.PUBLIC, String.class, "getEnvironment");
			getEnvironment.annotate(Required.class);
			JAnnotationUse description = getEnvironment.annotate(Description.class);
			description.param(JExpr.lit("The name of the environment; typically one of (dev, test, stage, prod)"));

			//   void setEnvironment(String envName);
			
			optionsClass.method(JMod.PUBLIC, model.VOID, "setEnvironment").param(String.class, "envName");
			

			// }
		}



		private void processMethod() throws BeamTransformGenerationException {
			
			if (singleSource()) {
				processOneDataSource();
			} else {
				processManyDataSources();
			}
			
			
		}

		private void processManyDataSources() throws BeamTransformGenerationException {
			// public static void process(Options options) {
			
			JMethod method = mainClass.method(JMod.PUBLIC | JMod.STATIC, model.VOID, "process");
			JVar optionsVar = method.param(optionsClass, "options");
			
			//   Pipeline p = Pipeline.create(options);
			
			AbstractJClass pipelineType = model.ref(Pipeline.class);
			
			JBlock body = method.body();
			JVar p = body.decl(pipelineType, "p", model.directClass(Pipeline.class.getName())
					.staticInvoke("create").arg(optionsVar));
			
			
			defineTupleTagsAndPcollections(body, p, optionsVar);
			List<GroupInfo> groupList = groupList();
			defineKeyedCollectionTuples(body, groupList);
			generateMergeFnClasses(groupList);
			applyMergeFnClasses(body, groupList);
			

		}

		private void applyMergeFnClasses(JBlock body, List<GroupInfo> groupList) throws BeamTransformGenerationException {
			
			if (groupList.isEmpty()) {
				fail("No groups found for {0}", targetNode.getPath());
			}
			if (groupList.size()==1) {
				applySingleMerge(body, groupList.get(0));
			} else {
				fail("Multiple groups not supported yet for {0}", targetNode.getPath());
			}
			
		}


		private void applySingleMerge(JBlock body, GroupInfo groupInfo) throws BeamTransformGenerationException {
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			AbstractJClass tableRowPCollectionClass = model.ref(TableRow.class).narrow(tableRowClass);
			AbstractJClass parDoClass = model.ref(ParDo.class);
			JDefinedClass mergeFnClass = groupInfo.getMergeFnClass();
			
			JVar kvpCollection = groupInfo.getKvpCollection();
			
			JVar outputRowCollection = body.decl(tableRowPCollectionClass, "outputRowCollection");
			
			outputRowCollection.init(kvpCollection.invoke("apply")
				.arg(parDoClass.staticInvoke("of").arg(mergeFnClass._new())));
			
			String targetTableSpec = targetTableSpec();
			String writeLabel = "Write" + RdfUtil.shortShapeName(targetNode.getId());
			AbstractJClass bigQueryIoClass = model.ref(BigQueryIO.class);
			AbstractJClass createDispositionClass = model.ref(CreateDisposition.class);
			AbstractJClass writeDispositionClass = model.ref(WriteDisposition.class);
			
			body.add(outputRowCollection.invoke("apply").arg(JExpr.lit(writeLabel))
					.arg(bigQueryIoClass.staticInvoke("writeTableRows")
							.invoke("to").arg(targetTableSpec)
							.invoke("withCreateDisposition").arg(createDispositionClass.staticRef("CREATE_NEVER"))
							.invoke("withWriteDisposition").arg(writeDispositionClass.staticRef("WRITE_APPEND"))));
			
		}


		private void generateMergeFnClasses(List<GroupInfo> groupList) throws BeamTransformGenerationException {
			for (GroupInfo groupInfo : groupList) {
				MergeFnGenerator generator = new MergeFnGenerator(groupInfo);
				generator.generate();
			}
			
		}


		private void defineKeyedCollectionTuples(JBlock body, List<GroupInfo> groupList) {
			// For now, we require that the key is a string.
			// We should relax this condition in the future.
			
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
			AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
			AbstractJClass pCollectionClass = model.ref(PCollection.class).narrow(kvClass);
			AbstractJClass keyedPCollectionTupleClass = model.ref(KeyedPCollectionTuple.class);
			AbstractJClass coGroupByKeyClass = model.ref(CoGroupByKey.class);
			
			int count = 1;
			for (GroupInfo groupInfo : groupList) {
				
				String varName = groupInfo.kvpCollectionName(count++, groupList.size());
				
				// PCollectionKV<String, CoGbkResult>> kvpCollection = KeyedPCollectionTuple
				//   .of($firstTupleTag, $firstPCollection)
				//   .and($secondTupleTag, $secondPCollection)
				//   .and($thirdTupleTag, $thirdPCollection)
				//   ...
				//   .apply(CoGroupByKey.<String>create());
				
				JVar var = body.decl(pCollectionClass, varName);
				JInvocation invoke = null;
				
				for (SourceInfo source : groupInfo.getSourceList()) {
					if (invoke == null) {
						invoke = keyedPCollectionTupleClass
								.staticInvoke("of").arg(source.getTupleTag()).arg(source.getPcollection());
					} else {
						invoke = invoke.invoke("and").arg(source.getTupleTag()).arg(source.getPcollection());
					}
				}
				
				invoke = invoke.invoke("apply").arg(coGroupByKeyClass.staticInvoke("create").narrow(stringClass));
				var.init(invoke);
				
				groupInfo.setKvpCollection(var);
				
			}
			
		}


		private List<GroupInfo> groupList() throws BeamTransformGenerationException {
			List<GroupInfo> list = new ArrayList<>();
			
			for (SourceInfo sourceInfo : sortedSourceInfoList()) {
				ShowlChannel channel = sourceInfo.getChannel();
				ShowlStatement statement = channel.getJoinStatement();
				if (statement instanceof ShowlEqualStatement) {
					ShowlEqualStatement equal = (ShowlEqualStatement) statement;
					
					ShowlExpression left = equal.getLeft();
					ShowlExpression right = equal.getRight();
					
					ShowlPropertyShape leftProperty = propertyOf(left);
					ShowlPropertyShape rightProperty = propertyOf(right);
					
					SourceInfo leftInfo = sourceInfoFor(leftProperty);
					SourceInfo rightInfo = sourceInfoFor(rightProperty);
					
					GroupInfo group = new GroupInfo();
					group.getSourceList().add(leftInfo);
					group.getSourceList().add(rightInfo);
					
					sortSourceInfo(group.getSourceList());
					
					list.add(group);
					
				} else if (statement != null) {
					fail("Unsupported statement: " + statement.toString());
				}
			}
			return list;
		}
		
		private SourceInfo sourceInfoFor(ShowlPropertyShape p) throws BeamTransformGenerationException {
			ShowlNodeShape root = p.getRootNode();
			SourceInfo result = sourceInfoMap.get(RdfUtil.uri(root.getId()));
			if (result == null) {
				fail("SourceInfo not found for {0}", p.getPath());
			}
			return result;
		}


		private ShowlPropertyShape propertyOf(ShowlExpression e) throws BeamTransformGenerationException {
			if (e instanceof ShowlPropertyExpression) {
				return ((ShowlPropertyExpression) e).getSourceProperty();
			}
			fail("Cannot get property from: {0}", e.displayValue());
			return null;
		}


		private List<SourceInfo> sortedSourceInfoList() {
			List<SourceInfo> list = new ArrayList<>(sourceInfoMap.values());
			sortSourceInfo(list);
			return list;
		}
		
		private void sortSourceInfo(List<SourceInfo> list) {

			Collections.sort(list, new Comparator<SourceInfo>(){

				@Override
				public int compare(SourceInfo a, SourceInfo b) {
					URI nodeA = RdfUtil.uri(a.getFocusNode().getId());
					URI nodeB = RdfUtil.uri(b.getFocusNode().getId());
					
					return nodeA.getLocalName().compareTo(nodeB.getLocalName());
				}
				
			});
		}


		private void defineTupleTagsAndPcollections(JBlock block, JVar pipeline, JVar options) throws BeamTransformGenerationException {

			Set<ShowlNodeShape> set = new HashSet<>();
			AbstractJClass pCollectionClass = model.ref(PCollection.class).narrow(ReadableFile.class);
			AbstractJClass fileIoClass = model.ref(FileIO.class);
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(stringClass);
			
		
			for (SourceInfo sourceInfo : sortedSourceInfoList()) {
				ShowlNodeShape node = sourceInfo.getFocusNode();
				set.add(node);
				URI shapeId = RdfUtil.uri(node.getId());
				
				String shapeName = shapeId.getLocalName();
				if (shapeName.endsWith("Shape")) {
					shapeName = shapeName.substring(0, shapeName.length()-5);
				}
				shapeName = StringUtil.firstLetterLowerCase(shapeName);
				
				// final TupleTag<String> ${shapeName}Tag = new TupleTag<String>();
				
				JVar tagVar = mainClass.field(JMod.STATIC | JMod.FINAL, tupleTagClass, shapeName + "Tag", tupleTagClass._new());
			
				
				// PCollection<ReadableFile> $shapeName = p
				//  .apply(FileIO.match().filepattern(sourceURI($pattern, options))
				//  .apply(FileIO.readMatches());
				
				GoogleCloudStorageBucket bucket = node.getShape().findDataSource(GoogleCloudStorageBucket.class);
				assertTrue(bucket != null, "GoogleCloudStorageBucket data source not found for {0}", shapeId.getLocalName());
				
				String bucketId = bucket.getId().stringValue();
				
				AbstractJClass parDoClass = model.ref(ParDo.class);
				JDefinedClass readFn = sourceInfo.getReadFileFn();
				
				JVar pcollection = block.decl(pCollectionClass, shapeName, pipeline
					.invoke("apply").arg(
						fileIoClass.staticInvoke("match").invoke("filepattern").arg(
							JExpr.invoke("sourceURI").arg(JExpr.lit(bucketId)).arg(options)))
					.invoke("apply").arg(fileIoClass.staticInvoke("readMatches"))
					.invoke("apply").arg(parDoClass.staticInvoke("of").arg(readFn._new()))
				);
				
				sourceInfo.setPcollection(pcollection);
				sourceInfo.setTupleTag(tagVar);
			}
			
			
		}





		private void processOneDataSource() throws BeamTransformGenerationException {

			// public static void process(Options options) {
			
			JMethod method = mainClass.method(JMod.PUBLIC | JMod.STATIC, model.VOID, "process");
			JVar optionsVar = method.param(optionsClass, "options");
			
			
			
			
			//   Pipeline p = Pipeline.create(options);
			
			AbstractJClass pipelineType = model.ref(Pipeline.class);
			JBlock body = method.body();
			JVar p = body.decl(pipelineType, "p", model.directClass(Pipeline.class.getName())
					.staticInvoke("create").arg(optionsVar));

			//   String sourceURI = sourceURI(options);
			AbstractJClass stringClass = model.ref(String.class);
			JVar sourceURI = body.decl(stringClass, "sourceURI", JExpr.invoke("sourceURI").arg(optionsVar));
			
			//   p.apply(FileIO.match().filepattern(options.getSourceUri()))
			
			AbstractJClass fileIoClass = model.directClass(FileIO.class.getName());
			JInvocation pipeline = p.invoke("apply").arg(
					fileIoClass
						.staticInvoke("match")
						.invoke("filepattern").arg(sourceURI));

			//     p.apply(FileIO.readMatches())
			
			pipeline = pipeline.invoke("apply").arg(fileIoClass.staticInvoke("readMatches"));
			
			//     p.apply("ReadFiles", ParDo.of(new $readFileFnClass()))
			
			JDefinedClass readFileFnClass = sourceInfoMap.values().iterator().next().getReadFileFn();
			
			AbstractJClass parDoClass = model.directClass(ParDo.class.getName());
			pipeline = pipeline.invoke("apply")
					.arg(JExpr.lit("ReadFiles"))
					.arg(parDoClass.staticInvoke("of")
							.arg(readFileFnClass._new()));
			
			//    p.apply("To${targetShapeName}", ParDo.of(new $toTargetFnClass()));
			String targetShapeName = RdfUtil.localName(targetNode.getId());
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
			body.add(p.invoke("run"));
			
		}


		private String targetTableSpec() throws BeamTransformGenerationException {
			// For now we only support BigQuery
			for (DataSource ds : targetNode.getShape().getShapeDataSource()) {
				if (ds instanceof GoogleBigQueryTable) {
					GoogleBigQueryTable table = (GoogleBigQueryTable) ds;
					return table.getQualifiedTableName();
				}
			}
			fail("Target table not found for {0}", RdfUtil.compactId(targetNode.getId(), nsManager));
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
		
		private String mainPackage() throws BeamTransformGenerationException {
			URI shapeId = RdfUtil.uri(targetNode.getId());
			
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
			fail("Prefix not found for namespace <{0}>", uri.getNamespace());
		}
		fail("URI expected but id is a BNode");
		return null;
	}

	private String namespacePrefix(URI id) throws BeamTransformGenerationException {
		Namespace ns = nsManager.findByName(id.getNamespace());
		if (ns == null) {
			throw new BeamTransformGenerationException("Prefix not found for <" + id.getNamespace() + ">");
		}
		return ns.getPrefix();
	}

	private String className(String simpleName) {
		return basePackage + "." + simpleName;
	}
	
	private String className(String namespacePrefix, String simpleName) throws BeamTransformGenerationException {
		return className(namespacePrefix + "." + simpleName);
	}
	
	private static class RdfProperty {
		private URI id;
		private URI range;
		
		

		public RdfProperty(URI id, URI range) {
			this.id = id;
			this.range = range;
		}

		public URI getId() {
			return id;
		}

		public URI getRange() {
			return range;
		}

	}
	
	
	private static class GroupInfo {
		private List<SourceInfo> sourceList = new ArrayList<>();
		private JVar kvpCollection;
		private JDefinedClass mergeFnClass;
		private JVar outputRowCollection;
		
		public GroupInfo() {
		}
		
		public String kvpCollectionName(int count, int size) {
			
			return size>1 ? "kvpCollection" + count : "kvpCollection";
		}
		
		public String mergeClassName() {
			StringBuilder builder = new StringBuilder();
			builder.append("Merge");
			String and = "";
			for (SourceInfo sourceInfo : sourceList) {
				String shortName = RdfUtil.shortShapeName(sourceInfo.getFocusNode().getId());
				builder.append(and);
				and = "And";
				builder.append(shortName);
				
			}
			builder.append("Fn");
			return builder.toString();
		}
		public JVar getKvpCollection() {
			return kvpCollection;
		}
		public void setKvpCollection(JVar kvpCollection) {
			this.kvpCollection = kvpCollection;
		}
		public List<SourceInfo> getSourceList() {
			return sourceList;
		}
		public JDefinedClass getMergeFnClass() {
			return mergeFnClass;
		}
		public void setMergeFnClass(JDefinedClass mergeFnClass) {
			this.mergeFnClass = mergeFnClass;
		}
		public JVar getOutputRowCollection() {
			return outputRowCollection;
		}
		public void setOutputRowCollection(JVar outputRowCollection) {
			this.outputRowCollection = outputRowCollection;
		}
		
		
		
	}
	
	private static class SourceInfo {
		private ShowlChannel channel;
		private JVar pcollection;
		private JVar tupleTag;
		
		private JDefinedClass readFileFn;
		

		public SourceInfo(ShowlChannel channel) {
			this.channel = channel;
		}

		public ShowlChannel getChannel() {
			return channel;
		}

		public ShowlPropertyShape getKey() throws BeamTransformGenerationException {
			if (channel.getJoinStatement() != null) {
				Set<ShowlPropertyShape> set = new HashSet<>();
				channel.getJoinStatement().addDeclaredProperties(channel.getSourceNode(), set);
				
				if (set.size()==1) {
					return set.iterator().next();
				}
				
			}
			throw new BeamTransformGenerationException("key not found in " + channel.toString());
		}

		public JVar getPcollection() {
			return pcollection;
		}

		public void setPcollection(JVar pcollection) {
			this.pcollection = pcollection;
		}

		public JVar getTupleTag() {
			return tupleTag;
		}

		public void setTupleTag(JVar tupleTag) {
			this.tupleTag = tupleTag;
		}
		
		public JDefinedClass getReadFileFn() {
			return readFileFn;
		}

		public void setReadFileFn(JDefinedClass readFileFn) {
			this.readFileFn = readFileFn;
		}
		
		public ShowlNodeShape getFocusNode() {
			return channel.getSourceNode();
		}
		
		public String toString() {
			return "SourceInfo(" + channel.toString() + ")";
		}
		
	}
	
	

	private interface ExpressionHandler {
		
		IJExpression javaExpression(Expression e) throws BeamTransformGenerationException;
	}
}
