package io.konig.datacatalog;

import java.util.ArrayList;
import java.util.Collections;
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

import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlManager;
import io.konig.core.vocab.Konig;
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
	private Link termStatus;
	private List<Link> qualifiedSecurityClassificationList;
	private List<MappedField> mappingList;
	
	public PropertyInfo(URI resourceId, PropertyConstraint constraint, Link termStatus, PageRequest request) throws DataCatalogException {
		this(resourceId, constraint, request, false);
		this.termStatus = termStatus;
	}

	public PropertyInfo(URI resourceId, PropertyConstraint constraint, PageRequest request) throws DataCatalogException {
		this(resourceId, constraint, request, true);
	}
	
	public PropertyInfo(URI resourceId, PropertyConstraint constraint, PageRequest request, boolean computeTermStatus) throws DataCatalogException {
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
		
		if (computeTermStatus) {
			URI termStatus = constraint.getTermStatus();
			if (termStatus == null) {
				Vertex v = request.getGraph().getVertex(constraint.getPredicate());
				if (v != null) {
					termStatus = v.getURI(Konig.termStatus);
				}
			}
			if (termStatus != null) {
				this.termStatus = request.termStatusLink(termStatus);
			}
		}
		
		propertyHref = request.relativePath(resourceId, constraint.getPredicate());
		qualifiedSecurityClassificationList = quantifiedSecurityClassificationList(constraint.getQualifiedSecurityClassification(), request);
		

	}
	
	

	private List<Link> quantifiedSecurityClassificationList(List<URI> uriList, PageRequest request) throws DataCatalogException {
		if (uriList == null) {
			return null;
		}
		
		List<Link> list = new ArrayList<>();
		for (URI uri : uriList) {
			String relativePath = request.relativePath(uri);
			list.add(new Link(uri.getLocalName(), relativePath));
		}
		return list;
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

	public List<Link> getQualifiedSecurityClassificationList() {
		return qualifiedSecurityClassificationList;
	}
	
	public boolean anySecurityClassification() {
		return qualifiedSecurityClassificationList!=null && !qualifiedSecurityClassificationList.isEmpty();
	}

	public Link getTermStatus() {
		return termStatus;
	}

	public void setTermStatus(Link termStatus) {
		this.termStatus = termStatus;
	}

	public List<MappedField> getMappingList() {
		return mappingList==null ? Collections.emptyList() : mappingList;
	}

	public void setMappingList(List<MappedField> mappingList) {
		this.mappingList = mappingList;
	}
	


}