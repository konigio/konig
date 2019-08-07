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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.NullableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.FileIO.ReadableFile;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.CreateDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import org.apache.beam.sdk.io.gcp.bigquery.TableRowJsonCoder;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.transforms.join.CoGroupByKey;
import org.apache.beam.sdk.transforms.join.KeyedPCollectionTuple;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
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
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.uuid.Generators;
import com.google.api.client.util.DateTime;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.EClassType;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.IJStatement;
import com.helger.jcodemodel.JAnnotationUse;
import com.helger.jcodemodel.JAssignment;
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
import com.helger.jcodemodel.JLambda;
import com.helger.jcodemodel.JLambdaParam;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JStringLiteral;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;
import com.helger.jcodemodel.JWhileLoop;

import io.konig.core.Context;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlBasicStructExpression;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlClass;
import io.konig.core.showl.ShowlDataSource;
import io.konig.core.showl.ShowlDerivedPropertyExpression;
import io.konig.core.showl.ShowlDerivedPropertyList;
import io.konig.core.showl.ShowlDerivedPropertyShape;
import io.konig.core.showl.ShowlDirectPropertyExpression;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlEnumIndividualReference;
import io.konig.core.showl.ShowlEnumJoinInfo;
import io.konig.core.showl.ShowlEnumNodeExpression;
import io.konig.core.showl.ShowlEnumPropertyExpression;
import io.konig.core.showl.ShowlEqualStatement;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlFilterExpression;
import io.konig.core.showl.ShowlFunctionExpression;
import io.konig.core.showl.ShowlIriReferenceExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPredicatePath;
import io.konig.core.showl.ShowlProperty;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlPropertyShapeGroup;
import io.konig.core.showl.ShowlSchemaService;
import io.konig.core.showl.ShowlStatement;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.showl.StaticDataSource;
import io.konig.core.showl.expression.ShowlExpressionBuilder;
import io.konig.core.showl.expression.ShowlLiteralExpression;
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
import io.konig.formula.FunctionExpression;
import io.konig.formula.FunctionModel;
import io.konig.formula.IriTemplateExpression;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.gcp.datasource.BigQueryTableReference;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudStorageBucket;
import io.konig.gcp.datasource.GoogleCloudStorageFolder;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class BeamTransformGenerator {
  private static final Logger logger = LoggerFactory.getLogger(BeamTransformGenerator.class);
  private static final List<RewriteRule> rewriteRuleList = new ArrayList<>();
  
  static {
    rewriteRuleList.add(new RewriteRule("(DoFn$", "("));
    rewriteRuleList.add(new RewriteRule("(DoFn.", "("));
    rewriteRuleList.add(new RewriteRule("@DoFn$", "@"));
    rewriteRuleList.add(new RewriteRule("@DoFn.", "@"));
    rewriteRuleList.add(new RewriteRule(".DoFn$", ".DoFn."));
    rewriteRuleList.add(new RewriteRule(".{}()", "{}"));
    rewriteRuleList.add(new RewriteRule("import org.apache.beam.sdk.transforms.DoFn;", 
    		"import org.apache.beam.sdk.transforms.DoFn;\n" +
        "import org.apache.beam.sdk.transforms.DoFn.ProcessElement;\n" +
    		"import org.apache.beam.sdk.transforms.DoFn.ProcessContext;"));
  }
  
  private PipelineConfig pipelineConfig;
  private String basePackage;
  private NamespaceManager nsManager;
  private JavaDatatypeMapper datatypeMapper;
  private ShowlSchemaService schemaService;
  private OwlReasoner reasoner;
  private ShowlExpressionBuilder expressionBuilder;
  
  private boolean failFast;
  private boolean encounteredError;
  
  public BeamTransformGenerator(PipelineConfig pipelineConfig, String basePackage, ShowlSchemaService schemaService, ShowlExpressionBuilder expressionBuilder) {
  	this.pipelineConfig = pipelineConfig;
    this.basePackage = basePackage;
    this.schemaService = schemaService;
    this.reasoner = schemaService.getOwlReasoner();
    this.expressionBuilder = expressionBuilder;
    this.nsManager = reasoner.getGraph().getNamespaceManager();
    datatypeMapper = new BasicJavaDatatypeMapper();
    
  }
  
  private String errorBuilderClassName() {
  	return basePackage + ".common.ErrorBuilder";
  }
  
  public boolean isFailFast() {
		return failFast;
	}

	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	public boolean isEncounteredError() {
		return encounteredError;
	}

	@SuppressWarnings("deprecation")
  public void generateAll(BeamTransformRequest request) throws BeamTransformGenerationException, IOException {

    
    
    List<File> childProjectList = new ArrayList<>();
    
    for (ShowlNodeShape node : request.getNodeList()) {
      // Consider refactoring so that we don't need to check that explicitDerivedFrom is not empty.
      // The list of nodes from the request should already be filtered!
      
        File projectDir = projectDir(request, node);
        childProjectList.add(projectDir);
        JCodeModel model = new JCodeModel();
        
        try {
          generateTransform(model, node);
          
          File javaDir = new File(projectDir, "src/main/java");
          javaDir.mkdirs();
          
          model.build(javaDir);
          rewrite(javaDir);
        } catch (Throwable e) {
        	String msg = "Failed to produce transform for " + node.getPath();
        	if (failFast) {
            throw new BeamTransformGenerationException("Failed to save Beam Transform code", e);
        	} else {
        		logger.error(msg, e);
        		encounteredError = true;
        	}
        }

        try {
          buildPom(request, projectDir, node);
        } catch (Throwable e) {
        	String msg = "Failed to generate pom.xml for " + node.getPath();
        	if (failFast) {
        		throw new BeamTransformGenerationException(msg, e);
        	} else {
        		logger.error(msg, e);
        		encounteredError = true;
        	}
        }
    }

    generateBeamParentPom(request, childProjectList);
    
    
  }
  
  private void generateBeamParentPom(BeamTransformRequest request, List<File> childProjectList) throws IOException, BeamTransformGenerationException {
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
      context.put("konigVersion", request.getKonigVersion());
      context.put("childProjectList", childProjectList);
      if(!request.getNodeList().isEmpty()) {
    	  context.put("batchEtlBucketIri", batchEtlBucketIri(request.getNodeList().get(0)));  
      }
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
  
  private String addOptionalParameters() {
	  StringBuilder br = new StringBuilder();
      br.append("#if($!{gcpNetwork})");
      br.append("<argument>--network=${gcpNetwork}</argument>");
      br.append("#end");
      br.append("#if($!{gcpSubNetwork})");
      br.append("<argument>--subnetwork=${gcpSubNetwork}</argument>");
      br.append("#end");
      br.append("#if($!{gcpWorkerMachineType})");
      br.append("<argument>--workerMachineType=${gcpWorkerMachineType}</argument>");
      br.append("#end");
      br.append("#if($!{region})");
      br.append("<argument>--region=${region}</argument>");
      br.append("#end ");
      return br.toString();
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
    context.put("konigVersion", request.getKonigVersion());
    context.put("batchEtlBucketIri", batchEtlBucketIri(node));
    context.put("addOptionalParameters", addOptionalParameters());
    
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
      if (ds != null && Konig.GoogleCloudStorageBucket.equals(ds.getDataSource())) {
          String result = bucketBaseIri(ds.getDataSource().getId());
          if (result != null) {
            return result;
          }
          fail("Could not detect batchEtlBucketIri for {0}", node.getPath());
        }
    }
   
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
    
  	// TODO: Split the base Worker class into SingleSourceWorker and MultiSourceWorker.
  	
  	Worker worker = isOverlay(targetShape) ? new OverlayWorker(model, targetShape) : new Worker(model, targetShape);
    
    return worker.generateTransform();
  }

	private boolean isOverlay(ShowlNodeShape targetNode) {
		boolean result = targetNode.getShapeDataSource().getDataSource().getEtlPattern().contains(Konig.OverlayPattern);
		return result;
	}

  
  class Worker {
    protected JCodeModel model;
    protected ShowlNodeShape targetNode;
    
    protected JDefinedClass mainClass;
    protected Map<URI,BeamChannel> sourceInfoMap = new LinkedHashMap<>();
//    private Map<URI,JDefinedClass> readFileFnMap = new HashMap<>();
//    private JDefinedClass readFileFnClass;
    protected JDefinedClass optionsClass;
    private JDefinedClass toTargetFnClass;
    private JDefinedClass mergeClass;
    
    private JDefinedClass iriClass;
    
    private Map<URI,Map<URI, RdfProperty>> enumClassProperties = new HashMap<>();
    private BeamTypeManager typeManager;
    

		private boolean isOverlay() {
			return BeamTransformGenerator.this.isOverlay(targetNode);
		}

    private BeamTypeManager typeManager() {
    	if (typeManager == null) {
    		typeManager = new BeamTypeManagerImpl(basePackage, reasoner, model, nsManager);
    	}
    	return typeManager;
    }
		
    public Worker(JCodeModel model, ShowlNodeShape targetShape) {
      this.model = model;
      this.targetNode = targetShape;
    }

    private boolean singleSource() {
      return singleChannel() != null;
    }
    
    protected URI keyType(ShowlPropertyShape p) {
			if (isIriReference(p)) {
				return XMLSchema.STRING;
			}
			
			return p.getValueType(reasoner);
		}
    
    private boolean isIriReference(ShowlPropertyShape p) {
			if (Konig.id.equals(p.getPredicate())) {
				return true;
			}
			PropertyConstraint c = p.getPropertyConstraint();
			if (c!=null && c.getNodeKind()==NodeKind.IRI) {
				return true;
			}
			return false;
		}

		private Class<?> javaType(ShowlPropertyShape p) throws BeamTransformGenerationException {
      Class<?> type = tryJavaDatatype(p);
      if (type == null || type == URI.class) {
        
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
      
      URI datatype = null;
      
      if (constraint != null) {
      	datatype = constraint.getDatatype();
      }
      
      if (datatype == null) {
      	URI range = p.getProperty().inferRange(reasoner);
      	if (range != null && reasoner.isDatatype(range)) {
      		datatype = range;
      	}
      }
      
      if (datatype == null) {
        return null;
      }
      if (XMLSchema.DATE.equals(datatype)) {
      	return String.class;
      }
      
      return datatypeMapper.javaDatatype(datatype);
    }

    
    protected JDefinedClass generateTransform() throws BeamTransformGenerationException {
      
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
        
        
        Map<URI, JFieldVar> enumIndex = enumIndex(owlClass, enumClass, propertyMap);
        
        
        enumClassProperties.put(owlClass, propertyMap);
        
        //  private static Map<String, $enumClassName> localNameMap = new HashMap<>();
        
        AbstractJClass stringClass = model.ref(String.class);
        AbstractJClass mapClass = model.ref(Map.class).narrow(stringClass).narrow(enumClass);
        AbstractJClass hashMapClass = model.ref(HashMap.class).narrow(stringClass).narrow(enumClass);
        
        JVar localNameMap = enumClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, mapClass, "localNameMap").init(hashMapClass._new());
        
        enumClass.init();
        
        JBlock staticInit = enumClass.init();
        for (Vertex individual : individuals) {
          JEnumConstant constant = enumMember(enumIndex, enumClass, staticInit, individual);
          
          constant.name();
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
          
          IJExpression returnValue = pipelineConfig.isCaseInsensitiveEnumLookup() ?
          		JExpr.cond(param.eqNull(), JExpr._null(), field.invoke("get").arg(param.invoke("toUpperCase"))) :
          		field.invoke("get").arg(param);
          
          method.body()._return(returnValue);
               
        }
        
        //   private IRI id;
        
        JFieldVar idField = enumClass.field(JMod.PRIVATE, iriClass, "id");

        //  public $enumClassName findByLocalName(String localName) {
        //    return localNameMap.get(localName);
        //  }
        
        JMethod findByLocalNameMethod = enumClass.method(JMod.PUBLIC | JMod.STATIC, enumClass, "findByLocalName");
        IJExpression localNameVar = findByLocalNameMethod.param(stringClass, "localName");
        
        if (pipelineConfig.isCaseInsensitiveEnumLookup()) {
        	localNameVar = JExpr.cond(localNameVar.eqNull(), JExpr._null(), localNameVar.invoke("toUpperCase"));
        }
        
        findByLocalNameMethod.body()._return(localNameMap.invoke("get").arg(localNameVar));
        
        //   public $enumClassName id(String namespace, String localName) {
        //     id = new IRI(namespace, localName);
        //     localNameMap.put(localName, this);
        //     return this;
        //   }
        
        JMethod idMethod = enumClass.method(JMod.PRIVATE, enumClass, "id");
        JVar namespaceParam = idMethod.param(stringClass, "namespace");
        IJExpression localNameParam = idMethod.param(stringClass, "localName");
        
        idMethod.body().assign(idField, iriClass._new().arg(namespaceParam).arg(localNameParam));
        
        if (pipelineConfig.isCaseInsensitiveEnumLookup()) {
        	localNameParam = localNameParam.invoke("toUpperCase");
        }
        
        idMethod.body().add(localNameMap.invoke("put").arg(localNameParam).arg(JExpr._this()));
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

    private Map<URI, JFieldVar> enumIndex(URI owlClass, JDefinedClass enumClass, Map<URI, RdfProperty> propertyMap) throws BeamTransformGenerationException {

      Map<URI, JFieldVar> map = new HashMap<>();
    	Set<ShowlPredicatePath> set = ShowlUtil.uniqueKeys(owlClass, targetNode);
    	for (ShowlPredicatePath p : set) {
    		RdfProperty property = propertyMap.get(p.getLast());
    	
    		if (property == null) {
    			fail("Enum property {0} not found for {1}", p.toString(), RdfUtil.localName(targetNode.getId()));
    		}

        URI propertyId = property.getId();
        String fieldName = propertyId.getLocalName();
        String mapName = fieldName + "Map";
        JFieldVar mapField = enumClass.fields().get(mapName);
        
        if (mapField==null) {
          Class<?> datatypeJavaClass = datatypeMapper.javaDatatype(property.getRange());
          AbstractJClass datatypeClass = model.ref(datatypeJavaClass);
          AbstractJClass mapClass = model.ref(Map.class).narrow(datatypeClass, enumClass);
          AbstractJClass hashMapClass = model.ref(HashMap.class).narrow(datatypeClass, enumClass);
          mapField = enumClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, mapClass, mapName, hashMapClass._new());
        }
        
        map.put(propertyId, mapField);
      }
      return map;
    }



		private JEnumConstant enumMember(Map<URI, JFieldVar> enumIndex, JDefinedClass enumClass, JBlock staticInit, Vertex individual) throws BeamTransformGenerationException {
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
        	if (pipelineConfig.isCaseInsensitiveEnumLookup()) {
        		litValue = JExpr.lit(object.stringValue().toUpperCase());
        	}
          staticInit.add(mapField.invoke("put").arg(litValue).arg(constant));
        }
        
          
      }

      staticInit.add(invoke);
      
      return constant;
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
      
      int first = localName.codePointAt(0);
      
      if (!Character.isJavaIdentifierStart(first)) {
      	localName = "_" + localName;
      }
      
      StringBuilder builder = new StringBuilder();
      for (int i=0; i<localName.length();) {
      	int c = localName.charAt(i);
      	if (Character.isJavaIdentifierPart(c)) {
      		builder.appendCodePoint(c);
      	} else {
      		builder.append('_');
      	}
      	i += Character.charCount(c);
      }
      
      
      
      return builder.toString();
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
      
      if (reasoner.isEnumerationClass(node.getOwlClass().getId())) {
        enumClasses.add(node.getOwlClass());
      }
      for (ShowlDirectPropertyShape p : node.getProperties()) {
        if (p.getValueShape() != null) {
          addEnumClasses(enumClasses, p.getValueShape());
        }
      }
      
    }


    private void declareToTargetClass() throws BeamTransformGenerationException, JClassAlreadyExistsException {
      if (singleSource()) {

    	SimpleTargetFnGenerator generator = new SimpleTargetFnGenerator(basePackage,nsManager,model,reasoner,typeManager);
        //ToTargetFnGenerator2 generator = new ToTargetFnGenerator2();
    	toTargetFnClass = generator.generate(targetNode, model.ref(TableRow.class));
      }
    }

    protected void declareReadFileFnClass() throws JClassAlreadyExistsException, BeamTransformGenerationException {
      if (singleSource()) {
      
      	declareSingleSourceReadFileFn();

 
      } else {
        declareFileToKvFn();
      }
    }
    

		private void declareSingleSourceReadFileFn() throws JClassAlreadyExistsException, BeamTransformGenerationException {

      ShowlChannel channel = singleChannel();
      ShowlNodeShape sourceNode = channel.getSourceNode();
      BeamChannel sourceInfo = new BeamChannel(channel);

      sourceInfoMap.put(RdfUtil.uri(sourceNode.getId()), sourceInfo);
      ReadFileFnGenerator generator = new ReadFileFnGenerator(sourceInfo);
      generator.generate();
			
		}
		
		private BeamChannel beamChannelForProperty(ShowlPropertyShape p) throws BeamTransformGenerationException {
			URI id = RdfUtil.uri(p.getRootNode().getId());
			BeamChannel channel = sourceInfoMap.get(id);
			if (channel == null) {
				fail("BeamChannel not found for {0}", p.getPath());
			}
			return channel;
		}


		private ShowlChannel singleChannel() {
      ShowlChannel channel = null;
      if (!isOverlay()) {
	      for (ShowlChannel c : targetNode.getChannels()) {
	        ShowlNodeShape sourceNode = c.getSourceNode();
	        if (!reasoner.isEnumerationClass(sourceNode.getOwlClass().getId())) {
	          if (channel == null) {
	            channel = c;
	          } else {
	            return null;
	          }
	        }
	      }
      }
      return channel;
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


    protected JDefinedClass generateFileToKvFn(ShowlPropertyShape keyProperty, ShowlChannel channel) throws BeamTransformGenerationException, JClassAlreadyExistsException {
      
      ShowlNodeShape node = channel.getSourceNode();
      if (isEnumNode(node)) {
        return null;
      }
      
      if (logger.isTraceEnabled()) {
        logger.trace("generateFileToKvFn({})", keyProperty.getPath());
      }
      
      URI sourceNodeId = RdfUtil.uri(channel.getSourceNode().getId());
      BeamChannel info = sourceInfoMap.get(sourceNodeId);
      if (info == null) {
        info = new BeamChannel(channel);
        sourceInfoMap.put(sourceNodeId, info);
      }
      DataSource ds = node.getShapeDataSource().getDataSource();
	  if (ds.isA(Konig.GoogleCloudStorageFolder) || ds.isA(Konig.GoogleCloudStorageBucket)) {
			FileToKvFnGenerator generator = new FileToKvFnGenerator(info, keyProperty);
			return generator.generate();
	  }
     return null;
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
    
    abstract class FnGenerator  {

		  protected JDefinedClass thisClass;
      protected JMethod concatMethod = null;
      protected JMethod requiredMethod = null;

			protected	BeamExpressionTransform etran;
			
		
			
			protected BeamExpressionTransform etran() {
				if (etran == null) {
					etran = new BeamExpressionTransform(reasoner, typeManager(), model, thisClass);
				}
				return etran;
			}
    }
    
		class TableRowToKvFnGenerator {
			private JDefinedClass fnClass;
			private ShowlNodeShape sourceNode;
			public TableRowToKvFnGenerator(ShowlNodeShape node) {
			 this.sourceNode = node;
			}

			public JDefinedClass generate() throws BeamTransformGenerationException {
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				AbstractJClass stringClass = model.ref(String.class);
				AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(tableRowClass);
				String fnClassName = fnClassName();
				try {

					fnClass = model._class(JMod.PUBLIC, fnClassName);
					AbstractJClass doFnClass = model.ref(DoFn.class).narrow(tableRowClass).narrow(kvClass);

					fnClass._extends(doFnClass);
					processElement(kvClass);

					JMethod method = fnClass.method(JMod.PRIVATE, stringClass, "getKey");
					JVar row = method.param(tableRowClass, "row");

					method.body()._return(row.invoke("get").arg(JExpr.lit("id")).castTo(stringClass));

				} catch (JClassAlreadyExistsException e) {
					fail("Failed to create {fnClassName} ", e);
				}
				return fnClass;
			}

			private void processElement(AbstractJClass outputClass) {
				AbstractJClass processContextClass = model.ref(ProcessContext.class);
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				AbstractJClass stringClass = model.ref(String.class);
				AbstractJClass kvClass = model.ref(KV.class);
				AbstractJClass throwableClass = model.ref(Throwable.class);
				AbstractJClass tupleTagClass = model.ref(TupleTag.class);

				JVar deadLetterTag = fnClass.field(JMod.PUBLIC | JMod.STATIC,
						tupleTagClass.narrow(model.ref(String.class)), "deadLetterTag")
						.init(tupleTagClass._new().narrow(model.ref(String.class)).invoke("{}"));
				
				JVar successTag = fnClass
						.field(JMod.PUBLIC | JMod.STATIC, tupleTagClass.narrow(outputClass), "successTag")
						.init(model.ref(TupleTag.class)._new().narrow(outputClass).invoke("{}"));
				
				JMethod method = fnClass.method(JMod.PUBLIC, model.VOID, "processElement");
				method.annotate(model.directClass(ProcessElement.class.getName()));

				JVar c = method.param(processContextClass, "c");

				JTryBlock tryBlock = method.body()._try();
				JVar row = tryBlock.body().decl(tableRowClass, "row").init(c.invoke("element"));
				JVar key = tryBlock.body().decl(stringClass, "key").init(JExpr.invoke("getKey").arg(row));

				tryBlock.body()
						.add(c.invoke("output").arg(successTag).arg(kvClass.staticInvoke("of").arg(key).arg(row)));

				JCatchBlock catchBlock = tryBlock._catch(throwableClass);
				JVar oops = catchBlock.param("oops");
				catchBlock.body().add(c.invoke("output").arg(deadLetterTag).arg(oops.invoke("getMessage")));

			}

			private String fnClassName() throws BeamTransformGenerationException {
				String shortName = ShowlUtil.shortShapeName(RdfUtil.uri(sourceNode.getId()));

				return mainPackage() + "." + shortName + "ToKvFn";
			}

		}

    private abstract class BaseReadFnGenerator extends FnGenerator {
		
		  protected BeamChannel sourceBeamChannel;
		
		  protected Map<Class<?>, JMethod> getterMap = new HashMap<>();
		  private JFieldVar patternField = null;
		  
		  public BaseReadFnGenerator(BeamChannel sourceInfo) {
		    this.sourceBeamChannel = sourceInfo;
		  }
		  
		  
		  protected void processElement(AbstractJClass outputClass) throws BeamTransformGenerationException {
		    
		    
		    
		    // private static final Logger LOGGER = LoggerFactory.getLogger("ReadFn");
		    AbstractJClass loggerClass = model.ref(Logger.class);
		    AbstractJClass tableRowClass = model.ref(TableRow.class);
		    AbstractJClass tupleTagClass = model.ref(TupleTag.class);
		    AbstractJClass tupleTagTableRowClass =  tupleTagClass.narrow(tableRowClass);
		    
			JFieldVar logger = thisClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC , loggerClass, 
					"LOGGER", 
					model.ref(LoggerFactory.class).staticInvoke("getLogger").arg("ReadFn"));
			
			JVar deadLetterTag = thisClass.field(JMod.PUBLIC | JMod.STATIC , tupleTagTableRowClass, 
					"deadLetterTag").init(tupleTagTableRowClass._new().invoke("{}"));
			
			JVar successTag = thisClass.field(JMod.PUBLIC | JMod.STATIC , tupleTagClass.narrow(outputClass), 
					"successTag").init(tupleTagClass._new().narrow(outputClass).invoke("{}"));
			
		    // @ProcessElement
		    // public void processElement(ProcessContext c, PipelineOptions options) {
		    
		    JMethod method = 
		        thisClass.method(JMod.PUBLIC, model.VOID, "processElement");
		    method.annotate(model.directClass(ProcessElement.class.getName()));
		    AbstractJClass processContextClass = model.directClass(ProcessContext.class.getName());
		    AbstractJClass pipelineOptionsClass = model.directClass(PipelineOptions.class.getName());
		    BlockInfo blockInfo = etran().beginBlock(method.body());
		    
		    JVar c = method.param(processContextClass, "c");
		    JVar pipelineOptions = method.param(pipelineOptionsClass, "options");
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
		        .arg(csvFormatClass.staticRef("RFC4180").invoke("withFirstRecordAsHeader").invoke("withSkipHeaderRecord")));
		    
		    // validateHeaders(csv);
		    innerBody.add(JExpr.invoke("validateHeaders").arg(csv));
		    
		    AbstractJClass hashMapClass = model.ref(HashMap.class);
		    
		    //private void validateHeaders(CSVParser csv)
		    JMethod methodValidateHeaders = thisClass.method(JMod.PRIVATE, model.VOID , "validateHeaders");
		    methodValidateHeaders.param(csvParserClass, "csv");
		    
		    //private void validateHeader(HashMap headerMap, String columnName, StringBuilder builder) 
		    JMethod methodValidateHeader = thisClass.method(JMod.PRIVATE, model.VOID , "validateHeader");
		    methodValidateHeader.param(model.ref(HashMap.class), "headerMap");        
		    JVar columnName = methodValidateHeader.param(model.ref(String.class), "columnName");
		    methodValidateHeader.param(model.ref(StringBuilder.class), "builder");
		    
		    JBlock methodValidateHeaderBody = methodValidateHeaders.body();
		    
		    //HashMap<String, Integer> headerMap = ((HashMap<String, Integer> ) csv.getHeaderMap());
		    JVar headerMap = methodValidateHeaderBody.decl(hashMapClass.narrow(model.ref(String.class), model.ref(Integer.class)), "headerMap")
	        		.init(csv.invoke("getHeaderMap").castTo(hashMapClass.narrow(model.ref(String.class), model.ref(Integer.class))));
	        		    
		    //StringBuilder builder = new StringBuilder();
		    JVar builder = methodValidateHeaderBody.decl(model.ref(StringBuilder.class), "builder").init(JExpr._new(model.ref(StringBuilder.class)));
		    
		    //       for(CSVRecord record : csv) {
		    
		    AbstractJClass csvRecordClass = model.ref(CSVRecord.class);
		    JForEach forEachRecordLoop = innerBody.forEach(csvRecordClass, "record", csv);
		    JVar record = forEachRecordLoop.var();
		    JBlock forEachRecord = forEachRecordLoop.body();
		    JVar exceptionMessageBr = forEachRecord.decl(model.ref(StringBuilder.class), "builder")
					.init(JExpr._new(model.ref(StringBuilder.class)));
		    
		    //         TableRow row = new TableRow();
		    
		    JVar row = forEachRecord.decl(tableRowClass, "row").init(tableRowClass._new());
		    
		    List<ShowlPropertyShape> sourceProperties = sourceProperties();
		
		    Set<URI> predicateSet = new HashSet<>();
		    for (ShowlPropertyShape sourceProperty : sourceProperties) {
		    	
		    	// It is possible that sourceProperties contains two different instances
		    	// of the same predicate.  We really ought to refactor by use a set of
		    	// ShowlPropertyShapeGroup instead of a list of ShowlPropertyShape.
		    	
		    	// For now, however, we'll just use a HashSet to keep track of properties that
		    	// have been processed to ensure that we do not process the same property twice.
		    	
		    	URI predicate = sourceProperty.getPredicate();
		    	if (predicateSet.contains(predicate)) {
		    		continue;
		    	}
		    	
		    	predicateSet.add(predicate);
		    	
		      Class<?> datatype = javaType(sourceProperty);
		      
		      AbstractJClass datatypeClass = datatype==GregorianCalendar.class ? model.ref(Long.class) : model.ref(datatype);
		      String fieldName = sourceProperty.getPredicate().getLocalName();
		      JMethod getter = getterMap.get(datatype);
		      if (getter == null) {
		        fail("Getter not found for {0}", datatype.getSimpleName());
		      }
		      
		      //validateHeader(headerMap, ${fieldName}, builder);
		      methodValidateHeaderBody.add(JExpr.invoke("validateHeader").arg(headerMap).arg(fieldName).arg(builder));
		      
		      //     $fieldName = ${getter}(record.get("${fieldName}"));
		      JVar fieldVar = forEachRecord.decl(datatypeClass, fieldName, 
		          JExpr.invoke(getter)
		          .arg(csv)
		          .arg(JExpr.lit(fieldName))
                  .arg(record)
                  .arg(exceptionMessageBr));
		      
		      blockInfo.putPropertyValue(sourceProperty.asGroup(), fieldVar);
		      
		      
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

		    createDerivedKey(forEachRecord);
		    
		    JBlock outputBlock = forEachRecord._if(row.invoke("isEmpty"))._then();
		    
		    outputBlock.add(exceptionMessageBr.invoke("append").arg(JExpr.lit("record is empty")));
		    
		    
		    
		    JConditional errorBlockIf = forEachRecord._if(exceptionMessageBr.invoke("length").gt0());
		    JBlock errorBlock = errorBlockIf._then();
			//._throw(JExpr._new(model.ref(Exception.class)).arg(exceptionMessageBr.invoke("toString")));
		    
		    //TableRow errorRow = row;
        	//errorRow.set("errorId", Generators.timeBasedGenerator().generate());
        	//errorRow.set("errorCreated", new Date().getTime() /1000);
        	//errorRow.set("errorMessage", builder.toString());
        	//errorRow.set("pipelineJobName", options.getJobName());
		    ShowlNodeShape sourceNode = sourceBeamChannel.getFocusNode();
		    String sourceShapeName = ShowlUtil.shortShapeName(sourceNode);	
		    JVar errorRow = errorBlock.decl(tableRowClass, "errorRow").init(tableRowClass._new());
		    errorBlock.add(errorRow.invoke("set").arg("errorId").arg(model.ref(Generators.class).staticInvoke("timeBasedGenerator").invoke("generate").invoke("toString")));
		    errorBlock.add(errorRow.invoke("set").arg("errorCreated").arg(model.ref(Date.class)._new().invoke("getTime").div(1000)));
		    errorBlock.add(errorRow.invoke("set").arg("errorMessage").arg(exceptionMessageBr.invoke("toString")));
		    errorBlock.add(errorRow.invoke("set").arg("pipelineJobName").arg(pipelineOptions.invoke("getJobName")));
		    errorBlock.add(errorRow.invoke("set").arg(sourceShapeName).arg(row));
		    errorBlock.add(c.invoke("output").arg(deadLetterTag).arg(errorRow));
		    //     } finally {
		    //        reader.close();
		    //     }
		   
		    deliverOutput(errorBlockIf._else(), c, row, successTag);
		    
		    innerTry._finally().add(stream.invoke("close"));
		    
		    //   } catch (Exception e) {
		    //     e.printStackTrace();
		    //   }
            AbstractJClass exceptionClass = model.directClass(Exception.class.getName());
            JCatchBlock catchBlock = tryBlock._catch(exceptionClass);
            JVar e = catchBlock.param("e");
            catchBlock.body().add(e.invoke("printStackTrace"));

		 // if (builder.length()> 0) {
		    JBlock headerBlock = methodValidateHeaderBody._if(builder.invoke("length").gt(JExpr.lit(0)))._then();
		    //LOGGER.warn("Mapping for {} not found", builder.toString());
		    headerBlock.add(logger.invoke("warn").arg("Mapping for {} not found").arg(builder.invoke("toString")));
		    
		    //if (headerMap.get(columnName) == null) {
		    JBlock headerBlock1 = methodValidateHeader.body()._if(headerMap.invoke("get").arg(columnName).eqNull())._then();
		    //builder.append(columnName);
		    headerBlock1.add(builder.invoke("append").arg(columnName));
		    headerBlock1.add(builder.invoke("append").arg(";"));
		    
		    etran().endBlock();
		  }
		
		
		  protected void createDerivedKey(JBlock forEachRecord) throws BeamTransformGenerationException {
				// Do nothing by default.
		  	// Subclasses may override
				
			}
		
		
		
		  abstract protected void deliverOutput(JBlock outputBlock, JVar c, JVar row) throws BeamTransformGenerationException;
		
		  abstract protected void deliverOutput(JBlock outputBlock, JVar c, JVar row, JVar tupleTag) throws BeamTransformGenerationException;
		  
		  protected List<ShowlPropertyShape> sourceProperties() throws BeamTransformGenerationException {
		    
		
		    List<ShowlPropertyShape> list = targetNode.selectedPropertiesOf(sourceBeamChannel.getFocusNode());
		    
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
		          javaClass == GregorianCalendar.class ? model.ref(Long.class) : model.ref(javaClass);
		      
		      AbstractJClass hashMap = model.ref(HashMap.class).narrow(model.ref(String.class), model.ref(Integer.class)); 
		      
		      AbstractJClass exception = model.ref( Exception.class ); 
		      
		      // $returnType ${returnType}Value(String stringValue) {
		      JMethod method = thisClass.method(JMod.PRIVATE, returnType, methodName)._throws(exception);
		
		      getterMap.put(javaClass, method);
		      JVar csvParser = method.param(model.ref(CSVParser.class), "csv");
		      JVar fieldName = method.param(stringClass, "fieldName");
		      JVar record = method.param(model.ref(CSVRecord.class), "record");
		      JVar exceptionMessageBr = method.param(model.ref(StringBuilder.class), "exceptionMessageBr");
		      
		      //   if (stringValue != null) {
		      
		      JVar headerMap = method.body().decl(hashMap, "headerMap")
		        		.init(csvParser.invoke("getHeaderMap").castTo(hashMap));
		        
		      JBlock ifConditionBlock = method.body()._if(headerMap.invoke("get").arg(fieldName).ne(JExpr._null()))._then().block();
		
		      JVar stringValue = ifConditionBlock.decl(stringClass, "stringValue").init(record.invoke("get").arg(fieldName));
		      JConditional if1 = ifConditionBlock._if(stringValue.ne(JExpr._null()));
		     
		      
		      
		      //     stringValue = stringValue.trim();
		      if1._then().assign(stringValue, stringValue.invoke("trim"));
		      
		      
		      
		      // if (stringValue.equals("InjectErrorForTesting")) 
		      JBlock errorTestingBlock =  if1._then()._if(stringValue.invoke("equals").arg(JExpr.lit("InjectErrorForTesting")))._then();
		      // throw new java.lang.Exception("Error in pipeline : InjectErrorForTesting");  
		      errorTestingBlock._throw(JExpr._new(exception).arg("Error in pipeline : InjectErrorForTesting"));
		      
		      //     if (stringValue.length() > 0) {
		      
		      JBlock block1 = if1._then()._if(stringValue.invoke("length").gt(JExpr.lit(0)))._then();
		      JTryBlock tryBlock = block1._try();
	          JBlock tryBody = tryBlock.body();
		      if (javaClass.equals(String.class)) {
		    	  tryBody._return(stringValue);
		             
		      } else if (javaClass==Boolean.class) {
		    	  tryBody._return(JExpr.lit("true").invoke("equalsIgnoreCase").arg(stringValue));
		        
		      } else if (javaClass == Long.class) { 
		        AbstractJClass longClass = model.ref(Long.class);
		        tryBody._return(longClass._new().arg(stringValue));
		        
		      } else if (javaClass == Integer.class){
		    	AbstractJClass intergerClass = model.ref(Integer.class);
			    tryBody._return(intergerClass._new().arg(stringValue));
			      
		      } else if (javaClass == Double.class || javaClass == double.class) {
		        AbstractJClass doubleClass = model.ref(Double.class);
		        tryBody._return(doubleClass._new().arg(stringValue));
		        
		      } else if (javaClass == Float.class) {
			        AbstractJClass floatClass = model.ref(Float.class);
			        tryBody._return(floatClass._new().arg(stringValue));
			        
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
		        
		        AbstractJClass dateTime = model.ref(DateTime.class);
		        JVar dateTimeVar = tryBody.decl(dateTime, "dateTimeValue").init(dateTime._new().arg(stringValue));		        
		        JConditional isDateIf = tryBody._if(dateTimeVar.invoke("isDateOnly"));
		        isDateIf._then()._return(dateTimeVar.invoke("getValue").div(1000));
		        
		        AbstractJClass instantClass = model.ref(Instant.class);
		        AbstractJClass offsetDateTimeClass = model.ref(OffsetDateTime.class);
		        AbstractJClass matcherClass = model.ref(Matcher.class);
		        AbstractJClass zonedDateTimeClass = model.ref(ZonedDateTime.class);
		        JConditional outerIf = tryBody._if(stringValue.invoke("contains").arg(JExpr.lit("T")));
		        
		        JConditional innerIf = outerIf._then()._if(stringValue.invoke("contains").arg(JExpr.lit("/")));
		        
		        innerIf._then()._return(instantClass.staticInvoke("from").arg(
		            zonedDateTimeClass.staticInvoke("parse").arg(stringValue)).invoke("toEpochMilli").div(1000));
		        
		        innerIf._elseif(stringValue.invoke("contains").arg("Z"))._then()._return(
		            instantClass.staticInvoke("parse").arg(stringValue).invoke("toEpochMilli").div(1000));
		        
		        innerIf._else()._return(instantClass.staticInvoke("from").arg(
		            offsetDateTimeClass.staticInvoke("parse").arg(stringValue)).invoke("toEpochMilli").div(1000));
		        
		        JVar matcher = tryBody.decl(matcherClass, "matcher", 
		            datePattern.invoke("matcher").arg(stringValue));
		        
		        JConditional ifMatches = tryBody._if(matcher.invoke("matches"));
		        
		        JBlock ifMatchesBlock = ifMatches._then();
		        
		        JVar datePart = ifMatchesBlock.decl(stringClass, "datePart", 
		            matcher.invoke("group").arg(JExpr.lit(1)));
		        
		        JVar zoneOffset = ifMatchesBlock.decl(stringClass, "zoneOffset", 
		            matcher.invoke("group").arg(JExpr.lit(2)));
		        
		        ifMatchesBlock._if(zoneOffset.invoke("length").eq(JExpr.lit(0))
		            .cor(zoneOffset.invoke("equals").arg(JExpr.lit("Z"))))._then().add(
		                JExpr.assign(stringValue, datePart.plus("T00:00:00.000").plus(zoneOffset)));
		        
		        ifMatchesBlock._return(instantClass.staticInvoke("from").arg(
		            offsetDateTimeClass.staticInvoke("parse").arg(stringValue)).invoke("toEpochMilli").div(1000));
		      
		      
		        
		      } else {
		        // TODO: Handle other datatypes
		        fail("Field type {0} not supported yet, for property {1}.", typeName, p.getPath());
		      }
		      
		      //     }
		      //   }
		      // }
		      
		      JCatchBlock catchBlock = tryBlock._catch(model.ref(Exception.class));
	          JVar message = catchBlock.body().decl(model._ref(String.class), "message");
	          message.init(model.directClass("String").staticInvoke("format").arg("Invalid "+ typeName + " value for %s;").arg(JExpr.ref(fieldName)));
	          
	          catchBlock.body().add(exceptionMessageBr.invoke("append").arg(message));
	          method.body()._return(JExpr._null());
		    }
		    
		    
		  }
		  
		
		}


		abstract class BaseTargetFnGenerator2 extends FnGenerator {
		
		
		
					abstract protected void declareClass() throws BeamTransformGenerationException;
					
		      abstract protected BeamChannel beamChannel(ShowlNodeShape sourceNode) throws BeamTransformGenerationException;
		      
		      protected JDefinedClass generate() throws BeamTransformGenerationException {
						declareClass();
		      	processElementMethod();
		      	return thisClass;
		      }
		      
		
		
					protected String sourceRowName(ShowlChannel channel) {
						String shapeName = RdfUtil.shortShapeName(channel.getSourceNode().getShape().getId());
						shapeName = StringUtil.firstLetterLowerCase(shapeName);
						
						return shapeName + "Row";
					}
		
		     
		      protected BeamMethod processProperty(
		      		String targetPropertyPrefix, 
		      		ShowlDirectPropertyShape direct
		      ) throws BeamTransformGenerationException {
		
		      	
		      	JDefinedClass errorBuilderClass = errorBuilderClass();
		        AbstractJClass tableRowClass = model.ref(TableRow.class);
		        
		      	String methodName = targetPropertyPrefix + direct.getPredicate().getLocalName();
		      	
		      	
		      	// private void $methodName(TableRow $sourceRow1, TableRow $sourceRow2, ..., TableRow outputRow, ErrorBuilder errorBuilder) {
		      	
		      	JMethod method = thisClass.method(JMod.PRIVATE, model.VOID, methodName);
		      	BeamExpressionTransform etran = etran();
		      	BlockInfo blockInfo = etran.beginBlock(method.body());
		      	BeamMethod beamMethod = new BeamMethod(method);
		      	
		      	blockInfo.beamMethod(beamMethod);
		      	// TODO: declare nodeTableRow
		      	try {
		      	
		      		etran.addRowParameters(beamMethod, direct.getSelectedExpression());

		      		JVar errorBuilderParam = etran.addErrorBuilderParam(beamMethod);
		      		JVar outputRowParam = etran.addTableRowParam(beamMethod, direct.getDeclaringShape().effectiveNode());
		      		
			      	
			      	if (direct.getSelectedExpression() instanceof ShowlArrayExpression) {
			      		processArrayProperty(beamMethod, direct);
			      	} else 	if (direct.isEnumIndividual()) {
			      		ShowlEnumJoinInfo enumJoinInfo = ShowlEnumJoinInfo.forEnumProperty(direct);
			      		processEnumNode(beamMethod,  outputRowParam, direct, enumJoinInfo, errorBuilderParam);
			      		
			      	} else if (direct.getValueShape() != null) {
			      		
			      		//  TableRow $nestedRecord = new TableRow();
			      		JVar nestedRecord = method.body().decl(tableRowClass, direct.getPredicate().getLocalName()).init(tableRowClass._new());
			
			      		String prefix = methodName + "_";
			  				for (ShowlDirectPropertyShape child : direct.getValueShape().getProperties()) {
			  					BeamMethod childMethod = processProperty(prefix, child);
			  					etran.invoke(childMethod);
			  				}
			  				
			  				//  if (errorBuilder.isEmpty() && !$nestedRecord.isEmpty() ) {
			  				//    outputRow.set("$targetProperty", $nestedRecord);
			  				//    return true;
			  				//  } else {
			  				//    return false;
			  				//  }
			  				
			  				JConditional ifStatement = method.body()._if(errorBuilderParam.invoke("isEmpty").cand(nestedRecord.invoke("isEmpty").not()));
			  				
			  				ifStatement._then().add(
			  						outputRowParam.invoke("set").arg(JExpr.lit(direct.getPredicate().getLocalName())).arg(nestedRecord));
				  				
				  			
			      	} else {
			      		
			      		for (ShowlPropertyShape p : beamMethod.getSourceProperties()) {
			      			etran.generateSourceProperty(p);
			      		}
			
				      	
				
				    		
				    		// if ($sourceProperty1 !=null && $sourceProperty2!=null ...) {
				    		//   outputRow.set("$targetProperty", $expression);
				      	//   return true;
				    		// } else {
				      	//    $addErrorMessage
				      	//    return false;
				      	// }
				      	
				      	IJExpression condition = null;
				      	for (ShowlPropertyShape p : beamMethod.getSourceProperties()) {
				      		
				      		JVar propertyValue = blockInfo.getPropertyValue(p.asGroup());
				      		
				      		IJExpression c = propertyValue.neNull();
				      		
				      		if (condition == null) {
				      			condition = c;
				      		} else {
				      			condition = condition.cand(c);
				      		}
				      	}
				      	
				      	JConditional ifStatement = condition==null ? null : method.body()._if(condition);
				      	
				      	String targetPropertyName = direct.getPredicate().getLocalName();
				      	
				      	ShowlExpression e = selectedExpression(direct);
				      	
				      	IJExpression value = etran().transform(e);
				      	
				      	JBlock thenBlock = condition==null ? method.body() : ifStatement._then();
				      		
				      	thenBlock.add(outputRowParam.invoke("set")
				      			.arg(JExpr.lit(targetPropertyName)).arg(value));
				      	
				      	
				      	if (condition != null && direct.isRequired()) {
			
					      	// Construct the error message.
				      		
				      		Set<ShowlPropertyShape> sourcePropertySet = beamMethod.getSourceProperties();
				      		
					      	if (sourcePropertySet.size()==1) {
					      		
					      		String sourcePath = sourcePropertySet.iterator().next().getPath();
					      		
					      		StringBuilder message = new StringBuilder();
					      		message.append("Cannot set ");
					      		message.append(direct.fullPath());
					      		message.append(" because ");
					      		message.append(sourcePath);
					      		message.append(" is null");
					      		
					      		ifStatement._else().add(errorBuilderParam.invoke("addError").arg(JExpr.lit(message.toString())));
					      		
					      	} else {
					      		StringBuilder message = new StringBuilder();
					      		message.append("Cannot set ");
					      		message.append(direct.fullPath());
					      		
					      		ifStatement._else().add(errorBuilderParam.invoke("addError").arg(JExpr.lit(message.toString())));
					      	}
				      	}
			      	}
			      
			      	return beamMethod;
		      	} finally {
		      		etran.endBlock();
		      	}
					}

					private void processArrayProperty(BeamMethod beamMethod, ShowlDirectPropertyShape targetProperty) throws BeamTransformGenerationException {
					
		      	BlockInfo blockInfo = etran().peekBlockInfo();
		      	
		      	ShowlArrayExpression e = (ShowlArrayExpression) targetProperty.getSelectedExpression();
		      	
		      	AbstractJClass tableRowClass = model.ref(TableRow.class);
		      	AbstractJClass listClass = model.ref(List.class).narrow(tableRowClass);
		      	AbstractJClass arrayListClass = model.ref(ArrayList.class);
		      	
		      	JBlock block = blockInfo.getBlock();
		      	
		      	JVar list = block.decl(listClass, "list").init(arrayListClass._new());
		      	blockInfo.setListVar(list);
		      	
		      	int memberIndex = 0;
		      	
		      	for (ShowlExpression member : e.getMemberList()) {
		      		if (member instanceof ShowlStructExpression) {
		      			processArrayMember(beamMethod, memberIndex, list, (ShowlStructExpression) member,
		      					targetProperty);
		      			
		      		} else {
		      			fail("Array member of type {0} not supported for {1}", 
		      					member.getClass().getSimpleName(), targetProperty.getPath());
		      		}
		      		memberIndex++;
		      	}
		      	
		      	String fieldName = targetProperty.getPredicate().getLocalName();
		      	JConditional ifStatement = block._if(list.invoke("isEmpty").not());
		      	ifStatement._then().add(
		      			blockInfo.getOutputRow().invoke("set")
		      			.arg(JExpr.lit(fieldName))
		      			.arg(list));
		      	
		      	if (targetProperty.isRequired()) {
		      		String err = MessageFormat.format(
		      				"The ''{0}'' collection must contain at least one value, but found none", fieldName);
		      		ifStatement._else().add(
		      				blockInfo.getErrorBuilderVar().invoke("addError").arg(JExpr.lit(err)));
		      	}
						
					}

					private void processArrayMember(BeamMethod thisBeamMethod, int memberIndex, JVar list,
							ShowlExpression member, ShowlDirectPropertyShape targetProperty) throws BeamTransformGenerationException {
						
						String methodName = thisBeamMethod.name() + '_' + memberIndex;

						BeamExpressionTransform etran = etran();
						BlockInfo callerBlockInfo = etran.peekBlockInfo();
						
						JVar callerList = callerBlockInfo.getListVar();
						
						JMethod method = thisClass.method(JMod.PRIVATE, model.VOID, methodName);
						BeamMethod beamMethod = new BeamMethod(method);
						BlockInfo thisBlock = etran().beginBlock(beamMethod);
						try {
							thisBlock.beamMethod(beamMethod);
							thisBlock.addListParam(beamMethod, callerList.type(), callerList.name());
							etran.addRowParameters(beamMethod, member);
							beamMethod.addErrorBuilderParam(errorBuilderClass());
							
							etran.processProperty(targetProperty, member);
							
							
							
							
						} finally {
							etran().endBlock();
						}
						

						etran.invoke(beamMethod);
					}

					private void processEnumNode(
							BeamMethod thisBeamMethod,
							JVar outputRow, 
							ShowlDirectPropertyShape targetProperty, 
							ShowlEnumJoinInfo enumJoinInfo,
							JVar errorBuilder
					) throws BeamTransformGenerationException {
						
						if (enumJoinInfo.getHardCodedReference() != null) {
							processHardCodedEnum(thisBeamMethod, targetProperty, enumJoinInfo, errorBuilder);
						} else if (enumJoinInfo.getSourceProperty() != null) {
							joinEnumNode(thisBeamMethod, outputRow, targetProperty, enumJoinInfo, errorBuilder);
						} else if (enumJoinInfo.getExpression()!=null) {
							joinEnumNodeByExpression(thisBeamMethod, outputRow, targetProperty, enumJoinInfo, errorBuilder);
						} else {
							fail("processEnumNode: enum lookup method not supported for {0}", targetProperty.getPath());
						}
						
					}
		
					private void joinEnumNodeByExpression(
							BeamMethod thisBeamMethod,
							JVar outputRow,
							ShowlDirectPropertyShape targetProperty, 
							ShowlEnumJoinInfo enumJoinInfo, 
							JVar errorBuilder
					) throws BeamTransformGenerationException {
						
						String targetPropertyName = targetProperty.getPredicate().getLocalName();
						
						String enumClassName = enumClassName(targetProperty.getOwlClassId());
						JDefinedClass enumClass = model._getClass(enumClassName);
						
						ShowlExpression joinExpression = enumJoinInfo.getExpression();
						BeamExpressionTransform etran = etran();
						
						JBlock body = thisBeamMethod.getMethod().body();
						
						IJExpression initExpression = etran.transform(joinExpression);
						JVar enumMember = body.decl(enumClass, targetPropertyName).init(initExpression);
						JConditional ifStatement = body._if(enumMember.neNull()); 
						
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						String enumRowName = targetPropertyName + "Row";
						
						JBlock thenBlock = ifStatement._then();
						JVar enumRow = thenBlock.decl(tableRowClass, enumRowName).init(tableRowClass._new());

						String methodName = thisBeamMethod.name();
						String prefix = methodName + "_";
						for (ShowlDirectPropertyShape child : targetProperty.getValueShape().getProperties()) {
							JMethod fieldMethod = enumPropertyMethod(prefix, enumClass, child);
							thenBlock.add(JExpr.invoke(fieldMethod).arg(enumMember).arg(enumRow).arg(errorBuilder));
						
						}
						thenBlock.add(outputRow.invoke("set")
								.arg(JExpr.lit(targetProperty.getPredicate().getLocalName()))
								.arg(enumRow)
							);
						
						if (targetProperty.isRequired()) {
								
							JStringLiteral message = JExpr.lit(
									"Required field '" + targetProperty.getPredicate().getLocalName() + "' is NULL");

							body.add(errorBuilder.invoke("addError").arg(message));
						}
						
						
					}

					private JMethod enumPropertyMethod(String prefix, JDefinedClass enumClass, ShowlDirectPropertyShape p) throws BeamTransformGenerationException {
					
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						AbstractJClass errorBuilderClass = errorBuilderClass();
						
						
						String methodName = prefix + p.getPredicate().getLocalName();
						JMethod method = thisClass.method(JMod.PRIVATE, model.VOID, methodName);
						
						String enumMemberName = StringUtil.firstLetterLowerCase(enumClass.name());
						String rowName = enumMemberName + "Row";
						
						JVar enumMember = method.param(enumClass, enumMemberName);
						JVar row = method.param(tableRowClass, rowName);
						JVar errorBuilder = method.param(errorBuilderClass, "errorBuilder");
						
						URI predicate = p.getPredicate();
						
						if (Konig.id.equals(predicate)) {
							method.body().add(row.invoke("set").arg(JExpr.lit("id")).arg(enumMember.invoke("getId").invoke("getLocalName")));
						} else {
							AbstractJClass objectClass = model.ref(Object.class);
							
							String getterName = "get" + StringUtil.capitalize(predicate.getLocalName());
							
							JVar value = method.body().decl(objectClass, predicate.getLocalName()).init(enumMember.invoke(getterName));
							JConditional ifStatement = method.body()._if(value.neNull());
							JBlock thenBlock = ifStatement._then();
							thenBlock.add(row.invoke("set").arg(JExpr.lit(predicate.getLocalName())).arg(value));
							
							
							if (p.isRequired()) {
								JBlock elseBlock = ifStatement._else();
								StringBuilder msg = new StringBuilder();
								msg.append(p.getPath());
								msg.append(" must not be null, but is not defined for ");
								JInvocation invoke = errorBuilder.invoke("addError")
										.arg(JExpr.lit(msg.toString()).plus(enumMember.invoke("name")));
								
								elseBlock.add(invoke);
							}
						}
						
						return method;
					}

					private void joinEnumNode(BeamMethod thisBeamMethod, JVar outputRow, 
							ShowlDirectPropertyShape targetProperty, ShowlEnumJoinInfo enumJoinInfo, JVar errorBuilder) throws BeamTransformGenerationException {
						
						BlockInfo blockInfo = etran().peekBlockInfo();
						
						ShowlPropertyShape enumProperty = enumJoinInfo.getEnumProperty();
						ShowlPropertyShape sourceProperty =  enumJoinInfo.getSourceProperty();
		
		        JVar inputRow = blockInfo.getTableRowVar(sourceProperty.getDeclaringShape().effectiveNode());
		        
		        String sourceKeyName = inputRow.name() + "_" + sourceProperty.getPredicate().getLocalName();
		        
		        
		
		        String enumKeyName = enumProperty.getPredicate().getLocalName();
						String findMethodName = enumProperty.getPredicate().equals(Konig.id) ?
		        		"findByLocalName" : "findBy" + StringUtil.capitalize(enumKeyName);
		        
		        /*
		         *  For now we assume that the source key is a string.  We'll relax this assumption later.
		         */
						
						//
		        
		        // String $sourceKey = inputRow.get("$sourceKey");
		        // if ($sourceKey != null) {
		    		//   TableRow $nestedRecord = new TableRow();
						//   $enumClass enumMember = $enumClass.$findMethod($sourceKey.toString());
		        //  ...
		        //  }
		        
		        JBlock block = thisBeamMethod.getMethod().body();
		        
		        AbstractJClass objectClass = model.ref(Object.class);
		        JVar sourceKeyVar = block.decl(objectClass, sourceKeyName, inputRow.invoke("get").arg(JExpr.lit(sourceProperty.getPredicate().getLocalName())));		        
						
		        block = block._if(sourceKeyVar.neNull())._then();
		
						AbstractJClass tableRowClass = model.ref(TableRow.class);
		    		JVar nestedRecord = block.decl(tableRowClass, targetProperty.getPredicate().getLocalName() + "Row").init(tableRowClass._new());
		       
		        
						String enumClassName = enumClassName(targetProperty.getOwlClassId());
						JDefinedClass enumClass = model._getClass(enumClassName);
						
						String targetPropertyName = targetProperty.getPredicate().getLocalName();
			
		    		JVar enumMember = block.decl(enumClass, targetPropertyName).init(enumClass.staticInvoke(findMethodName).arg(sourceKeyVar.invoke("toString")));
		    		
		    		ShowlNodeShape enumSourceNode = enumSourceNode(targetProperty);
		    		blockInfo.putEnumMember(targetProperty.getValueShape().effectiveNode(), enumMember);
		    		blockInfo.putEnumMember(enumSourceNode.effectiveNode(), enumMember);
		    		
//		    		BeamChannel channel = beamChannel(enumSourceNode);
		    		
//		    		channel.setSourceRow(enumMember);
						
						String prefix = thisBeamMethod.name() + "_";
						
		
						for (ShowlDirectPropertyShape child : targetProperty.getValueShape().getProperties()) {
							ShowlExpression e = child.getSelectedExpression();
							if (e instanceof ShowlEnumPropertyExpression) {
								BeamMethod childMethod = processProperty(prefix, child);
								etran.invoke(childMethod);
							} else if (e instanceof ShowlPropertyExpression) {
								ShowlPropertyShape p = ((ShowlPropertyExpression) e).getSourceProperty();
								if (p.getPredicate().equals(sourceProperty.getPredicate())) {
									// nestedRecord.set("$childPredicate", $sourceKeyVar);
									String childPredicate = child.getPredicate().getLocalName();
									block.add(nestedRecord.invoke("set").arg(JExpr.lit(childPredicate)).arg(sourceKeyVar));
								}
							} else {
								fail("Unsupported expression {0} at {1}", e.displayValue(), child.getPath());
							}
						}
						
						//  if (!outputRow.isEmpty()) {
						//    outputRow.set("$targetPropertyName", $nestedRecord);
						//  }
						block._if(outputRow.invoke("isEmpty").not())._then().add(
								outputRow.invoke("set").arg(JExpr.lit(targetPropertyName)).arg(nestedRecord));
						
					}
		
					private void processHardCodedEnum(
							BeamMethod thisBeamMethod, 
							ShowlDirectPropertyShape targetProperty,
							ShowlEnumJoinInfo enumJoinInfo,
							JVar errorBuilder
					) throws BeamTransformGenerationException {
							
						ShowlEnumIndividualReference ref = enumJoinInfo.getHardCodedReference();
						URI enumIndividualId = ref.getIriValue();
						String enumMemberName = enumMemberName(enumIndividualId);
						String enumClassName = enumClassName(targetProperty.getOwlClassId());
						
						String targetPropertyName = targetProperty.getPredicate().getLocalName();
						
						JDefinedClass enumClass = model._getClass(enumClassName);
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						
		
						JBlock block = thisBeamMethod.getMethod().body();
						
		    		//  TableRow $nestedRecord = new TableRow();
		    		JVar nestedRecord = block.decl(tableRowClass, targetProperty.getPredicate().getLocalName() + "Row").init(tableRowClass._new());
		
						// $enumClass $targetPropertyName = $enumType.$enumMemberName;
			
		    		JVar enumMember = block.decl(enumClass, targetPropertyName).init(enumClass.staticRef(enumMemberName));
		    		
		    		ShowlNodeShape enumSourceNode = enumSourceNode(targetProperty);
		    		BeamChannel channel = beamChannel(enumSourceNode);
		    		
		    		channel.setSourceRow(enumMember);
		    		
		    		String methodName = thisBeamMethod.name();
						
						String prefix = methodName + "_";
						
		
						for (ShowlDirectPropertyShape child : targetProperty.getValueShape().getProperties()) {
							BeamMethod childMethod = processProperty(prefix, child);
							etran.invoke(childMethod);
						}
						
					}
		
					private ShowlNodeShape enumSourceNode(ShowlDirectPropertyShape targetProperty) throws BeamTransformGenerationException {
						ShowlExpression e = targetProperty.getSelectedExpression();
						if (e instanceof ShowlEnumNodeExpression) {
							return ((ShowlEnumNodeExpression) e).getEnumNode();
						}
						fail("enum source node not found for {0}", targetProperty.getPath());
						return null;
					}
		
					/**
		       * This is a complete hack.  
		       * TODO: remove this hack once ShowlStructExpression is fully supported.
		       */
		      protected ShowlExpression selectedExpression(ShowlDirectPropertyShape direct) {
						ShowlExpression e = direct.getSelectedExpression();
						if (e == null && direct.getValueShape()!=null) {
							return new ShowlBasicStructExpression(direct);
						}
						return e;
					}
		
		      

					protected JDefinedClass errorBuilderClass() throws BeamTransformGenerationException {
						String errorBuilderClassName = errorBuilderClassName();
						JDefinedClass errorBuilderClass = model._getClass(errorBuilderClassName);
						
						if (errorBuilderClass == null) {
							
							try {
								
								AbstractJClass stringBuilderClass = model.ref(StringBuilder.class);
								AbstractJClass stringClass = model.ref(String.class);
								
								errorBuilderClass = model._class(JMod.PUBLIC, errorBuilderClassName);
								JVar buffer = errorBuilderClass.field(JMod.PRIVATE, stringBuilderClass, "buffer");
								buffer.init(JExpr._new(stringBuilderClass));
								
								JMethod isEmpty = errorBuilderClass.method(JMod.PUBLIC, model._ref(boolean.class), "isEmpty");
								isEmpty.body()._return(buffer.invoke("length").eq(JExpr.lit(0)));
								
								JMethod addError = errorBuilderClass.method(JMod.PUBLIC, model.VOID, "addError");
								JVar text = addError.param(stringClass, "text");
								
								addError.body()._if(JExpr.invoke("isEmpty").not())._then().add(buffer.invoke("append").arg(JExpr.lit("; ")));
								addError.body().add(buffer.invoke("append").arg(text));
								
								JMethod toString = errorBuilderClass.method(JMod.PUBLIC, stringClass, "toString");
								toString.body()._return(buffer.invoke("toString"));
								
								
								
							} catch (JClassAlreadyExistsException e) {
								throw new BeamTransformGenerationException("Failed to create ErrorBuilder class", e);
							}
							
						}
						
						return errorBuilderClass;
						
					}
					

					
		      private void addPropertyGroups(ShowlDirectPropertyShape direct, Set<ShowlPropertyShapeGroup> groupSet) throws BeamTransformGenerationException {
						ShowlExpression e = direct.getSelectedExpression();
						if (e != null) {
							BeamUtil.collectSourceProperties(groupSet, e, reasoner);
							
						} else if (direct.getValueShape() != null) {
							// In the future, we might construct a ShowlStructExpression for all
							// properties with a value Shape.  In that case, we would never reach this block of code.
							
							for (ShowlDirectPropertyShape p : direct.getValueShape().getProperties()) {
								addPropertyGroups(p, groupSet);
							}
						} else {
							fail("{0} has no selectedExpression and no valueShape", direct.getPath());
						}
						
					}
					
		      // TODO: Eliminate this method.
		      // We should be using ShowlStructExpression for well-defined value shapes instead of omitting the selected expression.
		      // This method is a temporary work around.
					private void addProperties(ShowlDirectPropertyShape direct, Set<ShowlPropertyShape> sourcePropertySet) throws BeamTransformGenerationException {
						
						ShowlExpression e = direct.getSelectedExpression();
						if (e == null) {
							if (direct.getValueShape() != null) {
								for (ShowlDirectPropertyShape p : direct.getValueShape().getProperties()) {
									addProperties(p, sourcePropertySet);
								}
							} else {
								throw new BeamTransformGenerationException("Property has no selected expression: " + direct.getPath());
							}
						} else if (e instanceof ShowlEnumNodeExpression) {
							ShowlEnumNodeExpression enumExpr = (ShowlEnumNodeExpression)e;
							ShowlChannel channel = enumExpr.getChannel();
							if (channel != null && channel.getJoinStatement()!=null) {
								sourcePropertySet.addAll(BeamUtil.nonEnumProperties(channel.getJoinStatement(), reasoner));
							}
						} else {
		
							e.addProperties(sourcePropertySet);
						}
						
					}
		
					protected void processElementMethod() throws BeamTransformGenerationException {
		
						JDefinedClass errorBuilderClass = errorBuilderClass();
						AbstractJClass processContextClass = model.directClass(ProcessContext.class.getName());
						AbstractJClass throwableClass = model.ref(Throwable.class);
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						AbstractJClass tupleTagClass = model.ref(TupleTag.class);
						// @ProcessElement
						// public void processElement(ProcessContext c) {
		
						JMethod method = thisClass.method(JMod.PUBLIC, model.VOID, "processElement");
						JVar deadLetterTag = thisClass
								.field(JMod.PUBLIC | JMod.STATIC, tupleTagClass.narrow(model.ref(String.class)), "deadLetterTag")
								.init(tupleTagClass._new().narrow(model.ref(String.class)).invoke("{}"));
		
						JVar successTag = thisClass.field(JMod.PUBLIC | JMod.STATIC, tupleTagClass.narrow(tableRowClass), "successTag")
								.init(tupleTagClass._new().narrow(tableRowClass).invoke("{}"));
		
						BlockInfo blockInfo = etran().beginBlock(method.body());
						
						try {
						
							method.annotate(ProcessElement.class);
							JVar c = method.param(processContextClass, "c");
			
							// try {
							JTryBlock tryBlock = method.body()._try();
			
							// ErrorBuilder errorBuilder = new ErrorBuilder();
							JVar errorBuilder = tryBlock.body().decl(errorBuilderClass, "errorBuilder").init(errorBuilderClass._new());
							blockInfo.errorBuilderVar(errorBuilder);
			
							// TableRow outputRow = new TableRow();
			
							JVar outputRow = tryBlock.body().decl(tableRowClass, "outputRow").init(tableRowClass._new());
							blockInfo.putTableRow(targetNode.effectiveNode(), outputRow);
			
							// KV<String, CoGbkResult> e = c.element();
			
							/* ... OR ... */
			
							// TableRow inputRow = c.element();
			
							contextElement(tryBlock.body(), c);
			
							Collection<ShowlDirectPropertyShape> propertyList = sortProperties(targetNode);
			
							for (ShowlDirectPropertyShape direct : propertyList) {
								BeamMethod childMethod = processProperty("", direct);
								etran.invoke(childMethod);
							}
			
							// if (!outputRow.isEmpty() && errorBuilder.isEmpty()) {
							//   c.output(outputRow);
							// }
			
							tryBlock.body()._if(outputRow.invoke("isEmpty").not().cand(errorBuilder.invoke("isEmpty")))._then()
									.add(c.invoke("output").arg(successTag).arg(outputRow));
			
							JBlock exceptionBlock = tryBlock.body()._if(errorBuilder.invoke("isEmpty").not())._then();
							exceptionBlock.add(errorBuilder.invoke("addError").arg(outputRow.invoke("toString")));
							exceptionBlock._throw(JExpr._new(model.ref(Exception.class)).arg(errorBuilder.invoke("toString")));
			
							JCatchBlock catchBlock = tryBlock._catch(throwableClass);
							JVar oopsVar = catchBlock.param("oops");
							catchBlock.body().add(c.invoke("output").arg(deadLetterTag).arg(oopsVar.invoke("getMessage")));
							
						} finally {
							etran().endBlock();
						}
		
					}
		
					/**
					 * Sort the target properties so that those properties that are constructed by formulas come after the properties
					 * that they depend upon.
					 */
					@SuppressWarnings("unchecked")
					private Collection<ShowlDirectPropertyShape> sortProperties(ShowlNodeShape targetNode) {
						
						Collection<ShowlDirectPropertyShape> list = targetNode.getProperties();
						if (!hasFormula(list)) {
							return list;
						}
						
						List<PropertyDependencies> dependList = new ArrayList<>();
						for (ShowlDirectPropertyShape p : list) {
							Set<ShowlPropertyShape> set = null;
							ShowlExpression formula = p.getFormula();
							if (formula == null) {
								set = Collections.EMPTY_SET;
							} else {
								set = new HashSet<>();
								formula.addProperties(set);
							}
							dependList.add(new PropertyDependencies(p, set));
						}
						
						Collections.sort(dependList);
						
						List<ShowlDirectPropertyShape> result = new ArrayList<>();
						for (PropertyDependencies p : dependList) {
							result.add(p.getTargetProperty());
						}
						
						
						return result;
					}

					
					private boolean hasFormula(Collection<ShowlDirectPropertyShape> list) {
						for (ShowlDirectPropertyShape direct : list) {
							if (direct.getFormula()!=null) {
								return true;
							}
						}
						return false;
					}

					abstract protected JVar contextElement(JBlock body, JVar c) throws BeamTransformGenerationException;
		
					protected void transformProperty(BeamChannel sourceInfo, JBlock body, ShowlDirectPropertyShape p, JVar inputRow, JVar outputRow, JVar enumObject) throws BeamTransformGenerationException {
		
		        ShowlExpression e = p.getSelectedExpression();
		        
		        if (e instanceof ShowlFilterExpression) {
		        	e = ((ShowlFilterExpression) e).getValue();
		        }
		        
		        if (p.getValueShape() != null) {
		          transformObjectProperty(sourceInfo, body, p, inputRow, outputRow);
		        } else if (e == null) {
		          fail("Mapping not found for property {0}({1})", p.getPath(), new Integer(p.hashCode()).toString());
		        } else if (e instanceof ShowlDirectPropertyExpression) {
		          ShowlDirectPropertyShape other = ((ShowlDirectPropertyExpression) e).getSourceProperty();
		          transformDirectProperty(body, p, other, inputRow, outputRow);
		          
		        } else if (e instanceof ShowlFunctionExpression) {
		          transformFunction(body, p, (ShowlFunctionExpression)e, inputRow, outputRow);
		        } else if (e instanceof ShowlEnumPropertyExpression) {
		          transformEnumProperty(body, p, (ShowlEnumPropertyExpression)e, inputRow, outputRow, enumObject);
		        } else if (p.getValueShape() != null) {
		          transformObjectProperty(sourceInfo, body, p, inputRow, outputRow);
		          
		        } else if (e instanceof ShowlDerivedPropertyExpression) {
		          transformDerivedProperty(body, p, (ShowlDerivedPropertyExpression) e, inputRow, outputRow);
		          
		        } else if (e instanceof ShowlEnumIndividualReference) {
		        	transformEnumIndividualReference(body, p, (ShowlEnumIndividualReference)e, inputRow, outputRow);
		          
		        } else {
		          fail("At {0}, expression not supported: {1}", p.getPath(), e.displayValue());
		        }
		        
		
		        
		        
		        
		      }
		

					private void transformEnumIndividualReference(JBlock body, ShowlDirectPropertyShape p,
							ShowlEnumIndividualReference e, JVar inputRow, JVar outputRow) {
		      	
		      	String targetPropertyName = p.getPredicate().getLocalName();
		      	String enumValue = e.getIriValue().getLocalName();
						
		        //   outputRow.set("$targetPropertyName", inputRow.get("$enumValue");
		      	
		      	body.add(outputRow.invoke("set").arg(JExpr.lit(targetPropertyName)).arg(enumValue));
						
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
		          JVar inputRow, JVar outputRow, JVar enumObject) throws BeamTransformGenerationException {
		      	
		      	if (enumObject == null) {
		      		throw new BeamTransformGenerationException("enumObject must not be null for " + p.getPath());
		      	}
		        
		        URI predicate = p.getPredicate();
		        String fieldName = predicate.getLocalName();
		        
		        String getterName = "get" + StringUtil.capitalize(fieldName);
		        
		        AbstractJClass fieldType = model.ref(Object.class);
		        
		        
		        // Object $fieldName = enumObject.get("$fieldName");
		        JVar field = body.decl(fieldType, fieldName, enumObject.invoke(getterName));
		        
		        // if ($field != null) {
		        //  outputRow.set("$fieldName", $field.toString());
		        // }
		        
		        IJExpression fieldArg = p.getPredicate().equals(Konig.id) ?
		            field.invoke("toString") : field;
		
		        body._if(field.ne(JExpr._null()))._then().add(outputRow.invoke("set").arg(JExpr.lit(fieldName)).arg(fieldArg));
		        
		        
		      }
		
		      private void transformFunction(JBlock body, ShowlDirectPropertyShape p, ShowlFunctionExpression e, JVar inputRow, JVar outputRow) throws BeamTransformGenerationException {
		        
		        FunctionExpression function = e.getFunction();
		        if (function.getModel() == FunctionModel.CONCAT) {
		          transformConcat(body, p, e, inputRow, outputRow);
		        } else {
		        	fail("Function {0} not supported at {1}", function.toSimpleString(), p.getPath());
		        }
		        
		      }
		
		      private void transformConcat(JBlock body, ShowlDirectPropertyShape p, ShowlFunctionExpression sfunc, JVar inputRow, JVar outputRow) throws BeamTransformGenerationException {
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
		        
		        TableRowShowlExpressionHandler handler = new TableRowShowlExpressionHandler(inputRow);      
		        for (ShowlExpression arg : sfunc.getArguments()) {
		          IJExpression e = handler.javaExpression(arg);
		          concatInvoke.arg(e);
		        }
		        
		//        FunctionExpression function = sfunc.getFunction();
		//        TableRowExpressionHandler handler = new TableRowExpressionHandler(inputRow);
		//        for (Expression arg : function.getArgList()) {
		//          IJExpression e = handler.javaExpression(arg);
		//          concatInvoke.arg(e);
		//        }
		
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
		          AbstractJClass stringBuilder = model.ref(StringBuilder.class);
		          
		          requiredMethod = thisClass.method(JMod.PRIVATE, objectClass, "required");
		          JVar row = requiredMethod.param(tableRowClass, "row");
		          JVar fieldName = requiredMethod.param(stringClass, "fieldName");
		          JVar builder = requiredMethod.param(stringBuilder, "builder");
		          //  Object value = row.get(fieldName);
		          JVar value = requiredMethod.body().decl(objectClass, "value", row.invoke("get").arg(fieldName));
		          
		          //  if (value == null) {
		          //    throw new RuntimeException("Field " + fieldName + " must not be null.");
		          //  }
		          
		          requiredMethod.body()._if(value.eq(JExpr._null()))._then()
		          .add(builder.invoke("append").arg(JExpr.lit("Required field ")
		        		  .plus(JExpr.ref(fieldName))
		        		  .plus(JExpr.lit(" is NULL;"))));
		          
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
		
		      protected void transformObjectProperty(BeamChannel sourceInfo, JBlock body, ShowlDirectPropertyShape p, JVar inputRow,
		          JVar outputRow) throws BeamTransformGenerationException {
		        
		        ShowlNodeShape valueShape = p.getValueShape();
		      
		        if (logger.isTraceEnabled()) {
		          logger.trace("transformObjectProperty({})", p.getPath());
		        }
		
		        ShowlEnumJoinInfo enumJoin = ShowlEnumJoinInfo.forEnumProperty(p);
		        if (enumJoin != null) {
		          if (enumJoin.getHardCodedReference() != null) {
		            transformHardCodedEnumObject(sourceInfo, body, p, enumJoin.getHardCodedReference());
		          } else {
		            transformEnumObject(sourceInfo, body, p, enumJoin);
		          }
		          return;
		        }
		        
		        
		        String targetFieldName = p.getPredicate().getLocalName();
		        AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		        
		        
		        // TableRow $targetFieldName = new TableRow();
		        
		        JVar fieldRow = body.decl(tableRowClass, targetFieldName, tableRowClass._new());
		        
		        List<ShowlDirectPropertyShape> filterList = null;
		        
		        for (ShowlDirectPropertyShape direct : valueShape.getProperties()) {
		        	
		
		          ShowlExpression e = direct.getSelectedExpression();
		
		          if (e instanceof ShowlFilterExpression) {
		          	if (filterList == null) {
		          		filterList = new ArrayList<>();
		          	}
		          	filterList.add(direct);
		          } else {
		        	
		          	transformProperty(sourceInfo, body, direct, inputRow, fieldRow, null);
		          }
		          
		        }
		        
		        JBlock thenBlock = body._if(fieldRow.invoke("isEmpty").not())
		          ._then();
		        
		        if (filterList != null) {
		        	for (ShowlDirectPropertyShape direct : filterList) {
		        		transformProperty(sourceInfo, thenBlock, direct, inputRow, fieldRow, null);
		        	}
		        }
		        
		        thenBlock.add(
		            outputRow.invoke("set")
		              .arg(JExpr.lit(targetFieldName))
		              .arg(fieldRow));
		        
		      }
		
		      
		
		
		      private void transformHardCodedEnumObject(BeamChannel sourceInfo, JBlock body, ShowlDirectPropertyShape p,
		      		ShowlEnumIndividualReference iriRef) throws BeamTransformGenerationException {
		        
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
		          transformProperty(sourceInfo, method.body(), direct, null, fieldRow, enumObject);
		          
		        }
		        //     if (!$fieldRow.isEmpty()) {
		            //       outputRow.set("$targetFieldName", $targetFieldName);
		            //     }
		        method.body()._if(fieldRow.invoke("isEmpty").not())._then()
		          .add(outputRowParam.invoke("set").arg(JExpr.lit(targetFieldName)).arg(fieldRow));
		        
		        
		        
		        body.add(JExpr.invoke(method).arg(outputRowParam));
		        
		      }
		
		      private JVar hardCodedEnumObject(JBlock block, ShowlEnumIndividualReference iriRef, ShowlNodeShape valueShape) throws BeamTransformGenerationException {
		
		        
		      
		        String individualLocalName = iriRef.getIriValue().getLocalName();
		        
		        String enumClassName = enumClassName(valueShape.getOwlClass().getId());
		        AbstractJClass enumClass = model.directClass(enumClassName);
		        JFieldRef fieldRef = enumClass.staticRef(individualLocalName);
		        URI property = valueShape.getAccessor().getPredicate();
		        
		        
		        
		        String varName = property.getLocalName();
		        return block.decl(enumClass, varName, fieldRef);
		      }
		
		      protected void transformEnumObject(BeamChannel sourceInfo, JBlock body, ShowlDirectPropertyShape p, 
		          ShowlEnumJoinInfo joinInfo) throws BeamTransformGenerationException {
		        
		
		        ShowlNodeShape valueShape = p.getValueShape();
		        
		        URI targetProperty = p.getPredicate();
		        
		        String targetFieldName = targetProperty.getLocalName();
		        AbstractJClass tableRowClass = model.ref(TableRow.class);
		        
		        String enumTransformMethodName = "transform" + StringUtil.capitalize(targetFieldName);
		        
		        JMethod method = toTargetFnClass.method(JMod.PRIVATE, model.VOID, enumTransformMethodName);
		        
		        JVar inputRowParam = method.param(tableRowClass, "inputRow");
		        
		        JVar outputRowParam = method.param(tableRowClass, "outputRow");
		        
		        BeamEnumInfo enumInfo = new BeamEnumInfo(joinInfo);
		
		        enumObject(enumInfo, method.body(), valueShape, inputRowParam);
		        
		        JVar enumObject = enumInfo.getEnumObjectVar();
		        JBlock thenBlock = enumInfo.getConditionalStatement()._then();
		        
		        // TableRow $targetFieldName = new TableRow();
		        
		        JVar fieldRow = thenBlock.decl(tableRowClass, targetFieldName + "Row", tableRowClass._new());
		        
		        for (ShowlDirectPropertyShape direct : valueShape.getProperties()) {
		        	
		        	if (direct == joinInfo.getTargetProperty()) {
		        		// outputRow.set("$propertyName", $sourceKeyVar);
		        		thenBlock.add(
		        				fieldRow.invoke("set").arg(JExpr.lit(direct.getPredicate().getLocalName())).arg(enumInfo.getSourceKeyVar()));
		        		continue;
		        	}
		          transformProperty(sourceInfo, thenBlock, direct, inputRowParam, fieldRow, enumObject);
		          
		        }
		        //     if (!$fieldRow.isEmpty()) {
		            //       outputRow.set("$targetFieldName", $targetFieldName);
		            //     }
		        thenBlock.add(outputRowParam.invoke("set").arg(JExpr.lit(targetFieldName)).arg(fieldRow));
		        
		        
		        
		        body.add(JExpr.invoke(method).arg(inputRowParam).arg(outputRowParam));
		        
		      }
		
		      protected void enumObject(BeamEnumInfo enumInfo, JBlock block, ShowlNodeShape valueShape, JVar inputRow) throws BeamTransformGenerationException {
		
		    	  ShowlEnumJoinInfo joinInfo = enumInfo.getJoinInfo();
		        
		        String sourceKeyName = joinInfo.getSourceProperty().getPredicate().getLocalName();
		
		        String enumClassName = enumClassName(valueShape.getOwlClass().getId());
		        AbstractJClass enumClass = model.directClass(enumClassName);
		        URI property = valueShape.getAccessor().getPredicate();
		        String varName = property.getLocalName();
		        String enumKeyName = joinInfo.getEnumProperty().getPredicate().getLocalName();
		        
		        String findMethodName = joinInfo.getEnumProperty().getPredicate().equals(Konig.id) ?
		        		"findByLocalName" : "findBy" + StringUtil.capitalize(enumKeyName);
		        
		        /*
		         *  For now we assume that the source key is a string.  We'll relax this assumption later.
		         */
		        
		        // String $sourceKeyName = inputRow.get("$sourceKeyName").toString();
		        // if ($sourceKeyName != null) {
		        //   $varName = $enumClass.$findMethodName($sourceKeyName);
		        //  ...
		        //  }
		        
		        
		        AbstractJClass stringClass = model.ref(String.class);
		        
		        JVar sourceKeyVar = block.decl(stringClass, sourceKeyName, inputRow.invoke("get").arg(JExpr.lit(sourceKeyName)));
		        
		        JConditional conditional = block._if(sourceKeyVar.neNull());
		        conditional._then().assign(sourceKeyVar, sourceKeyVar.invoke("toString"));
		        JVar enumObjectVar = conditional._then().decl(enumClass, varName, enumClass.staticInvoke(findMethodName).arg(sourceKeyVar));
		        
		        enumInfo.setSourceKeyVar(sourceKeyVar);
		        enumInfo.setEnumObjectVar(enumObjectVar);
		        enumInfo.setConditionalStatement(conditional);
		        
		      }
		
		      
		
		      protected void transformDirectProperty(JBlock body, ShowlDirectPropertyShape p, ShowlDirectPropertyShape other,
		          JVar inputRow, JVar outputRow) {
		        
		        // Object $sourcePropertyName = inputRow.get("$sourcePropertyName");
		        String sourcePropertyName = other.getPredicate().getLocalName();
		        AbstractJClass objectClass = model.ref(Object.class);
		        JVar sourcePropertyVar = null;
		        
		        if(p.getPropertyConstraint().getMinCount() > 0){
		        	 sourcePropertyVar = body.decl(objectClass, sourcePropertyName, 
		        			 JExpr.invoke("required").arg(inputRow).arg(JExpr.lit(sourcePropertyName)).arg(JExpr.ref("builder")));
		        } else {
		         sourcePropertyVar = body.decl(objectClass, sourcePropertyName, 
		            inputRow.invoke("get").arg(JExpr.lit(sourcePropertyName)));
		        }
		        
		        
		        // if ($sourcePropertyVar != null) {
		        //   outputRow.set("$targetPropertyName", inputRow.get("$sourcePropertyName");
		        // }
		
		        String targetPropertyName = p.getPredicate().getLocalName();
		        body._if(sourcePropertyVar.ne(JExpr._null()))._then().add(outputRow.invoke("set")
		            .arg(JExpr.lit(targetPropertyName))
		            .arg(sourcePropertyVar));
		        
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


		class OverlayReadTargetFnGenerator2 {
    	
    	private GetKeyMethodGenerator getKeyMethodGenerator;
    	
    	private JDefinedClass fnClass;
    	
    	
    	
    	public OverlayReadTargetFnGenerator2(GetKeyMethodGenerator getKeyMethodGenerator) {
				this.getKeyMethodGenerator = getKeyMethodGenerator;
			}

			public JDefinedClass generate() throws BeamTransformGenerationException {
    		
    		String fnClassName = fnClassName();
    		try {
					fnClass = model._class(JMod.PUBLIC, fnClassName);
					
					processElement();
					
					getKeyMethodGenerator.generate(model, fnClass, "getKey");
					
				} catch (JClassAlreadyExistsException e) {
					fail("Failed to create {fnClassName} ", e);
				}
    		return fnClass;
    	}


			private void processElement() {
				
//			@ProcessElement
//			public void processElement(ProcessContext c) {
//				try {
//					TableRow row = c.element();
//					
//					String key = getKey(row);
//					c.output(KV.of(key, row));
//					
//				} catch (Throwable oops) {
//					oops.printStackTrace();
//				}
//			}

				
				AbstractJClass processContextClass = model.ref(ProcessContext.class);
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				AbstractJClass stringClass = model.ref(String.class);
        AbstractJClass kvClass = model.ref(KV.class);
				AbstractJClass throwableClass = model.ref(Throwable.class);
				AbstractJClass tupleTagClass = model.ref(TupleTag.class);
				
		JVar deadLetterTag = fnClass.field(JMod.PUBLIC | JMod.STATIC , tupleTagClass.narrow(model.ref(String.class)), 
				"deadLetterTag").init(tupleTagClass._new().narrow(model.ref(String.class)).invoke("{}"));
		
		JVar successTag = fnClass.field(JMod.PUBLIC | JMod.STATIC , tupleTagClass.narrow(tableRowClass), 
				"successTag").init(tupleTagClass._new().narrow(model.ref(TableRow.class)).invoke("{}"));		
        JMethod method = 
            fnClass.method(JMod.PUBLIC, model.VOID, "processElement");
        method.annotate(model.directClass(ProcessElement.class.getName()));
        
        JVar c = method.param(processContextClass, "c");
        
        JTryBlock tryBlock = method.body()._try();
        JVar row = tryBlock.body().decl(tableRowClass, "row").init(c.invoke("element"));
        JVar key = tryBlock.body().decl(stringClass, "key").init(JExpr.invoke("getKey").arg(row));
        
        tryBlock.body().add(c.invoke("output").arg(successTag).arg(kvClass.staticInvoke("of").arg(key).arg(row)));
        	
        JCatchBlock catchBlock = tryBlock._catch(throwableClass);
        JVar oops = catchBlock.param("oops");
        catchBlock.body().add(c.invoke("output").arg(deadLetterTag).arg(oops.invoke("getMessage")));
        
				
			}

			private String fnClassName() throws BeamTransformGenerationException {
				String shortName = ShowlUtil.shortShapeName(RdfUtil.uri(targetNode.getId()));
				
				return  mainPackage() + "." + shortName + "ToKvFn";
			}
    	
    }


    /**
     * Generates a DoFn that converts ReadableFile instances to KV<String, TableRow> instances.
     * @author Greg McFall
     *
     */
    class FileToKvFnGenerator extends BaseReadFnGenerator {
      
      private ShowlPropertyShape keyProperty;
      private JVar keyPropertyVar;


      public FileToKvFnGenerator(BeamChannel beamChannel, ShowlPropertyShape keyProperty) throws BeamTransformGenerationException {
        super(beamChannel);
        if (keyProperty == null) {
          fail("keyProperty is null for source node {0}", beamChannel.getFocusNode().getPath());
        }
        this.keyProperty = keyProperty;
        if (logger.isTraceEnabled()) {
          logger.trace("new FileToKvFnGenerator({}, keyProperty={})", 
              beamChannel.getFocusNode().getPath(), keyProperty.getPath());
        }
      }

      private JDefinedClass generate() throws BeamTransformGenerationException, JClassAlreadyExistsException {
        
        
        // public class Read${shapeName}Fn extends DoFn<ReadableFile, KV<String,TableRow>> {
        URI shapeId = RdfUtil.uri(sourceBeamChannel.getFocusNode().getId());
        String shapeName = RdfUtil.shortShapeName(shapeId);

        AbstractJClass stringClass = model.ref(String.class);
        AbstractJClass readableFileClass = model.ref(ReadableFile.class);
        AbstractJClass tableRowClass = model.ref(TableRow.class.getName());
        
        AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(tableRowClass);
        AbstractJClass doFnClass = model.ref(DoFn.class).narrow(readableFileClass).narrow(kvClass);
        
        
        String simpleClassName = "Read" + shapeName + "Fn";
        String className = className(namespacePrefix(shapeId), simpleClassName);
    
        logger.trace("generating class {}", className);
        
        thisClass = model._class(className)._extends(doFnClass);
        
        sourceBeamChannel.setReadFileFn(thisClass);
        
        
        processElement(kvClass);
        
        return thisClass;
      }

      @Override
      protected void createDerivedKey(JBlock block) throws BeamTransformGenerationException {
				if (keyPropertyVar == null) {
					
					URI keyType = keyType(keyProperty);
					
					if (!XMLSchema.STRING.equals(keyType)) {
						fail("Unsupported key type {0} for {1}", 
								keyType==null ? "null" : keyType.getLocalName(), 
								keyProperty.getPath());
					}
					
					AbstractJClass stringClass = model.ref(String.class);

					ShowlExpression e = keyProperty.getFormula();
					if (keyProperty instanceof ShowlDirectPropertyShape) {
						e = new ShowlDirectPropertyExpression((ShowlDirectPropertyShape)keyProperty);
					}

					if(e != null) {
						IJExpression initValue = etran().transform(e);
					keyPropertyVar = block.decl(stringClass, keyProperty.getPredicate().getLocalName()+ "Key").init(initValue);
					} else {
						keyPropertyVar = block.decl(stringClass, keyProperty.getPredicate().getLocalName()+ "Key");
						
					}
					BlockInfo blockInfo = etran().peekBlockInfo();
					blockInfo.putPropertyValue(keyProperty.asGroup(), keyPropertyVar);
					
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
          fail("keyProperty {0} not found for {1}", keyProperty.getPredicate().getLocalName(), sourceBeamChannel.getFocusNode().getPath());
        }
    
        // c.output(KV.of($keyPropertyVar.toString(), row));

        AbstractJClass kvClass = model.ref(KV.class);
        
        outputBlock.add(c.invoke("output").arg(kvClass.staticInvoke("of").arg(keyPropertyVar.invoke("toString")).arg(row)));
        
      }
      
      @Override
      protected void deliverOutput(JBlock outputBlock, JVar c, JVar row, JVar tupleTag) throws BeamTransformGenerationException {
        
    	  if (keyPropertyVar == null) {
              fail("keyProperty {0} not found for {1}", keyProperty.getPredicate().getLocalName(), sourceBeamChannel.getFocusNode().getPath());
            }
    
        // c.output(KV.of($keyPropertyVar.toString(), row));

        AbstractJClass kvClass = model.ref(KV.class);
        
        outputBlock.add(c.invoke("output").arg(tupleTag).arg(kvClass.staticInvoke("of").arg(keyPropertyVar.invoke("toString")).arg(row)));
        
      }

    }
    
    private class ToTargetFnGenerator2 extends BaseTargetFnGenerator2 {

    	private BeamChannel beamChannel;
    	private Map<ShowlNodeShape, BeamChannel> beamChannelMap;
      
      @Override
			protected void declareClass() throws BeamTransformGenerationException {

        // public class ReadFileFn extends DoFn<FileIO.ReadableFile, TableRow> {
      	
      	String prefix = namespacePrefix(targetNode.getId());
        String localName = RdfUtil.localName(targetNode.getId());
        String className = className(prefix, "To" + localName + "Fn");
        
        try {
	        toTargetFnClass = thisClass = model._class(className);
	        AbstractJClass tableRowClass = model.ref(TableRow.class);
	        AbstractJClass doFnClass = model.ref(DoFn.class).narrow(tableRowClass).narrow(tableRowClass);
	        
	        thisClass._extends(doFnClass);
        } catch (JClassAlreadyExistsException e) {
					throw new BeamTransformGenerationException("Failed to create class " + className, e);
				} finally {
        	
        }
      }



			@Override
			protected BeamChannel beamChannel(ShowlNodeShape sourceNode) throws BeamTransformGenerationException {
				
				// We created the primary BeamChannel in the contextElement(...) method.
				// If the sourceNode parameter matches, return the primary Channel. 
				
				ShowlNodeShape sourceRoot = sourceNode.getRoot();
				if (beamChannel.getChannel().getSourceNode() == sourceRoot) {
					return beamChannel;
				}
				
				// Since we did not match the primary channel, consider the case of an enumerated value.
				
				if (ShowlUtil.isEnumSourceNode(sourceRoot, reasoner)) {
					if (beamChannelMap == null) {
						beamChannelMap = new HashMap<>();
					}
					BeamChannel result = beamChannelMap.get(sourceNode);
					if (result == null) {
					
						ShowlChannel channel = new ShowlChannel(sourceRoot, null);
						result =  new BeamChannel(channel);
						beamChannelMap.put(sourceRoot, result);
					}
					return result;
				}

				throw new BeamTransformGenerationException("Channel not found for " + sourceNode);
				
			}

			@Override
			protected JVar contextElement(JBlock block, JVar c) {

				beamChannel = new BeamChannel(singleChannel());

        AbstractJClass tableRowClass = model.ref(TableRow.class);
				String sourceRowName = sourceRowName(beamChannel.getChannel());

				// TableRow $sourceRowName = context.element();
				JVar sourceRowVar = block.decl(tableRowClass, sourceRowName, c.invoke("element").castTo(tableRowClass));
        
        beamChannel.setSourceRow(sourceRowVar);
				return sourceRowVar;
			}


      
    }
    
    private class MergeFnGenerator2 extends BaseTargetFnGenerator2 {
      private GroupInfo groupInfo;

      private MergeFnGenerator2(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
      }
      

			protected void declareSourceRows(JBlock block, JVar contextElement) throws BeamTransformGenerationException {

        AbstractJClass tableRowClass = model.ref(TableRow.class);
				for (BeamChannel sourceInfo : groupInfo.getSourceList()) {
					ShowlChannel channel = sourceInfo.getChannel();

					// TableRow $sourceRowName = sourceRow(contextElement, $beamClass.$tagName);

					String sourceRowName = sourceRowName(channel);
					JVar sourceRowVar = block.decl(tableRowClass, sourceRowName)
							.init(JExpr.invoke("sourceRow").arg(contextElement).arg(mainClass.staticRef(sourceInfo.getTupleTag())));
					sourceInfo.setSourceRow(sourceRowVar);
				}
				
				sourceRowMethod();
			}
      
    

      @Override
			protected BeamChannel beamChannel(ShowlNodeShape sourceNode) throws BeamTransformGenerationException {
				ShowlNodeShape sourceRoot = sourceNode.getRoot();
				
				for (BeamChannel sourceInfo : groupInfo.getSourceList()) {
					if (sourceInfo.getFocusNode().getRoot() == sourceRoot) {
						return sourceInfo;
					}
				}
				
				throw new BeamTransformGenerationException("Failed to get SourceInfo for " + sourceNode.getPath());
			}


			
      


      @Override
			protected void declareClass() throws BeamTransformGenerationException {
        AbstractJClass stringClass = model.ref(String.class);
        AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
        AbstractJClass tableRowClass = model.ref(TableRow.class);
        AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
        AbstractJClass doFnClass = model.ref(DoFn.class).narrow(kvClass).narrow(tableRowClass);
        String className = mainPackage() + "." + groupInfo.mergeClassName();
        
        
        
        try {
					thisClass = model._class(className)._extends(doFnClass);
				} catch (JClassAlreadyExistsException e) {
					throw new BeamTransformGenerationException("Failed to create MergeFn class " + className, e);
				}

        groupInfo.setMergeFnClass(thisClass);
				
			}

			private void sourceRowMethod() {

        AbstractJClass stringClass = model.ref(String.class);
        AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
        AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
        AbstractJClass tableRowClass = model.ref(TableRow.class);
        AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(tableRowClass);
        AbstractJClass iteratorClass = model.ref(Iterator.class).narrow(tableRowClass);
        
        // private TableRow sourceRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tag) {

        JMethod method = thisClass.method(JMod.PUBLIC, tableRowClass, "sourceRow");
        JVar e = method.param(kvClass, "e");
        JVar tag = method.param(tupleTagClass, "tag");
        
        // Iterator<TableRow> sequence = e.getValue().getAll(tag).iterator();
        JVar sequence = method.body().decl(
        		iteratorClass, "sequence").init(
        				e.invoke("getValue").invoke("getAll").arg(tag).invoke("iterator"));
        
        method.body()._return(JExpr.cond(
        		sequence.invoke("hasNext"),
        		sequence.invoke("next"), 
        		JExpr._null()));
        
      }


			@Override
			protected JVar contextElement(JBlock block, JVar c) throws BeamTransformGenerationException {

        //     KV<String, CoGbkResult> e = c.element();
				
        AbstractJClass stringClass = model.ref(String.class);
        AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
        AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
        JVar contextElement = block.decl(kvClass, "e").init(c.invoke("element"));
        declareSourceRows(block, contextElement);
        
        return contextElement;
			}
      
		

      
      
    }
    
    private class TableRowShowlExpressionHandler implements ShowlExpressionHandler {

      private JVar inputRow;
      
      public TableRowShowlExpressionHandler(JVar inputRow) {
        this.inputRow = inputRow;
      }

      @Override
      public IJExpression javaExpression(ShowlExpression e) throws BeamTransformGenerationException {
        if (e instanceof ShowlLiteralExpression) {

          Literal literal = ((ShowlLiteralExpression) e).getLiteral();
          if (literal.getDatatype().equals(XMLSchema.STRING)) {
            return JExpr.lit(literal.stringValue());
          } else {
            fail("Typed literal not supported in expression: {0}", e.toString());
          }
        } else if (e instanceof ShowlPropertyExpression) {
          ShowlPropertyShape p = ((ShowlPropertyExpression)e).getSourceProperty();
          p = p.maybeDirect();
          URI iri = p.getPredicate();

          
          return JExpr.invoke("required").arg(inputRow).arg(JExpr.lit(iri.getLocalName()));
        } else if (e instanceof ShowlIriReferenceExpression) {
          ShowlIriReferenceExpression iriRef = (ShowlIriReferenceExpression) e;
          URI predicate = iriRef.getIriValue();

          return JExpr.invoke("required").arg(inputRow).arg(JExpr.lit(predicate.getLocalName()));
        }

        fail("Unsupported expression: {0}", e.toString());
        return null;
      }
      
    }
    
    
    private class ReadFileFnGenerator extends BaseReadFnGenerator {

      
      
      
      public ReadFileFnGenerator(BeamChannel sourceInfo) {
        super(sourceInfo);
      }

      private void generate() throws JClassAlreadyExistsException, BeamTransformGenerationException {
        
        // public class ReadFileFn extends DoFn<FileIO.ReadableFile, TableRow> {
        
        
        
        
        ShowlNodeShape sourceNode = sourceBeamChannel.getFocusNode();
        String sourceShapeName = RdfUtil.uri(sourceNode.getId()).getLocalName();
        
        String simpleClassName = "Read" + sourceShapeName + "Fn";
        
        String nsPrefix = namespacePrefix(sourceNode.getId());
        
      
        thisClass = model._class(className(nsPrefix + "." + simpleClassName));
        
        sourceBeamChannel.setReadFileFn(thisClass);
        
        
        
        
        AbstractJClass superClass =  model.directClass(DoFn.class.getName()).narrow(ReadableFile.class, TableRow.class);
      
        thisClass._extends(superClass);
        
        processElement(model.ref(TableRow.class));
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
      
      @Override
      protected void deliverOutput(JBlock outputBlock, JVar c, JVar row, JVar tupleTag) {
        outputBlock.add(c.invoke("output").arg(tupleTag).arg(row));
        
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
			/*
			 * We assume that all datasources have the same variable name for
			 * the environment name.
			 * 
			 * Scan all datasources and confirm this assumption; throw an
			 * exception if the assumption is not true.
			 */
			String varName = null;
			String datasourceId = null;
			for (ShowlChannel channel : targetNode.getChannels()) {
				DataSource ds = channel.getSourceNode().getShapeDataSource().getDataSource();
				if (ds.isA(Konig.GoogleCloudStorageFolder) || ds.isA(Konig.GoogleCloudStorageBucket)) {
					datasourceId = ds.getId().stringValue();
					int varStart = datasourceId.lastIndexOf('$');
					int varEnd = datasourceId.indexOf('}', varStart) + 1;
					String varName2 = datasourceId.substring(varStart, varEnd);
					if (varName == null) {
						varName = varName2;
					} else if (!varName.equals(varName2)) {
						String msg = MessageFormat.format("Conflicting variables for environment data sources for {0}",
								targetNode.getPath());
						throw new BeamTransformGenerationException(msg);
					}
					if (varName == null) {
						throw new BeamTransformGenerationException(
								"Environment name variable not found for target " + targetNode.getPath());
					}
				}
			}

			if (varName != null) {
				// private String sourceUri(String pattern, Options options) {
				// return pattern.replace("${environmentName}", options.getEnvironment());
				AbstractJClass stringClass = model.ref(String.class);
				JMethod method = mainClass.method(JMod.PRIVATE | JMod.STATIC, stringClass, "sourceURI");
				JVar pattern = method.param(stringClass, "pattern");
				JVar options = method.param(optionsClass, "options");
				method.body()._return(
						pattern.invoke("replace").arg(JExpr.lit(varName)).arg(options.invoke("getEnvironment")));
			}

		}



    private void singleSourceUriMethod() {

      // private String sourceURI(Options options) {
      AbstractJClass stringClass = model.ref(String.class);
      JMethod method = mainClass.method(JMod.PRIVATE | JMod.STATIC, stringClass, "sourceURI");
      JVar options = method.param(optionsClass, "options");
      
      // String envName = options.getEnvironment();
      
      method.body().decl(stringClass, "envName", options.invoke("getEnvironment"));

      
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
      
      
      optionsClass.method(JMod.PUBLIC, String.class, "getNetwork");
      
      optionsClass.method(JMod.PUBLIC, model.VOID, "setNetwork").param(String.class, "gcpNetwork");
      
      optionsClass.method(JMod.PUBLIC, String.class, "getSubnetwork");
      
      optionsClass.method(JMod.PUBLIC, model.VOID, "setSubnetwork").param(String.class, "subnetwork");
      
      optionsClass.method(JMod.PUBLIC, String.class, "getWorkerMachineType");
      
      optionsClass.method(JMod.PUBLIC, model.VOID, "setWorkerMachineType").param(String.class, "workerMachineType");
      
      // }
    }



    protected void processMethod() throws BeamTransformGenerationException {
      
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
      body.add(p.invoke("run"));   

    }

    protected void applyMergeFnClasses(JBlock body, List<GroupInfo> groupList) throws BeamTransformGenerationException {
      
      if (groupList.isEmpty()) {
      	if (isOverlay()) {
      		return;
      	}
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
      AbstractJClass pcollectionTupleClass = model.ref(PCollectionTuple.class);
      AbstractJClass tupleTagListClass = model.ref(TupleTagList.class);
      AbstractJClass parDoClass = model.ref(ParDo.class);
      //JDefinedClass mergeFnClass = groupInfo.getMergeFnClass();
      
      JVar kvpCollection = groupInfo.getKvpCollection();
      
      JVar outputRowCollection = body.decl(pcollectionTupleClass, "outputRowCollection");
      
      outputRowCollection.init(kvpCollection.invoke("apply")
    	        .arg(parDoClass.staticInvoke("of").arg(mergeClass._new())
    	        		.invoke("withOutputTags").arg(mergeClass.staticRef("successTag"))
    	        				.arg(tupleTagListClass.staticInvoke("of").arg(mergeClass.staticRef("deadLetterTag")))));
      
      String targetTableSpec = targetTableSpec();
      String writeLabel = "Write" + RdfUtil.shortShapeName(targetNode.getId());
      AbstractJClass bigQueryIoClass = model.ref(BigQueryIO.class);
      AbstractJClass createDispositionClass = model.ref(CreateDisposition.class);
      AbstractJClass writeDispositionClass = model.ref(WriteDisposition.class);
      
      body.add(outputRowCollection.invoke("get").arg(mergeClass.staticRef("successTag"))
    		  .invoke("apply").arg(JExpr.lit(writeLabel)).arg(bigQueryIoClass.staticInvoke("writeTableRows")
              .invoke("to").arg(targetTableSpec)
              .invoke("withCreateDisposition").arg(createDispositionClass.staticRef("CREATE_NEVER"))
              .invoke("withWriteDisposition").arg(writeDispositionClass.staticRef("WRITE_APPEND"))));
      
      body.add(writeErrorTable(outputRowCollection, targetNode ,mergeClass));
    }


    protected void generateMergeFnClasses(List<GroupInfo> groupList) throws BeamTransformGenerationException {
		Map<ShowlEffectiveNodeShape, IJExpression> map = new HashMap<>();
		for (ShowlChannel channel : targetNode.getChannels()) {
			ShowlEffectiveNodeShape sourceNode = channel.getSourceNode().effectiveNode();
			if (!map.containsKey(sourceNode)) {
				String sName = RdfUtil.shortShapeName(sourceNode.canonicalNode().getId()) + "Tag";
				IJExpression var = this.mainClass.staticRef(StringUtil.firstLetterLowerCase(sName));
				map.put(sourceNode, var);
			}
		}
		AbstractJClass stringClass = model.ref(String.class);
		AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
		AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
		
		MergeTargetFnGenerator generator = new MergeTargetFnGenerator(map,basePackage,nsManager,model,reasoner,typeManager());
		mergeClass = generator.generate(targetNode, kvClass);
     // for (GroupInfo groupInfo : groupList) {
        //MergeFnGenerator2 generator = new MergeFnGenerator2(groupInfo);
        
     //}
      
    }


    protected void defineKeyedCollectionTuples(JBlock body, List<GroupInfo> groupList) {
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
        
		for (BeamChannel source : groupInfo.getSourceList()) {
			if (invoke == null) {
				if(source.getTupleTag() != null){
					JInvocation invocation = null;
					if(source.getReadFileFn() != null){
						invocation = source.getPcollection().invoke("get").arg(source.getReadFileFn().staticRef("successTag"));
					} 
					invoke = keyedPCollectionTupleClass.staticInvoke("of").arg(source.getTupleTag()).arg(invocation==null?source.getPcollection():invocation);
					
				} else {
					invoke = keyedPCollectionTupleClass.staticInvoke("of").arg(source.getPcollection());
				}
			} else {
				if(source.getTupleTag() != null){
					JInvocation invocation = null;
					
					if(source.getReadFileFn() != null){
						invocation = source.getPcollection().invoke("get").arg(source.getReadFileFn().staticRef("successTag"));
					}
					invoke = invoke.invoke("and").arg(source.getTupleTag()).arg(invocation==null?source.getPcollection():invocation);
				}else {
					invoke = invoke.invoke("and").arg(source.getPcollection());
				}
			}
		}
        
        invoke = invoke.invoke("apply").arg(coGroupByKeyClass.staticInvoke("create").narrow(stringClass));
        var.init(invoke);
        
        groupInfo.setKvpCollection(var);
        
      }
      
    }


    protected List<GroupInfo> groupList() throws BeamTransformGenerationException {
      List<GroupInfo> list = new ArrayList<>();
      
      for (BeamChannel sourceInfo : sortedSourceInfoList()) {
        ShowlChannel channel = sourceInfo.getChannel();
        ShowlStatement statement = channel.getJoinStatement();
        if (statement instanceof ShowlEqualStatement) {
          ShowlEqualStatement equal = (ShowlEqualStatement) statement;
          
          ShowlExpression left = equal.getLeft();
          ShowlExpression right = equal.getRight();
          
          ShowlPropertyShape leftProperty = propertyOf(left);
          ShowlPropertyShape rightProperty = propertyOf(right);
          
          BeamChannel leftInfo = sourceInfoFor(leftProperty);
          BeamChannel rightInfo = sourceInfoFor(rightProperty);
          
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
    
    private BeamChannel sourceInfoFor(ShowlPropertyShape p) throws BeamTransformGenerationException {
      ShowlNodeShape root = p.getRootNode();
      BeamChannel result = sourceInfoMap.get(RdfUtil.uri(root.getId()));
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


    private List<BeamChannel> sortedSourceInfoList() {
      List<BeamChannel> list = new ArrayList<>(sourceInfoMap.values());
      sortSourceInfo(list);
      return list;
    }
    
    private void sortSourceInfo(List<BeamChannel> list) {

      Collections.sort(list, new Comparator<BeamChannel>(){

        @Override
        public int compare(BeamChannel a, BeamChannel b) {
          URI nodeA = RdfUtil.uri(a.getFocusNode().getId());
          URI nodeB = RdfUtil.uri(b.getFocusNode().getId());
          
          return nodeA.getLocalName().compareTo(nodeB.getLocalName());
        }
        
      });
    }


    protected void defineTupleTagsAndPcollections(JBlock block, JVar pipeline, JVar options) throws BeamTransformGenerationException {

      Set<ShowlNodeShape> set = new HashSet<>();
      AbstractJClass pCollectionTupleClass = model.ref(PCollectionTuple.class);
      AbstractJClass tableRowClass = model.ref(TableRow.class);
      AbstractJClass stringClass = model.ref(String.class);
      AbstractJClass fileIoClass = model.ref(FileIO.class);
      AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(tableRowClass);
      AbstractJClass tupleTagListClass = model.ref(TupleTagList.class);
      AbstractJClass parDoClass = model.ref(ParDo.class);
      
      for (BeamChannel sourceInfo : sortedSourceInfoList()) {
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
        DataSource sourceDs = node.getShapeDataSource().getDataSource();
        if(sourceDs instanceof GoogleCloudStorageFolder ||
        		sourceDs instanceof GoogleCloudStorageBucket) {      
            // PCollection<KV<String, TableRow>> $shapeName = p
            //  .apply(FileIO.match().filepattern(sourceURI($pattern, options))
            //  .apply(FileIO.readMatches());
            
            String dataSourcePattern = node.getShapeDataSource().getDataSource().getId().stringValue() + "/*";           
            
            JDefinedClass readFn = sourceInfo.getReadFileFn();
            
            JVar pcollectionTuple = block.decl(pCollectionTupleClass, shapeName, pipeline
                    .invoke("apply").arg(
                      fileIoClass.staticInvoke("match").invoke("filepattern").arg(
                    JExpr.invoke("sourceURI").arg(JExpr.lit(dataSourcePattern)).arg(options)))
                    .invoke("apply").arg(fileIoClass.staticInvoke("readMatches"))
                    .invoke("apply").arg(parDoClass.staticInvoke("of").arg(readFn._new())
                  		  .invoke("withOutputTags").arg(readFn.staticRef("successTag"))
                  				  .arg(tupleTagListClass.staticInvoke("of").arg(readFn.staticRef("deadLetterTag"))))
                  );
            
            block.add(writeErrorTable(pcollectionTuple, node, readFn));
            sourceInfo.setPcollection(pcollectionTuple);
            sourceInfo.setTupleTag(tagVar);
        }else if(sourceDs instanceof GoogleBigQueryTable) {
        	
			JDefinedClass targetTableToKvFile = declareTableRowToKvFnGenerator(node);
        	AbstractJClass bigQueryIoClass = model.directClass(BigQueryIO.class.getName());
        	AbstractJClass tableReferenceClass = model.ref(TableReference.class);

        	BigQueryTableReference tableRef = sourceTableReference(node);		
    		JVar targetTableRef = block.decl(tableReferenceClass, tableRef.getTableId()+"TableRef").init(tableReferenceClass._new());
    		block.add(targetTableRef.invoke("setDatasetId").arg(JExpr.lit(tableRef.getDatasetId())));
    		block.add(targetTableRef.invoke("setTableId").arg(JExpr.lit(tableRef.getTableId())));

    		JVar targetTableCollection = block.decl(pCollectionTupleClass, shapeName, pipeline
                    .invoke("apply").arg(bigQueryIoClass.staticInvoke("readTableRows").invoke("from")
                    		.arg(targetTableRef)).invoke("apply").arg(parDoClass.staticInvoke("of").arg(targetTableToKvFile._new())
                    .invoke("withOutputTags").arg(targetTableToKvFile.staticRef("successTag"))
    				  .arg(tupleTagListClass.staticInvoke("of").arg(targetTableToKvFile.staticRef("deadLetterTag")))));
    		
        	sourceInfo.setTupleTag(tagVar);
        	sourceInfo.setPcollection(targetTableCollection);
        	sourceInfo.setReadFileFn(targetTableToKvFile);
        }
        
      }
      
      
    }
    	
    private JDefinedClass declareTableRowToKvFnGenerator(ShowlNodeShape node) throws BeamTransformGenerationException{
    	TableRowToKvFnGenerator generator = new TableRowToKvFnGenerator(node);
    	return generator.generate();
    }
   
    
		private IJStatement writeErrorTable(JVar outputTuple, ShowlNodeShape node, JDefinedClass fnClass) throws BeamTransformGenerationException {
			AbstractJClass bigQueryIoClass = model.ref(BigQueryIO.class);
			AbstractJClass createDispositionClass = model.ref(CreateDisposition.class);
			AbstractJClass writeDispositionClass = model.ref(WriteDisposition.class);
			
			String tableName = BeamUtil.errorTableDataset(node)+"."+ BeamUtil.errorTableName(node.getShape().getIri());
			
			String writeLabel = "Write" + RdfUtil.shortShapeName(node.getId());
			return outputTuple.invoke("get").arg(fnClass.staticRef("deadLetterTag"))
					.invoke("setCoder").arg(model.ref(TableRowJsonCoder.class).staticInvoke("of"))
					.invoke("apply").arg(JExpr.lit(writeLabel + "ErrorLog"))
						.arg(bigQueryIoClass.staticInvoke("writeTableRows").invoke("to")
							.arg(tableName)
							.invoke("withCreateDisposition").arg(createDispositionClass.staticRef("CREATE_NEVER"))
							.invoke("withWriteDisposition").arg(writeDispositionClass.staticRef("WRITE_APPEND")));
		}

		private JVar processGoogleStorageBucketDataSource(JBlock body, JVar optionsVar, JVar p,
				ShowlNodeShape sourceNode) throws BeamTransformGenerationException {

			// String sourceURI = sourceURI(options);
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass parDoClass = model.directClass(ParDo.class.getName());
			AbstractJClass fileIoClass = model.directClass(FileIO.class.getName());
			JDefinedClass readFileFnClass = sourceInfoMap.values().iterator().next().getReadFileFn();

			JVar sourceURI = body.decl(stringClass, "sourceURI", JExpr.invoke("sourceURI").arg(optionsVar));
			JVar outputTuple = body.decl(model.ref(PCollectionTuple.class), "outputTuple");

			// p.apply(FileIO.match().filepattern(options.getSourceUri()))

			JInvocation pipeline = p.invoke("apply")
					.arg(fileIoClass.staticInvoke("match").invoke("filepattern").arg(sourceURI));

			// p.apply(FileIO.readMatches())

			pipeline = pipeline.invoke("apply").arg(fileIoClass.staticInvoke("readMatches"));

			// p.apply("ReadFiles", ParDo.of(new $readFileFnClass()))

			pipeline = pipeline.invoke("apply").arg(JExpr.lit("ReadFiles"))
					.arg(parDoClass.staticInvoke("of").arg(readFileFnClass._new()).invoke("withOutputTags")
							.arg(readFileFnClass.staticRef("successTag")).arg(model.ref(TupleTagList.class)
									.staticInvoke("of").arg(readFileFnClass.staticRef("deadLetterTag"))));

			outputTuple.init(pipeline);
			body.add(writeErrorTable(outputTuple, sourceNode, readFileFnClass));

			return outputTuple;
		}
	
		private JVar processBigQueryDataSource(JBlock body, JVar p, ShowlNodeShape sourceNode)
				throws BeamTransformGenerationException {

			AbstractJClass bigQueryIoClass = model.ref(BigQueryIO.class);
			AbstractJClass tableReferenceClass = model.ref(TableReference.class);
			AbstractJClass pCollectionClass = model.ref(PCollection.class);
			AbstractJClass parDoClass = model.directClass(ParDo.class.getName());
			BigQueryTableReference tableRef = sourceTableReference(sourceNode);

			JVar targetTableRef = body.decl(tableReferenceClass, "targetTableRef").init(tableReferenceClass._new());

			body.add(targetTableRef.invoke("setDatasetId").arg(JExpr.lit(tableRef.getDatasetId())));
			body.add(targetTableRef.invoke("setTableId").arg(JExpr.lit(tableRef.getTableId())));

			String sourceTableName = sourceTableName(sourceNode);

			JVar sourceTableCollection = body.decl(pCollectionClass, sourceTableName);
			JInvocation pipeline = p.invoke("apply")
					.arg(bigQueryIoClass.staticInvoke("readTableRows").invoke("from").arg(targetTableRef));
			sourceTableCollection.init(pipeline);
			return sourceTableCollection;

		}
	private BigQueryTableReference sourceTableReference(ShowlNodeShape sourceNode) throws BeamTransformGenerationException {

		DataSource ds = sourceNode.getShapeDataSource().getDataSource();
		if (ds instanceof GoogleBigQueryTable) {
			return ((GoogleBigQueryTable) ds).getTableReference();
		} 
		fail("Expected GoogleBigQueryTable data source for {0}", sourceNode.getPath());
		return null;
	}
	
	private String sourceTableName(ShowlNodeShape sourceNode) {
		
		return StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(RdfUtil.uri(sourceNode.getId()))) + "Table";
	}
	
    private void processOneDataSource() throws BeamTransformGenerationException {

      // public static void process(Options options) {
      
      JMethod method = mainClass.method(JMod.PUBLIC | JMod.STATIC, model.VOID, "process");
      JVar optionsVar = method.param(optionsClass, "options");
      JDefinedClass readFileFnClass = sourceInfoMap.values().iterator().next().getReadFileFn();
      ShowlNodeShape sourceNode = sourceInfoMap.values().iterator().next().getChannel().getSourceNode();
      //   Pipeline p = Pipeline.create(options);
      
      AbstractJClass pipelineType = model.ref(Pipeline.class);
      AbstractJClass parDoClass = model.directClass(ParDo.class.getName());
      
      JBlock body = method.body();
      JVar p = body.decl(pipelineType, "p", model.directClass(Pipeline.class.getName())
          .staticInvoke("create").arg(optionsVar));
      
      DataSource sourceDs = sourceNode.getShapeDataSource().getDataSource();
      
      
      String targetShapeName = RdfUtil.localName(targetNode.getId());
      String toTargetLabel = "To" + targetShapeName;
      JInvocation pipeline = null;
      if(sourceDs instanceof GoogleCloudStorageFolder ||
    		  sourceDs instanceof GoogleCloudStorageBucket) {
    	  JVar readOutput = processGoogleStorageBucketDataSource(body, optionsVar, p, sourceNode);
          pipeline = readOutput.invoke("get").arg(readFileFnClass.staticRef("successTag"))
        		  .invoke("apply").arg(JExpr.lit(toTargetLabel)).arg(parDoClass.staticInvoke("of")
                      .arg(toTargetFnClass._new()).invoke("withOutputTags")
                      .arg(toTargetFnClass.staticRef("successTag"))
                      .arg(model.ref(TupleTagList.class).staticInvoke("of")
                    		  .arg(toTargetFnClass.staticRef("deadLetterTag"))));
        
      } else if(sourceDs instanceof GoogleBigQueryTable) {
    	  JVar readOutput = processBigQueryDataSource(body, p, sourceNode);
          pipeline = readOutput.invoke("apply")
              .arg(JExpr.lit(toTargetLabel))
              .arg(parDoClass.staticInvoke("of")
                      .arg(toTargetFnClass._new()).invoke("withOutputTags")
                      .arg(toTargetFnClass.staticRef("successTag"))
                      .arg(model.ref(TupleTagList.class).staticInvoke("of")
                    		  .arg(toTargetFnClass.staticRef("deadLetterTag"))));
         
      }
      JVar outputTuple2 = body.decl(model.ref(PCollectionTuple.class), "outputTuple2");
      outputTuple2.init(pipeline);
     
      
      //   p.apply("Write${targetShapeName}", BigQueryIO.writeTableRows()
      //        .to("$tableSpec").withCreateDisposition(CreateDisposition.CREATE_NEVER)
      //        .withWriteDisposition(WriteDisposition.WRITE_APPEND));
     
      String targetTableSpec = targetTableSpec();
      String writeLabel = "Write" + targetShapeName;
      AbstractJClass bigQueryIoClass = model.ref(BigQueryIO.class);
      AbstractJClass createDispositionClass = model.ref(CreateDisposition.class);
      AbstractJClass writeDispositionClass = model.ref(WriteDisposition.class);
      
      body.add(outputTuple2.invoke("get").arg(toTargetFnClass.staticRef("successTag")).invoke("apply").arg(JExpr.lit(writeLabel))
              .arg(bigQueryIoClass.staticInvoke("writeTableRows")
                  .invoke("to").arg(targetTableSpec)
                  .invoke("withCreateDisposition").arg(createDispositionClass.staticRef("CREATE_NEVER"))
                  .invoke("withWriteDisposition").arg(writeDispositionClass.staticRef("WRITE_APPEND"))));
          
      body.add(writeErrorTable(outputTuple2, targetNode ,toTargetFnClass));
         
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
    
    String mainPackage() throws BeamTransformGenerationException {
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
  
  private class PropertyPair {
  	private ShowlPropertyShape sourceProperty;
  	private ShowlPropertyShape targetProperty;
		public PropertyPair(ShowlPropertyShape sourceProperty, ShowlPropertyShape targetProperty) {
			this.sourceProperty = sourceProperty;
			this.targetProperty = targetProperty;
		}
		public ShowlPropertyShape getSourceProperty() {
			return sourceProperty;
		}
		public ShowlPropertyShape getTargetProperty() {
			return targetProperty;
		}
  	
  	
  }

  private class OverlayWorker extends Worker {

  	private PropertyPair keyPair;
  	
  	private BeamChannel sourceChannel;
  	private BeamChannel targetChannel;

  	JDefinedClass targetToKvFn;
  	private JDefinedClass mergeClass;
  	private GroupInfo groupInfo;
  	
  	
		public OverlayWorker(JCodeModel model, ShowlNodeShape targetShape) {
			super(model, targetShape);
			
		}
		
		protected List<GroupInfo> groupList() throws BeamTransformGenerationException {
      List<GroupInfo> list = new ArrayList<>();
      
      GroupInfo group = new GroupInfo();
      group.getSourceList().add(sourceChannel);
      group.getSourceList().add(targetChannel);
      list.add(group);
      groupInfo = group;
      return list;
    }

		protected void generateMergeFnClasses(List<GroupInfo> groupList) throws BeamTransformGenerationException {

			OverlayMergeFnGenerator2 generator = new OverlayMergeFnGenerator2();
			mergeClass = generator.generate();
			groupInfo.setMergeFnClass(mergeClass);
		}

		protected JDefinedClass generateTransform() throws BeamTransformGenerationException {
			createTargetChannel();
			createSourceChannel();
			createKeyPair();
			JDefinedClass result = super.generateTransform();
			return result;
		}
		

		

		protected void defineTupleTagsAndPcollections(JBlock block, JVar pipeline, JVar options) throws BeamTransformGenerationException {
			 super.defineTupleTagsAndPcollections(block, pipeline, options);
			 
			 /*
			  * Declare the TupleTag for the target node as a static field on the main class.
			  */
			 
			 // static final TupleTag<TableRow> $targetTag = new TupleTag<TableRow>();
			 
			 AbstractJClass tableRowClass = model.ref(TableRow.class);
			 AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(tableRowClass);
			 String targetTagName = targetTagName();
			
			 JVar targetTupleTagVar = mainClass.field(JMod.FINAL | JMod.STATIC, tupleTagClass, targetTagName).init(tupleTagClass._new());
			 targetChannel.setTupleTag(targetTupleTagVar);
			 targetChannel.setReadFileFn(targetToKvFn);
			 /**
			  * Generate the PCollection for target data from BigQuery
			  */
			 
			 AbstractJClass stringClass = model.ref(String.class);
			 AbstractJClass tableReferenceClass = model.ref(TableReference.class);
			 AbstractJClass bigQueryIoClass = model.ref(BigQueryIO.class);
			 AbstractJClass parDoClass = model.ref(ParDo.class);
			 AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass, tableRowClass);
			 AbstractJClass pCollectionClass = model.ref(PCollection.class).narrow(kvClass);
			 

//				TableReference targetTableRef = new TableReference();
//				targetTableRef.setDatasetId("datasetId");
//				targetTableRef.setTableId("tableId");
			 
			 BigQueryTableReference tableRef = tableReference();
			 
			 JVar targetTableRef = block.decl(tableReferenceClass, "targetTableRef").init(tableReferenceClass._new());
			 
			 block.add(targetTableRef.invoke("setDatasetId").arg(JExpr.lit(tableRef.getDatasetId())));
			 block.add(targetTableRef.invoke("setTableId").arg(JExpr.lit(tableRef.getTableId())));
			 
			 String targetTableName = targetTableName();
			 
			 // PCollection<KV<String,TableRow>> $targetTable = p.apply(BigQueryIO.readTableRows().from(targetTableRef)).apply(ParDo.of(new $targetTableToKvFn()));
			 JVar targetTableCollection = block.decl(pCollectionClass, targetTableName).init(
					 pipeline.invoke("apply").arg(
							 bigQueryIoClass.staticInvoke("readTableRows").invoke("from").arg(targetTableRef))
					 		.invoke("apply").arg(parDoClass.staticInvoke("of").arg(targetToKvFn._new()))
					 );
			 targetChannel.setPcollection(targetTableCollection);
			 
		 }
		
		
		private String targetTableName() {
		
			return StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(RdfUtil.uri(targetNode.getId()))) + "Table";
		}

		class OverlayMergeFnGenerator2 extends BaseTargetFnGenerator2 {
					private JMethod dateTimeMethod;
					
					public JDefinedClass generate() throws BeamTransformGenerationException {
						
						declareClass();
						processElement();
						
						return thisClass;
					}
		
					private void processElement() throws BeamTransformGenerationException {
						
						AbstractJClass processContextClass = model.ref(ProcessContext.class);
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						AbstractJClass tupleTagClass = model.ref(TupleTag.class);
						AbstractJClass stringClass = model.ref(String.class);
						AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
		        AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
						AbstractJClass throwableClass = model.ref(Throwable.class);
						
						JVar sourceTag = sourceChannel.getTupleTag();
						JVar targetTag = targetChannel.getTupleTag();
						
						JVar deadLetterTag = thisClass.field(JMod.PUBLIC | JMod.STATIC , tupleTagClass.narrow(model.ref(String.class)), 
								"deadLetterTag").init(tupleTagClass._new().narrow(model.ref(String.class)).invoke("{}"));
						
						JVar successTag = thisClass.field(JMod.PUBLIC | JMod.STATIC , tupleTagClass.narrow(tableRowClass), 
								"successTag").init(tupleTagClass._new().narrow(model.ref(TableRow.class)).invoke("{}"));
						
		//			@ProcessElement 
		//			public void processElement(ProcessContext c) {
		//				try {
		//					KV<String, CoGbkResult> e = c.element();
		//					
		//					TableRow outputRow = baselineRow(e, MainClass.personTargetTag);
		//					overlay(outputRow, e, MainClass.personSourceTag);
		//					
		//					
		//					if (!outputRow.isEmpty()) {
		//						c.output(outputRow);
		//					}
		//				} catch (Throwable oops) {
		//					
		//				}
		//			}
						
		        JMethod method = 
		            thisClass.method(JMod.PUBLIC, model.VOID, "processElement");
		        method.annotate(model.directClass(ProcessElement.class.getName()));
		        
		        JMethod baselineRow = baselineRow();
		        
		        JVar c = method.param(processContextClass, "c");
		        
		        JTryBlock tryBlock = method.body()._try();
		        JBlock body = tryBlock.body();
		        JVar e = body.decl(kvClass, "e").init(c.invoke("element"));
		        
		        
		       
		        JVar outputRow = body.decl(tableRowClass, "outputRow")
		        		.init(JExpr.invoke(baselineRow).arg(e).arg(targetTag))
		        		;
		        
		        body.add(JExpr.invoke(overlay()).arg(outputRow).arg(e).arg(sourceTag));
		        
		        body._if(outputRow.invoke("isEmpty").not())._then().add(c.invoke("output").arg(successTag).arg(outputRow));
		        	
		        JCatchBlock catchBlock = tryBlock._catch(throwableClass);
		        JVar oops = catchBlock.param("oops");
		        catchBlock.body().add(c.invoke("output").arg(deadLetterTag).arg(oops.invoke("getMessage")));
						
					}
		
					private JMethod overlay() throws BeamTransformGenerationException {

//					private void overlay(TableRow outputRow, KV<String, CoGbkResult> e, TupleTag<TableRow> tupleTag) {
//		
//						Iterator<TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
//						
//						while (sequence.hasNext()) {
//							TableRow sourceRow = sequence.next();
//							TableRow targetRow = transform(sourceRow);
//							if (targetRow != null) {
//								copy(targetRow, outputRow);
//							}
//						}
//					}
						
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						AbstractJClass stringClass = model.ref(String.class);
						AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
						AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
						AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(TableRow.class);
						AbstractJClass iteratorClass = model.ref(Iterator.class).narrow(tableRowClass);
						
						JMethod method = thisClass.method(JMod.PRIVATE, model.VOID, "overlay");
						JBlock block = method.body();
						JVar outputRow = method.param(tableRowClass, "outputRow");
						JVar e = method.param(kvClass, "e");
						JVar tupleTag = method.param(tupleTagClass, "tupleTag");
						
						JVar sequence = block.decl(iteratorClass, "sequence")
								.init(e.invoke("getValue").invoke("getAll").arg(tupleTag).invoke("iterator"));
						
					
						block = block._while(sequence.invoke("hasNext")).body();
						
						JVar sourceRow = block.decl(tableRowClass, "sourceRow").init(sequence.invoke("next"));
						JVar targetRow = block.decl(tableRowClass, "targetRow").init(JExpr.invoke(transform()).arg(sourceRow));
						
						block._if(targetRow.neNull())._then().add(JExpr.invoke(copy()).arg(targetRow).arg(outputRow));
						
						
						return method;
					}

					private JMethod copy() {
						
//					private void copy(TableRow targetRow, TableRow outputRow) {
//					
//					for (Entry<String, Object> entry : targetRow.entrySet()) {
//						String fieldName = entry.getKey();
//						Object value = entry.getValue();
//						if (value instanceof TableRow) {
//							Object outputValue = outputRow.get(fieldName);
//							if (outputValue instanceof TableRow) {
//								copy((TableRow)value, (TableRow)outputValue);
//							} else {
//								outputRow.put(fieldName, value);
//							}
//							
//						} else {
//							outputRow.put(fieldName, value);
//						}
//					}
//					
//				}						
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						AbstractJClass stringClass = model.ref(String.class);
						AbstractJClass objectClass = model.ref(Object.class);
						AbstractJClass entryClass = model.ref(Entry.class).narrow(stringClass).narrow(objectClass);
						
						JMethod method = thisClass.method(JMod.PRIVATE, model.VOID, "copy");
						JVar targetRow = method.param(tableRowClass, "targetRow");
						JVar outputRow = method.param(tableRowClass, "outputRow");
						
						
						JForEach forEach = method.body().forEach(entryClass, "entry", targetRow.invoke("entrySet"));
						JBlock block = forEach.body();
						
						JVar entry = forEach.var();
						
						JVar fieldName = block.decl(stringClass, "fieldName").init(entry.invoke("getKey"));
						JVar value = block.decl(objectClass, "value").init(entry.invoke("getValue"));
						
						JConditional ifStatement = block._if(value._instanceof(tableRowClass));
						JBlock thenBlock = ifStatement._then();
						
						JVar outputValue = thenBlock.decl(objectClass, "outputValue").init(outputRow.invoke("get").arg(fieldName));
						JConditional innerIf = thenBlock._if(outputValue._instanceof(tableRowClass));
						JBlock innerThen = innerIf._then();
						innerThen.add(JExpr.invoke(method).arg(value.castTo(tableRowClass)).arg(outputValue.castTo(tableRowClass)));
						innerIf._else().add(outputRow.invoke("put").arg(fieldName).arg(value));
						
						ifStatement._else().add(outputRow.invoke("put").arg(fieldName).arg(value));
						
						return method;
					}

			private JMethod transform() throws BeamTransformGenerationException {
				AbstractJClass tableRowClass = model.ref(TableRow.class);
				JMethod method = thisClass.method(JMod.PRIVATE, tableRowClass, "transform");
				JVar sourceRow = method.param(tableRowClass, "sourceRow");
				
				sourceChannel.setSourceRow(sourceRow);
				JBlock block = method.body();
				JVar targetRow = block.decl(tableRowClass, "targetRow").init(tableRowClass._new());

				JDefinedClass errorBuilderClass = errorBuilderClass();


				// ErrorBuilder errorBuilder = new ErrorBuilder();
				block.decl(errorBuilderClass, "errorBuilder").init(errorBuilderClass._new());
				for (ShowlDirectPropertyShape direct : targetNode.getProperties()) {
					BeamMethod childMethod = processProperty("", direct);
					etran.invoke(childMethod);
				}

				
				block._return(targetRow);
				

				return method;
			}

					private JMethod baselineRow() {
		
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						AbstractJClass stringClass = model.ref(String.class);
						AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
		        AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
		        AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(tableRowClass);
		        AbstractJClass iteratorClass = model.ref(Iterator.class).narrow(tableRowClass);
		        AbstractJClass longClass = model.ref(Long.class);
		        
		//			private TableRow baselineRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tupleTag) {
		        
		        JMethod method = thisClass.method(JMod.PRIVATE, tableRowClass, "baselineRow");
		        JMethod dateTimeMethod = dateTime();
		        JVar e = method.param(kvClass, "e");
		        JVar tupleTag = method.param(tupleTagClass, "tupleTag");
		        
		//				
		//				Iterator<TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
		//				TableRow result = null;
		//				Long latest = null;
		        
		        JVar sequence = method.body().decl(iteratorClass, "sequence")
		        		.init(e.invoke("getValue").invoke("getAll").arg(tupleTag).invoke("iterator"));
		        
		        JVar result = method.body().decl(tableRowClass, "result").init(JExpr._null());
		        JVar latest = method.body().decl(longClass, "latest").init(JExpr._null());
		        
		//				while (sequence.hasNext()) {
		//					TableRow row = sequence.next();
		//					Long modified = dateTime(row, "modified");
		//					if (modified!=null && (latest==null || modified > latest)) {
		//						latest = modified;
		//						result = row;
		//					}
		//				}
		        
		        JWhileLoop loop = method.body()._while(sequence.invoke("hasNext"));
		        
		        JVar row = loop.body().decl(tableRowClass, "row").init(sequence.invoke("next"));
		        JVar modified = loop.body().decl(longClass, "modified")
		        		.init(JExpr.invoke(dateTime()).arg(row).arg(JExpr.lit("modified")));
		        
		        loop.body()._if(modified.neNull().cand(latest.eqNull().cor(modified.gt(latest))))
		        	._then().assign(latest, modified).assign(result, row);
		        
		        
		//				
		//				if (result == null) {
		//					result = new TableRow();
		//				}
		//				
		//				return result;
		//			}
						
		        method.body()._if(result.eqNull())._then().assign(result, tableRowClass._new());
		        
		        method.body()._return(result);
		        
						return method;
					}
		
					private JMethod dateTime() {
						if (dateTimeMethod == null) {
							AbstractJClass longClass = model.ref(Long.class);
							AbstractJClass tableRowClass = model.ref(TableRow.class);
							AbstractJClass stringClass = model.ref(String.class);
							AbstractJClass objectClass = model.ref(Object.class);
							AbstractJClass dateTimeClass = model.ref(DateTime.class);
		
						//	private Long dateTime(TableRow row, String fieldName) {
						//		Object value = row.get(fieldName);
						//    Long result = null;
						//		if (value instanceof String) {
						//			result = new DateTime((String) value).getValue();
					  //      row.set(fieldName, result);
						//     
						//		} else if (value instanceof Long) {
					  //      result = (Long) value;
					  //    }
						//		return result;
						//	}
							
							dateTimeMethod = thisClass.method(JMod.PRIVATE, longClass, "dateTime");
							JVar row = dateTimeMethod.param(tableRowClass, "row");
							JVar fieldName = dateTimeMethod.param(stringClass, "fieldName");
							
							JBlock body = dateTimeMethod.body();
							
							JVar value = body.decl(objectClass, "value").init(row.invoke("get").arg(fieldName));
							JVar result = body.decl(longClass, "result").init(JExpr._null());
							
							JConditional ifStatement = body._if(value._instanceof(stringClass));
							JBlock thenBlock = ifStatement._then();
							
							thenBlock.add(result.assign(dateTimeClass._new().arg(value.castTo(stringClass)).invoke("getValue")));
							thenBlock.add(row.invoke("set").arg(fieldName).arg(result));
							
							JBlock elseBlock = ifStatement._elseif(value._instanceof(longClass))._then();
							elseBlock.add(result.assign(value.castTo(longClass)));
							
							body._return(result);
							
						}
						return dateTimeMethod;
					}
		
					protected void declareClass() throws BeamTransformGenerationException {
						String thisClassName = mergeFnClassName();
						
						AbstractJClass stringClass = model.ref(String.class);
						AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
						AbstractJClass tableRowClass = model.ref(TableRow.class);
						AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
						AbstractJClass doFnClass = model.ref(DoFn.class).narrow(kvClass).narrow(tableRowClass);
						
						try {
							thisClass = model._class(JMod.PUBLIC, thisClassName)._extends(doFnClass);
						} catch (JClassAlreadyExistsException e) {
							fail("Failed to declare class {0}", thisClassName);
						}
						
					}
		
					private String mergeFnClassName() throws BeamTransformGenerationException {
						
						return mainPackage() + "." + ShowlUtil.shortShapeName(targetNode) + "MergeFn";
					}


					@Override
					protected BeamChannel beamChannel(ShowlNodeShape sourceNode) throws BeamTransformGenerationException {
						return sourceInfoMap.get(RdfUtil.uri(sourceNode.getRoot().getId()));
					}

					@Override
					protected JVar contextElement(JBlock body, JVar c) throws BeamTransformGenerationException {
						throw new BeamTransformGenerationException("Not implemented");
					}
				}

		private BigQueryTableReference tableReference() throws BeamTransformGenerationException {

			DataSource ds = targetNode.getShapeDataSource().getDataSource();
			if (ds instanceof GoogleBigQueryTable) {
				return ((GoogleBigQueryTable) ds).getTableReference();
			} 
			fail("Expected GoogleBigQueryTable data source for {0}", targetNode.getPath());
			return null;
		}

		private String targetTagName() {
			return StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(RdfUtil.uri(targetNode.getId())) + "Tag");
		}

		private void createTargetChannel() {
			
			ShowlChannel showlChannel = new ShowlChannel(targetNode, null);
			targetChannel = new BeamChannel(showlChannel);
			
			
			
		}
		private void createSourceChannel() throws BeamTransformGenerationException {
			List<ShowlChannel> channelList = targetNode.nonEnumChannels(reasoner);
			if (channelList.size() == 1) {
				ShowlChannel channel = channelList.get(0);
				sourceChannel = new BeamChannel(channelList.get(0));
				
				sourceInfoMap.put(RdfUtil.uri(channel.getSourceNode().getId()), sourceChannel);
			} else {
				fail("Expected a single source but found {0} for {1}", channelList.size(), targetNode.getPath());
			}
			
		}
		
		private void createKeyPair() throws BeamTransformGenerationException {
			
			ShowlNodeShape sourceNode = sourceChannel.getFocusNode();

			
			ShowlPropertyShape targetId = targetNode.findOut(Konig.id);
			ShowlPropertyShape sourceId = sourceNode.findOut(Konig.id);
			
			if (sourceId != null && targetId!=null) {
				keyPair = new PropertyPair(sourceId, targetId);
				return;
			}
			
			// For now, we only support keys that are direct properties of the target shape.
			// We can relax this constraint later if the need arises.
			
			outer: for (ShowlDirectPropertyShape targetDirect : targetNode.getProperties()) {
				URI keyType = targetDirect.getValueType(reasoner);

				if (keyType!=null && reasoner.isInverseFunctionalProperty(keyType)) {
					URI predicate = targetDirect.getPredicate();
					
					ShowlDirectPropertyShape sourceDirect = sourceNode.getProperty(predicate);
					if (sourceDirect != null) {
						keyPair(sourceDirect, targetDirect);
					} else {
						ShowlDerivedPropertyList derivedList = sourceNode.getDerivedProperty(predicate);
						for (ShowlDerivedPropertyShape derived : derivedList) {
							PropertyConstraint constraint = derived.getPropertyConstraint();
							if (constraint != null && constraint.getFormula() != null) {
									keyPair(derived, targetDirect);
									continue outer;
							}
						}
					}
				}
			}
			
		
			if (keyPair == null) {
				fail("Found no common inverse functional property on {0} and {1}");
			}
			
			
		}
		
		private void keyPair(ShowlPropertyShape source, ShowlDirectPropertyShape target) {
			boolean replacePair = false;
			if (keyPair == null) {
				replacePair = true;
			} else {
				boolean alphaOrder = false;
				ShowlPropertyShape prior = keyPair.getTargetProperty();
				if (target.getValueShape() == null) {
					if (prior.getValueShape() != null) {
						// prefer an unstructured key
						replacePair = true;
					} else {
						alphaOrder = true;
					}
				} else {
					alphaOrder = true;
				}
				
				if (alphaOrder) {
					String priorName = prior.getPredicate().getLocalName();
					String newName = target.getPredicate().getLocalName();
					
					if (newName.compareTo(priorName) < 0) {
						replacePair = true;
					}
				}
				
				
			}
			if (replacePair) {
				keyPair = new PropertyPair(source, target);
			}
			
		}
		protected void declareReadFileFnClass() throws JClassAlreadyExistsException, BeamTransformGenerationException {

			targetToKvFn = generateTargetToKvFn();
			JDefinedClass sourceToKvFn = generateFileToKvFn(keyPair.getSourceProperty(), sourceChannel.getChannel());
    	
//  	for (ShowlChannel channel : targetNode.nonEnumChannels(reasoner)) {
//       
//       ShowlStatement joinStatement = channel.getJoinStatement();
//       
//       if (joinStatement == null) {
//         continue;
//       }
//   
//       ShowlPropertyShape leftKey = leftKey(joinStatement);
//       ShowlPropertyShape rightKey = rightKey(joinStatement);
//       
//       
//       generateFileToKvFn(leftKey, channel(leftKey, channel));
//       generateFileToKvFn(rightKey, channel(rightKey, channel));
//       
//       
//     }
			
			
		}
		private JDefinedClass generateTargetToKvFn() throws BeamTransformGenerationException {
			GetKeyMethodGenerator getKeyMethodGenerator = createGetKeyMethodGeneratorForTarget();
			
			OverlayReadTargetFnGenerator2 generator = new OverlayReadTargetFnGenerator2(getKeyMethodGenerator);
			return generator.generate();
			
		}

		private GetKeyMethodGenerator createGetKeyMethodGeneratorForTarget() throws BeamTransformGenerationException {
			ShowlPropertyShape targetKey = keyPair.getTargetProperty();
			URI keyType = keyType(targetKey);
			
			if (keyType == null) {
				fail("Unknown key type for {0}", targetKey.getPath());
			}
			
			if (XMLSchema.STRING.equals(keyType)) {
				return new StringGetKeyMethodGenerator(targetKey.getPredicate().getLocalName());
			}
			fail("Unsupported key type {0} for {1}", keyType.getLocalName(), targetKey.getPath());
			return null;
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
    private List<BeamChannel> sourceList = new ArrayList<>();
    private JVar kvpCollection;
    private JDefinedClass mergeFnClass;
    
    public GroupInfo() {
    }
    
    public String kvpCollectionName(int count, int size) {
      
      return size>1 ? "kvpCollection" + count : "kvpCollection";
    }
    
    public String mergeClassName() {
      StringBuilder builder = new StringBuilder();
      builder.append("Merge");
      String and = "";
      for (BeamChannel sourceInfo : sourceList) {
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
    public List<BeamChannel> getSourceList() {
      return sourceList;
    }
    public JDefinedClass getMergeFnClass() {
      return mergeFnClass;
    }
    public void setMergeFnClass(JDefinedClass mergeFnClass) {
      this.mergeFnClass = mergeFnClass;
    }
    
    
    
  }
  
  interface GetKeyMethodGenerator {
  	
  	JMethod generate(JCodeModel model, JDefinedClass declaringClass, String methodName) throws BeamTransformGenerationException;  
  }
  
  static class StringGetKeyMethodGenerator implements GetKeyMethodGenerator {
  	private String fieldName;

		public StringGetKeyMethodGenerator(String fieldName) {
			this.fieldName = fieldName;
		}

		@Override
		public JMethod generate(JCodeModel model, JDefinedClass declaringClass, String methodName) throws BeamTransformGenerationException {
			
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			JMethod method = declaringClass.method(JMod.PRIVATE, stringClass, methodName);
			JVar row = method.param(tableRowClass, "row");
			
			method.body()._return(row.invoke("get").arg(JExpr.lit(fieldName)).castTo(stringClass));
			
			return method;
		}
  	
  }
  
  private interface ShowlExpressionHandler {
    IJExpression javaExpression(ShowlExpression e) throws BeamTransformGenerationException;
  }
  
  
  private static class BeamEnumInfo {
	  
	  private JVar enumObjectVar;
	  private JConditional conditionalStatement;
	  private ShowlEnumJoinInfo joinInfo;
	  private JVar sourceKeyVar;
	  
		public BeamEnumInfo(ShowlEnumJoinInfo joinInfo) {
			this.joinInfo = joinInfo;
		}
	
		public JVar getEnumObjectVar() {
			return enumObjectVar;
		}
	
		public void setEnumObjectVar(JVar enumObjectVar) {
			this.enumObjectVar = enumObjectVar;
		}
	
		public JConditional getConditionalStatement() {
			return conditionalStatement;
		}
	
		public void setConditionalStatement(JConditional conditionalStatement) {
			this.conditionalStatement = conditionalStatement;
		}
	
		public ShowlEnumJoinInfo getJoinInfo() {
			return joinInfo;
		}
	
		public JVar getSourceKeyVar() {
			return sourceKeyVar;
		}
	
		public void setSourceKeyVar(JVar sourceKeyVar) {
			this.sourceKeyVar = sourceKeyVar;
		}
  }
  
  /**
   * @deprecated Should be replaced by BeamParameter
   * @author Greg McFall
   *
   */
  static class PropertyMethodParameter {
  
  	private BeamChannel channel;

		public PropertyMethodParameter(BeamChannel channel) {
			this.channel = channel;
		}

		public BeamChannel getChannel() {
			return channel;
		}
  	
		
  }
  
  /**
   * Encapsulates information about a method that constructs the value for a target property.
   * @deprecated This should be replaced by BeamMethod
   * @author Greg McFall
   *
   */
  
}