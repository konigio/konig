package io.konig.schemagen.java;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import io.konig.core.AmbiguousPreferredClassException;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.util.SmartJavaDatatypeMapper;
import io.konig.core.vocab.Konig;
import io.konig.runtime.io.TypeSet;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class JavaClassBuilder {
	private JavaNamer namer;
	private JavaDatatypeMapper mapper;
	private ClassStructure hierarchy;
	private OwlReasoner reasoner;
	private Graph graph;
	private TypeInfo owlThing;
	private Filter filter;
	
	public JavaClassBuilder(ShapeManager shapeManager, JavaNamer namer, OwlReasoner reasoner) {
//		classAnalyzer = new ClassAnalyzer(shapeManager, reasoner);
		hierarchy = new ClassStructure();
		hierarchy.init(shapeManager, reasoner);
		
		this.namer = namer;
		this.reasoner = reasoner;
		this.graph = reasoner.getGraph();
		mapper = new SmartJavaDatatypeMapper(reasoner);
		filter = new Filter(null);
	}
	

	public JavaClassBuilder(ClassStructure structure, JavaNamer namer, OwlReasoner reasoner, Filter filter) {
		hierarchy = structure;
		this.namer = namer;
		this.reasoner = reasoner;
		this.graph = reasoner.getGraph();
		mapper = new SmartJavaDatatypeMapper(reasoner);
		this.filter = filter;
	}
	
	public JavaDatatypeMapper getMapper() {
		return mapper;
	}





	public void setMapper(JavaDatatypeMapper mapper) {
		this.mapper = mapper;
	}


	public void buildAll(Collection<Shape> collection, JCodeModel model) throws SchemaGeneratorException {
		for (Shape shape : collection) {
			if (shape.getOr() == null) {
				buildClass(shape, model);
			}
		}
	}
	
	private TypeInfo buildOwlThing(JCodeModel model) throws SchemaGeneratorException {

		String javaClassName = namer.javaClassName(OWL.THING);
		String interfaceName = namer.javaInterfaceName(OWL.THING);
		JDefinedClass prior = model._getClass(javaClassName);
		JDefinedClass priorInterface = model._getClass(interfaceName);
		
		if (prior != null && priorInterface!=null) {
			return new TypeInfo(OWL.THING, prior, priorInterface);
		}

		try {
			JDefinedClass dc = model._class(javaClassName);
			JDefinedClass dci = model._class(interfaceName, ClassType.INTERFACE);
			
			dc._implements(dci);
			
			TypeInfo typeInfo = new TypeInfo(OWL.THING, dc, dci);
			
			
			JClass uriClass = model.ref(URI.class);
			

			dc.field(JMod.PRIVATE, URI.class, "id");
			createThingConstructors(model, typeInfo);
			createStaticTypeVar(model, typeInfo, OWL.THING);
			createTypesField(model, typeInfo, null);
			createTypeGetter(model, typeInfo);
			createGetter(typeInfo, "id", "id", uriClass);
			createSetter(typeInfo, "id", "id", uriClass, uriClass);
			
			declareTypeGetterInterface(model, typeInfo);
			declareHashCodeMethod(model, typeInfo);
			declareEqualsMethod(model, typeInfo);
			
			Shape shape = getClassShape(OWL.THING);
			createFields(model, shape, typeInfo);
			
			return typeInfo;
		} catch (JClassAlreadyExistsException e) {
			throw new SchemaGeneratorException(e);
		}
	}


	
	private void createThingConstructors(JCodeModel model, TypeInfo typeInfo) {
		
		typeInfo.implClass.constructor(JMod.PUBLIC);
		
		JMethod ctor = typeInfo.implClass.constructor(JMod.PUBLIC);
		JVar idVar = ctor.param(URI.class, "id");
		ctor.body().assign(JExpr._this().ref("id"), idVar);
		
		
	}

	private void createTypesField(JCodeModel model, TypeInfo typeInfo, List<TypeInfo> superList) {
		
		JClass setClass = model.ref(Set.class);
		JClass uriSetClass = setClass.narrow(URI.class);
		
		JClass setImplClass = model.ref(TypeSet.class);
		
		JFieldRef typeField = typeInfo.interfaceClass.staticRef("TYPE");
		
		JInvocation init =
				JExpr._new(setImplClass);
		
		init = init.invoke("append").arg(typeField);
		
		if (superList != null) {
			
			if (superList.isEmpty()) {
				JFieldRef thingTypeField = owlThing.interfaceClass.staticRef("TYPE");
				init = init.invoke("append").arg(thingTypeField);
			} else {
				for (TypeInfo superType : superList) {
					
					JFieldRef superTypeField = superType.interfaceClass.staticRef("TYPES");
					init = init.invoke("appendAll").arg(superTypeField);
				}
			}
			
			
		}
		
		typeInfo.interfaceClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, uriSetClass, "TYPES").init(init);
		
	}

	private void declareTypeGetterInterface(JCodeModel model, TypeInfo pair) {

		JDefinedClass dci = pair.interfaceClass;
		dci.method(JMod.NONE, URI.class, "getType");
		

		JClass setClass = model.ref(Set.class);
		JClass uriSetClass = setClass.narrow(URI.class);
		dci.method(JMod.NONE, uriSetClass, "getAllTypes");
		
	}

	private void declareEqualsMethod(JCodeModel model, TypeInfo pair) {
		JDefinedClass dc = pair.implClass;
		JDefinedClass dci = pair.interfaceClass;

		JFieldRef idField = JExpr.ref("id");
		JMethod method = dc.method(JMod.PUBLIC, boolean.class, "equals");

		JVar param = method.param(Object.class, "other");
		
		JBlock body = method.body();
		body._if(idField.ne(JExpr._null()).cand(param._instanceof(dci)))
			._then()._return(idField.invoke("equals").arg(
					((JExpression)JExpr.cast(dci, param)).invoke("getId")));
		
		body._return(JExpr._this().eq(param));
		
	}

	private void declareHashCodeMethod(JCodeModel model, TypeInfo pair) {
		
		JDefinedClass dc = pair.implClass;
		
		JMethod method = dc.method(JMod.PUBLIC, int.class, "hashCode");
		JFieldRef idField = JExpr.ref("id");
		
		JBlock body = method.body();

		body._if(idField.ne(JExpr._null()))._then()._return(idField.invoke("hashCode"));
		body._return(JExpr._super().invoke("hashCode"));
		
		
	}
	
	public void buildClass(Resource owlClassId, JCodeModel model) throws SchemaGeneratorException {
		Shape shape = getClassShape(owlClassId);
		buildClass(shape, model);
	}
	
	public void buildAllClasses(JCodeModel model) throws SchemaGeneratorException {
		
		List<Vertex> list = graph.v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		for (Vertex v : list) {
			Resource id = v.getId();
			if (id instanceof URI) {
				buildClass(id, model);
			}
		}
	}

	public TypeInfo buildClass(Shape shape, JCodeModel model) throws SchemaGeneratorException {
		
		if (owlThing == null) {
			owlThing = buildOwlThing(model);
		}
		
		URI targetClass = shape.getTargetClass();
		
		if (targetClass == null || !filter.acceptIndividual(targetClass.stringValue())) {
			return null;
		}
		
		
		try {
			Vertex preferred = reasoner.preferredClass(targetClass);
			targetClass = (URI) preferred.getId();
		} catch (AmbiguousPreferredClassException e1) {
			throw new SchemaGeneratorException(e1);
		}
		
		String javaClassName = namer.javaClassName(targetClass);
		String interfaceClassName = namer.javaInterfaceName(targetClass);

		JDefinedClass prior = model._getClass(javaClassName);
		JDefinedClass priorInterface = model._getClass(interfaceClassName);
		if (prior != null && priorInterface!=null) {
			
			return new TypeInfo(targetClass, prior, priorInterface);
		}
		
		try {
			JDefinedClass dc = model._class(javaClassName);
			JDefinedClass dci = model._class(interfaceClassName, ClassType.INTERFACE);
			TypeInfo typeInfo = new TypeInfo(targetClass, dc, dci);
			dc._implements(dci);
			createStaticTypeVar(model, typeInfo, targetClass);
			createTypeGetter(model, typeInfo);
			
			List<TypeInfo> superList = superClassList(model, typeInfo);
			declareSuperClasses(typeInfo, superList);

			createTypesField(model, typeInfo, superList);
			createStaticIndividuals(model, typeInfo);
			createConstructors(model, typeInfo);
			
			createFields(model, shape, typeInfo);
			return typeInfo;
		} catch (JClassAlreadyExistsException e) {
			throw new SchemaGeneratorException(e);
		}
		
	}
	
	private void createStaticIndividuals(JCodeModel model, TypeInfo typeInfo) throws JClassAlreadyExistsException {

		if (reasoner.isEnumerationClass(typeInfo.owlClass)) {
			
			JDefinedClass factory = typeInfo.interfaceClass._class("Factory");
			JType stringClass = model.ref(String.class);
			JMethod valueOf = factory.method(JMod.PUBLIC, typeInfo.interfaceClass, "valueOf");
			JVar nameVar = valueOf.param(stringClass, "name");
			typeInfo.interfaceClass.field(JMod.PUBLIC | JMod.FINAL | JMod.STATIC, factory, "$").init(JExpr._new(factory));
			JSwitch factorySwitch = valueOf.body()._switch(nameVar);
			
			NamespaceManager nsManager = graph.getNamespaceManager();
			
			
			List<Vertex> list = reasoner.getGraph().v(typeInfo.owlClass).in(RDF.TYPE).toVertexList();
			for (Vertex v : list) {
				if (v.getId() instanceof URI) {
					URI id = (URI) v.getId();
					
					JClass uriImplClass = model.ref(URIImpl.class);
					
					String name = id.getLocalName();
					JVar field = typeInfo.interfaceClass
						.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, typeInfo.interfaceClass, name)
						.init(JExpr._new(typeInfo.implClass).arg(JExpr._new(uriImplClass).arg(JExpr.lit(id.stringValue()))));
					
					Namespace ns = nsManager.findByName(id.getNamespace());
					if (ns == null) {
						throw new SchemaGeneratorException("Prefix not found for namespace " + id.getNamespace());
					}
					StringBuilder curie = new StringBuilder();
					curie.append(ns.getPrefix());
					curie.append(':');
					curie.append(id.getLocalName());
					factorySwitch._case(JExpr.lit(id.stringValue()));
					factorySwitch._case(JExpr.lit(curie.toString()));
					factorySwitch._case(JExpr.lit(id.getLocalName())).body()._return(field);
				}
			}
			
			JClass exceptionClass = model.ref(IllegalArgumentException.class);
			
			valueOf.body()._throw(JExpr._new(exceptionClass).arg(JOp.plus(JExpr.lit("Invalid name: "), nameVar)));
			
			
		}
		
	}
	

	private void createConstructors(JCodeModel model, TypeInfo typeInfo) {

		typeInfo.implClass.constructor(JMod.PUBLIC);
		
		JMethod ctor = typeInfo.implClass.constructor(JMod.PUBLIC);
		JVar idVar = ctor.param(URI.class, "id");
		
		ctor.body().invoke("super").arg(idVar);
		
	}

	private void declareSuperClasses(TypeInfo typeInfo, List<TypeInfo> superList) {
		
		JDefinedClass dc = typeInfo.implClass;
		JDefinedClass dci = typeInfo.interfaceClass;
		
		if (superList.size()==1) {
			TypeInfo superInfo = superList.get(0);
			dc._extends(superInfo.implClass);
		} else if (superList.isEmpty()) {
			dc._extends(owlThing.implClass);
			dci._extends(owlThing.interfaceClass);
		}
		
		for (TypeInfo superInfo : superList) {
			dci._extends(superInfo.interfaceClass);
		}
		
	}
	
	private Shape getClassShape(Resource owlClass) {
		return hierarchy.getShapeForClass(owlClass);
	}

	private List<TypeInfo> superClassList(JCodeModel model, TypeInfo targetClass) {
		List<TypeInfo> list = new ArrayList<>();
		
		
		List<Vertex> vertices = graph.v(targetClass.owlClass).out(RDFS.SUBCLASSOF).toVertexList();
		
		
		List<URI> uriList = new ArrayList<>();
		for (Vertex v : vertices) {
			if (v.getId() instanceof URI) {
				uriList.add((URI)v.getId());
			}
		}
		List<URI> clone = new ArrayList<URI>(uriList);
		Iterator<URI> sequence = uriList.iterator();
		while (sequence.hasNext()) {
			URI a = sequence.next();
			for (URI b : clone) {
				if (a == b) {
					continue;
				}
				if (reasoner.isSubClassOf(b, a)) {
					sequence.remove();
				}
			}
		}
		
		
		for (URI uri : uriList) {

			Shape superShape = getClassShape(uri);
			
			TypeInfo info = buildClass(superShape, model);
			list.add(info);
		}
		
		return list;
	}

	private void createStaticTypeVar(JCodeModel model, TypeInfo pair, URI targetClass) {
		
		JClass uriClass = model.ref(URIImpl.class);
		
		
		
		pair.interfaceClass.field(JMod.STATIC | JMod.PUBLIC | JMod.FINAL, URI.class, "TYPE").init(
			JExpr._new(uriClass).arg(JExpr.lit(targetClass.stringValue())));
	}

	

	private void createFields(JCodeModel model, Shape shape, TypeInfo pair) {
		
		List<PropertyConstraint> list = shape.getProperty();
		for (PropertyConstraint p : list) {
			URI predicate = p.getPredicate();
			if (
				predicate==null ||
				RDF.TYPE.equals(predicate) ||
				Konig.id.equals(predicate) ||
				!filter.acceptIndividual(predicate.stringValue())
			) {
				continue;
			}
			createField(model, p, pair);
		}
		
	}

	private void createField(JCodeModel model, PropertyConstraint p, TypeInfo pair) {
		
		URI datatype = p.getDatatype();
		Resource owlClass = p.getValueClass();
		
//		Shape valueShape = p.getValueShape(classAnalyzer.getShapeManager());
//		
//		if (valueShape != null) {
//			if (owlClass == null) {
//				owlClass = valueShape.getTargetClass();
//			}
//		}
		
		FieldDisposition fd = fieldDisposition(p, pair);
		if (fd == FieldDisposition.INHERITED_FIELD) {
			return;
		}
		
		if (RDF.LANGSTRING.equals(datatype)) {
			// TODO: implement rdf:langString value
		} else if (datatype != null) {
			createDatatypeField(model, p, pair, fd);
		} else if (owlClass instanceof URI) {
			createObjectField(model, p, pair, fd);
		}
		
		
	}

	private FieldDisposition fieldDisposition(PropertyConstraint p, TypeInfo pair) {
		FieldDisposition fd = FieldDisposition.NEW_FIELD;
		List<URI> stack = new ArrayList<>(reasoner.superClasses(pair.owlClass));
		if (!stack.contains(OWL.THING)) {
			stack.add(OWL.THING);
		}
		URI predicate = p.getPredicate();
		URI pDatatype = p.getDatatype();
		Resource pValueClass = p.getValueClass();
		
		for (int i=0; i<stack.size(); i++) {
			URI superType = stack.get(i);
			Shape superShape = hierarchy.getShapeForClass(superType);
			if (superShape != null) {
				PropertyConstraint q = superShape.getPropertyConstraint(predicate);
				if (q != null) {
					URI qDatatype = q.getDatatype();
					Resource qValueClass = q.getValueClass();
					if (qDatatype != null) {
						if (qDatatype.equals(pDatatype)) {
							fd = FieldDisposition.INHERITED_FIELD;
						} else {
							return FieldDisposition.RESTRICTED_FIELD;
						}
					} else if (qValueClass != null) {
						if (qValueClass.equals(pValueClass)) {
							fd = FieldDisposition.INHERITED_FIELD;
						} else {
							return FieldDisposition.RESTRICTED_FIELD;
						}
					}
				}
			}
			Set<URI> set = reasoner.superClasses(superType);
			for (URI type : set) {
				if (!stack.contains(type)) {
					stack.add(type);
				}
			}
		}
		
		return fd;
	}


	private void createObjectField(JCodeModel model, PropertyConstraint p, TypeInfo pair, FieldDisposition fd) {
		
		URI predicate = p.getPredicate();
		
		if (RDF.TYPE.equals(predicate)) {
			return;
		}
		URI owlClass = (URI) p.getValueClass();
		
		JClass jClass = null;
		
		TypeInfo valueType = null;
		
		if (RDF.TYPE.equals(predicate)) {
			jClass = model.ref(URI.class);
		} else {
			valueType = javaClass(model, owlClass);
			jClass = valueType.interfaceClass;
		}
				
		JDefinedClass dc = pair.implClass;
		
		Integer maxCount = p.getMaxCount();
		
		String fieldName = predicate.getLocalName();
		String methodBaseName = fieldName;
		JClass fieldType = jClass;
		
		if (maxCount==null || maxCount>1) {
			
			createAddMethod(model, pair, fieldName, jClass);
			methodBaseName = fieldName;
			JClass listClass = model.ref(Set.class);
			fieldType = listClass.narrow(jClass);
			jClass = listClass.narrow(jClass.wildcard());
			
			
		}

		dc.field(JMod.PRIVATE, fieldType, predicate.getLocalName());
		
		createGetter(pair, methodBaseName, fieldName, jClass);
		if (fd != FieldDisposition.RESTRICTED_FIELD) {
			createSetter(pair, methodBaseName, fieldName, jClass, fieldType);
		}
	}

	private void createAddMethod(JCodeModel model, TypeInfo declaringClass, String fieldName, JClass jClass) {
		String methodName = addMethodName(fieldName);
		JFieldRef field = JExpr.ref(fieldName);
		
		JClass listClass = model.ref(LinkedHashSet.class);
		listClass = listClass.narrow(jClass);
		
		JMethod method = declaringClass.implClass.method(JMod.PUBLIC, void.class, methodName);
		JVar param = method.param(jClass, "value");
		JBlock body = method.body();
		
		JMethod imethod = declaringClass.interfaceClass.method(JMod.NONE, void.class, methodName);
		imethod.param(jClass, "value");
		
		
		body._if(field.eq(JExpr._null()))._then().assign(field, JExpr._new(listClass));
		body.add(field.invoke("add").arg(param));
		
		
	}



	private TypeInfo javaClass(JCodeModel model, URI owlClass) {
		
		String className = namer.javaClassName(owlClass);
		String interfaceName = namer.javaInterfaceName(owlClass);
		
		JDefinedClass dc = model._getClass(className);
		
		TypeInfo result = null;
		if (dc == null) {
			Shape shape = getClassShape(owlClass);
			result = buildClass(shape, model);
		} else {
			JDefinedClass dci = model._getClass(interfaceName);
			result = new TypeInfo(owlClass, dc, dci);
		}
		return result;
	}

	private void createDatatypeField(JCodeModel model, PropertyConstraint p, TypeInfo pair, FieldDisposition fd) {
		
		URI predicate = p.getPredicate();
		
		URI datatype = p.getDatatype();
		
		String fieldName = predicate.getLocalName();
		String methodBaseName = fieldName;
		
		JDefinedClass dc = pair.implClass;
		
		Class<?> javaType = mapper.javaDatatype(datatype);
		if (javaType == null) {
			throw new RuntimeException("Java datatype not defined: " + datatype.stringValue() + " for property " + p.getPredicate().getLocalName() + " on " + dc.fullName());
		}
		JClass jClass = model.ref(javaType);
		JClass fieldType = jClass;
		Integer maxCount = p.getMaxCount();
		
		if (maxCount==null || maxCount>1) {
			
			
			createAddMethod(model, pair, fieldName, jClass);
			methodBaseName = fieldName;
			
			JClass listClass = model.ref(Set.class);
			fieldType = listClass.narrow(jClass);
			jClass = listClass.narrow(jClass.wildcard());
		}

		dc.field(JMod.PRIVATE, fieldType, predicate.getLocalName());
		
		createGetter(pair, methodBaseName, fieldName, jClass);
		if (fd != FieldDisposition.RESTRICTED_FIELD) {
			createSetter(pair, methodBaseName, fieldName, jClass, fieldType);
		}

	}

	
	private void createSetter(TypeInfo declaringClass, String methodBaseName, String fieldName, JClass javaType, JClass fieldType) {
		
		String methodName = setterName(methodBaseName);
		JMethod imethod = declaringClass.interfaceClass.method(JMod.NONE, void.class, methodName);
		imethod.param(javaType, fieldName);
		
		JMethod method = declaringClass.implClass.method(JMod.PUBLIC, void.class, methodName);
		JExpression param = method.param(javaType, fieldName);
		if (fieldType != javaType) {
			param = JExpr.cast(fieldType, param);
		}
		method.body().assign(JExpr._this().ref(fieldName), param);
	}

	private String setterName(String fieldName) {
		StringBuilder builder = new StringBuilder("set");
		capitalize(builder, fieldName);
		return builder.toString();
	}

	
	private void createTypeGetter(JCodeModel model, TypeInfo pair) {
		
		JClass setClass = model.ref(Set.class);
		JClass uriSetClass = setClass.narrow(URI.class);
		
		JDefinedClass dc = pair.implClass;
		JFieldRef typeRef = pair.interfaceClass.staticRef("TYPE");
		JMethod method = dc.method(JMod.PUBLIC, URI.class, "getType");
		method.body()._return(typeRef);
		
		JFieldRef typeSetField = pair.interfaceClass.staticRef("TYPES");
		
		dc.method(JMod.PUBLIC, uriSetClass, "getAllTypes")
			.body()._return(typeSetField);
	}
	
	


	private void createGetter(TypeInfo declaringClass, String methodBaseName, String fieldName, JClass returnType) {
		
		String methodName = getterName(methodBaseName);
		declaringClass.interfaceClass.method(JMod.NONE, returnType, methodName);
		JMethod method = declaringClass.implClass.method(JMod.PUBLIC, returnType, methodName);
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

	private static class TypeInfo {
		
		URI owlClass;
		JDefinedClass implClass;
		JDefinedClass interfaceClass;
		
		public TypeInfo(URI owlClass, JDefinedClass implClass, JDefinedClass interfaceClass) {
			this.owlClass = owlClass;
			this.implClass = implClass;
			this.interfaceClass = interfaceClass;
		}
	}
	
	private static enum FieldDisposition {
		NEW_FIELD,
		RESTRICTED_FIELD,
		INHERITED_FIELD
	}

}
