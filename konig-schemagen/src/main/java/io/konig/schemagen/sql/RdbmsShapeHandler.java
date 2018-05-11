package io.konig.schemagen.sql;

import java.io.File;
import java.util.Collection;

import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;

public class RdbmsShapeHandler implements ShapeVisitor {
	
	private RdbmsShapeGenerator generator;
	private ShapeFileGetter fileGetter;
	private ShapeWriter shapeWriter;
	private NamespaceManager nsManager;
	
	
	
	public RdbmsShapeHandler(RdbmsShapeGenerator generator, ShapeFileGetter fileGetter, ShapeWriter shapeWriter,
			NamespaceManager nsManager) {
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

	@Override
	public void visit(Shape shape) {
		
		if (isRdbmsShape(shape)) {
		
			Shape rdbmsShape = generator.createRdbmsShape(shape);

			// The generator will return a null value if the supplied shape is already
			// suitable for use in an RDBMS according to our standards.
			
			// If the return value is not null then we need to edit the original shape, 
			// save those changes to the Turtle file for the original shape,
			// and save the new RDBMS shape to a new file.
			
			if (rdbmsShape != null) {
				editOriginalShape(shape);
				save(shape);
				save(rdbmsShape);
			}
		}

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



	/**
	 * Remove the RDBMS data source(s) from the given shape.
	 */
	private void editOriginalShape(Shape shape) {
		// TODO Auto-generated method stub
		
	}



	/**
	 * Returns true if Shape has DataSource of type GoogleBigQueryTable, GoogleCloudSqlTable, or AwsAurora
	 * @param shape
	 * @return
	 */
	private boolean isRdbmsShape(Shape shape) {
		// TODO Auto-generated method stub
		return false;
	}

}
