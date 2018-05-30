package io.konig.datacatalog;

import java.util.List;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
	private List<io.konig.shacl.Link> quantifiedSecurityClassificationList;
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
				typeHref = request.relativePath(resourceId, targetClass);
			}
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
		if (constraint.getQualifiedSecurityClassification()!=null){
			quantifiedSecurityClassificationList = constraint.getQualifiedSecurityClassification();
		}

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

	public List<io.konig.shacl.Link> getQuantifiedSecurityClassificationList() {
		return quantifiedSecurityClassificationList;
	}

	public void setQuantifiedSecurityClassificationList(List<io.konig.shacl.Link> quantifiedSecurityClassificationList) {
		this.quantifiedSecurityClassificationList = quantifiedSecurityClassificationList;
	}

}