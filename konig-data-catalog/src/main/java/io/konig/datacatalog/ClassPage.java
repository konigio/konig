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
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Vertex;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ClassPage {
	private static final String CLASS_TEMPLATE = "data-catalog/velocity/class.vm";

	
	public void render(ClassRequest request, PageResponse response) throws DataCatalogException, IOException {
		ClassStructure structure = request.getClassStructure();
		
		Vertex owlClass = request.getOwlClass();

		DataCatalogUtil.setSiteName(request);
		Shape shape = structure.getShapeForClass(owlClass.getId());

		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		
		URI classId = (URI) owlClass.getId();
		request.setPageId(classId);
		request.setActiveLink(null);
		
		context.put("ClassName", classId.getLocalName());
		context.put("ClassId", classId.stringValue());
		setShapes(request, classId);
		
		List<PropertyInfo> propertyList = new ArrayList<>();
		context.put("PropertyList", propertyList);
		for (PropertyConstraint p : shape.getProperty()) {
			if (RDF.TYPE.equals(p.getPredicate())) {
				continue;
			}
			propertyList.add(new PropertyInfo(classId, p, request));
		}
		
		DataCatalogUtil.sortProperties(propertyList);
		
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
					String href = request.relativePath(classId, shapeId);
					
					linkList.add(new Link(shapeName, href));
				}
			}
			if (!linkList.isEmpty()) {
				request.getContext().put("ShapeList", linkList);
			}
		}
		
	}

}
