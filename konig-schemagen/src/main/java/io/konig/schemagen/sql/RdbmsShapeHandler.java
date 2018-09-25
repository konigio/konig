package io.konig.schemagen.sql;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.KonigException;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class RdbmsShapeHandler implements ShapeVisitor {
	
	private ShapeVisitor callback;
	private RdbmsShapeGenerator generator;
	private static final Logger LOG = LoggerFactory.getLogger(RdbmsShapeHandler.class);
	private List<Shape> rdbmsChildShapes = null;
	private Collection<Shape> shapes =null;
	
	public RdbmsShapeHandler(
			ShapeVisitor callback,
			RdbmsShapeGenerator generator) {
		this.callback = callback;
		this.generator = generator;
	}

	public void visitAll(Collection<Shape> shapeList) {
		shapes = shapeList;
		for (Shape shape : shapeList) {
			visit(shape);
		}
	}
	
	private void addRdbmsChildShape(Shape parentShape,URI relationshipProperty, PropertyConstraint pc) throws RDFParseException, IOException {
		Shape childShape = null;
		if(pc != null) {
			childShape = getRdbmsShapeFromLogicalShape(pc.getShape());
			Shape rdbmsChildShape = generator.createOneToManyChildShape(parentShape, relationshipProperty ,childShape);
			if(rdbmsChildShape != null) {
				rdbmsChildShapes.add(rdbmsChildShape);
			}
		} else {
			childShape = parentShape;
		}
		for (PropertyConstraint p : childShape.getTabularOriginShape().getProperty()) {
			if(p.getShape() != null && p.getMaxCount()==null){
				addRdbmsChildShape(childShape,p.getPredicate(), p);
			}
			if(p.getMaxCount() == null && p.getShape() == null && childShape.getNodeKind() ==  NodeKind.IRI) {
				addRdbmsChildShape(childShape,p.getPredicate(), p);
			}
		}
	}

	@Override
	public void visit(Shape shape) {
		
		if (shape.getTabularOriginShape()!=null && noProperties(shape)) {
			
			Shape rdbmsShape = null;
			rdbmsChildShapes =  new ArrayList<>();
			try {
				rdbmsShape = generator.createRdbmsShape(shape);
				addRdbmsChildShape(shape, null, null);
			} catch (Exception e) {
				throw new KonigException(e);
			}

			// The generator will return a null value if the supplied shape is already
			// suitable for use in an RDBMS according to our standards.
			
			// If the return value is not null then we need to edit the original shape, 
			// save those changes to the Turtle file for the original shape,
			// and save the new RDBMS shape to a new file.
			
			if (rdbmsShape != null) {	
				rdbmsShape.setId(shape.getId());
				if (callback != null) {
					callback.visit(rdbmsShape);
				}
			}
		}

	}
	

	private boolean noProperties(Shape shape) {
		
		return shape.getProperty()==null || shape.getProperty().isEmpty();
	}

	
	public Shape getRdbmsShapeFromLogicalShape(Shape childShape) {
		for(Shape shape:shapes){
			if(shape.getTabularOriginShape()!=null && shape.getTabularOriginShape().getId().equals(childShape.getId())){
				return shape;
			}
		}
		return null;
	}

}
