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


import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.pojo.BeanUtil;
import io.konig.core.util.BasicJavaDatatypeMapper;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.util.SimpleValueFormat;
import io.konig.runtime.io.BaseJsonWriter;
import io.konig.runtime.io.ValidationException;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.NodeKind;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.ClassAnalyzer;

public class JsonWriterBuilder {
	
	private ShapeManager shapeManager;
	private JavaNamer javaNamer;
	private JavaDatatypeMapper datatypeMapper;
	private OwlReasoner owlReasoner;
	private ClassStructure classStructure;
	private ClassAnalyzer classAnalyzer;
	private Filter filter;
	
	
	
	public JsonWriterBuilder(OwlReasoner owlReasoner, ShapeManager shapeManager, JavaNamer javaNamer) {
		this(null, owlReasoner, shapeManager, javaNamer, new Filter(null));
	}

	public JsonWriterBuilder(ClassStructure classStructure, OwlReasoner owlReasoner, ShapeManager shapeManager, JavaNamer javaNamer, Filter filter) {
		if (classStructure == null) {
			SimpleValueFormat iriTemplate = new SimpleValueFormat("http://example.com/shapes/canonical/{targetClassNamespacePrefix}/{targetClassLocalName}");
			classStructure = new ClassStructure(iriTemplate, shapeManager, owlReasoner);
		}
		this.owlReasoner = owlReasoner==null ? new OwlReasoner(new MemoryGraph()) : owlReasoner;
		this.shapeManager = shapeManager;
		this.javaNamer = javaNamer;
		datatypeMapper = new BasicJavaDatatypeMapper();
		classAnalyzer = new ClassAnalyzer(shapeManager, owlReasoner);
		this.filter = filter;
		this.classStructure = classStructure;
	}


	public void buildAll(Collection<Shape> list, JCodeModel model) throws SchemaGeneratorException {
		for (Shape shape : list) {
			
			if (shape.getId() instanceof URI && shape.getTargetClass() instanceof URI && filter.acceptShape(shape)) {
				buildJsonWriter(shape, model);
			}
		}
	}


	public JDefinedClass buildJsonWriter(Shape shape, JCodeModel model) throws SchemaGeneratorException {

		Resource shapeId = shape.getId();
		if (!(shapeId instanceof URI)) {
			throw new SchemaGeneratorException("Shape must have a URI id");
		}
		
		URI shapeURI = (URI) shapeId;
		
		URI targetClassId = shape.getTargetClass();
		if (targetClassId == null) {
			throw new SchemaGeneratorException("Target class not defined for shape: " + shape.getId());
		}
		
		String targetClassName = javaNamer.javaInterfaceName(targetClassId);
		
		JType targetClass = model.ref(targetClassName);
		
		
		
		String javaClassName = javaNamer.writerName(shapeURI, Format.JSON);
		
		JDefinedClass dc = model._getClass(javaClassName);
		if (dc == null) {
			try {
				dc = model._class(javaClassName);
				dc._extends(BaseJsonWriter.class);
				
				JVar instanceField = dc.field(JMod.PRIVATE | JMod.STATIC, dc, "INSTANCE");
				instanceField.init(JExpr._new(dc));
				
				createGetInstanceMethod(model, dc, instanceField);
				createWriteMethod(model, dc, shape, targetClass);
				
			} catch (Throwable e) {
				throw new SchemaGeneratorException("Failed to generate JsonWriter for shape: " + shapeURI, e);
			}
		}
		
		
		return dc;
	}




	private void createGetInstanceMethod(JCodeModel model, JDefinedClass dc, JVar instanceField) {
		
		dc.method(JMod.PUBLIC | JMod.STATIC, dc, "instance").body()._return(instanceField);
		
	}




