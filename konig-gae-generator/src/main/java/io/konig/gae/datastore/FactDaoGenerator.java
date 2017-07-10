package io.konig.gae.datastore;

/*
 * #%L
 * Konig GAE Generator
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
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.EmbeddedEntity;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.sun.codemodel.ClassType;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCatchBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldRef;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JTryBlock;
import com.sun.codemodel.JVar;

import io.konig.core.pojo.BeanUtil;
import io.konig.core.util.JavaDatatypeMapper;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.TIME;
import io.konig.schemagen.java.JavaNamer;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class FactDaoGenerator {

	private DaoNamer daoNamer;
	private EntityNamer entityNamer;
	private JavaNamer javaNamer;
	private JavaDatatypeMapper datatypeMapper;
	private ShapeManager shapeManager;
	private boolean findByDimensionOnly=true;
	
	

	public FactDaoGenerator setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
		return this;
	}

	public FactDaoGenerator setDaoNamer(DaoNamer daoNamer) {
		this.daoNamer = daoNamer;
		return this;
	}
	
	public FactDaoGenerator setEntityNamer(EntityNamer entityNamer) {
		this.entityNamer = entityNamer;
		return this;
	}


	public FactDaoGenerator setJavaNamer(JavaNamer javaNamer) {
		this.javaNamer = javaNamer;
		return this;
	}



	public FactDaoGenerator setDatatypeMapper(JavaDatatypeMapper datatypeMapper) {
		this.datatypeMapper = datatypeMapper;
		return this;
	}



	public boolean isFindByDimensionOnly() {
		return findByDimensionOnly;
	}

	public FactDaoGenerator setFindByDimensionOnly(boolean findByDimensionOnly) {
		this.findByDimensionOnly = findByDimensionOnly;
		return this;
	}
	
	public void generateAllFactDaos(JCodeModel model) throws CodeGeneratorException {
		for (Shape shape : shapeManager.listShapes()) {
			if (isFactShape(shape)) {
				generateDao(shape, model);
			}
		}
	}

	private boolean isFactShape(Shape shape) {
		for (PropertyConstraint p : shape.getProperty()) {
			URI stereotype = p.getStereotype();
			if (Konig.measure.equals(stereotype) || Konig.dimension.equals(stereotype) || Konig.attribute.equals(stereotype)) {
				return true;
			}
		}
		return false;
	}

	public void generateDao(Shape shape, JCodeModel model) throws CodeGeneratorException {
		
		if (shape.getOr() != null) {
			throw new CodeGeneratorException("Cannot create DAO for facts with an sh:or constraint: " + shape.getId());
		}
		
		try {

			URI targetClass = shape.getTargetClass();
			if (targetClass == null) {
				throw new CodeGeneratorException("Target class not defined on Shape: " + shape.getId());
			}
			
			String pojoInterfaceName = javaNamer.javaInterfaceName(targetClass);
			String pojoImplName = javaNamer.javaClassName(targetClass);
			String daoClassName = daoNamer.daoName(targetClass);
			
			DaoInfo info = new DaoInfo();
			info.model = model;
			info.shape = shape;
			info.owlClass = targetClass;
			info.entityType = entityNamer.entityName(targetClass);
			info.pojoInterface = model.ref(pojoInterfaceName);
			info.pojoImpl = model.ref(pojoImplName);
			info.daoClass = model._class(daoClassName);
			
			declareEntityKind(info);
			declareConstraintsClass(info);
			buildToEntityMethod(info);
			buildToPojoMethod(info);
			buildPutMethod(info);
			buildFindMethod(info);
			
		} catch (JClassAlreadyExistsException e) {
			throw new CodeGeneratorException(e);
		} finally {
			
		}
		
		
	}
	
	private void buildFindMethod(DaoInfo info) throws CodeGeneratorException {
		JCodeModel model = info.model;
		
		JClass iterableClass = model.ref(List.class);
		JClass narrowIterable = iterableClass.narrow(info.pojoInterface);
		
		
		JMethod method = info.daoClass.method(JMod.PUBLIC, narrowIterable, "find");
		JVar param = method.param(info.constraintsClass, "constraints");
		
		JBlock body = method.body();
		
		JClass listClass = model.ref(List.class).narrow(info.pojoInterface);
		JClass arrayListClass = model.ref(ArrayList.class).narrow(info.pojoInterface);
		
		JClass filterListClass = model.ref(List.class).narrow(Filter.class);
		JClass filterArrayListClass = model.ref(ArrayList.class).narrow(Filter.class);
		
		
		JVar filterListVar = body.decl(filterListClass, "filterList");
		filterListVar.init(JExpr._new(filterArrayListClass));
		buildFilter(info, body, info.shape, filterListVar, param, null);
		
		JClass queryClass = model.ref(Query.class);
		
		JVar queryVar = body.decl(queryClass, "theQuery");
		queryVar.init(JExpr._new(queryClass).arg(info.entityKind));
		
		JBlock block = body._if(filterListVar.invoke("size").eq(JExpr.lit(1)))._then();
		block.add(queryVar.invoke("setFilter").arg(filterListVar.invoke("get").arg(JExpr.lit(0))));
		
		block = body._if(filterListVar.invoke("size").gt(JExpr.lit(1)))._then();
		
		JClass compositeFilterClass = model.ref(CompositeFilter.class);
		JClass compositeOperator = model.ref(CompositeFilterOperator.class);
		JFieldRef and = compositeOperator.staticRef("AND");
		
		block.add(queryVar.invoke("setFilter").arg(JExpr._new(compositeFilterClass).arg(and).arg(filterListVar)));
		
		JClass entityListClass = model.ref(List.class).narrow(Entity.class);
		JClass entityClass = model.ref(Entity.class);

		JClass serviceType = model.ref(DatastoreService.class);
		JClass factoryType = model.ref(DatastoreServiceFactory.class);
		JClass fetchOptionsBuilder = model.ref(FetchOptions.Builder.class);

		JVar resultVar = body.decl(listClass, "result");
		resultVar.init(JExpr._new(arrayListClass));

		JVar datastore = body.decl(serviceType, "datastore");
		datastore.init(factoryType.staticInvoke("getDatastoreService"));
		
		JVar entityListVar = body.decl(entityListClass, "entityList");
		entityListVar.init(datastore.invoke("prepare")
			.arg(queryVar).invoke("asList").arg(fetchOptionsBuilder.staticInvoke("withDefaults")));

		JForEach forEach = body.forEach(entityClass, "entity", entityListVar);
		JVar entityVar = forEach.var();
		block = forEach.body();
		
		block.add(resultVar.invoke("add").arg(JExpr.invoke("toPojo").arg(entityVar)));
		
		
		body._return(resultVar);
		
	}

	private void buildFilter(DaoInfo info, JBlock body, Shape shape, JVar filterListVar, JVar constraints, String fieldPrefix) throws CodeGeneratorException {
		
		JBlock thenBlock = body._if(constraints.ne(JExpr._null()))._then();
		
		for (PropertyConstraint p : shape.getProperty()) {
			buildFilter(info, thenBlock, p, filterListVar, constraints, fieldPrefix);
		}
		
	}

	private void buildFilter(DaoInfo info, JBlock block, PropertyConstraint p, JVar filterListVar,
			JVar constraints, String fieldPrefix) throws CodeGeneratorException {
		
		JCodeModel model = info.model;
		URI predicate = p.getPredicate();
		if (predicate == null) {
			return;
		}
		
		URI stereotype = p.getStereotype();
		if (findByDimensionOnly && !(stereotype==null || Konig.dimension.equals(stereotype))) {
			return;
		}
		
		String fieldName = predicate.getLocalName();
		String fieldPath = fieldPath(fieldPrefix, fieldName);
		String getterMethod = BeanUtil.getterName(predicate);
		JClass uriType = model.ref(URI.class);

		JClass filterPredicateClass = model.ref(FilterPredicate.class);
		JClass filterOperator = model.ref(FilterOperator.class);
		JFieldRef greaterThanOrEqual = filterOperator.staticRef("GREATER_THAN_OR_EQUAL");
		JFieldRef greaterThan = filterOperator.staticRef("GREATER_THAN");
		JFieldRef lessThanOrEqual = filterOperator.staticRef("LESS_THAN_OR_EQUAL");
		JFieldRef lessThan = filterOperator.staticRef("LESS_THAN");
		JFieldRef equal = filterOperator.staticRef("EQUAL");
		
		URI datatype = p.getDatatype();
		if (datatype != null) {
			
			Class<?> javaType = datatypeMapper.javaDatatype(datatype);
			boolean temporal = javaType == GregorianCalendar.class;
			
			JClass fieldType = null;
			boolean isRange = false;
			if (isInteger(javaType)) {
				fieldType = model.ref(IntegerRange.class);
				isRange = true;
			} else if (isRealNumber(javaType)) {
				fieldType = model.ref(RealNumberRange.class);
				isRange = true;
			} else if (javaType==GregorianCalendar.class) {
				fieldType = model.ref(CalendarRange.class);
				isRange = true;
			} else {
				fieldType = model.ref(javaType);
			}
			
			JVar field = block.decl(fieldType, fieldName);
			field.init(constraints.invoke(getterMethod));
			
			
			
			JBlock thenBlock = block._if(field.ne(JExpr._null()))._then();
			if (isRange) {

				JExpression value = field.invoke("getMinInclusive");
				
				
				
				JBlock block2 = thenBlock._if(value.ne(JExpr._null()))._then();

				block2.add(filterListVar.invoke("add").arg(JExpr._new(filterPredicateClass)
						.arg(JExpr.lit(fieldPath))
						.arg(greaterThanOrEqual)
						.arg(rangeValue(temporal, value))
				));
				
				value = field.invoke("getMinExclusive");
				
				
				block2 = thenBlock._if(value.ne(JExpr._null()))._then();

				block2.add(filterListVar.invoke("add").arg(JExpr._new(filterPredicateClass)
						.arg(JExpr.lit(fieldPath))
						.arg(greaterThan)
						.arg(rangeValue(temporal, value))
				));
				
				value =  field.invoke("getMaxInclusive");
				
				block2 = thenBlock._if(value.ne(JExpr._null()))._then();

				block2.add(filterListVar.invoke("add").arg(JExpr._new(filterPredicateClass)
						.arg(JExpr.lit(fieldPath))
						.arg(lessThanOrEqual)
						.arg(rangeValue(temporal, value))
				));
				
				value = field.invoke("getMaxExclusive");
				
				block2 = thenBlock._if(value.ne(JExpr._null()))._then();

				block2.add(filterListVar.invoke("add").arg(JExpr._new(filterPredicateClass)
						.arg(JExpr.lit(fieldPath))
						.arg(lessThan)
						.arg(rangeValue(temporal, value))
				));
				
				
				
			} else {
				
				JBlock block2 = thenBlock._if(field.ne(JExpr._null()))._then();

				block2.add(filterListVar.invoke("add").arg(JExpr._new(filterPredicateClass)
						.arg(JExpr.lit(fieldPath))
						.arg(equal)
						.arg(field)
				));
			}
			
		} else {
			URI valueClass = getValueClass(p);
			
			if (valueClass != null) {

				if (p.getShapeId() == null) {

					JVar field = block.decl(uriType, fieldName);
					field.init(constraints.invoke(getterMethod));

					JBlock thenBlock = block._if(field.ne(JExpr._null()))._then();

					thenBlock.add(filterListVar.invoke("add").arg(JExpr._new(filterPredicateClass)
							.arg(JExpr.lit(fieldPath))
							.arg(equal)
							.arg(field.invoke("stringValue"))));
					
				} else {
					Shape shape = p.getShape(shapeManager);
					if (shape == null) {
						throw new CodeGeneratorException("Shape not found: " + p.getShapeId());
					}
					URI targetClass = shape.getTargetClass();

					String className = targetClass.getLocalName() + "Constraints";
					JClass fieldType = model.ref(className);

					JVar field = block.decl(fieldType, fieldName);
					field.init(constraints.invoke(getterMethod));
					
					buildFilter(info, block, shape, filterListVar, field, concatFieldPrefix(fieldPrefix, fieldName));
				}
			}
			
		}
		
		
	}
	
	private String fieldPath(String prefix, String fieldName) {
		return prefix == null ? fieldName : prefix + fieldName;
	}
	
	private String concatFieldPrefix(String a, String b) {
		StringBuilder builder = new StringBuilder();
		if (a != null) {
			builder.append(a);
		}
		builder.append(b);
		builder.append('.');
		
		return builder.toString();
	}

	private JExpression rangeValue(boolean temporal, JExpression value) {
		if (temporal) {
			value = value.invoke("getTime");
		}
		return value;
	}

	private void declareConstraintsClass(DaoInfo info) throws JClassAlreadyExistsException, CodeGeneratorException {
		
		JDefinedClass dc = info.daoClass;
		
		JDefinedClass constraints = dc._class(JMod.PUBLIC | JMod.STATIC, "Constraints", ClassType.CLASS);
		info.constraintsClass = constraints;
		
		buildConstraintProperties(info, constraints, info.shape);
		
		
		
	}

	private void buildConstraintProperties(DaoInfo info, JDefinedClass constraints, Shape shape) throws CodeGeneratorException {
		
		for (PropertyConstraint p : shape.getProperty()) {
			declareConstraintProperty(info, constraints, p);
		}
		
	}

	private void declareConstraintProperty(DaoInfo info, JDefinedClass dc, PropertyConstraint p) throws CodeGeneratorException {
		JCodeModel model = info.model;
		URI predicate = p.getPredicate();
		if (predicate == null) {
			return;
		}
		
		URI stereotype = p.getStereotype();
		if (findByDimensionOnly && !(stereotype==null || Konig.dimension.equals(stereotype))) {
			return;
		}
		
		String fieldName = predicate.getLocalName();
		JClass uriType = model.ref(URI.class);
		
		URI datatype = p.getDatatype();
		if (datatype != null) {
			
			Class<?> javaType = datatypeMapper.javaDatatype(datatype);
			
			JClass fieldType = null;
			if (isInteger(javaType)) {
				fieldType = model.ref(IntegerRange.class);
			} else if (isRealNumber(javaType)) {
				fieldType = model.ref(RealNumberRange.class);
			} else if (javaType==GregorianCalendar.class) {
				fieldType = model.ref(CalendarRange.class);
			} else {
				fieldType = model.ref(javaType);
			}
			
			JVar field = dc.field(JMod.PRIVATE, fieldType, fieldName);
			buildGetterField(dc, fieldType, predicate, field);
			buildSetterField(dc, fieldType, predicate, field);
			
		} else {
			URI valueClass = getValueClass(p);
			
			if (valueClass != null) {

				if (p.getShapeId() == null) {
					JVar field = dc.field(JMod.PRIVATE, uriType, fieldName);
					buildGetterField(dc, uriType, predicate, field);
					buildSetterField(dc, uriType, predicate, field);
				} else {
					Shape shape = p.getShape(shapeManager);
					if (shape == null) {
						throw new CodeGeneratorException("Shape not found: " + p.getShapeId());
					}
					JDefinedClass fieldType = nestedConstraints(info, shape);
					JVar field = dc.field(JMod.PRIVATE, fieldType, fieldName);
					buildGetterField(dc, fieldType, predicate, field);
					buildSetterField(dc, fieldType, predicate, field);
				}
			}
			
		}
		
	}
	
	private JDefinedClass nestedConstraints(DaoInfo info, Shape shape) throws CodeGeneratorException {
		URI targetClass = shape.getTargetClass();
		if (targetClass == null) {
			throw new CodeGeneratorException("Target class not defined on shape: " + shape.getId());
		}
		String className = targetClass.getLocalName() + "Constraints";
		try {
			JDefinedClass nested = info.daoClass._class(JMod.PUBLIC | JMod.STATIC, className);
			buildConstraintProperties(info, nested, shape);
			return nested;
			
		} catch (JClassAlreadyExistsException e) {
			throw new CodeGeneratorException(e);
		}
		
	}

	private void buildSetterField(JDefinedClass dc, JClass fieldType, URI predicate, JVar field) {
		
		String setterName = BeanUtil.setterName(predicate);
		
		JMethod method = dc.method(JMod.PUBLIC, dc, setterName);
		JVar param = method.param(fieldType, predicate.getLocalName());
		
		method.body().assign(JExpr._this().ref(field), param)._return(JExpr._this());
		
	}

	private void buildGetterField(JDefinedClass dc, JClass fieldType, URI predicate, JVar field) {
		
		String getterName = BeanUtil.getterName(predicate);
		JMethod method = dc.method(JMod.PUBLIC, fieldType, getterName);
		method.body()._return(field);
		
	}

	private boolean isRealNumber(Class<?> javaType) {
		return javaType==Float.class || javaType==Double.class;
	}

	private boolean isInteger(Class<?> javaType) {
	
		return javaType==Long.class || javaType==Integer.class || javaType==Short.class;
	}

	private void buildToPojoMethod(DaoInfo info) throws CodeGeneratorException {
		
		JCodeModel model = info.model;
		JDefinedClass dc = info.daoClass;
		JClass entityClass = model.ref(Entity.class);
		
		JMethod method = dc.method(JMod.PUBLIC, info.pojoInterface, "toPojo");
		JVar entityVar = method.param(entityClass, "entity");
		JBlock body = method.body();
		
		JVar pojoVar = body.decl(info.pojoInterface, "pojo");
		pojoVar.init(JExpr._new(info.pojoImpl));
		
		setPojoProperties(info, body, pojoVar, entityVar, info.shape);
		
		body._return(pojoVar);
		
		
	}

	private void setPojoProperties(DaoInfo info, JBlock body, JVar pojoVar, JVar entityVar, Shape shape) throws CodeGeneratorException {
		
		for (PropertyConstraint p : shape.getProperty()) {
			setPojoProperty(info, body, pojoVar, entityVar, p);
		}
		
	}

	private void setPojoProperty(DaoInfo info, JBlock body, JVar pojoVar, JVar entityVar, PropertyConstraint p) throws CodeGeneratorException {
		
		URI predicate = p.getPredicate();
		if (predicate == null) {
			return;
		}
		Integer maxCount = p.getMaxCount();
		if (maxCount==null || maxCount>1) {
			throw new CodeGeneratorException("Multi-value properties not supported");
		}
		JCodeModel model = info.model;
		URI datatype = p.getDatatype();
		String propertyName = predicate.getLocalName();
		
		String setterMethod = BeanUtil.setterName(predicate);
		
		if (datatype != null) {
			Class<?> javaType = datatypeMapper.javaDatatype(datatype);
			
			if (javaType == GregorianCalendar.class) {
				JClass propertyType = model.ref(Date.class);
				JClass calendarType = model.ref(GregorianCalendar.class);

				JVar var = body.decl(propertyType, propertyName);
				
				JExpression initValue = (JExpression)
					JExpr.cast(propertyType, entityVar.invoke("getProperty").arg(JExpr.lit(propertyName)));
				
				var.init(initValue);

				JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
				String calendarName = propertyName + "Calendar";
				
				JVar calendarVar = thenBlock.decl(calendarType, calendarName);
				calendarVar.init(JExpr._new(calendarType));
				
				thenBlock.add(calendarVar.invoke("setTime").arg(var));
				thenBlock.add(pojoVar.invoke(setterMethod).arg(calendarVar));
				
			} else if (javaType == Short.class) {

				JClass longType = model.ref(Long.class);
				
				JVar var = body.decl(longType, propertyName);
				
				JExpression initValue = (JExpression)
					JExpr.cast(longType, entityVar.invoke("getProperty").arg(JExpr.lit(propertyName)));
				
				var.init(initValue);
				
				JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
				thenBlock.add(pojoVar.invoke(setterMethod).arg(var.invoke("shortValue")));
				
			} else if (javaType == Integer.class) {

				JClass longType = model.ref(Long.class);
				
				JVar var = body.decl(longType, propertyName);
				
				JExpression initValue = (JExpression)
					JExpr.cast(longType, entityVar.invoke("getProperty").arg(JExpr.lit(propertyName)));
				
				var.init(initValue);
				
				JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
				thenBlock.add(pojoVar.invoke(setterMethod).arg(var.invoke("intValue")));
				
			} else {
				JClass propertyType = model.ref(javaType);
				
				JVar var = body.decl(propertyType, propertyName);
				
				JExpression initValue = (JExpression)
					JExpr.cast(propertyType, entityVar.invoke("getProperty").arg(JExpr.lit(propertyName)));
				
				var.init(initValue);
				
				JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
				thenBlock.add(pojoVar.invoke(setterMethod).arg(var));
			}
		} else {
			
			URI valueClass = getValueClass(p);
			
			if (valueClass != null) {
				
				JClass valueInterfaceType = model.ref(javaNamer.javaInterfaceName(valueClass));
				JClass valueImplType = model.ref(javaNamer.javaClassName(valueClass));
				JClass uriImpl = model.ref(URIImpl.class);
				
				
				if (p.getShapeId() == null) {
					JClass stringClass = model.ref(String.class);
					
					JVar var = body.decl(stringClass, propertyName);
					
					JExpression initValue = (JExpression)
						JExpr.cast(stringClass, entityVar.invoke("getProperty").arg(JExpr.lit(propertyName)));
					
					var.init(initValue);
					
					JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
					
					JVar varPojo = thenBlock.decl(valueInterfaceType, propertyName + "Pojo");
					varPojo.init(JExpr._new(valueImplType));
					
					thenBlock.add(varPojo.invoke("setId").arg(JExpr._new(uriImpl).arg(var)));
					thenBlock.add(pojoVar.invoke(setterMethod).arg(varPojo));
					
					
				} else {
					Shape valueShape = p.getShape(shapeManager);
					if (valueShape == null) {
						throw new CodeGeneratorException("Shape not found: " + p.getShapeId());
					}
					
					JClass embeddedType = model.ref(EmbeddedEntity.class);
					
					
					
					JVar var = body.decl(embeddedType, propertyName);
					JExpression initValue = (JExpression)
						JExpr.cast(embeddedType, entityVar.invoke("getProperty").arg(JExpr.lit(propertyName)));
					
					var.init(initValue);
					
					JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
					
					JVar varPojo = thenBlock.decl(valueImplType, propertyName + "Pojo");
					varPojo.init(JExpr._new(valueImplType));
					setPojoProperties(info, thenBlock, varPojo, var, valueShape);
					
					thenBlock.add(pojoVar.invoke(setterMethod).arg(varPojo));
					
					
				}
				
				
			}
		}
		
		
	}

	private void buildPutMethod(DaoInfo info) {
		JCodeModel model = info.model;
		
		JDefinedClass dc = info.daoClass;
		
		JMethod method = dc.method(JMod.PUBLIC, void.class, "put");
		JVar pojoVar = method.param(info.pojoInterface, "pojo");
		JBlock body = method.body();
		
		JClass serviceType = model.ref(DatastoreService.class);
		JClass factoryType = model.ref(DatastoreServiceFactory.class);
		JClass entityType = model.ref(Entity.class);
		JClass illegalArg = model.ref(IllegalArgumentException.class);
		JClass concurrentModification = model.ref(ConcurrentModificationException.class);
		JClass datastoreFailure = model.ref(DatastoreFailureException.class);
		JClass accessException = model.ref(DataAccessException.class);
		
		method._throws(accessException);
		
		JVar datastore = body.decl(serviceType, "datastore");
		datastore.init(factoryType.staticInvoke("getDatastoreService"));
		
		JVar entityVar = body.decl(entityType, "entity");
		entityVar.init(JExpr.invoke("toEntity").arg(pojoVar));
		
		JTryBlock tryBlock = body._try();
		JBlock tryBody = tryBlock.body();
		tryBody.add(datastore.invoke("put").arg(entityVar));
		
		JCatchBlock catchBlock = tryBlock._catch(illegalArg);
		JVar param = catchBlock.param("e1");
		catchBlock.body()._throw(JExpr._new(accessException).arg(param));
		
		catchBlock = tryBlock._catch(concurrentModification);
		param = catchBlock.param("e2");
		catchBlock.body()._throw(JExpr._new(accessException).arg(param));

		catchBlock = tryBlock._catch(datastoreFailure);
		param = catchBlock.param("e3");
		catchBlock.body()._throw(JExpr._new(accessException).arg(param));
		
		
	}

	private void declareEntityKind(DaoInfo info) {
		JVar var = info.daoClass.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, String.class, "ENTITY_KIND");
		var.init(JExpr.lit(info.entityType));
		
		info.entityKind = var;
		
	}

	private void buildToEntityMethod(DaoInfo info) throws CodeGeneratorException {
	
		JCodeModel model = info.model;
		JDefinedClass dc = info.daoClass;
		JClass entityClass = model.ref(Entity.class);
		
		JMethod method = dc.method(JMod.PUBLIC, entityClass, "toEntity");
		JVar pojoVar = method.param(info.pojoInterface, "pojo");
		JBlock body = method.body();
		JVar entityVar = body.decl(entityClass, "entity");
		entityVar.init(JExpr._new(entityClass).arg(info.entityKind));
		
		setEntityValues(model, body, entityVar, pojoVar, info.shape);
		
		body._return(entityVar);
	}

	private void setEntityValues(JCodeModel model, JBlock body, JVar entityVar, JVar pojoVar, Shape shape) throws CodeGeneratorException {

		List<PropertyConstraint> plist = shape.getProperty();
		for (PropertyConstraint p : plist) {
			setEntityProperty(model, body, entityVar, pojoVar, p);
		}
		
	}

	private void setEntityProperty(JCodeModel model, JBlock body, JVar entityVar, JVar pojoVar, PropertyConstraint p) throws CodeGeneratorException {
		
		URI datatype = p.getDatatype();
		URI predicate = p.getPredicate();
		if (predicate == null) {
			return;
		}
		
		String getterMethod = BeanUtil.getterName(predicate);
		Integer maxCount = p.getMaxCount();
		if (maxCount==null || maxCount>1) {
			throw new CodeGeneratorException("Multi-valued properties not supported");
		}
		String varName = predicate.getLocalName();
		
		if (datatype != null) {
			
			Class<?> javaType = datatypeMapper.javaDatatype(datatype);
			if (javaType == null) {
				throw new CodeGeneratorException("Java type not known for datatype: " + datatype);
			}
			JClass jtype = model.ref(javaType);
			
			JVar var = body.decl(jtype, varName);
			var.init(pojoVar.invoke(getterMethod));
			
			JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
			if (javaType == GregorianCalendar.class) {

				thenBlock.add(entityVar.invoke("setProperty").arg(JExpr.lit(varName)).arg(var.invoke("getTime")));
			} else {
				thenBlock.add(entityVar.invoke("setProperty").arg(JExpr.lit(varName)).arg(var));
			}
			
		} else {

			URI valueClass = getValueClass(p);
			if (valueClass != null) {
				String typeName = javaNamer.javaInterfaceName(valueClass);
				// The following handling for durationUnit is a hack
				// TODO: Find a cleaner solution
				if (predicate.equals(Konig.durationUnit)) {
					typeName = javaNamer.javaInterfaceName(TIME.TemporalUnit);
				}
				JClass jtype = model.ref(typeName);
				
				JVar var = body.decl(jtype, varName);
				var.init(pojoVar.invoke(getterMethod));
				
				JBlock thenBlock = body._if(var.ne(JExpr._null()))._then();
				
				if (p.getShapeId()==null) {
					thenBlock.add(entityVar.invoke("setProperty")
						.arg(JExpr.lit(varName))
						.arg(var.invoke("getId").invoke("stringValue")));
				} else {
					Shape shape = p.getShape(shapeManager);
					if (shape != null) {
						JClass embeddedType = model.ref(EmbeddedEntity.class);
						JVar embeddedVar = thenBlock.decl(embeddedType, "embedded");
						embeddedVar.init(JExpr._new(embeddedType));
						
						setEntityValues(model, thenBlock, embeddedVar, var, shape);
						thenBlock.add(entityVar.invoke("setIndexedProperty")
							.arg(JExpr.lit(varName))
							.arg(embeddedVar)
						);
						
					}
				}
			}
		}
		
	}

	private URI getValueClass(PropertyConstraint p) {
		Resource valueClass = p.getValueClass();
		if (valueClass instanceof URI) {
			return (URI) valueClass;
		}
		Resource shapeId = p.getShapeId();
		if (shapeId instanceof URI) {
			Shape valueShape = p.getShape(shapeManager);
			if (valueShape != null) {
				return valueShape.getTargetClass();
			}
		}
		return null;
	}

	private static class DaoInfo {
		private JCodeModel model;
		private JVar entityKind;
		private Shape shape;
		private URI owlClass;
		private String entityType;
		private JClass pojoInterface;
		private JClass pojoImpl;
		private JDefinedClass daoClass;
		private JDefinedClass constraintsClass;
	}
	

}
