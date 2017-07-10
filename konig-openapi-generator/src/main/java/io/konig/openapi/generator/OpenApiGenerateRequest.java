package io.konig.openapi.generator;

import java.io.Reader;
import java.io.Writer;

import io.konig.shacl.ShapeManager;

public class OpenApiGenerateRequest {
	private ShapeManager shapeManager;
	private Reader openApiInfo;
	private Writer writer;

	public OpenApiGenerateRequest() {
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public OpenApiGenerateRequest setShapeManager(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
		return this;
	}

	public Reader getOpenApiInfo() {
		return openApiInfo;
	}

	public OpenApiGenerateRequest setOpenApiInfo(Reader openApiInfo) {
		this.openApiInfo = openApiInfo;
		return this;
	}

	public Writer getWriter() {
		return writer;
	}
	

	public OpenApiGenerateRequest setWriter(Writer writer) {
		this.writer = writer;
		return this;
	}
	
	

}
