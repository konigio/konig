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


import java.text.MessageFormat;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.Vertex;

public class ValueExchange {
	
	private Vertex subject;
	private URI predicate;
	private Value object;
	private PojoExchange pojoExchange;
	private PojoContext context;

	private Object javaSubject;
	private Object javaObject;
	
	private PojoBuilder pojoBuilder;
	private Class<?> javaObjectType;
	private ValueBuilder valueBuilder;
	private boolean structured=false;
	
	private ValueExchange parent;
	private ValueExchange child;

	public ValueExchange() {
	}


	public Vertex getSubject() {
		return subject;
	}


	public void setSubject(Vertex subject) {
		this.subject = subject;
	}


	public URI getPredicate() {
		return predicate;
	}


	public void setPredicate(URI predicate) {
		this.predicate = predicate;
	}


	public Value getObject() {
		return object;
	}


	public void setObject(Value object) {
		this.object = object;
	}


	public PojoContext getContext() {
		return context;
	}


	public void setContext(PojoContext context) {
		this.context = context;
	}


	public Object getJavaSubject() {
		return javaSubject;
	}


	public void setJavaSubject(Object javaSubject) {
		this.javaSubject = javaSubject;
	}


	public Object getJavaObject() {
		return javaObject;
	}


	public void setJavaObject(Object javaObject) {
		this.javaObject = javaObject;
	}


	public PojoBuilder getPojoBuilder() {
		return pojoBuilder;
	}


	public void setPojoBuilder(PojoBuilder pojoBuilder) {
		this.pojoBuilder = pojoBuilder;
	}


	public ValueBuilder getValueBuilder() {
		return valueBuilder;
	}


	public void setValueBuilder(ValueBuilder propertyBuilder) {
		this.valueBuilder = propertyBuilder;
	}

	public String toString() {
		String template = "ValueExchange[predicate={0}]";
		return MessageFormat.format(template, predicate.stringValue());
	}


	public PojoExchange getPojoExchange() {
		return pojoExchange;
	}


	public void setPojoExchange(PojoExchange pojoExchange) {
		this.pojoExchange = pojoExchange;
	}


	public boolean isStructured() {
		return structured;
	}


	public void setStructured(boolean structured) {
		this.structured = structured;
	}


	public Class<?> getJavaObjectType() {
		return javaObjectType;
	}


	public void setJavaObjectType(Class<?> javaObjectType) {
		this.javaObjectType = javaObjectType;
	}

	public ValueExchange getParent() {
		return parent;
	}


	public ValueExchange getChild() {
		return child;
	}


	public void setChild(ValueExchange child) {
		this.child = child;
	}


	public ValueExchange pushClone() {
		
		child = new ValueExchange();
		
		child.setContext(context);
		child.setJavaObject(javaObject);
		child.setJavaObjectType(javaObjectType);
		child.setJavaSubject(javaSubject);
		child.setObject(object);
		child.setPojoBuilder(pojoBuilder);
		child.setPojoExchange(pojoExchange);
		child.setPredicate(predicate);
		child.setStructured(structured);
		child.setSubject(subject);
		child.setValueBuilder(valueBuilder);
		
		child.parent = parent;
		return child;
	}
	
	public ValueExchange push() {
		return push(null, predicate);
	}

	public ValueExchange push(URI predicate) {
		return push(null, predicate);
	}
	

	public ValueExchange push(Vertex childSubject, URI predicate) {
		
		if (childSubject == null && subject != null && object instanceof Resource ) {
			Graph graph = subject.getGraph();
			childSubject = graph.getVertex((Resource)object);
		}
		
		child = new ValueExchange();
		child.setContext(context);
		child.setJavaSubject(javaObject);
		child.setPojoExchange(pojoExchange);
		child.setSubject(childSubject);
		child.setPojoBuilder(pojoBuilder);
		child.setValueBuilder(valueBuilder);
		child.setPredicate(predicate);
		
		child.parent = this;
		
		return child;
	}
	
	public ValueExchange pop() {
		if (parent != null) {
			parent.child = null;
		}
		return parent;
	}
	
	public ValueExchange getRoot() {
		ValueExchange root = this;
		while (root.parent != null) {
			root = root.parent;
		}
		return root;
	}
	
	public String getPath() {
		ValueExchange ve = getRoot();
		StringBuilder builder = new StringBuilder();
		while (ve != null) {
			URI predicate = ve.getPredicate();
			String name = predicate == null ? null : predicate.getLocalName();
			if (name != null) {
				if (builder.length()>0) {
					builder.append('.');
				}
				builder.append(name);
			}
			if (ve == this) {
				break;
			}
			ve = ve.getChild();
		}
		return builder.toString();
	}

}
