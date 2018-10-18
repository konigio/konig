package io.konig.validation;

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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.NamespaceManager;

public class ModelValidationReport {
	
	private NamespaceManager namespaceManager;
	private Map<URI,ClassReport> classReports = new HashMap<>();
	private Map<URI,PropertyReport> propertyReports = new HashMap<>();
	private Map<URI,NamedIndividualReport> individualReports = new HashMap<>();
	
	private ModelValidationRequest request;
	
	private List<URI> classPropertyDisjointViolation = new ArrayList<>();
	private List<NodeShapeReport> shapeReports = new ArrayList<>();
	private List<PropertyShapeReference> propertyShapeCaseViolation=new ArrayList<>();
	
	
	private ModelStatistics statistics;
	
	public ClassReport findClassReport(URI classId) {
		return classReports.get(classId);
	}
	
	public void add(ClassReport report) {
		classReports.put(report.getClassId(), report);
	}

	public Collection<ClassReport> getClassReports() {
		return classReports.values();
	}
	public Collection<PropertyReport> getPropertyReports() {
		return propertyReports.values();
	}
	
	public void add(PropertyReport report) {
		propertyReports.put(report.getPropertyId(), report);
	}
	
	public PropertyReport findPropertyReport(URI propertyId) {
		return propertyReports.get(propertyId);
	}
	
	public Collection<NamedIndividualReport> getNamedIndividualReports() {
		return individualReports.values();
	}
	
	public void add(NamedIndividualReport report) {
		individualReports.put(report.getIndividualId(), report);
	}
	
	public NamedIndividualReport findNamedIndividualReport(URI individualId) {
		return individualReports.get(individualId);
	}
	
	public List<PropertyShapeReference> getPropertyShapeCaseViolation() {
		return propertyShapeCaseViolation;
	}

	public List<URI> getClassPropertyDisjointViolation() {
		return classPropertyDisjointViolation;
	}
	
	public NodeShapeReport findNodeReport(Resource shapeId) {
		for (NodeShapeReport n : shapeReports) {
			if (shapeId.equals(n.getShapeId())) {
				return n;
			}
		}
		return null;
	}

	public List<NodeShapeReport> getShapeReports() {
		return shapeReports;
	}

	public void add(NodeShapeReport shapeReport) {
		shapeReports.add(shapeReport);
	}

	public ModelStatistics getStatistics() {
		return statistics;
	}

	public void setStatistics(ModelStatistics statistics) {
		this.statistics = statistics;
	}


	public ModelValidationRequest getRequest() {
		return request;
	}

	public void setRequest(ModelValidationRequest request) {
		this.request = request;
	}

	public NamespaceManager getNamespaceManager() {
		return namespaceManager;
	}

	public void setNamespaceManager(NamespaceManager namespaceManager) {
		this.namespaceManager = namespaceManager;
	}
	
	

}
