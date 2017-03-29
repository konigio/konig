package io.konig.datacatalog;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class DataCatalogBuilder {
	
	private ShapeWriterFactory shapeWriterFactory;
	private ClassIndexWriterFactory classIndexWriterFactory;
	
	public DataCatalogBuilder() {
	}

	public void build(File baseDir, Graph graph, ShapeManager shapeManager) throws DataCatalogException {

		classIndexWriterFactory = new ClassIndexWriterFactory(baseDir);
		shapeWriterFactory = new ShapeWriterFactory(new File(baseDir, "shapes"));
		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		
		VelocityEngine engine = new VelocityEngine(properties);
		VelocityContext context = new VelocityContext();
		

		PageRequest request = new PageRequest(engine, context, graph, shapeManager);
		try {
			buildShapePages(request);
			buildClassIndex(request);
		} catch (IOException e) {
			throw new DataCatalogException(e);
		}
		
	
		
	}

	private void buildClassIndex(PageRequest request) throws IOException, DataCatalogException {
		
		ClassIndexPage page = new ClassIndexPage();
		
		PageResponse response = new PageResponseImpl(classIndexWriterFactory.createWriter(request, null));
		page.render(request, response);
		
	}

	private void buildShapePages(PageRequest baseRequest) throws IOException, DataCatalogException {

		ShapeRequest request = new ShapeRequest(baseRequest);
		ShapeManager shapeManager = request.getShapeManager();
		ShapePage shapePage = new ShapePage();
		
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;
				request.setShape(shape);
				PageResponse response = new PageResponseImpl(shapeWriterFactory.createWriter(request, shapeURI));
				
				shapePage.render(request, response);
			}
		}
		
	}

}
