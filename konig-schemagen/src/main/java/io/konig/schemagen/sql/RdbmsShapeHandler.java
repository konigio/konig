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


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.aws.datasource.AwsAurora;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.RelationshipDegree;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;

public class RdbmsShapeHandler implements ShapeVisitor {
	
	private ShapeVisitor callback;
	private RdbmsShapeGenerator generator;
	private ShapeFileGetter fileGetter;
	private ShapeWriter shapeWriter;
	private NamespaceManager nsManager;
	private static final Logger LOG = LoggerFactory.getLogger(RdbmsShapeHandler.class);
	private List<Shape> rdbmsChildShapes = null;
	private Collection<Shape> shapes =null;
	private RdbmsShapeHelper rdbmsShapeHelper;
	
	public RdbmsShapeHandler(
			ShapeVisitor callback,
			RdbmsShapeGenerator generator, ShapeFileGetter fileGetter, ShapeWriter shapeWriter,
			NamespaceManager nsManager, RdbmsShapeHelper rdbmsShapeHelper) {
		this.callback = callback;
		this.generator = generator;
		this.fileGetter = fileGetter;
		this.shapeWriter = shapeWriter;
		this.nsManager = nsManager;
		this.rdbmsShapeHelper=rdbmsShapeHelper;
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
		}
	}

	@Override
	public void visit(Shape shape) {
		
		if (shape.getTabularOriginShape()!=null && !hasParentShape(shape)) {
			
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
				save(rdbmsShape);
				if(!rdbmsChildShapes.isEmpty()) {
					for(Shape rdbmsChildShape : rdbmsChildShapes) {						
						save(rdbmsChildShape);
					}
				}
				if (callback != null) {
					callback.visit(rdbmsShape);
				}
			}
		}

	}
	
	private boolean hasParentShape(Shape shape) {
		for(Shape s:shapes){			
			if(hasParentShape(s,shape.getTabularOriginShape()))
				return true;
		}
		return false;
	}

	private boolean hasParentShape(Shape parentShape, Shape childShape) {
		for(PropertyConstraint pc:parentShape.getProperty()){
			if(pc.getShape()!=null && pc.getShape().getId().equals(childShape.getId())){
				return true;
			}
		}
		return false;
		
	}

	private void save(Shape shape) {
		if (!(shape.getId() instanceof URI)) {
			throw new KonigException("Shape must be identified by a URI");
		}
		File file = fileGetter.getFile((URI)shape.getId());
		try {			
				shapeWriter.writeTurtle(nsManager, shape, file);			
		} catch (Exception e) {
			throw new KonigException(e);
		}
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