	private void createWriteMethod(JCodeModel model, JDefinedClass dc, Shape shape, JType targetClass) throws ValidationException {
		
		URI owlClass = shape.getTargetClass();
		Shape canonicalShape = classStructure.getShapeForClass(owlClass);

		JMethod method = dc.method(JMod.PUBLIC, void.class, "write");
		method._throws(ValidationException.class);
		method._throws(IOException.class);
		
		JType objectClass = model._ref(Object.class);
		JType jsonGeneratorClass = model._ref(JsonGenerator.class);
		JType validationExceptionClass = model._ref(ValidationException.class);
		
		JVar dataVar = method.param(objectClass, "objectParam");
		JVar jsonVar = method.param(jsonGeneratorClass, "jsonGenerator");
		
		JBlock body = method.body();
		
		
		StringBuilder builder = new StringBuilder();
		builder.append("The data parameter must be an object of type " + targetClass.fullName());
		String message = builder.toString();
		
		body._if(dataVar._instanceof(targetClass).not())._then()._throw(
			JExpr._new(validationExceptionClass).arg(JExpr.lit(message)));

		JVar sourceVar = body.decl(targetClass, "sourceVar").init(JExpr.cast(targetClass, dataVar));
		
		
		body.invoke(jsonVar, "writeStartObject");
		
		handleIdProperty(model, body, shape, sourceVar, jsonVar);
		
		List<PropertyConstraint> propertyList = shape.getProperty();
		for (PropertyConstraint p : propertyList) {
			
			URI predicate = p.getPredicate();
			if (!filter.acceptIndividual(predicate.stringValue())) {
				continue;
			}
			
			PropertyConstraint q = canonicalShape.getPropertyConstraint(predicate);
			
			String fieldName = predicate.getLocalName();
			String getterName = BeanUtil.getterName(predicate);
			URI datatype = p.getDatatype();
			Shape valueShape = p.getShape(shapeManager);
			int pMax = p.getMaxCount()==null ? Integer.MAX_VALUE : p.getMaxCount();
			int qMax = q.getMaxCount()==null ? Integer.MAX_VALUE : q.getMaxCount();
			NodeKind nodeKind = p.getNodeKind();
			
			if (XMLSchema.ANYURI.equals(datatype)) {

				if (p.getValueClass()==null) {
					anyURI(model, body, p, sourceVar, jsonVar);
				} else {
					handleIriReference(model, body, p, sourceVar, jsonVar);
				}
				
			} else 	if (datatype != null) {
				
				Class<?> javaType = datatypeMapper.javaDatatype(datatype);
				JClass propertyType = model.ref(javaType);
				
				JacksonType jacksonType = JacksonType.type(datatype);
				
				if (pMax==1 && qMax==1) {

					JVar fieldValue = body.decl(propertyType, fieldName).init(
						JExpr._this().invoke("value").arg(sourceVar.invoke(getterName)).arg(propertyType.staticRef("class"))
					);					
					
					body._if(fieldValue.ne(JExpr._null()))._then()
						.add(jacksonType.writeTypeField(model, jsonVar, fieldName, fieldValue));
					
				} else if (pMax>1 && qMax>1) {
					
					JClass collectionClass = model.ref(Collection.class);
					JClass narrowCollection = collectionClass.narrow(propertyType.wildcard());
					
					String setName = fieldName + "Set";
					
					JVar property = body.decl(narrowCollection, setName).init(sourceVar.invoke(getterName));
					
					JBlock thenBlock = body._if(property.ne(JExpr._null()))._then();
					
					JForEach forEach = thenBlock
						.add(jsonVar.invoke("writeArrayFieldStart").arg(JExpr.lit(fieldName)))
						.forEach(propertyType, fieldName, property);
					
					JVar fieldValue = forEach.var();
					
					forEach.body()
						.add(jacksonType.writeType(model, jsonVar, fieldValue));
					
					thenBlock.add(jsonVar.invoke("writeEndArray"));
				} else if (pMax==1 && qMax>1) {
					
					JClass collectionClass = model.ref(Collection.class);
					JClass narrowCollection = collectionClass.narrow(propertyType.wildcard());
					JClass iteratorType = model.ref(Iterator.class).narrow(propertyType.wildcard());
					
					String setName = fieldName + "Set";
					String iteratorName = fieldName + "Iterator";
					
					JVar property = body.decl(narrowCollection, setName).init(sourceVar.invoke(getterName));

					JBlock then = body._if(property.ne(JExpr._null()))._then();

					JVar iterator = then.decl(iteratorType, iteratorName).init(property.invoke("iterator"));
					JVar fieldValue = then.decl(propertyType, fieldName + "Value").init(JOp.cond(iterator.invoke("hasNext"), iterator.invoke("next"), JExpr._null()));
					then.add(jacksonType.writeTypeField(model, jsonVar, fieldName, fieldValue));
					
				} else {
					String msg = MessageFormat.format("For predicate {0}, canonical shape has maxCount=1 but shape {1} has maxCount>1", 
						predicate.stringValue(), shape.getId().stringValue());
					throw new ValidationException(msg);
				}
			} else if (valueShape != null) {
				OrConstraint or = valueShape.getOr();
				if (or != null) {
					// TODO: what if pMax and qMax are different?
					handleOrContraint(model, body, sourceVar, jsonVar, p, or, fieldName, getterName, pMax);
					continue;
				}
				if (or==null && !(valueShape.getId() instanceof URI)) {
					throw new SchemaGeneratorException("Id is not defined for shape");
				}
				URI valueShapeId = (URI) valueShape.getId();
				String writerClassName = javaNamer.writerName(valueShapeId, Format.JSON);
				
				JClass writerClass = model.ref(writerClassName);
				
				
				URI valueClass = valueShape.getTargetClass();
				if (valueClass == null) {
					throw new SchemaGeneratorException("Target class is not defined for shape: " + valueShapeId);
				}
				
				String valueClassName = javaNamer.javaInterfaceName(valueClass);
				JClass propertyType = model.ref(valueClassName);
				
				if (pMax==1 && qMax==1) {

					JVar fieldValue = body.decl(propertyType, fieldName).init(sourceVar.invoke(getterName));	

					body._if(fieldValue.ne(JExpr._null()))._then()
						.add(jsonVar.invoke("writeFieldName").arg(JExpr.lit(fieldName)))
						.add(writerClass.staticInvoke("instance").invoke("write").arg(fieldValue).arg(jsonVar));
				} else if (pMax>1 && qMax>1) {
					

					JClass collectionClass = model.ref(Collection.class);
					JClass narrowCollection = collectionClass.narrow(propertyType.wildcard());
					

					String setName = fieldName + "Set";
					
					JVar property = body.decl(narrowCollection, setName).init(sourceVar.invoke(getterName));
					
					JBlock thenBlock = body._if(property.ne(JExpr._null()))._then();
					
					JForEach forEach = thenBlock
						.add(jsonVar.invoke("writeArrayFieldStart").arg(JExpr.lit(fieldName)))
						.forEach(propertyType, fieldName, property);
					
					JVar fieldValue = forEach.var();
					
					forEach.body()
						.add(writerClass.staticInvoke("instance").invoke("write").arg(fieldValue).arg(jsonVar));
					
					thenBlock.add(jsonVar.invoke("writeEndArray"));
					
				} else if (pMax==1 && qMax>1) {
					JClass collectionClass = model.ref(Collection.class);
					JClass narrowCollection = collectionClass.narrow(propertyType.wildcard());
					JClass iteratorType = model.ref(Iterator.class).narrow(propertyType.wildcard());
					
					String setName = fieldName + "Set";
					String iteratorName = fieldName + "Iterator";
					
					JVar property = body.decl(narrowCollection, setName).init(sourceVar.invoke(getterName));

					JBlock then = body._if(property.ne(JExpr._null()))._then();

					JVar iterator = then.decl(iteratorType, iteratorName).init(property.invoke("iterator"));
					JVar fieldValue = then.decl(propertyType, fieldName + "Value").init(JOp.cond(iterator.invoke("hasNext"), iterator.invoke("next"), JExpr._null()));
					then.add(writerClass.staticInvoke("instance").invoke("write").arg(fieldValue).arg(jsonVar));
				} else {

					String msg = MessageFormat.format("For predicate {0}, canonical shape has maxCount=1 but shape {1} has maxCount>1", 
						predicate.stringValue(), shape.getId().stringValue());
					throw new ValidationException(msg);
				}
				
			} else if (nodeKind == NodeKind.IRI) {
				
				handleIriReference(model, body, p, sourceVar, jsonVar);
				
				
			}
		}
		
		body.invoke(jsonVar, "writeEndObject");
		
	}




