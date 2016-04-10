package io.konig.schemagen;

import java.util.Collection;

import org.openrdf.model.Literal;

import io.konig.core.Graph;
import io.konig.core.impl.KonigLiteral;
import io.konig.core.vocab.KOL;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeMediaTypeNamer;

public class ShapeMediaTypeLinker {
	
	private ShapeMediaTypeNamer mediaTypeNamer;
	
	public ShapeMediaTypeLinker(ShapeMediaTypeNamer mediaTypeNamer) {
		this.mediaTypeNamer = mediaTypeNamer;
	}

	/**
	 * Assign the kol:mediaTypeBaseName property to a Shape.
	 * @param shape The Shape that is to be linked to a media type
	 * @param graph The graph that will record the link.
	 */
	public void assignMediaType(Shape shape, Graph graph) {
		String mediaTypeName = mediaTypeNamer.baseMediaTypeName(shape);
		Literal literal = new KonigLiteral(mediaTypeName);
		graph.edge(shape.getId(), KOL.mediaTypeBaseName, literal);
	}
	
	/**
	 * A convenience method that assigns the kol:mediaTypeBaseName to each
	 * shape in a given list.
	 * @param list The list of shapes that are to be linked to a media type
	 * @param graph The graph that will store the links to media types.
	 */
	public void assignAll(Collection<Shape> list, Graph graph) {
		for (Shape shape : list)  {
			assignMediaType(shape, graph);
		}
	}

}
