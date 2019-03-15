package io.konig.validation;

import org.openrdf.model.URI;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import io.konig.shacl.PropertyConstraint;

public class PropertyShapeReport implements Comparable<PropertyShapeReport>, ReportElement {

	private PropertyConstraint propertyShape;
	private boolean requiresShapeOrIriNodeKind;
	private boolean datatypeWithClass;
	private boolean datatypeWithShape;
	private boolean datatypeWithIriNodeKind;
	private boolean requiresDatatypeClassOrShape;
	private boolean requiresMinCount;
	private boolean requiresDescription;
	private URI invalidXmlSchemaDatatype;
	private TypeConflict typeConflict;
	
	public PropertyShapeReport(PropertyConstraint propertyShape) {
		this.propertyShape = propertyShape;
	}

	public boolean isRequiresShapeOrIriNodeKind() {
		return requiresShapeOrIriNodeKind;
	}

	public void setRequiresShapeOrIriNodeKind(boolean requiresShapeOrIriNodeKind) {
		this.requiresShapeOrIriNodeKind = requiresShapeOrIriNodeKind;
	}

	public PropertyConstraint getPropertyShape() {
		return propertyShape;
	}
	
	public boolean isValid() {
		return 
			!requiresShapeOrIriNodeKind &&
			!datatypeWithClass &&
			!datatypeWithIriNodeKind &&
			!datatypeWithShape &&
			!requiresDatatypeClassOrShape &&
			!requiresDescription &&
			invalidXmlSchemaDatatype==null &&
			typeConflict==null;
	}

	public boolean isDatatypeWithClass() {
		return datatypeWithClass;
	}

	public void setDatatypeWithClass(boolean datatypeWithClass) {
		this.datatypeWithClass = datatypeWithClass;
	}

	public boolean isDatatypeWithShape() {
		return datatypeWithShape;
	}

	public void setDatatypeWithShape(boolean datatypeWithShape) {
		this.datatypeWithShape = datatypeWithShape;
	}

	public boolean isDatatypeWithIriNodeKind() {
		return datatypeWithIriNodeKind;
	}

	public void setDatatypeWithIriNodeKind(boolean datatypeWithIriNodeKind) {
		this.datatypeWithIriNodeKind = datatypeWithIriNodeKind;
	}

	public TypeConflict getTypeConflict() {
		return typeConflict;
	}

	public void setTypeConflict(TypeConflict typeConflict) {
		this.typeConflict = typeConflict;
	}

	public boolean isRequiresDatatypeClassOrShape() {
		return requiresDatatypeClassOrShape;
	}

	public void setRequiresDatatypeClassOrShape(boolean requiresDatatypeClassOrShape) {
		this.requiresDatatypeClassOrShape = requiresDatatypeClassOrShape;
	}

	public boolean isRequiresMinCount() {
		return requiresMinCount;
	}

	public void setRequiresMinCount(boolean requiresMinCount) {
		this.requiresMinCount = requiresMinCount;
	}

	@Override
	public int compareTo(PropertyShapeReport o) {
		
		return propertyShape.getPredicate().stringValue().compareTo(o.getPropertyShape().getPredicate().stringValue());
	}

	public boolean getRequiresDescription() {
		return requiresDescription;
	}

	public void setRequiresDescription(boolean requiresDescription) {
		this.requiresDescription = requiresDescription;
	}

	public URI getInvalidXmlSchemaDatatype() {
		return invalidXmlSchemaDatatype;
	}

	public void setInvalidXmlSchemaDatatype(URI invalidXmlSchemaDatatype) {
		this.invalidXmlSchemaDatatype = invalidXmlSchemaDatatype;
	}

	@Override
	public int errorCount() {
		
		return 
			Sum.whereTrue(
					datatypeWithClass, datatypeWithIriNodeKind, datatypeWithShape,
					requiresDatatypeClassOrShape, requiresMinCount, requiresShapeOrIriNodeKind)
			+ Sum.whereNonNull(invalidXmlSchemaDatatype, typeConflict);
	}

	

}
