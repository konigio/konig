package io.konig.schemagen.java;

import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JConditional;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;

import io.konig.core.vocab.KOL;
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
		
		URI scopeClass = shape.getScopeClass();
		if (scopeClass != null) {
			ShapeMediaTypeNamer mediaTypeNamer = dataWriter.getMediaTypeNamer();
			String mediaTypeBaseName = mediaTypeNamer.baseMediaTypeName(shape);
			String className = mediaTypeBaseName + '.' + scopeClass.getLocalName() + "AvonWriter";
			className = dataWriter.getJavaNamer().writerName(className);
			JCodeModel model = dataWriter.getModel();
			
			String mediaTypeName = mediaTypeBaseName + "+avon";
			
			if (model._getClass(className) == null) {
				try {
					JDefinedClass dc = model._class(className);
					dc._implements(dataWriter.getDataWriterInterface());
					
					JFieldVar mediaTypeField = dc.field(JMod.STATIC | JMod.PUBLIC | JMod.FINAL, String.class, "MEDIA_TYPE");
					mediaTypeField.init(
						JExpr.lit(mediaTypeName)
					);
					
					JMethod method = dc.method(JMod.PUBLIC, String.class, "getMediaType");
					method.body()._return(mediaTypeField);
					method.annotate(Override.class);

					String javaClassName = dataWriter.getJavaNamer().javaClassName(scopeClass);
					JClass javaScopeClass = model.ref(javaClassName);
					
					buildWriteMethod(model, dc, javaScopeClass, shape);
					
					
				} catch (JClassAlreadyExistsException e) {
					throw new SchemaGeneratorException(e);
				}
			}
		}
		
		
		
	}

	private void buildWriteMethod(JCodeModel model, JDefinedClass dc, JClass javaScopeClass, Shape shape) {
		
		URI scopeClass = shape.getScopeClass();
		JMethod method = dc.method(JMod.PUBLIC, void.class, "write");
		JVar dataVar = method.param(Object.class, "data");
		JVar outVar = method.param(dataWriter.getDataSinkClass(), "out");
		method.annotate(Override.class);
		method._throws(dataWriter.getValidationExceptionClass());
		method._throws(model.ref(IOException.class));
		
		JConditional conditional = method.body()._if(dataVar._instanceof(javaScopeClass));
		
		JBlock block = conditional._then();
		JBlock elseBlock = conditional._else();
		
		elseBlock._throw(
			JExpr._new(dataWriter.getValidationExceptionClass()).arg(
				JExpr.lit("Invalid argument.  Expected data object of type " + scopeClass.getLocalName())
			)
		);
		
		JClass jsonGeneratorClass = model.ref(JsonGenerator.class);
		
		JVar jsonVar = block.decl(jsonGeneratorClass, "json", JExpr.invoke(outVar, "getJsonGenerator"));

		JVar subjectVar = block.decl(javaScopeClass, "subject", JExpr.cast(javaScopeClass, dataVar));
		
		block.invoke(jsonVar, "writeStartObject");
		
		for (PropertyConstraint p : shape.getProperty()) {
			handleProperty(block, subjectVar, jsonVar, p, scopeClass);
			
		}
		
		block.invoke(jsonVar, "writeEndObject");
		
		
		
	}

	private void handleProperty(JBlock block, JVar subjectVar, JVar jsonVar, PropertyConstraint p, URI subjectClass) {
		
		Integer maxCount = p.getMaxCount();
		Integer minCount = p.getMinCount();
		
		if (maxCount!=null && maxCount==1) {
			
			boolean required = minCount!=null && minCount==1;
			handleSingleValue(block, subjectVar, jsonVar, p, subjectClass, required);
		}
		
	}

	private void handleSingleValue(JBlock block, JVar subjectVar, JVar jsonVar, PropertyConstraint p, URI subjectClass, boolean required) {
		
		if (p.getPredicate().equals(KOL.id)) {
			
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
				String fieldName = p.getPredicate().getLocalName();

				String getterName = JavaClassBuilder.getterName(fieldName);
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
				String fieldName = p.getPredicate().getLocalName();
				
				String getterName = JavaClassBuilder.getterName(fieldName);
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
	

	

}
