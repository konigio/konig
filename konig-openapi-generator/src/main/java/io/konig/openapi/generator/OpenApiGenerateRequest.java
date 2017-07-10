package io.konig.openapi.generator;

/*
 * #%L
 * Konig OpenAPI Generator
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
