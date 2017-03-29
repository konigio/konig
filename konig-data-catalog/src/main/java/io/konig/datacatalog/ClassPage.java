package io.konig.datacatalog;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Vertex;
import io.konig.shacl.ClassManager;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ClassPage {
	private static final String CLASS_TEMPLATE = "data-catalog/velocity/class.vm";

	
	public void render(ClassRequest request, PageResponse response) throws DataCatalogException, IOException {
		ClassManager classManager = request.getClassManager();
		
		Vertex owlClass = request.getOwlClass();

		
		Shape shape = classManager.getLogicalShape(owlClass.getId());

		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		
		URI classId = (URI) owlClass.getId();
		
		context.put("ClassName", classId.getLocalName());
		context.put("ClassId", classId.stringValue());
		setShapes(request, classId);
		
		List<PropertyInfo> propertyList = new ArrayList<>();
		context.put("PropertyList", propertyList);
		for (PropertyConstraint p : shape.getProperty()) {
			propertyList.add(new PropertyInfo(classId, p, request));
		}
		
		Template template = engine.getTemplate(CLASS_TEMPLATE);

		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}


	private void setShapes(ClassRequest request, URI classId) throws DataCatalogException {
		
		List<Shape> shapeList = request.getShapeManager().getShapesByTargetClass(classId);
		if (!shapeList.isEmpty()) {
			List<Link> linkList = new ArrayList<>();
			for (Shape shape : shapeList) {
				Resource id = shape.getId();
				if (id instanceof URI) {
					URI shapeId = (URI) id;
					String shapeName = shapeId.getLocalName();
					String href = DataCatalogUtil.relativePath(request, classId, shapeId);
					
					linkList.add(new Link(shapeName, href));
				}
			}
			if (!linkList.isEmpty()) {
				request.getContext().put("ShapeList", linkList);
			}
		}
		
	}

}