	private void anyURI(JCodeModel model, JBlock body, PropertyConstraint p, JVar sourceVar, JVar jsonVar) {
		Integer maxCount = p.getMaxCount();
		URI predicate = p.getPredicate();
		String fieldName = predicate.getLocalName();
		String getterName = BeanUtil.getterName(predicate);
		
		JClass propertyType = model.ref(URI.class);
		
		if (maxCount != null && maxCount==1) {

			JVar fieldValue = body.decl(propertyType, fieldName).init(
					
				JExpr._this().invoke("value").arg(sourceVar.invoke(getterName)).arg(propertyType.staticRef("class"))
			);		

			JInvocation statement = jsonVar.invoke("writeStringField").arg(JExpr.lit(fieldName)).arg(fieldValue.invoke("stringValue"));
			
			body._if(fieldValue.ne(JExpr._null()))._then()
				.add(statement);
		} else {
			
			JClass collectionClass = model.ref(Collection.class);
			JClass narrowCollection = collectionClass.narrow(propertyType.wildcard());
			
			String setName = fieldName + "Set";
			
			JVar property = body.decl(narrowCollection, setName).init(sourceVar.invoke(getterName));
			
			JBlock thenBlock = body._if(property.ne(JExpr._null()))._then();
			
			JForEach forEach = thenBlock
				.add(jsonVar.invoke("writeArrayFieldStart").arg(JExpr.lit(fieldName)))
				.forEach(propertyType, fieldName, property);
			
			JVar fieldValue = forEach.var();
			
			JInvocation statement = jsonVar.invoke("writeString").arg(fieldValue.invoke("stringValue"));
			
			forEach.body()
				.add(statement);
			
			thenBlock.add(jsonVar.invoke("writeEndArray"));
		}
		
	}


