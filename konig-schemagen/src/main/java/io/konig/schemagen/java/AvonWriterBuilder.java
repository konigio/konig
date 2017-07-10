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


import java.awt.List;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JForEach;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JOp;
import com.sun.codemodel.JVar;

import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeMediaTypeNamer;

public class AvonWriterBuilder {
	private DataWriterBuilder dataWriter;

	public AvonWriterBuilder(DataWriterBuilder dataWriter) {
		this.dataWriter = dataWriter;
	}
	
	public void buildWriter(Shape shape) throws SchemaGeneratorException {
		
		Worker worker = new Worker();
		worker.buildWriter(shape);
	}
	
	private class Worker {
		
		private JDefinedClass dc;

		public void buildWriter(Shape shape) throws SchemaGeneratorException {
			
			URI targetClass = shape.getTargetClass();
			if (targetClass != null) {
				String className = avonWriterJavaClassName(shape);
				JCodeModel model = dataWriter.getModel();
				
				String mediaTypeName = avonMediaType(shape);
				
				if (model._getClass(className) == null) {
					try {
						dc = model._class(className);
						dc._implements(dataWriter.getDataWriterInterface());
						
						JFieldVar mediaTypeField = dc.field(JMod.STATIC | JMod.PUBLIC | JMod.FINAL, String.class, "MEDIA_TYPE");
						mediaTypeField.init(
							JExpr.lit(mediaTypeName)
						);
						
						JMethod method = dc.method(JMod.PUBLIC, String.class, "getMediaType");
						method.body()._return(mediaTypeField);
						method.annotate(Override.class);

						String javaClassName = dataWriter.getJavaNamer().javaClassName(targetClass);
						JClass javaTargetClass = model.ref(javaClassName);
						
						buildWriteMethod(model, dc, javaTargetClass, shape);
						
						
					} catch (JClassAlreadyExistsException e) {
						throw new SchemaGeneratorException(e);
					}
				}
			}
		}

		private String avonMediaType(Shape shape) {

			String result = null;
			URI targetClass = shape.getTargetClass();
			if (targetClass != null) {
				ShapeMediaTypeNamer mediaTypeNamer = dataWriter.getMediaTypeNamer();
				String mediaTypeBaseName = mediaTypeNamer.baseMediaTypeName(shape);
				result = mediaTypeBaseName + "+avon";
			}
			
			return result;
		}
		
		
		private String avonWriterJavaClassName(Shape shape) {

			String className = null;
			
			URI targetClass = shape.getTargetClass();
			if (targetClass != null) {
				ShapeMediaTypeNamer mediaTypeNamer = dataWriter.getMediaTypeNamer();
				String mediaTypeBaseName = mediaTypeNamer.baseMediaTypeName(shape);
				className = mediaTypeBaseName + '.' + targetClass.getLocalName() + "AvonWriter";
				className = dataWriter.getJavaNamer().writerName(className);
				
			} else {
				throw new SchemaGeneratorException("targetClass not defined for Shape: " + shape.getId());
			}
			
			return className;
		}

		private void buildWriteMethod(JCodeModel model, JDefinedClass dc, JClass javaTargetClass, Shape shape) {
			
			URI targetClass = shape.getTargetClass();
			JMethod method = dc.method(JMod.PUBLIC, void.class, "write");
			JVar dataVar = method.param(Object.class, "data");
			JVar outVar = method.param(dataWriter.getDataSinkClass(), "out");
			method.annotate(Override.class);
			method._throws(dataWriter.getValidationExceptionClass());
			method._throws(model.ref(IOException.class));
			
			JConditional conditional = method.body()._if(dataVar._instanceof(javaTargetClass));
			
			JBlock block = conditional._then();
			JBlock elseBlock = conditional._else();
			
			elseBlock._throw(
				JExpr._new(dataWriter.getValidationExceptionClass()).arg(
					JExpr.lit("Invalid argument.  Expected data object of type " + targetClass.getLocalName())
				)
			);
			
			JClass jsonGeneratorClass = model.ref(JsonGenerator.class);
			
			JVar jsonVar = block.decl(jsonGeneratorClass, "json", JExpr.invoke(outVar, "getJsonGenerator"));

			JVar subjectVar = block.decl(javaTargetClass, "subject", JExpr.cast(javaTargetClass, dataVar));
			
			block.invoke(jsonVar, "writeStartObject");
			
			for (PropertyConstraint p : shape.getProperty()) {
				handleProperty(block, outVar, subjectVar, jsonVar, p, targetClass);
				
			}
			
			block.invoke(jsonVar, "writeEndObject");
			
			
			
		}

		private void handleProperty(JBlock block, JVar outVar, JVar subjectVar, JVar jsonVar, PropertyConstraint p, URI subjectClass) {
			
			Integer maxCount = p.getMaxCount();
			Integer minCount = p.getMinCount();
			
			if (maxCount!=null && maxCount==1) {
				
				boolean required = minCount!=null && minCount==1;
				handleSingleValue(block, outVar, subjectVar, jsonVar, p, subjectClass, required);
				
			} else {
				handleMultiValue(block, outVar, subjectVar, jsonVar, p, subjectClass);
			}
			
		}

