package io.konig.transform.proto;

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


import io.konig.formula.PathExpression;

public class ProtoPathFromItem implements ProtoFromItem {

	private ShapeModel rootShape;
	private PathExpression path;
	private ProtoFromItem rest;
	
	public ProtoPathFromItem(ShapeModel rootShape, PathExpression path) {
		this.rootShape = rootShape;
		this.path = path;
	}
	
	public ShapeModel getRootShape() {
		return rootShape;
	}
	
	public PathExpression getPath() {
		return path;
	}

	public ProtoFromItem getRest() {
		return rest;
	}

	public void setRest(ProtoFromItem rest) {
		this.rest = rest;
	}

	@Override
	public ProtoFromItem first() {
		return this;
	}

	@Override
	public ProtoFromItem rest() {
		return rest;
	}
	
	
}
