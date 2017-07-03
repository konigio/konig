package io.konig.openapi.generator;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.Shape;

public class Entity implements Comparable<Entity> {
	private URI owlClass;
	private List<Shape> shapeList = new ArrayList<>();
	private List<MediaType> mediaTypeList = new ArrayList<>();
	
	public Entity(URI owlClass) {
		this.owlClass = owlClass;
	}
	
	public void addShape(Shape shape) {
		shapeList.add(shape);
	}

	public URI getOwlClass() {
		return owlClass;
	}
	
	public void addMediaType(MediaType mediatype) {
		mediaTypeList.add(mediatype);
	}

	public List<MediaType> getMediaTypeList() {
		return mediaTypeList;
	}

	@Override
	public int compareTo(Entity other) {
		String a = owlClass.getLocalName().toLowerCase();
		String b = other.owlClass.getLocalName().toLowerCase();
		
		return a.compareTo(b);
	}
	
	
}