		private void handleMultiValue(JBlock block, JVar outVar, JVar subjectVar, JVar jsonVar, PropertyConstraint p, URI subjectClass) {
			JCodeModel model = dataWriter.getModel();
			JClass uriClass = model.ref(URI.class);
			Shape valueShape = p.getShape();
			URI datatype = p.getDatatype();

			String fieldName = p.getPredicate().getLocalName() + "List";
			String getterName = JavaClassBuilder.getterName(fieldName);
			
			String valueTypeName = valueType(p);
			
			JClass valueClass = ref(valueTypeName);
			
			JClass listClass = ref(List.class);
			listClass = listClass.narrow(valueClass);
			
			JVar localVar = block.decl(listClass, fieldName, subjectVar.invoke(getterName));
			
			Integer minCount = p.getMinCount();
			if (minCount != null && minCount>0) {
				
				String value = minCount==1 ? " value" : " values";
				
				block._if(JOp.cor(localVar.eq(JExpr._null()), JOp.lt(localVar.invoke("size"), JExpr.lit(minCount))))
					._then()._throw(
					JExpr._new(dataWriter.getValidationExceptionClass()).arg(
							JExpr.lit(subjectClass.getLocalName() + " " + fieldName + " must contain at least " +
									minCount + value)
						));
			}
			Integer maxCount = p.getMaxCount();
			if (maxCount !=null) {
				String value = maxCount==1 ? " value" : " values";
				block._if(JOp.cand(localVar.ne(JExpr._null()), JOp.gt(localVar.invoke("size"), JExpr.lit(maxCount))))
					._then()._throw(
							JExpr._new(dataWriter.getValidationExceptionClass()).arg(
									JExpr.lit(subjectClass.getLocalName() + " " + fieldName + " may contain no more than " +
											maxCount + value)
								));
					
			}
			
			String elementName = p.getPredicate().getLocalName();
			
			JBlock thenBlock = block._if(localVar.ne(JExpr._null()))._then();
			thenBlock.invoke(jsonVar, "writeFieldName").arg(JExpr.lit(p.getPredicate().getLocalName()));
			thenBlock.invoke(jsonVar, "writeStartArray");
			
			JForEach forEach = thenBlock.forEach(valueClass, elementName, localVar);
			
			if (datatype != null) {
				Class<?> fieldJavaType = dataWriter.javaDatatype(datatype);
				dataWriter.writeSimpleValue(forEach.body(), jsonVar, fieldJavaType, forEach.var());
			} else if (valueShape != null) {

				String writerClassName = avonWriterJavaClassName(valueShape);

				String writerVarName = StringUtil.firstLetterLowerCase(p.getPredicate().getLocalName() + "AvonWriter");

				JVar writerVar = dc.fields().get(writerVarName);
				if (writerVar == null) {
					JClass writerClass = ref(writerClassName);
					writerVar = dc.field(JMod.PRIVATE, writerClass, writerVarName, JExpr._new(writerClass));
				}
				
				forEach.body().invoke(writerVar, "write").arg(forEach.var()).arg(outVar);
			} else if (p.getValueClass() instanceof URI) {
				JClass namespacesClass = ref(dataWriter.getJavaNamer().namespacesClass());
				
				JVar elementIdVar = forEach.body().decl(uriClass, elementName + "Id", forEach.var().invoke("getId"));
				forEach.body()._if(JOp.eq(elementIdVar, JExpr._null()))._then()._throw(
						JExpr._new(dataWriter.getValidationExceptionClass()).arg(
								JExpr.lit(subjectClass.getLocalName() + " " + elementName + " must have a URI as an Id ")
							));
				
				forEach.body().invoke(jsonVar, "writeString").arg(namespacesClass.staticInvoke("curie").arg(elementIdVar));
			}
			
			thenBlock.invoke(jsonVar, "writeEndArray");
			
			
		}

