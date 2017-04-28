package io.konig.schemagen.jsonschema;

import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.ShapeIdGenerator;
import io.konig.core.util.ValueFormat;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class TemplateJsonSchemaNamer extends ShapeIdGenerator implements JsonSchemaNamer {
	
	public TemplateJsonSchemaNamer(NamespaceManager nsManager, ShapeManager shapeManager, ValueFormat template) {
		super(nsManager, shapeManager, template);
	}

	@Override
	public String schemaId(Shape shape) {
		if (shape.getId() instanceof URI) {
			return forShape((URI)shape.getId()).stringValue();
		}
		throw new KonigException("Shape must be identified by a URI");
	}

	@Override
	public String jsonSchemaFileName(Shape shape) {
		URI shapeId = (URI) shape.getId();
		StringBuilder builder = new StringBuilder();
		builder.append(namespace(shapeId).getPrefix());
		builder.append('.');
		
		return builder.toString();
	}

}
