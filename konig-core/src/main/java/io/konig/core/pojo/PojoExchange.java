package io.konig.core.pojo;

/*
 * #%L
 * Konig Core
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


import org.openrdf.model.Literal;

import io.konig.core.Vertex;

public class PojoExchange {

	/**
	 * The vertex representing the individual to be unmarshalled as a POJO.
	 * One of {@link #vertex} or {@link #value} must be defined.
	 * 
	 */
	private Vertex vertex;
	
	/**
	 * A literal used to create the POJO that represents the RDF individual.
	 * The literal will be passed to the POJO constructor.
	 * One of {@link #vertex} or {@link #value} must be defined.
	 */
	private Literal value;
	private Class<?> type;
	private PojoContext context;
	private PojoBuilder builder;
	
	
	private Object pojo;
	private ValueBuilder propertyBuilder;
	private String errorMessage;
	
	public PojoExchange() {
		
	}
	
	public PojoExchange(Class<?> type, PojoContext context) {
		this.type = type;
		this.context = context;
	}

	public Class<?> getJavaClass() {
		return type;
	}

	public void setJavaClass(Class<?> type) {
		this.type = type;
	}

	public PojoContext getContext() {
		return context;
	}

	public void setContext(PojoContext context) {
		this.context = context;
	}

	public PojoBuilder getPojoBuilder() {
		return builder;
	}

	public void setBuilder(PojoBuilder builder) {
		this.builder = builder;
	}

	public Object getPojo() {
		return pojo;
	}

	public void setPojo(Object pojo) {
		this.pojo = pojo;
	}

	public Vertex getVertex() {
		return vertex;
	}

	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}

	public ValueBuilder getValueBuilder() {
		return propertyBuilder;
	}

	public void setValueBuilder(ValueBuilder propertyBuilder) {
		this.propertyBuilder = propertyBuilder;
	}

	public Literal getValue() {
		return value;
	}

	public void setValue(Literal value) {
		this.value = value;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	

}
