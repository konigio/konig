package io.konig.schemagen.java;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import io.konig.core.AmbiguousPreferredClassException;
import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.ClassManager;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class JavaClassBuilder {
	private static final Logger logger = LoggerFactory.getLogger(JavaClassBuilder.class);
	private JavaNamer namer;
	private JavaDatatypeMapper mapper;
	private ClassManager classManager;
	private OwlReasoner reasoner;
	private Graph graph;
	private LogicalShapeNamer logicalShapeNamer;
	private JClass owlThing;
	
	public JavaClassBuilder(ClassManager classManager, LogicalShapeNamer logicalShapeNamer, JavaNamer namer, OwlReasoner reasoner) {
		this.classManager = classManager;
		this.logicalShapeNamer = logicalShapeNamer;
		this.namer = namer;
		this.reasoner = reasoner;
		this.graph = reasoner.getGraph();
		mapper = new SmartJavaDatatypeMapper(reasoner);
	}
	
	public void buildAll(Collection<Shape> collection, JCodeModel model) throws SchemaGeneratorException {
		for (Shape shape : collection) {
			buildClass(shape, model);
		}
	}
	
	private JClass buildOwlThing(JCodeModel model) throws SchemaGeneratorException {

		String javaClassName = namer.javaClassName(OWL.THING);
		JClass prior = model._getClass(javaClassName);
		if (prior != null) {
			return prior;
		}

		try {
			JDefinedClass dc = model._class(javaClassName);
			
			JClass typeList = model.ref(Set.class);
			typeList = typeList.narrow(URI.class);
			
			JClass uriClass = model.ref(URI.class);

			String fieldName = "id";
			dc.field(JMod.PRIVATE, URI.class, fieldName);
			createGetter(dc, fieldName, fieldName, uriClass);
			createSetter(dc, fieldName, fieldName, uriClass);
			
			fieldName = "typeSet";
			String methodBaseName = "typeSet";
			dc.field(JMod.PRIVATE, typeList, fieldName);
			createAddTypeMethod(model, dc, uriClass);
			createGetter(dc, methodBaseName, fieldName, typeList);
			createSetter(dc, methodBaseName, fieldName, typeList);
			
			
			
			return dc;
		} catch (JClassAlreadyExistsException e) {
			throw new SchemaGeneratorException(e);
		}
	}

	private void createAddTypeMethod(JCodeModel model, JDefinedClass dc, JClass jClass) {
		String methodName = addMethodName("type");
		JFieldRef field = JExpr.ref("typeSet");
		
		JClass listClass = model.ref(HashSet.class);
		listClass = listClass.narrow(jClass);
		
		JMethod method = dc.method(JMod.PUBLIC, void.class, methodName);
		JVar param = method.param(jClass, "value");
		JBlock body = method.body();
		
		
		body._if(field.eq(JExpr._null()))._then().assign(field, JExpr._new(listClass));
		body.add(field.invoke("add").arg(param));
		
		
	}

	public JClass buildClass(Shape shape, JCodeModel model) throws SchemaGeneratorException {
		
		if (owlThing == null) {
			owlThing = buildOwlThing(model);
		}
		
		URI scopeClass = shape.getScopeClass();
		
		try {
			Vertex preferred = reasoner.preferredClass(scopeClass);
			scopeClass = (URI) preferred.getId();
		} catch (AmbiguousPreferredClassException e1) {
			throw new SchemaGeneratorException(e1);
		}
		
		String javaClassName = namer.javaClassName(scopeClass);

		JClass prior = model._getClass(javaClassName);
		if (prior != null) {
			return prior;
		}
		
		try {
			JDefinedClass dc = model._class(javaClassName);
			declareStaticTypeVar(model, dc, scopeClass);
			declareSuperClass(model, dc, shape);
			declareCtor(model, dc);
			createFields(model, shape, dc);
			return dc;
		} catch (JClassAlreadyExistsException e) {
			throw new SchemaGeneratorException(e);
		}
		
	}

	private void declareCtor(JCodeModel model, JDefinedClass dc) {
		
		JFieldRef typeRef = dc.staticRef("TYPE");
		dc.constructor(JMod.PUBLIC).body().invoke("addType").arg(typeRef);
		
		
	}

	private void declareStaticTypeVar(JCodeModel model, JDefinedClass dc, URI scopeClass) {
		
		JClass uriClass = model.ref(URIImpl.class);
		
		dc.field(JMod.STATIC | JMod.PUBLIC | JMod.FINAL, URI.class, "TYPE").init(
			JExpr._new(uriClass).arg(JExpr.lit(scopeClass.stringValue())));
	}

	private void declareSuperClass(JCodeModel model, JDefinedClass dc, Shape shape) {
		URI scopeClassId = shape.getScopeClass();
		
		List<Vertex> superClassList = graph.v(scopeClassId).out(RDFS.SUBCLASSOF).toVertexList();
		
		if (superClassList.isEmpty()) {
			dc._extends(owlThing);
		} else if (superClassList.size()==1) {
			
			Vertex superVertex = superClassList.get(0);
			Resource superId = superVertex.getId();
			if (superId instanceof URI) {
				
				try {

					URI superIRI = reasoner.preferredClassAsURI((URI) superId);
					
					
					Shape superShape = classManager.getLogicalShape(superIRI);
					if (superShape == null) {
						URI shapeId = logicalShapeNamer.logicalShapeForOwlClass(superIRI);
						superShape = new Shape(shapeId);
						superShape.setScopeClass(superIRI);
						classManager.addLogicalShape(shape);
					}
					
					JClass jSuper = buildClass(superShape, model);
					
					dc._extends(jSuper);
					
				} catch (AmbiguousPreferredClassException e) {
					throw new SchemaGeneratorException(e);
				} 
			}
			
		} else {
			logger.warn("Ignoring subClassOf statement for class " + scopeClassId.getLocalName() + 
				": multiple inheritance not supported yet");
		}
		
	}

	private void createFields(JCodeModel model, Shape shape, JDefinedClass dc) {
		
		List<PropertyConstraint> list = shape.getProperty();
		for (PropertyConstraint p : list) {
			createField(model, p, dc);
		}
		
	}

	private void createField(JCodeModel model, PropertyConstraint p, JDefinedClass dc) {
		
		URI datatype = p.getDatatype();
		Resource owlClass = p.getValueClass();
		
		if (datatype != null) {
			createDatatypeField(model, p, dc);
		} else if (owlClass instanceof URI) {
			createObjectField(model, p, dc);
		}
		
		
	}

	private void createObjectField(JCodeModel model, PropertyConstraint p, JDefinedClass dc) {
		
		URI predicate = p.getPredicate();
		URI owlClass = (URI) p.getValueClass();
		
		JClass jClass = javaClass(model, owlClass);
		
		Integer maxCount = p.getMaxCount();
		
		String fieldName = predicate.getLocalName();
		String methodBaseName = fieldName;
		
		if (maxCount==null || maxCount>1) {
			
			createAddMethod(model, dc, fieldName, jClass);
			methodBaseName = fieldName + "List";
			JClass listClass = model.ref(List.class);
			jClass = listClass.narrow(jClass);
			
		}

		dc.field(JMod.PRIVATE, jClass, predicate.getLocalName());
		
		createGetter(dc, methodBaseName, fieldName, jClass);
		createSetter(dc, methodBaseName, fieldName, jClass);
	}

	private void createAddMethod(JCodeModel model, JDefinedClass dc, String fieldName, JClass jClass) {
		String methodName = addMethodName(fieldName);
		JFieldRef field = JExpr.ref(fieldName);
		
		JClass listClass = model.ref(ArrayList.class);
		listClass = listClass.narrow(jClass);
		
		JMethod method = dc.method(JMod.PUBLIC, void.class, methodName);
		JVar param = method.param(jClass, "value");
		JBlock body = method.body();
		
		
		body._if(field.eq(JExpr._null()))._then().assign(field, JExpr._new(listClass));
		body.add(field.invoke("add").arg(param));
		
		
	}



	private JClass javaClass(JCodeModel model, URI owlClass) {
		
		String className = namer.javaClassName(owlClass);
		JClass result = model._getClass(className);
		if (result == null) {
			Shape shape = classManager.getLogicalShape(owlClass);
			
			result = buildClass(shape, model);
		}
		return result;
	}

	private void createDatatypeField(JCodeModel model, PropertyConstraint p, JDefinedClass dc) {
		
		URI predicate = p.getPredicate();
		URI datatype = p.getDatatype();
		
		String fieldName = predicate.getLocalName();
		String methodBaseName = fieldName;
		
		Class<?> javaType = mapper.javaDatatype(datatype);
		JClass jClass = model.ref(javaType);
		
		Integer maxCount = p.getMaxCount();
		
		if (maxCount==null || maxCount>1) {
			
			
			createAddMethod(model, dc, fieldName, jClass);
			methodBaseName = fieldName + "List";
			
			JClass listClass = model.ref(List.class);
			jClass = listClass.narrow(jClass);
		}

		dc.field(JMod.PRIVATE, jClass, predicate.getLocalName());
		
		createGetter(dc, methodBaseName, fieldName, jClass);
		createSetter(dc, methodBaseName, fieldName, jClass);
		
	}

	
	private void createSetter(JDefinedClass dc, String methodBaseName, String fieldName, JClass javaType) {
		
		String methodName = setterName(methodBaseName);
		JMethod method = dc.method(JMod.PUBLIC, void.class, methodName);
		JVar param = method.param(javaType, fieldName);
		method.body().assign(JExpr._this().ref(fieldName), param);
	}

	private String setterName(String fieldName) {
		StringBuilder builder = new StringBuilder("set");
		capitalize(builder, fieldName);
		return builder.toString();
	}

	private void createGetter(JDefinedClass dc, String methodBaseName, String fieldName, JClass javaType) {
		String methodName = getterName(methodBaseName);
		JMethod method = dc.method(JMod.PUBLIC, javaType, methodName);
		method.body()._return(JExpr.ref(fieldName));
		
	}
	
	public static String getterName(String fieldName) {
		StringBuilder builder = new StringBuilder("get");
		capitalize(builder, fieldName);
		return builder.toString();
	}
	
	private String addMethodName(String fieldName) {
		StringBuilder builder = new StringBuilder("add");
		capitalize(builder, fieldName);
		return builder.toString();
	}

	public static void capitalize(StringBuilder builder, String fieldName) {
		
		for (int i=0; i<fieldName.length(); i++) {
			char c = fieldName.charAt(i);
			if (i==0) {
				c = Character.toUpperCase(c);
			}
			builder.append(c);
		}
		
	}


}