	private void handleOrContraint(
		JCodeModel model, JBlock body, JVar sourceVar, JVar jsonVar, PropertyConstraint p, OrConstraint or, 
		String fieldName, String getterName, Integer maxCount) {
		
		Resource fieldType = classAnalyzer.mergeTargetClass(or.getShapes());

		if (!(fieldType instanceof URI)) {
			return;
		}
		
		String valueClassName = javaNamer.javaInterfaceName((URI)fieldType);
		JClass propertyType = model.ref(valueClassName);
		
		if (maxCount != null && maxCount==1) {

			JVar fieldValue = body.decl(propertyType, fieldName).init(sourceVar.invoke(getterName));	
			
			JBlock notNull = body._if(fieldValue.ne(JExpr._null()))._then();
			
			notNull.add(jsonVar.invoke("writeFieldName").arg(JExpr.lit(fieldName)));
			
			
			
			JConditional conditional = null;
			
			for (Shape s : or.getShapes()) {
				
				URI targetClass = s.getTargetClass();
				if (targetClass == null) {
					throw new SchemaGeneratorException("Target Class is not defined on Shape: " + s.getId());
				}
				JClass classOption = model.ref(javaNamer.javaInterfaceName(targetClass));
				
				JExpression instanceOfClass = fieldValue._instanceof(classOption);
				if (conditional == null) {
					conditional = notNull._if(instanceOfClass);
				} else {
					conditional = conditional._elseif(instanceOfClass);
				}

				URI valueShapeId = (URI) s.getId();
				String writerClassName = javaNamer.writerName(valueShapeId, Format.JSON);
				
				JClass writerClass = model.ref(writerClassName);
				
				conditional._then()
					
					.add(writerClass.staticInvoke("instance").invoke("write")
						.arg(fieldValue).arg(jsonVar));
				
				
			}
		} else {

			JClass setType = model.ref(Set.class).narrow(propertyType.wildcard());
			
			JVar setVar = body.decl(setType, fieldName + "Set").init(sourceVar.invoke(getterName));	
			
			JBlock notNull = body._if(setVar.ne(JExpr._null()))._then();

			notNull.add(jsonVar.invoke("writeArrayFieldStart").arg(JExpr.lit(fieldName)));
			
			JForEach forEach = notNull.forEach(propertyType, fieldName, setVar);
			JVar elemVar = forEach.var();
			JBlock forEachBody = forEach.body();
					
			JConditional conditional = null;
			
			for (Shape s : or.getShapes()) {
				
				URI targetClass = s.getTargetClass();
				if (targetClass == null) {
					throw new SchemaGeneratorException("Target Class is not defined on Shape: " + s.getId());
				}
				JClass classOption = model.ref(javaNamer.javaInterfaceName(targetClass));
				
				JExpression instanceOfClass = elemVar._instanceof(classOption);
				if (conditional == null) {
					conditional = forEachBody._if(instanceOfClass);
				} else {
					conditional = conditional._elseif(instanceOfClass);
				}

				URI valueShapeId = (URI) s.getId();
				String writerClassName = javaNamer.writerName(valueShapeId, Format.JSON);
				
				JClass writerClass = model.ref(writerClassName);
				
				conditional._then()
					
					.add(writerClass.staticInvoke("instance").invoke("write")
						.arg(elemVar).arg(jsonVar));
				
				
			}
			notNull.add(jsonVar.invoke("writeEndArray"));
			
		}
		
	}


