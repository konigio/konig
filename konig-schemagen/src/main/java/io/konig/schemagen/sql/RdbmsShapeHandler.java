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
import io.konig.gcp.datasource.GoogleBigQueryTable;
import io.konig.gcp.datasource.GoogleCloudSqlTable;
import io.konig.shacl.PropertyConstraint;
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
	
	public RdbmsShapeHandler(
			ShapeVisitor callback,
			RdbmsShapeGenerator generator, ShapeFileGetter fileGetter, ShapeWriter shapeWriter,
			NamespaceManager nsManager) {
		this.callback = callback;
		this.generator = generator;
		this.fileGetter = fileGetter;
		this.shapeWriter = shapeWriter;
		this.nsManager = nsManager;
	}

	public void visitAll(Collection<Shape> shapeList) {
		for (Shape shape : shapeList) {
			visit(shape);
		}
	}
	
	private void addRdbmsChildShape(Shape parentShape,URI relationshipProperty, PropertyConstraint pc) throws RDFParseException, IOException {
		Shape childShape = null;
		if(pc != null) {
			childShape = pc.getShape();
			Shape rdbmsChildShape = generator.createOneToManyChildShape(parentShape, relationshipProperty ,childShape);
			if(rdbmsChildShape != null) {
				rdbmsChildShapes.add(rdbmsChildShape);
			}
		} else {
			childShape = parentShape;
		}
		for (PropertyConstraint p : childShape.getProperty()) {
			if(p.getShape() != null){
				addRdbmsChildShape(childShape,p.getPredicate(), p);
			}
		}
	}

	@Override
	public void visit(Shape shape) {
		
		if (isRdbmsShape(shape)) {
			
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
				String rdbmsShapeId = getRdbmsShapeId(shape);
				if(rdbmsShapeId == null) {
					return;
				}
				editOriginalShape(shape);
				save(shape);				
				rdbmsShape.setId(new URIImpl(rdbmsShapeId));
				save(rdbmsShape);
				if(!rdbmsChildShapes.isEmpty()) {
					for(Shape rdbmsChildShape : rdbmsChildShapes) {
						String rdbmsChildShapeId = getRdbmsShapeId(rdbmsChildShape);
						if(rdbmsChildShapeId == null) {
							return;
						}
						rdbmsChildShape.setId(new URIImpl(rdbmsChildShapeId));
						save(rdbmsChildShape);
					}
				}
				if (callback != null) {
					callback.visit(rdbmsShape);
				}
			}
		}

	}
	
	private String getRdbmsShapeId(Shape shape) {
		URI shapeId=(URI)shape.getId();				
		String rdbmsShapeId = shapeId.toString().replaceAll(generator.getShapeIriPattern(),generator.getShapeIriReplacement());		
		if(shapeId.toString().equals(rdbmsShapeId)){
			LOG.warn("The IRI for the original shape does not match the regular expression");
			return null;
		}
		return rdbmsShapeId;
	}

	private void save(Shape shape) {
		if (!(shape.getId() instanceof URI)) {
			throw new KonigException("Shape must be identified by a URI");
		}
		File file = fileGetter.getFile((URI)shape.getId());
		try {
			if(!file.exists()){
				shapeWriter.writeTurtle(nsManager, shape, file);
			}
		} catch (Exception e) {
			throw new KonigException(e);
		}
	}



	/**
	 * Remove the RDBMS data source(s) from the given shape.
	 */
	private void editOriginalShape(Shape shape) {
		shape.setShapeDataSource(null);		
	}



	/**
	 * Returns true if Shape has DataSource of type GoogleBigQueryTable, GoogleCloudSqlTable, or AwsAurora
	 * @param shape
	 * @return
	 */
	private boolean isRdbmsShape(Shape shape) {
		boolean isRdbmsShape = false;
		GoogleBigQueryTable bigQueryTable = shape.findDataSource(GoogleBigQueryTable.class);
		AwsAurora auroraTable = shape.findDataSource(AwsAurora.class);
		GoogleCloudSqlTable gcpSqlTable = shape.findDataSource(GoogleCloudSqlTable.class);
		if (bigQueryTable!= null || auroraTable !=null || gcpSqlTable != null){
			isRdbmsShape = true;
		}
		return isRdbmsShape;
	}

}
