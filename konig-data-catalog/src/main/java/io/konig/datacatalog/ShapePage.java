package io.konig.datacatalog;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShapePage {
	private static final String SHAPE_TEMPLATE = "data-catalog/velocity/shape.vm";

	public void render(ShapeRequest request, PageResponse response) throws DataCatalogException {

		DataCatalogUtil.setSiteName(request);
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		Shape shape = request.getShape();
		Resource shapeId = shape.getId();
		URI shapeURI = null;
		if (shapeId instanceof URI) {
			shapeURI = (URI) shapeId;
			context.put("ShapeId", shapeURI.stringValue());
			context.put("ShapeName", shapeURI.getLocalName());
		} else {
			return;
		}
		URI targetClass = shape.getTargetClass();
		context.put("TargetClass", new Link(targetClass.getLocalName(), targetClass.stringValue()));
		request.setResourceId(shapeURI);
		request.setActiveLink(null);
		
		List<PropertyInfo> propertyList = new ArrayList<>();
		context.put("PropertyList", propertyList);
		for (PropertyConstraint p : shape.getProperty()) {
			propertyList.add(new PropertyInfo(shapeURI, p, request));
		}
		
		Template template = engine.getTemplate(SHAPE_TEMPLATE);
		
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
	}
	
	
}
