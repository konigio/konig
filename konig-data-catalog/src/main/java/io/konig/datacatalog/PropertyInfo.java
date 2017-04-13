package io.konig.datacatalog;

import org.openrdf.model.URI;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyStructure;

public class PropertyInfo {
	private PropertyConstraint constraint;
	private String predicateId;
	private String predicateLocalName;
	private String propertyHref;
	private String typeName;
	private String typeHref;
	private String description;
	
	public PropertyInfo(URI resourceId, PropertyConstraint constraint, PageRequest request) throws DataCatalogException {
		this.constraint = constraint;
		predicateId = constraint.getPredicate().stringValue();
		predicateLocalName = constraint.getPredicate().getLocalName();
		if (constraint.getDatatype() != null) {
			typeName = constraint.getDatatype().getLocalName();
		} else if (constraint.getValueClass() instanceof URI) {
			URI valueClass = (URI) constraint.getValueClass();
			typeName = valueClass.getLocalName();
			typeHref = request.relativePath(resourceId, valueClass);
		} else if (constraint.getShape() != null) {
			URI targetClass = constraint.getShape().getTargetClass();
			if (targetClass != null) {
				typeName = targetClass.getLocalName();
			}
			typeHref = request.relativePath(resourceId, targetClass);
		}
		description = RdfUtil.getDescription(constraint, request.getGraph());
		if (description == null) {
			PropertyStructure structure = request.getClassStructure().getProperty(constraint.getPredicate());
			if (structure != null) {
				description = structure.description();
			}
			if (description == null) {
				description = "";
			}
		}
		propertyHref = request.relativePath(resourceId, constraint.getPredicate());
	}
	
	public String getTypeHref() {
		return typeHref;
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

	public String getPropertyHref() {
		return propertyHref;
	}
	
}