	private void handleIdProperty(JCodeModel model, JBlock body, Shape shape, JVar sourceVar, JVar jsonVar) {
		
		
		if (shape.getNodeKind() == NodeKind.IRI) {
			URI targetClass = shape.getTargetClass();
			JBlock then = body._if(sourceVar.invoke("getId").ne(JExpr._null()))._then();
			
			String methodName = owlReasoner.isEnumerationClass(targetClass) ? "getLocalName" : "stringValue";
			
			then.add(
				jsonVar.invoke("writeStringField").arg(JExpr.lit("id")).arg(
					sourceVar.invoke("getId").invoke(methodName))
			);
			
		}
		
	}


	private void handleIriReference(JCodeModel model, JBlock body, PropertyConstraint p, JVar sourceVar, JVar jsonVar) throws ValidationException {

		URI predicate = p.getPredicate();
		if (RDF.TYPE.equals(predicate)) {
			handleTypeProperty(model, body, p, sourceVar, jsonVar);
		} else {
			if (!(p.getValueClass() instanceof URI)) {
				throw new ValidationException("Value class is not a URI for property " + predicate.stringValue());
			}
			String fieldName = predicate.getLocalName();
			String getterName = BeanUtil.getterName(predicate);
			URI valueClassId = (URI) p.getValueClass();
			String valueClassName = javaNamer.javaInterfaceName(valueClassId);
			
			JClass fieldType = model.ref(valueClassName);

			String methodName = owlReasoner.isEnumerationClass(valueClassId) ? "getLocalName" : "stringValue";
			Integer maxCount = p.getMaxCount();
			if (maxCount!=null && maxCount==1) {
				
				JVar fieldValue = body.decl(fieldType, fieldName).init(
					JExpr._this().invoke("value").arg(sourceVar.invoke(getterName)).arg(fieldType.staticRef("class"))
				);		
				
				
				body.add(jsonVar.invoke("writeStringField").arg(JExpr.lit(fieldName))
					.arg(fieldValue.invoke("getId").invoke(methodName)));
				
			} else {
				
				JClass setClass = model.ref(Set.class);
				JClass narrowSetClass = setClass.narrow(fieldType.wildcard());
				
				JVar setVar = body.decl(narrowSetClass, fieldName + "Set").init(sourceVar.invoke(getterName));
				body.add(jsonVar.invoke("writeArrayFieldStart").arg(JExpr.lit(fieldName)));
				
				JBlock thenBlock = body._if(setVar.ne(JExpr._null()))._then();
				
				JForEach forEach = thenBlock.forEach(fieldType, fieldName, setVar);
				JVar valueVar = forEach.var();
				forEach.body().add(jsonVar.invoke("writeString").arg(valueVar.invoke("getId").invoke(methodName)));
				
				body.add(jsonVar.invoke("writeEndArray"));
				
			}
			
		}
		
		
	}




	private void handleTypeProperty(JCodeModel model, JBlock body, PropertyConstraint p, JVar objectVar, JVar jsonVar) {
		
		JClass uriType = model.ref(URI.class);
		Integer maxCount = p.getMaxCount();
		if (maxCount!=null && maxCount==1) {
			
			JVar fieldValue = body.decl(uriType, "type").init(objectVar.invoke("getType"));	
			body.add(jsonVar.invoke("writeStringField").arg(JExpr.lit("type")).arg(fieldValue.invoke("getLocalName")));
			
		} else {
			
			JClass setClass = model.ref(Set.class);
			JClass uriSetClass = setClass.narrow(URI.class);
			
			JVar setVar = body.decl(uriSetClass, "type").init(objectVar.invoke("getAllTypes"));
			body.add(jsonVar.invoke("writeArrayFieldStart").arg(JExpr.lit("type")));
			JForEach forEach = body.forEach(uriType, "typeId", setVar);
			JVar typeId = forEach.var();
			forEach.body().add(jsonVar.invoke("writeString").arg(typeId.invoke("getLocalName")));
			
			body.add(jsonVar.invoke("writeEndArray"));
		}
		
	}



}
