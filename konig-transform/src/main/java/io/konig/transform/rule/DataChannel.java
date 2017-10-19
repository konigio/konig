package io.konig.transform.rule;

/*
 * #%L
 * Konig Transform
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


import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public class DataChannel extends AbstractPrettyPrintable implements Comparable<DataChannel>, FromItem {

	private String name;
	private Shape shape;
	private JoinStatement joinStatement;
	private DataSource datasource;
	private ShapeRule parent;
	private String variableName;
	
	public DataChannel(String name, Shape value) {
		this.name = name;
		this.shape = value;
	}

	public DataSource getDatasource() {
		return datasource;
	}

	public void setDatasource(DataSource datasource) {
		this.datasource = datasource;
	}

	@Override
	public int compareTo(DataChannel other) {
		return name.compareTo(other.name);
	}
	

	public String getName() {
		return name;
	}

	public Shape getShape() {
		return shape;
	}

	public JoinStatement getJoinStatement() {
		return joinStatement;
	}

	public void setJoinStatement(JoinStatement joinStatement) {
		this.joinStatement = joinStatement;
	}


	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("name", name);
		out.beginObjectField("shape", shape);
		out.field("id", shape.getId());
		out.endObjectField(shape);
		out.field("joinStatement", joinStatement);
		out.endObject();		
	}


	public ShapeRule getParent() {
		return parent;
	}


	public void setParent(ShapeRule parent) {
		this.parent = parent;
	}

	/**
	 * Get the name of the variable bound to this DataChannel.
	 * @return The name of the variable bound to this DataChannel, or null if there is no such variable.
	 */
	public String getVariableName() {
		return variableName;
	}

	/**
	 * Set the name of the variable bound to this DataChannel.
	 * @param variableName The name of the variable bound to this DataChannel.
	 */
	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}
	
	
	
}
