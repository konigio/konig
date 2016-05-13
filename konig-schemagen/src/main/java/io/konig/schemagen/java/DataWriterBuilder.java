package io.konig.schemagen.java;

import java.io.IOException;
import java.io.OutputStream;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JVar;

import io.konig.core.Graph;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.ShapeMediaTypeNamer;

public class DataWriterBuilder {
	private static final Logger logger = LoggerFactory.getLogger(DataWriterBuilder.class);
	private JPackage writerPackage;
	private JClass dataWriterInterface;
	private JCodeModel model;
	private JavaNamer javaNamer;
	private ShapeMediaTypeNamer mediaTypeNamer;
	private JClass dataSinkClass;
	private JClass validationExceptionClass;
	private JavaDatatypeMapper datatypeMapper;
	private Graph ontology;
	
	public DataWriterBuilder(
			Graph ontology, JavaDatatypeMapper datatypeMapper, 
			ShapeMediaTypeNamer mediaTypeNamer, JavaNamer javaNamer, JCodeModel model) {
		this.ontology = ontology;
		this.datatypeMapper = datatypeMapper;
		this.mediaTypeNamer = mediaTypeNamer;
		this.javaNamer = javaNamer;
		this.writerPackage = model._package(javaNamer.writerName(null));
		this.model = model;

		dataSinkClass = buildDataSinkClass();
		validationExceptionClass = buildValidationExceptionClass();
		dataWriterInterface = buildDataWriterInterface();
	}
	
	public JavaDatatypeMapper getDatatypeMapper() {
		return datatypeMapper;
	}

	public Graph getOntology() {
		return ontology;
	}

	public JClass getValidationExceptionClass() {
		return validationExceptionClass;
	}



	private JClass buildValidationExceptionClass() {

		try {
			JDefinedClass dc = writerPackage._class("ValidationException");
			dc._extends(RuntimeException.class);
			
			JMethod ctor = dc.constructor(JMod.PUBLIC);
			JVar messageVar = ctor.param(String.class, "message");
			JVar causeVar = ctor.param(Throwable.class, "cause");
			
			JBlock block = ctor.body();
			block.invoke("super").arg(messageVar).arg(causeVar);
			
			
			ctor = dc.constructor(JMod.PUBLIC);
			messageVar = ctor.param(String.class, "message");
			block = ctor.body();
			block.invoke("super").arg(messageVar);
			
			return dc;
		} catch (JClassAlreadyExistsException e) {
			throw new SchemaGeneratorException(e);
		}
	}


	public JClass getDataSinkClass() {
		return dataSinkClass;
	}


	private JClass buildDataSinkClass() {
		
		try {
			JDefinedClass dc = writerPackage._class("DataSink");
			
			JVar jsonVar = dc.field(JMod.PRIVATE, JsonGenerator.class, "json");
			
			JMethod ctor = dc.constructor(JMod.PUBLIC);
			JVar outVar = ctor.param(OutputStream.class, "out");
			
			JBlock body = ctor.body();

			JClass jsonFactoryClass = model.ref(JsonFactory.class);
			JVar factoryVar = body.decl(jsonFactoryClass, "jsonFactory", JExpr._new(jsonFactoryClass));

			body.assign(jsonVar, factoryVar.invoke("createGenerator").arg(outVar));
			body.invoke(jsonVar, "useDefaultPrettyPrinter");
			
			
			dc.method(JMod.PUBLIC, JsonGenerator.class, "getJsonGenerator")
				.body()._return(jsonVar);
			
			
			
			return dc;
		} catch (JClassAlreadyExistsException e) {
			throw new SchemaGeneratorException(e);
		}
	}

	public JavaNamer getJavaNamer() {
		return javaNamer;
	}



	public ShapeMediaTypeNamer getMediaTypeNamer() {
		return mediaTypeNamer;
	}

	public JPackage getWriterPackage() {
		return writerPackage;
	}

	public JClass getDataWriterInterface() {
		return dataWriterInterface;
	}
	
	public JClass datatype(URI owlClass) {
		Class<?> javaClass = datatypeMapper.javaDatatype(owlClass, ontology);
		return model.ref(javaClass);
	}
	
	public Class<?> javaDatatype(URI owlClass) {
		return datatypeMapper.javaDatatype(owlClass, ontology);
	}
	
	public JClass ref(Class<?> clazz) {
		return model.ref(clazz);
	}
	
	public void writeSimpleField(JBlock block, JVar jsonGenerator, String fieldName, Class<?> fieldType, JExpression fieldValue) {
		if (fieldType == String.class) {
			block.invoke(jsonGenerator, "writeStringField").arg(JExpr.lit(fieldName)).arg(fieldValue);
		} else {
			// TODO: handle other types
			logger.warn("Ignoring simple field of type "  + fieldType.getSimpleName()  );
		}
	}
	
	public void writeSimpleValue(JBlock block, JVar jsonGenerator, Class<?> fieldType, JExpression value) {
		if (fieldType == String.class) {
			block.invoke(jsonGenerator, "writeString").arg(value);
		} else {
			// TODO: handle other types
			logger.warn("Ignoring simple field of type " + fieldType.getSimpleName());
		}
	}


	public JCodeModel getModel() {
		return model;
	}


	public JDefinedClass buildDataWriterInterface() throws SchemaGeneratorException {
		
		JDefinedClass dc;
		try {
			dc = writerPackage._interface("DataWriter");
			
			JClass objectType = model.ref(Object.class);

			dc.method(0, String.class, "getMediaType");
			
			JMethod method = dc.method(0, void.class, "write");
			method.param(objectType, "data");
			method.param(dataSinkClass, "out");
			
			JClass ioException = model.ref(IOException.class);
			
			method._throws(validationExceptionClass);
			method._throws(ioException);
			
			return dc;
		} catch (JClassAlreadyExistsException e) {
			throw new SchemaGeneratorException(e);
		}
		
	}
	

}
