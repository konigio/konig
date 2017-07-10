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


import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;

public class Entity implements Comparable<Entity> {
	private URI owlClass;
	private List<Shape> shapeList = new ArrayList<>();
	private List<MediaType> mediaTypeList = new ArrayList<>();
	
	public Entity(URI owlClass) {
		this.owlClass = owlClass;
	}
	
	public void addShape(Shape shape) {
		shapeList.add(shape);
	}

	public URI getOwlClass() {
		return owlClass;
	}
	
	public void addMediaType(MediaType mediatype) {
		mediaTypeList.add(mediatype);
	}

	public List<MediaType> getMediaTypeList() {
		return mediaTypeList;
	}

	@Override
	public int compareTo(Entity other) {
		String a = owlClass.getLocalName().toLowerCase();
		String b = other.owlClass.getLocalName().toLowerCase();
		
		return a.compareTo(b);
	}
	
	
}