		private void handleSingleValue(JBlock block, JVar outVar, JVar subjectVar, JVar jsonVar, PropertyConstraint p, URI subjectClass, boolean required) {
			
			Shape valueShape = p.getShape();

			String fieldName = p.getPredicate().getLocalName();
			String getterName = JavaClassBuilder.getterName(fieldName);
			
			if (valueShape != null) {
				
				
				String writerClassName = avonWriterJavaClassName(valueShape);
				String valueClassName = valueType(p);
				JClass valueType = ref(valueClassName);

				String writerSimpleName = StringUtil.javaSimpleName(writerClassName);
				String writerVarName = StringUtil.firstLetterLowerCase(writerSimpleName);

				JVar writerVar = dc.fields().get(writerVarName);
				if (writerVar == null) {
					JClass writerClass = ref(writerClassName);
					writerVar = dc.field(JMod.PRIVATE, writerClass, writerVarName, JExpr._new(writerClass));
				}
				
				JVar localVar = block.decl(valueType, fieldName, subjectVar.invoke(getterName));
				JConditional condition = block._if(localVar.ne(JExpr._null()));
				condition._then().invoke(writerVar, "write").arg(localVar).arg(outVar);
				
				if (required) {
					condition._else()._throw(
						JExpr._new(dataWriter.getValidationExceptionClass()).arg(
								JExpr.lit(subjectClass.getLocalName() + " " + fieldName + " is required")
							)
						);
				}
				
				
					
				
				
				
			} else if (p.getPredicate().equals(Konig.id)) {
				
				JClass uriClass = dataWriter.getModel().ref(URI.class);
				JVar idVar = block.decl(uriClass, "id", JExpr.invoke(subjectVar, "getId"));
				if (required) {
					block._if(idVar.eq(JExpr._null()))._then()._throw(
						JExpr._new(dataWriter.getValidationExceptionClass()).arg(
							JExpr.lit("Id of " + subjectClass.getLocalName() + " is required" )
						)
					);
				}
				block.invoke(jsonVar, "writeStringField").arg(JExpr.lit("id")).arg(idVar.invoke("stringValue"));
			} else {
				
				URI datatype = p.getDatatype();
				Resource valueClassResource = p.getValueClass();
				if (datatype != null) {
					Class<?> fieldJavaType = dataWriter.javaDatatype(datatype);
					JClass fieldType = dataWriter.ref(fieldJavaType);

					JVar localVar = block.decl(fieldType, fieldName, subjectVar.invoke(getterName));
					if (required) {
						block._if(localVar.eq(JExpr._null()))._then()._throw(
						JExpr._new(dataWriter.getValidationExceptionClass()).arg(
							JExpr.lit(subjectClass.getLocalName() + " " + fieldName + " is required" )));
					}
					dataWriter.writeSimpleField(block, jsonVar, fieldName, fieldJavaType, localVar);
				} else if (valueClassResource instanceof URI) {
					URI valueClass = (URI) valueClassResource;
					String valueClassName = dataWriter.getJavaNamer().javaClassName(valueClass);
					JClass fieldType = dataWriter.getModel().ref(valueClassName);
					
					JVar localVar = block.decl(fieldType, fieldName, subjectVar.invoke(getterName));
					if (required) {
						JConditional condition = block._if(localVar.eq(JExpr._null()));
						
						condition._then()._throw(
						JExpr._new(dataWriter.getValidationExceptionClass()).arg(
							JExpr.lit(subjectClass.getLocalName() + " " + fieldName + " is required" )));
						
						String idName = fieldName + "Id";
						JClass uriType = dataWriter.getModel().ref(URI.class);
						JBlock elseBlock = condition._else();
						JVar stringValue = elseBlock.decl(uriType, idName, localVar.invoke("stringValue"));
						
						
						dataWriter.writeSimpleField(elseBlock, jsonVar, fieldName, String.class, stringValue);
						
					} else {

						JBlock thenBlock = block._if(localVar.ne(JExpr._null()))._then();
						
						
						String idName = fieldName + "Id";
						JClass uriType = dataWriter.getModel().ref(URI.class);
						
						JVar idVar = thenBlock.decl(uriType, idName, localVar.invoke("getId"));
						
						JBlock idBlock = thenBlock._if(idVar.ne(JExpr._null()))._then();
							
						dataWriter.writeSimpleField(idBlock, jsonVar, fieldName, String.class, idVar.invoke("stringValue"));
					}
					
							
					
				}
				
			}
			
		}
		
		private JClass ref(Class<?> type) {
			return dataWriter.getModel().ref(type);
		}
		
		private JClass ref(String javaClassName) {
			if (javaClassName == null) {
				return null;
			}
			return dataWriter.getModel().ref(javaClassName);
		}

		private String valueType(PropertyConstraint p) {
			URI owlClass = null;
			Resource valueClass = p.getValueClass();
			if (valueClass instanceof URI) {
				owlClass = (URI) valueClass;
			} else {
				owlClass = p.getDirectValueType();
				if (owlClass == null) {
					Shape valueShape = p.getShape();
					if (valueShape != null) {
						owlClass = valueShape.getTargetClass();
					}
				}
			}
			
			if (owlClass != null) {

				String valueClassName = dataWriter.getJavaNamer().javaClassName(owlClass);
				return valueClassName;
			}
			
			URI datatype = p.getDatatype();
			if (datatype != null) {
				Class<?> javaType = dataWriter.javaDatatype(datatype);
				return javaType.getName();
			}
			
			
			throw new SchemaGeneratorException("Type not found for " + p.getPredicate().getLocalName());
			
		}
		
		
		
		
	}
	
	

	

}
