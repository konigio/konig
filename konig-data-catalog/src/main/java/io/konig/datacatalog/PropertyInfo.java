package io.konig.datacatalog;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;

public class PropertyInfo {
	private PropertyConstraint constraint;
	private String predicateId;
	private String predicateLocalName;
	private String typeName;
	private String description;
	public PropertyInfo(PropertyConstraint constraint, PageRequest request) {
		this.constraint = constraint;
		predicateId = constraint.getPredicate().stringValue();
		predicateLocalName = constraint.getPredicate().getLocalName();
		if (constraint.getDatatype() != null) {
			typeName = constraint.getDatatype().getLocalName();
		}
		description = RdfUtil.getDescription(constraint, request.getGraph());
		if (description == null) {
			description = "";
		}
	}
	public PropertyConstraint getConstraint() {
		return constraint;
	}
	public String getPredicateId() {
		return predicateId;
	}
	public String getPredicateLocalName() {
		return predicateLocalName;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getDescription() {
		return description;
	}
	
}