package io.konig.schemagen;

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


import java.util.Collection;

import org.openrdf.model.Literal;

import io.konig.core.Graph;
import io.konig.core.impl.KonigLiteral;
import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeMediaTypeNamer;

public class ShapeMediaTypeLinker {
	
	private ShapeMediaTypeNamer mediaTypeNamer;
	
	public ShapeMediaTypeLinker(ShapeMediaTypeNamer mediaTypeNamer) {
		this.mediaTypeNamer = mediaTypeNamer;
	}

	/**
	 * Assign the kol:mediaTypeBaseName property to a Shape.
	 * @param shape The Shape that is to be linked to a media type
	 * @param graph The graph that will record the link.
	 */
	public void assignMediaType(Shape shape, Graph graph) {
		String mediaTypeName = mediaTypeNamer.baseMediaTypeName(shape);
		Literal literal = new KonigLiteral(mediaTypeName);
		graph.edge(shape.getId(), Konig.mediaTypeBaseName, literal);
	}
	
	/**
	 * A convenience method that assigns the kol:mediaTypeBaseName to each
	 * shape in a given list.
	 * @param list The list of shapes that are to be linked to a media type
	 * @param graph The graph that will store the links to media types.
	 */
	public void assignAll(Collection<Shape> list, Graph graph) {
		for (Shape shape : list)  {
			assignMediaType(shape, graph);
		}
	}

}
