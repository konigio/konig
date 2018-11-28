package io.konig.shacl.graph;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;

public class GraphPropertyShape implements Traversable {
	private GraphNodeShape declaringShape;
	private URI predicate;
	private PropertyConstraint propertyConstraint;
	private GraphPropertyGroup group;
	
	
	public GraphPropertyShape(GraphNodeShape declaringShape, URI predicate, PropertyConstraint propertyConstraint) {
		this.declaringShape = declaringShape;
		this.predicate = predicate;
		this.propertyConstraint = propertyConstraint;
	}
	
	public GraphNodeShape getDeclaringShape() {
		return declaringShape;
	}
	public URI getPredicate() {
		return predicate;
	}
	public PropertyConstraint getPropertyConstraint() {
		return propertyConstraint;
	}
	
	public GraphPropertyGroup getGroup() {
		return group;
	}

	public void setGroup(GraphPropertyGroup group) {
		this.group = group;
	}

	@Override
	public String getPath() {
		
		List<String> elements = new ArrayList<>();
		GraphNodeShape node = null;
		for (GraphPropertyShape p=this; p!=null; p=node.getAccessor()) {
			elements.add(p.getPredicate().getLocalName());
			node = p.getDeclaringShape();
		}
		StringBuilder builder = new StringBuilder();
		builder.append('{');
		builder.append(RdfUtil.localName(node.getShape().getId()));
		builder.append('}');
		for (int i=elements.size()-1; i>=0; i--) {
			builder.append('.');
			builder.append(elements.get(i));
		}
		
		return builder.toString();
	}

	public String toString() {
		return getPath();
	}

}
