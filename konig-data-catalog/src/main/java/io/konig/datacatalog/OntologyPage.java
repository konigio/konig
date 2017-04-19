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
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.VANN;
import io.konig.shacl.Shape;

public class OntologyPage {

	private static final String ONTOLOGY_TEMPLATE = "data-catalog/velocity/ontology.vm";
	private static final String NAMESPACE_URI = "NamespaceURI";
	private static final String NAMESPACE_PREFIX = "NamespacePrefix";
	private static final String ONTOLOGY_LABEL = "OntologyLabel";
	private static final String ONTOLOGY_DESCRIPTION = "OntologyDescription";
	private static final String CLASS_LIST = "ClassList";
	private static final String SHAPE_LIST = "ShapeList";
	

	public void render(OntologyRequest request, PageResponse response) throws DataCatalogException, IOException {
		Vertex ontology = request.getOntologyVertex();
		URI ontologyId = (URI) ontology.getId();

		DataCatalogUtil.setSiteName(request);
		VelocityContext context = request.getContext();
		context.put(NAMESPACE_URI, ontologyId.stringValue());
		
		request.setPageId(ontologyId);
		request.setActiveLink(null);
		
		Value prefix = ontology.getValue(VANN.preferredNamespacePrefix);
		if (prefix != null) {
			context.put(NAMESPACE_PREFIX, prefix.stringValue());
		}
		Value labelValue = ontology.getValue(RDFS.LABEL);
		String label = labelValue==null ? ontologyId.getLocalName() : labelValue.stringValue();
		context.put(ONTOLOGY_LABEL, label);
		
		String description = RdfUtil.getDescription(ontology);
		if (description != null) {
			context.put(ONTOLOGY_DESCRIPTION, description);
		}
		
		setClassList(request, ontologyId);
		setShapeList(request, ontologyId);

		VelocityEngine engine = request.getEngine();

		Template template = engine.getTemplate(ONTOLOGY_TEMPLATE);
		PrintWriter out = response.getWriter();
		template.merge(context, out);
		out.flush();
		
	}

	private void setShapeList(OntologyRequest request, URI ontologyId) throws DataCatalogException {
		String namespace = ontologyId.getNamespace();
		List<ResourceDescription> result = new ArrayList<>();
		List<Shape> shapeList = request.getShapeManager().listShapes();
		for (Shape shape : shapeList) {
			Resource id = shape.getId();
			if (id instanceof URI) {
				URI shapeId = (URI) id;
				if (namespace.equals(shapeId.getNamespace())) {
					String name = shapeId.getLocalName();
					String href = request.relativePath(ontologyId, shapeId);
					String description = shape.getComment();
					result.add(new ResourceDescription(href, name, description));
				}
			}
		}
		
		if (!result.isEmpty()) {
			request.getContext().put(SHAPE_LIST, result);
		}
		
	}

	private void setClassList(OntologyRequest request, URI ontologyId) throws DataCatalogException {
		String namespace = ontologyId.stringValue();
		List<ResourceDescription> result = new ArrayList<>();
		List<Vertex> list = request.getOwlClassList();
		for (Vertex owlClass : list) {
			Resource id = owlClass.getId();
			if (id instanceof URI) {
				URI classId = (URI) id;
				if (namespace.equals(classId.getNamespace())) {
					String name = classId.getLocalName();
					String href = request.relativePath(ontologyId, classId);
					String description = RdfUtil.getDescription(owlClass);
					
					result.add(new ResourceDescription(href, name, description));
				}
			}
		}
		
		if (!result.isEmpty()) {
			request.getContext().put(CLASS_LIST, result);
		}
		
	}
}
