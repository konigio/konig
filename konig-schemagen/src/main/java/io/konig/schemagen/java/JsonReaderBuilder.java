package io.konig.schemagen.java;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCase;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JLabel;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JSwitch;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.codemodel.JWhileLoop;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.Dag;
import io.konig.core.impl.RdfUtil;
import io.konig.core.pojo.BeanUtil;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.vocab.Schema;
import io.konig.runtime.io.BaseJsonReader;
import io.konig.runtime.io.ValidationException;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.NodeKind;
import io.konig.shacl.OrConstraint;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class JsonReaderBuilder {

	private ClassStructure hierarchy;
	private JavaNamer javaNamer;
	private JavaDatatypeMapper datatypeMapper;
	private OwlReasoner owlReasoner;
	
	
	public JsonReaderBuilder(ClassStructure hierarchy, JavaNamer javaNamer, JavaDatatypeMapper datatypeMapper,
			OwlReasoner owlReasoner) {
		this.hierarchy = hierarchy;
		this.javaNamer = javaNamer;
		this.datatypeMapper = datatypeMapper;
		this.owlReasoner = owlReasoner;
		applyOrdering();
	}
	
	private void applyOrdering() {
		
		Dag dag = new Dag();
		Shape thing = hierarchy.getShapeForClass(OWL.THING);
		buildDag(dag, thing);
		
		List<Resource> sequence = dag.sort();
		
		for (int i=0; i<sequence.size(); i++) {
			Resource classId = sequence.get(i);
			Shape shape = hierarchy.getShapeForClass(classId);
			shape.setOrdinal(i);
		}
	}

	private void buildDag(Dag dag, Shape shape) {
		URI subject = shape.getTargetClass();
		
		OrConstraint or = shape.getOr();
		if (or != null) {
			for (Shape subclass : or.getShapes()) {
				if (!hierarchy.isNullShape(subclass)) {
					URI object = subclass.getTargetClass();
					dag.addEdge(subject, object);
					buildDag(dag, subclass);
				}
			}
		}
		
	}

	public void buildAll(Collection<Shape> list, JCodeModel model) throws SchemaGeneratorException {
		for (Shape shape : list) {
			if (shape.getId() instanceof URI && shape.getTargetClass() instanceof URI) {
				buildJsonReader(shape, model);
			}
		}
	}
	
	public void produceAll(JCodeModel model) throws SchemaGeneratorException {
		for (Resource classId : hierarchy.listClasses()) {
			if (OWL.THING.equals(classId) || Schema.Thing.equals(classId)) {
				continue;
			}
			if (classId instanceof URI) {
				produceJsonReader((URI) classId, model);
			}
		}
	}
	
	public JDefinedClass produceJsonReader(URI targetClassId, JCodeModel model) throws SchemaGeneratorException {
		Shape shape = hierarchy.getShapeForClass(targetClassId);
		if (shape == null) {
			throw new SchemaGeneratorException("Class not found: <" + targetClassId + ">");
		}
		
		if (!(shape.getId() instanceof URI)) {
			throw new SchemaGeneratorException("URI not defined for logical shape for OWL class <" + targetClassId + ">");
		}
		URI shapeURI = (URI) shape.getId();

		String targetClassName = javaNamer.javaInterfaceName(targetClassId);

		JType targetInterface = model.ref(targetClassName);
		JClass targetClass = model.ref(javaNamer.javaClassName(targetClassId));

		String javaClassName = javaNamer.canonicalReaderName(targetClassId, Format.JSON);
		
		JDefinedClass dc = model._getClass(javaClassName);
		if (dc == null) {

			try {
				dc = model._class(javaClassName);
				JClass baseClass = model.ref(BaseJsonReader.class).narrow(targetInterface);
				dc._extends(baseClass);
				
				
				JVar instanceField = dc.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, dc, "INSTANCE");
				instanceField.init(JExpr._new(dc));
				
				createGetInstanceMethod(model, dc, instanceField);

				declareReadMethod(model, dc, shape, targetInterface, targetClass);
				
			} catch (Throwable e) {
				throw new SchemaGeneratorException("Failed to generate JsonWriter for shape: " + shapeURI, e);
			}
		}
		
		return dc;
	}

	private Map<URI, JVar> declareUpperProperties(JCodeModel model, JBlock body, Shape shape) {
		List<PropertyConstraint> upperProperties = hierarchy.getProperties(shape);
		Collections.sort(upperProperties, new Comparator<PropertyConstraint>() {

			@Override
			public int compare(PropertyConstraint a, PropertyConstraint b) {
				return a.getPredicate().getLocalName().compareTo(b.getPredicate().getLocalName());
			}
		});
		
		Map<URI, JVar> map = new HashMap<>();
		for (PropertyConstraint p : upperProperties) {
			URI predicate = p.getPredicate();
			if (RDF.TYPE.equals(predicate)) {
				continue;
			}
			
			if (predicate != null) {

				JVar var = declareField(model, body, p);
				map.put(predicate,  var);
			}
			
		}
		return map;
	}
	
	private JVar declareField(JCodeModel model, JBlock body, PropertyConstraint p) {
		URI predicate = p.getPredicate();
		boolean isFunctional = p.getMaxCount()!=null && p.getMaxCount()==1;
		JVar var = null;
		if (p.getDatatype() != null) {
			URI datatype = p.getDatatype();
			Class<?> javaType = datatypeMapper.javaDatatype(datatype);
			
			JType type = model._ref(javaType);
			var = body.decl(type,  predicate.getLocalName()).init(JExpr._null());
		} else {
			Resource valueClass = p.getValueClass();
			if (valueClass == null) {
				Shape valueShape = p.getShape();
				if (valueShape!=null) {
					valueClass = valueShape.getTargetClass();
				}
			}
			if (valueClass == null) {
				throw new KonigException("One of sh:datatype, sh:class, sh:shape must be defined on property <" + predicate + ">");
			}

			String javaTypeName = javaNamer.javaInterfaceName((URI)valueClass);
			JClass elementType = model.ref(javaTypeName);
			if (isFunctional) {
				var = body.decl(elementType, predicate.getLocalName()).init(JExpr._null());
			} else {
				var = declareSet(model, body, predicate, elementType, true);
			}
		}
		return var;
	}


	private void addSubclassProperties(JCodeModel model, JSwitch fieldSwitch, JVar jsonParser, Shape shape, 
			Map<URI, JVar> properties, Map<URI, AmbiguousProperty> ambiguousProperties, JVar pojoVar, JLabel outer) {

		
		JClass tokenType = model.ref(JsonToken.class);
		NamespaceManager nsManager = owlReasoner.getGraph().getNamespaceManager();
		OrConstraint or = shape.getOr();
		if (or != null) {
			for (Shape subclass : or.getShapes()) {
				JCase fieldCase = null;
				List<PropertyConstraint> propertyList = new ArrayList<>(subclass.getProperty());
				RdfUtil.sortByLocalName(propertyList);
				for (PropertyConstraint p : propertyList) {
					URI predicate = p.getPredicate();
					if (RDF.TYPE.equals(predicate)) {
						continue;
					}
					AmbiguousProperty ambiguous = ambiguousProperties.get(predicate);
					if (ambiguous!=null) {

						if (!ambiguous.isHandled()) {
							ambiguous.setHandled(true);
							addPropertyCase(model, pojoVar, properties, fieldSwitch, tokenType, shape, p, true, jsonParser);
						}
						continue;
					}
					
					fieldSwitch._case(JExpr.lit(predicate.stringValue()));
					Namespace ns = nsManager.findByName(predicate.getNamespace());
					if (ns != null) {
						StringBuilder curie = new StringBuilder();
						curie.append(ns.getPrefix());
						curie.append(':');
						curie.append(predicate.getLocalName());
						fieldSwitch._case(JExpr.lit(curie.toString()));
					}
					String fieldName = predicate.getLocalName();
					fieldCase = fieldSwitch._case(JExpr.lit(fieldName));
				}
				if (fieldCase != null) {
					URI targetClass = subclass.getTargetClass();
					JDefinedClass reader = produceJsonReader(targetClass, model);
					JInvocation parserInvoke = reader.staticInvoke("instance").invoke("read").arg(jsonParser);
					fieldCase.body().assign(pojoVar, parserInvoke);
					fieldCase.body()._break(outer);
				}

				addSubclassProperties(model, fieldSwitch, jsonParser, subclass, properties, ambiguousProperties, pojoVar, outer);
			}
		}
		
	}

	private void declareClassCodes(JCodeModel model, JDefinedClass dc, Shape targetClass) {
		
		if (hierarchy.hasSubClass(targetClass)) {
			for (Shape subtype : targetClass.getOr().getShapes()) {
				if (!hierarchy.isNullShape(subtype)) {
					declareClassCodeVar(model, dc, subtype);
					declareClassCodes(model, dc, subtype);
				}
			}
		}
		
	}

	private void declareClassCodeVar(JCodeModel model, JDefinedClass dc, Shape shape) {
		
		String classCodeName = classCodeName(shape.getTargetClass());
		dc.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, model.INT, classCodeName).init(JExpr.lit(shape.getOrdinal()));
	}

	private String classCodeName(URI targetClass) {
		
		StringBuilder builder = new StringBuilder();
		builder.append(targetClass.getLocalName());
		builder.append("Code");
		
		return builder.toString();
	}

	public JDefinedClass buildJsonReader(Shape shape, JCodeModel model) {
		Resource shapeId = shape.getId();
		if (!(shapeId instanceof URI)) {
			throw new SchemaGeneratorException("Shape must have a URI id");
		}
		
		URI shapeURI = (URI) shapeId;
		
		URI targetClassId = shape.getTargetClass();
		if (targetClassId == null) {
			throw new SchemaGeneratorException("Target class not defined for shape: " + shape.getId());
		}
		
		String targetInterfaceName = javaNamer.javaInterfaceName(targetClassId);

		JType targetInterface = model.ref(targetInterfaceName);
		JClass targetClass = model.ref(javaNamer.javaClassName(targetClassId));
		
		String javaClassName = javaNamer.canonicalReaderName(targetClassId, Format.JSON);
		
		JDefinedClass dc = model._getClass(javaClassName);
		if (dc == null) {
			try {
				dc = model._class(javaClassName);
				JClass baseClass = model.ref(BaseJsonReader.class).narrow(targetInterface);
				dc._extends(baseClass);
				
				
				JVar instanceField = dc.field(JMod.PRIVATE | JMod.STATIC, dc, "INSTANCE");
				instanceField.init(JExpr._new(dc));
				
				createGetInstanceMethod(model, dc, instanceField);
				declareReadMethod(model, dc, shape, targetInterface, targetClass);
				
			} catch (Throwable e) {
				throw new SchemaGeneratorException("Failed to generate JsonWriter for shape: " + shapeURI, e);
			}
		}
		
		
		return dc;
		
	}
	


	private void addParseTypeMethod(JCodeModel model, JDefinedClass dc, URI targetClassId, Shape shape) {
		NamespaceManager nsManager = owlReasoner.getGraph().getNamespaceManager();
		String pojoTypeName = javaNamer.javaInterfaceName(targetClassId);
		JType type = model.ref(pojoTypeName);
		JType jsonParserType = model.ref(JsonParser.class);
		JClass jsonTokenType = model.ref(JsonToken.class);
		JClass mathType = model.ref(Math.class);
		
		JMethod method = dc.method(JMod.PRIVATE, type, "parseType");
		JVar jsonParser = method.param(jsonParserType, "jsonParser");
		method._throws(ValidationException.class)._throws(IOException.class);
		
		// Declare code values for the possible types.
		
		JVar classCode = method.body().decl(model.INT, "classCode").init(JExpr.lit(-1));
		
		
		JVar tokenVar = method.body().decl(jsonTokenType, "token").init(jsonParser.invoke("getCurrentToken"));
		JVar isArrayVar = method.body().decl(model.BOOLEAN, "isArray").init(JExpr.FALSE);
		JConditional ifArray = method.body()._if(JOp.eq(tokenVar, jsonTokenType.staticRef("START_ARRAY")));
		ifArray._then().assign(tokenVar, jsonParser.invoke("nextToken")).assign(isArrayVar, JExpr.TRUE);

		JExpression test = JOp.eq(jsonParser.invoke("getCurrentToken"), jsonTokenType.staticRef("VALUE_STRING"));
		JWhileLoop loop = method.body()._while(test);
		
		JSwitch literalSwitch = loop.body()._switch(jsonParser.invoke("getValueAsString"));
		
		for (Shape s : hierarchy.subClasses(shape)) {
			if (hierarchy.isNullShape(s)) {
				continue;
			}
			URI classId = s.getTargetClass();
			String classCodeVarName = classCodeName(classId);
			
			JVar codeVar = dc.fields().get(classCodeVarName);
		
			
			literalSwitch._case(JExpr.lit(classId.stringValue()));
			Namespace ns = nsManager.findByName(classId.getNamespace());
			if (ns != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(ns.getPrefix());
				builder.append(':');
				builder.append(classId.getLocalName());
				literalSwitch._case(JExpr.lit(builder.toString()));
			}
			JCase caseBlock = literalSwitch._case(JExpr.lit(classId.getLocalName()));
			
			caseBlock.body().assign(classCode, mathType.staticInvoke("max").arg(classCode).arg(codeVar));
			caseBlock.body()._break();
		}
		loop.body()._if(JOp.not(isArrayVar))._then()._break();
		loop.body().assign(tokenVar, jsonParser.invoke("nextToken"));
		
		JSwitch codeSwitch = method.body()._switch(classCode);
		List<URI> subclassSet = hierarchy.listSubclasses(shape);
		for (URI subclass : subclassSet) {
			String codeName = classCodeName(subclass);
			JVar subclassCode = dc.fields().get(codeName);
			
			JCase codeCase = codeSwitch._case(subclassCode);
			JDefinedClass reader = produceJsonReader(subclass, model);
			
			codeCase.body()._return(reader.staticInvoke("instance").invoke("read").arg(jsonParser));
		}
		
		method.body()._return(JExpr._null());

	}



	private Set<URI> filterSubclass(Set<Resource> set, Resource baseClass) {
		Set<URI> result = new HashSet<>();
		for (Resource type : set) {
			if (type instanceof URI && owlReasoner.isSubClassOf(type,  baseClass)) {
				result.add((URI)type);
			}
		}
		return result;
	}

	private void declareReadMethod(JCodeModel model, JDefinedClass dc, Shape shape, JType targetInterface, JClass targetClass) throws SchemaGeneratorException {

		boolean anySubclass = hierarchy.hasSubClass(shape);
		
		if (anySubclass) {
			declareClassCodes(model, dc, shape);
		}
		
		JMethod method = dc.method(JMod.PUBLIC, targetInterface, "read");
		
		method._throws(ValidationException.class);
		method._throws(IOException.class);
		
		JType jsonParserClass = model._ref(JsonParser.class);

		JVar jsonParser = method.param(jsonParserClass, "jsonParser");
		
		JBlock body = method.body();

		Map<URI, JVar> properties = anySubclass ? declareUpperProperties(model, body, shape) : null;
		Map<URI,AmbiguousProperty> ambiguousProperties = anySubclass ? declareAmbiguousProperties(model, body, shape, properties) : null;
		
		JClass uriType = model.ref(URI.class);
		JClass uriImplType = model.ref(URIImpl.class);
		JVar idVar = anySubclass ? body.decl(uriType, "pojoId").init(JExpr._null()) : null;
		
		JVar pojoVar = body.decl(targetInterface, "pojoVar");
		if (anySubclass) {
			pojoVar.init(JExpr._null());
		} else {
			pojoVar.init(JExpr._new(targetClass));
		}
		
		JClass tokenType = model.ref(JsonToken.class);

		JExpression nullToken = JOp.eq(jsonParser.invoke("getCurrentToken"), JExpr._null());
		body._if(nullToken)._then().add(jsonParser.invoke("nextToken"));
		
		JExpression startObject = JOp.eq(jsonParser.invoke("getCurrentToken"), tokenType.staticRef("START_OBJECT"));
		body._if(startObject)._then().add(jsonParser.invoke("nextToken"));
		
		JLabel outer = null;
		if (anySubclass) {
			outer = body.label("outer");
		}
		JWhileLoop loop = body._while(JOp.eq(jsonParser.invoke("getCurrentToken"), tokenType.staticRef("FIELD_NAME")));
		JClass stringType = model.ref(String.class);
		JVar fieldNameVar = loop.body().decl(stringType, "jsonFieldName").init(jsonParser.invoke("getCurrentName"));
		JSwitch switchExpr = loop.body()._switch(fieldNameVar);
		
		JCase idCase = switchExpr._case(JExpr.lit("id"));
		JInvocation idValue = JExpr._new(uriImplType).arg(jsonParser.invoke("getValueAsString"));
		idCase.body().add(jsonParser.invoke("nextToken"));
		if (anySubclass) {
			idCase.body().assign(idVar, idValue);
		} else {
			idCase.body().add(pojoVar.invoke("setId").arg(idValue));
		}
		idCase.body()._break();
		
		for (PropertyConstraint p : hierarchy.getProperties(shape)) {
			addPropertyCase(model, pojoVar, properties, switchExpr, tokenType, shape, p, anySubclass, jsonParser);
		}
		if (anySubclass) {
			JCase typeCase = switchExpr._case(JExpr.lit("type"));
			typeCase.body().add(jsonParser.invoke("nextToken"));
			typeCase.body().assign(pojoVar, JExpr.invoke("parseType").arg(jsonParser));
			typeCase.body()._if(JOp.ne(pojoVar, JExpr._null()))._then()._break(outer);
			typeCase.body()._break();
			
			addSubclassProperties(model, switchExpr, jsonParser, shape, properties, ambiguousProperties, pojoVar, outer);
			
			
		}
		switchExpr._default().body().add(JExpr.invoke("skipField").arg(jsonParser));
		loop.body().add(jsonParser.invoke("nextToken"));
		if (anySubclass) {
			body._if(JOp.eq(pojoVar, JExpr._null()))._then().assign(pojoVar, JExpr._new(targetClass));
			body.add(pojoVar.invoke("setId").arg(idVar));
			for (Entry<URI,JVar> e : properties.entrySet()) {
				URI predicate = e.getKey();
				if (RDF.TYPE.equals(predicate)) {
					continue;
				}
				if(ambiguousProperties.containsKey(predicate)) {
					continue;
				}
				JVar valueVar = e.getValue();
				
				String setterName = BeanUtil.setterName(predicate);
				JInvocation statement = pojoVar.invoke(setterName).arg(valueVar);
				body._if(JOp.ne(valueVar, JExpr._null()))._then().add(statement);
			}
			for (AmbiguousProperty p : ambiguousProperties.values()) {
				URI predicate = p.getPredicate();
				JVar valueVar = p.getValueVar();
				Set<URI> domainIncludes = p.getDomainIncludes();
				String setterName = BeanUtil.setterName(predicate);
				
				JExpression valueNotNull = JOp.ne(valueVar, JExpr._null());
				JBlock thenBlock = body._if(valueNotNull)._then();
				
				for (URI domainId : domainIncludes) {
					String domainTypeName = javaNamer.javaInterfaceName(domainId);
					JClass domainType = model.ref(domainTypeName);
					
					JExpression cast = JExpr.cast(domainType, pojoVar);
					JInvocation statement = cast.invoke(setterName).arg(valueVar);
					thenBlock._if(pojoVar._instanceof(domainType))._then().add(statement);
				}
			}
		}
		body._return(pojoVar);
		

		if (anySubclass) {
			addParseTypeMethod(model, dc, (URI) shape.getTargetClass(), shape);
		}
	}
	
	

	private void addPropertyCase(JCodeModel model, JVar pojoVar, Map<URI, JVar> properties, JSwitch switchExpr, JClass tokenType, 
			Shape shape, PropertyConstraint p, boolean anySubclass, JVar jsonParser) {
		URI predicate = p.getPredicate();
		if (RDF.TYPE.equals(predicate)) {
			return;
		}
	
		if (predicate != null) {
			JCase caseBlock = switchExpr._case(JExpr.lit(predicate.getLocalName()));
			caseBlock.body().add(jsonParser.invoke("nextToken"));
			Integer maxCount = p.getMaxCount();
			boolean isFunctional = maxCount!=null && maxCount==1;
			String setterName = BeanUtil.setterName(predicate);
			URI datatype = p.getDatatype();
			NodeKind nodeKind = p.getNodeKind();
			if (datatype != null && literal(nodeKind)) {
				Class<?> javaType = datatypeMapper.javaDatatype(datatype);
				if (String.class.equals(javaType)) {
					stringValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);

				} else if (Integer.class.equals(javaType)) {
					intValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else if (Boolean.class.equals(javaType)) {
					booleanValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else if (Double.class.equals(javaType)) {
					doubleValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else if (Float.class.equals(javaType)) {
					floatValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else if (Long.class.equals(javaType)) {
					longValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else if (Short.class.equals(javaType)) {
					shortValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else if (GregorianCalendar.class.equals(javaType)) {

					calendar(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, setterName, tokenType);
				} else if (URI.class.equals(javaType)) {
					uriValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, setterName);
				} else if (Byte.class.equals(javaType)) {
					castInt(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, setterName, model.BYTE);
				} else if (Duration.class.equals(javaType)) {
					durationValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else if (LocalTime.class.equals(javaType)) {
					localTimeValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock);
					
				} else {
					// TODO: support other data types
					throw new SchemaGeneratorException("For predicate <" + predicate.stringValue() + ">, unsupported datatype: " + datatype.stringValue());
				}
			} else if (p.getShape()==null) {
				Resource valueClass = p.getValueClass();
				if (valueClass != null) {

					if (!(valueClass instanceof URI)) {
						throw new SchemaGeneratorException("The sh:class value must be an IRI for predicate " + predicate.stringValue());
					}
					URI valueClassId = (URI) valueClass;
					if (owlReasoner.isEnumerationClass(valueClassId)) {
						String enumTypeName = javaNamer.javaInterfaceName(valueClassId);
						JClass enumClass = model.ref(enumTypeName);
						if (isFunctional) {
							JFieldRef factory = enumClass.staticRef("$");
							JInvocation enumValue = factory.invoke("valueOf").arg(jsonParser.invoke("getValueAsString"));
							if (anySubclass) {
								caseBlock.body().assign(properties.get(predicate), enumValue);
							} else {
								caseBlock.body().add(pojoVar.invoke(setterName).arg(enumValue));
							}
						} else {
							throw new SchemaGeneratorException("Collections of enumerated values not supported yet: " + predicate);
						}
					} else {
						iriRef(model, shape.getTargetClass(), predicate, properties, isFunctional, pojoVar, jsonParser,  caseBlock, setterName, tokenType, valueClassId);
						
						
					}
				} else {
					throw new SchemaGeneratorException("Either sh:shape or sh:class must be defined for predicate: " + predicate.stringValue());
				}
			} else {
				Shape valueShape = p.getShape();
				if (valueShape.getId() instanceof URI) {
					URI valueShapeId = (URI) valueShape.getId();
					URI valueClassId = valueShape.getTargetClass();
					if (valueClassId == null) {
						throw new SchemaGeneratorException("Target Class not defined for Shape: " + valueShapeId);
					}
					
					
					JDefinedClass valueReaderClass = buildJsonReader(valueShape, model);
					
					if (isFunctional) {
						JInvocation value = valueReaderClass.staticInvoke("getInstance").invoke("read").arg(jsonParser);
						caseBlock.body().add(pojoVar.invoke(setterName).arg(value));
					} else {
						caseBlock.body().invoke("assertStartArray").arg(jsonParser);
						JVar setVar = declareSet(model, caseBlock.body(), predicate, model._ref(String.class));
						JInvocation statement = pojoVar.invoke(setterName).arg(setVar);
						caseBlock.body().add(statement);
						JExpression test = jsonParser.invoke("nextToken").invoke("equals").arg(tokenType.staticRef("END_ARRAY"));
						JWhileLoop arrayLoop = caseBlock.body()._while(JOp.not(test));
						JInvocation value = valueReaderClass.staticInvoke("getInstance").invoke("read").arg(jsonParser);
						arrayLoop.body().add(setVar.invoke("add").arg(value));
					}
					
					
				} else {
					throw new SchemaGeneratorException("Anonymous Shape not supported as value of property " + predicate);
				}
			}
				
			
			caseBlock.body()._break();
		}
		
	}

	private void durationValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {

		JClass durationType = model.ref(Duration.class);
		JExpression value = durationType.staticInvoke("parse").arg(jsonParser.invoke("getValueAsString"));
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}


	private void localTimeValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {

		JClass fieldType = model.ref(LocalTime.class);
		JExpression value = fieldType.staticInvoke("parse").arg(jsonParser.invoke("getValueAsString"));
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}

	private void floatValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {
		
		JExpression value = JExpr.cast(model.FLOAT, jsonParser.invoke("getValueAsDouble"));
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}

	private void shortValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {
		
		JExpression value = JExpr.cast(model.SHORT, jsonParser.invoke("getValueAsInt"));
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}

	private void longValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {
		
		JExpression value = jsonParser.invoke("getValueAsLong");
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}

	private void booleanValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {
		
		JExpression value = jsonParser.invoke("getValueAsBoolean");
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}


	private void doubleValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {
		
		JExpression value = jsonParser.invoke("getValueAsDouble");
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}

	private void intValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {
		
		JExpression value = jsonParser.invoke("getValueAsInt");
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}

	private void stringValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock) {
		
		JExpression value = jsonParser.invoke("getValueAsString");
		fieldValue(model, predicate, properties, isFunctional, pojoVar, jsonParser, caseBlock, value);
	}
	
	private void fieldValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional,
			JVar pojoVar, JVar jsonParser, JCase caseBlock, JExpression value) {

		JClass tokenType = model.ref(JsonToken.class);
		String setterName = BeanUtil.setterName(predicate);
		if (isFunctional) {
			if (properties == null) {
				JInvocation statement = pojoVar.invoke(setterName).arg(value);
				caseBlock.body().add(statement);
			} else {
				JVar fieldVar = properties.get(predicate);
				caseBlock.body().assign(fieldVar, value);
			}
		} else {
			caseBlock.body().invoke("assertStartArray").arg(jsonParser);
			JVar setVar = null;
			if (properties == null) {
				setVar = declareSet(model, caseBlock.body(), predicate, model._ref(String.class));
				JInvocation statement = pojoVar.invoke(setterName).arg(setVar);
				caseBlock.body().add(statement);
			} else {
				setVar = properties.get(predicate);
			}
			JExpression test = jsonParser.invoke("nextToken").invoke("equals").arg(tokenType.staticRef("END_ARRAY"));
			JWhileLoop arrayLoop = caseBlock.body()._while(JOp.not(test));
			arrayLoop.body().add(setVar.invoke("add").arg(value));
		}
	}

	private Map<URI, AmbiguousProperty> declareAmbiguousProperties(JCodeModel model, JBlock body, Shape shape, Map<URI, JVar> properties) {
		Map<URI, AmbiguousProperty> result = new HashMap<>();
		
		declareAmbiguousProperties(model, body, shape, result, properties);
		
		return result;
	}

	private void declareAmbiguousProperties(JCodeModel model, JBlock body, Shape shape,
			Map<URI, AmbiguousProperty> map, Map<URI,JVar> properties) {
		URI targetClass = shape.getTargetClass();
		OrConstraint or = shape.getOr();
		if (or != null) {
			for (Shape s : or.getShapes()) {
				for (PropertyConstraint p : s.getProperty()) {
					URI predicate = p.getPredicate();
					if (RDF.TYPE.equals(predicate)) {
						continue;
					}
					if (map.containsKey(predicate)) {
						continue;
					}
					
					Set<Resource> domainIncludes = hierarchy.domainIncludes(predicate);
					if (domainIncludes.size()>1) {
						Set<URI> set = filterSubclass(domainIncludes, targetClass);
						if (domainIncludes.size() > 1) {

							JVar var = declareField(model, body, p);
							properties.put(predicate, var);
							map.put(predicate, new AmbiguousProperty(predicate, set, var));
						}
					}
				}
				declareAmbiguousProperties(model, body, s, map, properties);
			}
		}
	}

	private void calendar(JCodeModel model, URI predicate, Map<URI, JVar> properties, boolean isFunctional, JVar pojoVar, JVar jsonVar,
			JCase caseBlock, String setterName, JClass tokenType) {
		
		JInvocation value = JExpr.invoke("dateTime").arg(jsonVar);
		if (isFunctional) {
			if (properties == null) {
				JInvocation statement = pojoVar.invoke(setterName).arg(value);
				caseBlock.body().add(statement);
			} else {
				JVar fieldVar = properties.get(predicate);
				caseBlock.body().assign(fieldVar, value);
			}
		} else {
			caseBlock.body().invoke("assertStartArray").arg(jsonVar);
			JVar setVar = null;
			if (properties == null) {
				setVar = declareSet(model, caseBlock.body(), predicate, model._ref(String.class));
				JInvocation statement = pojoVar.invoke(setterName).arg(setVar);
				caseBlock.body().add(statement);
			} else {
				setVar = properties.get(predicate);
				JClass setType = model.ref(HashSet.class);
				JInvocation newSet = JExpr._new(setType);
				caseBlock.body().assign(setVar, newSet);
			}
			JExpression test = jsonVar.invoke("nextToken").invoke("equals").arg(tokenType.staticRef("END_ARRAY"));
			JWhileLoop arrayLoop = caseBlock.body()._while(JOp.not(test));
			arrayLoop.body().add(setVar.invoke("add").arg(value));
			
		}
		
	}



	private void castInt(JCodeModel model, URI predicate, Map<URI, JVar> properties, 
			boolean isFunctional, JVar pojoVar, JVar jsonVar, JCase caseBlock, String setterName, JType expectedType) {

		JClass tokenType = model.ref(JsonToken.class);
		JExpression value = JExpr.cast(expectedType, jsonVar.invoke("getValueAsInt"));
		if (isFunctional) {
			if (properties == null) {
				JInvocation statement = pojoVar.invoke(setterName).arg(value);
				caseBlock.body().add(statement);
			} else {
				JVar fieldVar = properties.get(predicate);
				caseBlock.body().assign(fieldVar, value);
			}
		} else {
			caseBlock.body().invoke("assertStartArray").arg(jsonVar);
			JVar setVar = null;
			if (properties == null) {
				setVar = declareSet(model, caseBlock.body(), predicate, model._ref(String.class));
				JInvocation statement = pojoVar.invoke(setterName).arg(setVar);
				caseBlock.body().add(statement);
			} else {
				setVar = properties.get(predicate);
			}
			JExpression test = jsonVar.invoke("nextToken").invoke("equals").arg(tokenType.staticRef("END_ARRAY"));
			JWhileLoop arrayLoop = caseBlock.body()._while(JOp.not(test));
			arrayLoop.body().add(setVar.invoke("add").arg(value));
			
		}
		
	}
	
	private void uriValue(JCodeModel model, URI predicate, Map<URI, JVar> properties, 
			boolean isFunctional, JVar pojoVar, JVar jsonVar, JCase caseBlock, String setterName) {

		JClass tokenType = model.ref(JsonToken.class);
		JClass uriClass = model.ref(URIImpl.class);
		if (isFunctional) {
			JInvocation uriValue = JExpr._new(uriClass).arg(jsonVar.invoke("getValueAsString"));
			if (properties == null) {
				JInvocation statement = pojoVar.invoke(setterName).arg(uriValue);
				caseBlock.body().add(statement);
			} else {
				JVar fieldVar = properties.get(predicate);
				caseBlock.body().assign(fieldVar, uriValue);
			}
		} else {
			caseBlock.body().invoke("assertStartArray").arg(jsonVar);
			JVar setVar = null;
			if (properties == null) {
				setVar = declareSet(model, caseBlock.body(), predicate, model._ref(String.class));
				JInvocation statement = pojoVar.invoke(setterName).arg(setVar);
				caseBlock.body().add(statement);
			} else {
				setVar = properties.get(predicate);
			}
			JInvocation uriValue = JExpr._new(uriClass).arg(jsonVar.invoke("getValueAsString"));
			JExpression test = jsonVar.invoke("nextToken").invoke("equals").arg(tokenType.staticRef("END_ARRAY"));
			JWhileLoop arrayLoop = caseBlock.body()._while(JOp.not(test));
			arrayLoop.body().add(setVar.invoke("add").arg(uriValue));
			
		}
		
	}
	
	private void iriRef(JCodeModel model, URI targetClass, URI predicate, Map<URI, JVar> properties,  boolean isFunctional, JVar pojoVar, 
			JVar jsonVar, JCase caseBlock, String setterName, JClass tokenType, URI valueClassId) {

		JClass interfaceType = model.ref(javaNamer.javaInterfaceName(valueClassId));
		JClass classType = model.ref(javaNamer.javaClassName(valueClassId));
		JClass uriImplType = model.ref(URIImpl.class);
		
		if (isFunctional) {
			JExpression newURI = JExpr._new(uriImplType).arg(jsonVar.invoke("getText"));
			JExpression newObject = JExpr._new(classType).arg(newURI);
			if (properties == null) {

				JInvocation statement = pojoVar.invoke(setterName).arg(newObject);
				caseBlock.body().add(statement);
			} else {
				caseBlock.body().assign(properties.get(predicate), newObject);
			}
		} else {
			caseBlock.body().invoke("assertStartArray").arg(jsonVar);
			
			JVar setVar = null;
			if (properties == null) {
				setVar = declareSet(model, caseBlock.body(), predicate, interfaceType);
				JInvocation statement = pojoVar.invoke(setterName).arg(setVar);
				caseBlock.body().add(statement);
			} else {
				setVar = properties.get(predicate);
				JClass arrayListType = model.ref(HashSet.class);
				arrayListType = arrayListType.narrow(interfaceType);
				caseBlock.body().assign(setVar, JExpr._new(arrayListType));
			}
			caseBlock.body().add(jsonVar.invoke("nextToken"));
			
			
			JExpression test = jsonVar.invoke("getCurrentToken").invoke("equals").arg(tokenType.staticRef("END_ARRAY"));
			
			JWhileLoop arrayLoop = caseBlock.body()._while(JOp.not(test));
			
			JSwitch objectSwitch = arrayLoop.body()._switch(jsonVar.invoke("getCurrentToken"));
			JCase stringCase = objectSwitch._case(new JEnumValue("VALUE_STRING"));

			JExpression newURI = JExpr._new(uriImplType).arg(jsonVar.invoke("getText"));
			JExpression newObject = JExpr._new(classType).arg(newURI);
			
			stringCase.body().add(setVar.invoke("add").arg(newObject));
			stringCase.body()._break();
			
			JCase objectCase = objectSwitch._case(new JEnumValue("START_OBJECT"));
			if (targetClass.equals(valueClassId)) {
				objectCase.body().add(setVar.invoke("add").arg(JExpr.invoke("read").arg(jsonVar)));
			} else {
				JDefinedClass objectReader = produceJsonReader(valueClassId, model);
				objectCase.body().add(setVar.invoke("add").arg(objectReader.staticInvoke("instance").invoke("read").arg(jsonVar)));
			}
			objectCase.body()._break();
			
			
			arrayLoop.body().add(jsonVar.invoke("nextToken"));
			
		}
	}

	private boolean literal(NodeKind nodeKind) {
		
		return nodeKind==null || nodeKind==NodeKind.Literal;
	}

	private JVar declareSet(JCodeModel model, JBlock body, URI predicate, JType elementType) {
		return declareSet(model, body, predicate, elementType, false);
	}

	private JVar declareSet(JCodeModel model, JBlock body, URI predicate, JType elementType, boolean initNull) {
		
		String name = predicate.getLocalName();
		JClass arrayListType = model.ref(HashSet.class);
		arrayListType = arrayListType.narrow(elementType);
		
		
		JVar var = body.decl(arrayListType, name);
		if (initNull) {
			var.init(JExpr._null());
		} else {
			var.init(JExpr._new(arrayListType));
		}
		
		return var;
	}

	private void createGetInstanceMethod(JCodeModel model, JDefinedClass dc, JVar instanceField) {
		
		dc.method(JMod.PUBLIC | JMod.STATIC , dc, "instance").body()._return(instanceField);
		
	}
	
	private static class AmbiguousProperty {
		private URI predicate;
		private Set<URI> domainIncludes;
		private JVar valueVar;
		private boolean handled;
		
		public AmbiguousProperty(URI predicate, Set<URI> domainIncludes, JVar valueVar) {
			this.predicate = predicate;
			this.domainIncludes = domainIncludes;
			this.valueVar = valueVar;
		}

		public URI getPredicate() {
			return predicate;
		}

		public Set<URI> getDomainIncludes() {
			return domainIncludes;
		}

		public JVar getValueVar() {
			return valueVar;
		}

		public boolean isHandled() {
			return handled;
		}

		public void setHandled(boolean handled) {
			this.handled = handled;
		}
		
	}
}
