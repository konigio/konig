package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;

public class PropertySheet extends BaseSheetProcessor {

	private static final Logger logger = LoggerFactory.getLogger(PropertySheet.class);
	static private final SheetColumn PROPERTY_NAME = new SheetColumn("Property Name");
	static private final SheetColumn COMMENT = new SheetColumn("Comment");
	static private final SheetColumn PROPERTY_ID = new SheetColumn("Property Id", true);
	static private final SheetColumn DOMAIN = new SheetColumn("Domain");
	static private final SheetColumn RANGE = new SheetColumn("Range");
	static private final SheetColumn INVERSE_OF = new SheetColumn("Inverse Of");
	static private final SheetColumn PROPERTY_TYPE = new SheetColumn("Property Type");
	static private final SheetColumn SUBPROPERTY_OF = new SheetColumn("Subproperty Of");
	static private final SheetColumn STATUS = new SheetColumn("Status");
	static private final SheetColumn SUBJECT = new SheetColumn("Subject");
	static private final SheetColumn SECURITY_CLASSIFICATION = new SheetColumn("Security Classification");
	static private final SheetColumn RELATIONSHIP_DEGREE = new SheetColumn("Relationship Degree");
	private static final SheetColumn PROJECT = new SheetColumn("Project");
	
	private static SheetColumn[] COLUMNS = new SheetColumn[]{
		PROPERTY_NAME,
		COMMENT,
		PROPERTY_ID,
		DOMAIN,
		RANGE,
		INVERSE_OF,
		PROPERTY_TYPE,
		SUBPROPERTY_OF,
		STATUS,
		SUBJECT,
		SECURITY_CLASSIFICATION,
		RELATIONSHIP_DEGREE,
		PROJECT
	};
	
//	private Graph graph;

	@SuppressWarnings("unchecked")
	public PropertySheet(WorkbookProcessor processor) {
		super(processor);
		dependsOn(OntologySheet.class, SettingsSheet.class);
	}

	@Override
	public SheetColumn[] getColumns() {
		return COLUMNS;
	}

	@Override
	public void visit(SheetRow row) throws SpreadsheetException {
		Literal propertyName = stringLiteral(row, PROPERTY_NAME);
		Literal comment = stringLiteral(row, COMMENT);
		URI propertyId = iriValue(row, PROPERTY_ID);
		List<URI> domain = iriList(row, DOMAIN);
		List<URI> range = iriList(row, RANGE);
		URI inverseOf = iriValue(row, INVERSE_OF);
		List<URI> propertyType = iriList(row, PROPERTY_TYPE);
		URI subpropertyOf = iriValue(row, SUBPROPERTY_OF);
		URI termStatus = iriValue(row, STATUS);
		List<URI> securityClassification = iriList(row, SECURITY_CLASSIFICATION);
		Collection<URI> subjectList = iriList(row, SUBJECT);
		URI relationshipDegree = iriValue(row, RELATIONSHIP_DEGREE);
		

		logger.debug("visit - propertyId: {}", propertyId);
		
		Vertex subject = vertex(propertyId);
		propertyType = analyzePropertyType(subject, propertyType, range);
		edge(propertyId, RDF.TYPE, RDF.PROPERTY);
		for (URI value : propertyType) {
			if (!RDF.PROPERTY.equals(value)) {
				edge(propertyId, RDF.TYPE, value);
			}
		}

		edge(propertyId, RDFS.SUBPROPERTYOF, subpropertyOf);
		edge(propertyId, RDFS.LABEL, propertyName);
		edge(propertyId, RDFS.COMMENT, comment);
		assignSubject(propertyId, subjectList);

		if (!domain.isEmpty()) {
			if (domain.size() == 1) {

				URI domainValue = domain.get(0);
				Value oldDomain = subject.getValue(RDFS.DOMAIN);

				if (oldDomain != null && !domainValue.equals(oldDomain)) {
					String propertyCurie = optionalCurie(propertyId);
					String oldDomainCurie = optionalCurie((URI) oldDomain);
					String newDomainCurie = optionalCurie(domainValue);
					fail(row, DOMAIN, 
						"The domain of {0} is redefined from {1} to {2}", 
						propertyCurie, oldDomainCurie, newDomainCurie);
					
				}
				edge(propertyId, RDFS.DOMAIN, domainValue);
				edge(domainValue, RDF.TYPE, OWL.CLASS);
				removeDomainIncludes(propertyId);
			} else {
				for (URI value : domain) {
					edge(propertyId, Schema.domainIncludes, value);
					edge(value, RDF.TYPE, OWL.CLASS);
				}
			}
			
		}

		if (!range.isEmpty()) {
			if (range.size() == 1) {
				URI rangeValue = range.get(0);
				Value oldRange = subject.getValue(RDFS.RANGE);
				

				if (oldRange != null && !rangeValue.equals(oldRange)) {
					String propertyCurie = optionalCurie(propertyId);
					String oldRangeCurie = optionalCurie((URI) oldRange);
					String newRangeCurie = optionalCurie(rangeValue);
					fail(row, RANGE, 
						"The range of {0} is redefined from {1} to {2}", 
						propertyCurie, oldRangeCurie, newRangeCurie);
				}
				edge(propertyId, RDFS.RANGE, rangeValue);
				removeRangeIncludes(propertyId);
			} else {
				for (URI value : range) {
					edge(propertyId, Schema.rangeIncludes, value);
				}
			}
		}

		edge(propertyId, OWL.INVERSEOF, inverseOf);
		
		if (termStatus != null) {
			service(TermStatusProcessor.class).assertStatus(propertyId, termStatus);
		}

		if (!securityClassification.isEmpty()) {
			for (URI uri : securityClassification) {
				edge(propertyId, Konig.securityClassification, uri);
			}
		}
		if (relationshipDegree != null) {
			for (URI d : domain) {
				Vertex restrictionVertex = vertex();
				Resource restriction = restrictionVertex.getId();
				edge(restriction, RDF.TYPE, OWL.RESTRICTION);
				edge(restriction, OWL.ONPROPERTY, propertyId);
				edge(restriction, Konig.relationshipDegree, relationshipDegree);
				edge(d, RDF.TYPE, OWL.CLASS);
				edge(d, RDFS.SUBCLASSOF, restriction);

			}
		}

	}
	
	private void removeDomainIncludes(URI propertyId) {

		Graph graph = processor.getGraph();
		Vertex v = graph.getVertex(propertyId);
		Set<Edge> edgeSet = new HashSet<>(v.outProperty(Schema.domainIncludes));
		for (Edge e : edgeSet) {
			graph.remove(e);
		}
		
	}

	private void removeRangeIncludes(URI propertyId) {
		Graph graph = processor.getGraph();
		Vertex v = graph.getVertex(propertyId);
		Set<Edge> edgeSet = v.outProperty(Schema.rangeIncludes);
		for (Edge e : edgeSet) {
			graph.remove(e);
		}
		
	}

	private List<URI> analyzePropertyType(Vertex subject, List<URI> propertyType, List<URI> range) {
		if (propertyType == null) {
			propertyType = new ArrayList<>();

			boolean datatypeProperty = false;
			boolean objectProperty = false;

			if (range != null) {
				for (URI type : range) {

					if (processor.getOwlReasoner().isDatatype(type)) {
						datatypeProperty = true;
					} else {
						objectProperty = true;
					}
				}
			}

			if (datatypeProperty && !objectProperty) {
				propertyType.add(OWL.DATATYPEPROPERTY);
			}
			if (!datatypeProperty && objectProperty) {
				propertyType.add(OWL.OBJECTPROPERTY);
			}

		}
		return propertyType;
	}

}
