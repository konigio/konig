package io.konig.validation;

import java.util.HashMap;
import java.util.Iterator;

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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.StringUtil;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ModelValidator {
	

	public ModelValidationReport process(ModelValidationRequest request) {
		Worker worker = new Worker(request);
		
		return worker.run();
	}
	
	private class Worker {
		private ModelValidationRequest request;
		private ModelValidationReport report = new ModelValidationReport();
		private Graph graph;
		private Map<URI, PropertyInfo> propertyMap = new HashMap<>();
		

		public Worker(ModelValidationRequest request) {
			this.request = request;
			graph = request.getOwl().getGraph();
			report.setRequest(request);
		}

		public ModelValidationReport run() {
			setDefaults();
			applyInferencing();
			validateCase();
			validateClassPropertyDisjoint();
			validateDatatypeRange();
			validateShapes();
			validatePropertyRanges();
			computeStatistics();
			return report;
		}
		
		private NamespaceValidationConfig getNamespaceConfig(String namespaceName) {
			return request.getNamespaceConfig(namespaceName);
		}

		private void validatePropertyRanges() {
			OwlReasoner owl = request.getOwl();
			Iterator<Entry<URI,PropertyInfo>> sequence = propertyMap.entrySet().iterator();
			while (sequence.hasNext()) {
				Entry<URI,PropertyInfo> e = sequence.next();
				PropertyInfo info = e.getValue();
				if (!info.isConflict(owl)) {
					sequence.remove();
				} else {
					NamespaceValidationConfig config = getNamespaceConfig(info.getProperty().getNamespace());
					if (config==null || !config.isIgnoreRangeConflicts()) {
						if (isRangeConflict(info)) {
							producePropertyReport(e.getKey()).setRangeConflict(info.getRangeInfo());
						}
					}
				}
			}
			
		}

		private boolean isRangeConflict(PropertyInfo info) {
			URI rdfPropertyRange = info.rdfPropertyRange();
			URI top = null;
			for (RangeInfo r : info.getRangeInfo()) {
				if (r.getParentShapeId()==null) {
					continue;
				}
				URI  range = r.getDatatype();
				if (range == null) {
					range = r.getOwlClass();
				}
				if (range == null) {
					continue;
				}
				
				if (rdfPropertyRange != null && range!=null) {
					if (!owl().isSubClassOf(range, rdfPropertyRange)) {
						return true;
					}
				} else if (top == null) {
					top = range;
				} else if (owl().isSubClassOf(top, range)) {
					top = range;
				} else if (!owl().isSubClassOf(range, top)) {
					return true;
				}
			}
			return false;
		}
		
		private OwlReasoner owl() {
			return request.getOwl();
		}

		private void setDefaults() {
			if (request.getCommentConventions()==null) {
				request.setCommentConventions(new CommentConventions());
			}
			
		}

		private void validateDatatypeRange() {
			for (Vertex v : graph.vertices()) {
				if (v.getId() instanceof URI) {
					URI propertyId = (URI) v.getId();
					Vertex range = v.getVertex(RDFS.RANGE);
					if (range!=null && range.getId() instanceof URI) {
						URI rangeId = (URI) range.getId();
						addPropertyInfo(propertyId, rangeId);
						if (rangeId.getNamespace().equals(XMLSchema.NAMESPACE) && !XmlSchemaTerms.isXmlSchemaTerm(rangeId)) {
							producePropertyReport(propertyId).setInvalidXmlSchemaDatatype(rangeId);
						}
					}
				}
			}
			
		}

		private void addPropertyInfo(URI propertyId, URI rangeId) {
			if (!request.getTabularPropertyNamespaces().contains(propertyId.getNamespace())) {
				URI datatype = null;
				URI owlClass = null;
				if (request.getOwl().isSubclassOfLiteral(rangeId)) {
					datatype = rangeId;
				} else {
					owlClass = rangeId;
				}
				
				PropertyInfo info = producePropertyInfo(propertyId);
				info.add(new RangeInfo(null, datatype, owlClass));
				
			}
			
		}

		private PropertyInfo producePropertyInfo(URI propertyId) {
			PropertyInfo info = propertyMap.get(propertyId);
			if (info == null) {
				info = new PropertyInfo(propertyId);
				propertyMap.put(propertyId, info);
				
				Vertex v = graph.getVertex(propertyId);
				if (v != null && v.getValue(RDFS.COMMENT)!=null) {
					info.setHasDefinition(true);
				}
			}
			return info;
		}

		private void applyInferencing() {
			ShapeManager shapeManager = request.getShapeManager();
			OwlReasoner owl = request.getOwl();
			owl.inferClassFromSubclassOf();
			owl.inferRdfPropertiesFromPropertyConstraints(shapeManager, graph);
			owl.inferClassesFromShapes(shapeManager, graph);
			
		}

		private void computeStatistics() {
			ShapeManager shapeManager = request.getShapeManager();
			OwlReasoner owl = request.getOwl();
			
			List<Vertex> owlClasses = owl.owlClassList();
			Set<Vertex> namedIndividuals = owl.allNamedIndividuals();
			
			int classesCount = owlClasses.size();
			int propertyCount = owl.allRdfOwlAndShaclProperties(shapeManager).size();
			int namedIndividualCount = namedIndividuals.size();
			int shapeCount = shapeManager.listShapes().size();
			
			ModelStatistics stats = new ModelStatistics();
			stats.setNumberOfClasses(classesCount);
			stats.setNumberOfNamedIndividuals(namedIndividualCount);
			stats.setNumberOfProperties(propertyCount);
			stats.setNumberOfShapes(shapeCount);
			
			report.setStatistics(stats);
			
			evaluateClassDescriptions(owlClasses);
			evaluateNamedIndividualDescriptions(namedIndividuals);
			evaluatePropertyDescriptions();
			
			stats.setNumberOfErrors(report.errorCount());
			
		}

		private void evaluatePropertyDescriptions() {
			if (!propertyMap.isEmpty()) {

				int count=0;
				for (PropertyInfo info : propertyMap.values()) {
					if (info.isHasDefinition()) {
						count++;
					}
				}
				RationalNumber number = new RationalNumber(count, propertyMap.size());
				report.getStatistics().setPropertiesWithDescription(number);
			}
			
			
		}

		private void evaluateNamedIndividualDescriptions(Set<Vertex> namedIndividuals) {
			int noDescription = 0;
			for (Vertex v : namedIndividuals) {
				Resource id = v.getId();
				if (id instanceof URI) {
					if (v.getValue(RDFS.COMMENT) == null) {
						URI iri = (URI) id;
						if (request.getCommentConventions().getRequireNamedIndividualComments()) {
							NamedIndividualReport r = produceNamedIndividualReport(iri);
							r.setRequiresDescription(true);
						}
						noDescription++;
					}
				}
			}
			
			int totalCount = namedIndividuals.size();
			int withDescription = totalCount - noDescription;
			
			report.getStatistics().setNamedIndividualsWithDescription(new RationalNumber(withDescription, totalCount));
			
		}

		

		private void evaluateClassDescriptions(List<Vertex> owlClasses) {
			
			int noDescription = 0;
			for (Vertex v : owlClasses) {
				Resource id = v.getId();
				if (id instanceof URI) {
					if (v.getValue(RDFS.COMMENT) == null) {
						URI iri = (URI) id;
						if (request.getCommentConventions().getRequireClassComments()) {
							ClassReport r = produceClassReport(iri);
							r.setRequiresDescription(true);
						}
						noDescription++;
					}
				}
			}
			
			int totalClasses = owlClasses.size();
			int withDescription = totalClasses - noDescription;
			
			report.getStatistics().setClassesWithDescription(new RationalNumber(withDescription, totalClasses));
			
		}

		private void validateShapes() {
			
			CaseStyle expectedStyle = request.getCaseStyle().getNodeShapes();
			ShapeManager shapeManager = request.getShapeManager();
			if (shapeManager != null) {
				for (Shape shape : shapeManager.listShapes()) {
					NodeShapeReport shapeReport = new NodeShapeReport(shape.getId());
					
					if (expectedStyle != null) {
						CaseStyle actualStyle = caseStyle(shape.getId());
						if (expectedStyle != actualStyle) {
							shapeReport.setNameHasWrongCase(true);
						}
					}
					
					if (shape.getProperty().isEmpty()) {
						shapeReport.setNoProperties(true);
					}
					
					for (PropertyConstraint p : shape.getProperty()) {
						validatePropertyConstraint(shapeReport, p);
					}
					if (!shapeReport.isValid()) {
						report.add(shapeReport);
					}
				}
			}
			
		}
		

		private CaseStyle caseStyle(Resource id) {
			
			return id instanceof URI ? caseStyle((URI)id) : null;
		}

		private void validatePropertyConstraint(NodeShapeReport shapeReport, PropertyConstraint p) {
			
			
			URI predicate = p.getPredicate();
			if (predicate == null) {
				return;
			}
			PropertyShapeReport report = new PropertyShapeReport(p);
			OwlReasoner owl = request.getOwl();
			if (p.getValueClass() != null && p.getShape()==null && p.getNodeKind() != NodeKind.IRI) {
				report.setRequiresShapeOrIriNodeKind(true);
			}
			if (p.getMinCount()==null) {
				report.setRequiresMinCount(true);
			}
			if (p.getComment()==null) {
				Vertex v = graph.getVertex(predicate);
				if (v == null || v.getValue(RDFS.COMMENT)==null) {
					if (request.getCommentConventions().getRequirePropertyShapeComments()) {
						report.setRequiresDescription(true);
					}
				}
			}
			
			PropertyInfo info = producePropertyInfo(predicate);
			info.add(new RangeInfo(shapeReport.getShapeId(), p.getDatatype(), valueClass(p)));
			
			if (p.getComment() != null) {
				info.setHasDefinition(true);
			}
			
			if (p.getDatatype() != null) {
				URI datatype = p.getDatatype();
				if (datatype.getNamespace().equals(XMLSchema.NAMESPACE) && !XmlSchemaTerms.isXmlSchemaTerm(datatype)) {
					report.setInvalidXmlSchemaDatatype(datatype);
				}
				if (p.getValueClass() != null) {
					report.setDatatypeWithClass(true);
				}
				if (p.getShape() != null) {
					report.setDatatypeWithShape(true);
				}
				if (p.getNodeKind() == NodeKind.IRI) {
					report.setDatatypeWithIriNodeKind(true);
				}
			}
			
			if (p.getDatatype()==null && p.getShape()==null && p.getValueClass()==null) {
				report.setRequiresDatatypeClassOrShape(true);
			}
			
			if (predicate != null) {
				Vertex predicateVertex = graph.getVertex(predicate);
				if (predicateVertex != null) {
					Set<Value> rangeSet = predicateVertex.getValueSet(RDFS.RANGE);
					if (rangeSet.size()==1) {
						Value range = rangeSet.iterator().next();
						if (range instanceof URI) {
							URI rangeId = (URI) range;
							boolean datatypeRange = owl.isSubclassOfLiteral(rangeId);
							if (p.getDatatype()!=null && !datatypeRange) {
								report.setTypeConflict(new TypeConflict(p.getDatatype(), rangeId));
							}
							Resource valueClass = p.getValueClass();
							if (valueClass == null && p.getShape()!=null) {
								valueClass = p.getShape().getId();
							}
							if (datatypeRange && (valueClass instanceof URI)) {
								report.setTypeConflict(new TypeConflict((URI)valueClass, rangeId));
							}
						}
					} else if (rangeSet.size()>1) {
						// TODO: invalid range
					}
				}
			}
			
			
			if (!report.isValid()) {
				shapeReport.add(report);
			}
			
		}

		private URI valueClass(PropertyConstraint p) {
			if (p.getValueClass() instanceof URI) {
				return (URI) p.getValueClass();
			}
			Shape valueShape = p.getShape();
			if (valueShape != null && valueShape.getTargetClass()!= null) {
				return valueShape.getTargetClass();
			}
			return null;
		}

		private void validateClassPropertyDisjoint() {
			Set<URI> propertySet = request.getOwl().allRdfOwlAndShaclProperties(request.getShapeManager());
			Set<URI> classSet = request.getOwl().allClassIds();
			
			classSet.retainAll(propertySet);
			
			if (!classSet.isEmpty()) {
				report.getClassPropertyDisjointViolation().addAll(classSet);
			}
			
			
		}

		private void validateCase() {
			validateClassCase();
			validatePropertyCase();
			validateNamedIndividualCase();
			validatePropertyShapeCase();
			
		}

		private void validatePropertyShapeCase() {
			
			CaseStyle expectedStyle = request.getCaseStyle().getProperties();
			if (expectedStyle != null) {
				ShapeManager shapeManager  = request.getShapeManager();
				if (shapeManager != null) {
					List<Shape> shapeList = shapeManager.listShapes();
					List<PropertyShapeReference> violationList = report.getPropertyShapeCaseViolation();
					for (Shape shape : shapeList) {
						Resource shapeId = shape.getId();
						List<PropertyConstraint> propertyList = shape.getProperty();
						for (PropertyConstraint p : propertyList) {
							URI predicate = p.getPredicate();
							if (predicate != null) {
								CaseStyle actualStyle = caseStyle(predicate);
								if (!expectedStyle.equals(actualStyle)) {
									violationList.add(new PropertyShapeReference(shapeId, predicate));
								}
							}
						}
					}
				}
			}
			
			
		}

		

		private void validateNamedIndividualCase() {
			CaseStyle expectedStyle = request.getCaseStyle().getNamedIndividuals();
			if (expectedStyle != null) {
				Set<Vertex> individualList = request.getOwl().allNamedIndividuals();
				for (Vertex v : individualList) {
					Resource id = v.getId();
					if (id instanceof URI) {
						URI iri = (URI) id;
						CaseStyle actualStyle = caseStyle(iri);
						if (!expectedStyle.equals(actualStyle)) {
							NamedIndividualReport nir = produceNamedIndividualReport(iri);
							nir.setNameHasWrongCase(true);
						}
					}
				}
			}
			
		}

		private NamedIndividualReport produceNamedIndividualReport(URI iri) {
			NamedIndividualReport result = report.findNamedIndividualReport(iri);
			if (result == null) {
				result = new NamedIndividualReport(iri);
				report.add(result);
			}
			return result;
		}

		private void validatePropertyCase() {
			CaseStyle expectedStyle = request.getCaseStyle().getProperties();
			if (expectedStyle != null) {
				Set<Vertex> propertyList = request.getOwl().allRdfAndOwlProperties();
				for (Vertex v : propertyList) {
					Resource id = v.getId();
					if (id instanceof URI) {
						URI iri = (URI) id;
						CaseStyle actualStyle = caseStyle(iri);
						if (!expectedStyle.equals(actualStyle)) {
							PropertyReport p = producePropertyReport(iri);
							p.setNameHasWrongCase(true);
						}
					}
				}
			}
			
		}

		private PropertyReport producePropertyReport(URI iri) {
			PropertyReport p = report.findPropertyReport(iri);
			if (p == null) {
				p = new PropertyReport(iri);
				report.add(p);
			}
			return p;
		}


		private void validateClassCase() {
			CaseStyle expectedStyle = request.getCaseStyle().getClasses();
			if (expectedStyle != null) {
				
				Vertex classVertex = graph.getVertex(OWL.CLASS);
				if (classVertex != null) {
					List<Vertex> classList = classVertex.asTraversal().in(RDF.TYPE).toVertexList();
					for (Vertex v : classList) {
						Resource id = v.getId();
						if (id instanceof URI) {
							URI iri = (URI) id;
							if (request.getOwl().isDatatype(iri)) {
								continue;
							}
							CaseStyle actualStyle = caseStyle(iri);
							if (!expectedStyle.equals(actualStyle)) {
								produceClassReport(iri).setNameHasWrongCase(true);
							}
						}
					}
				}
			}
			
		}

		private ClassReport produceClassReport(URI iri) {
			
			ClassReport result = report.findClassReport(iri);
			if (result == null) {
				result = new ClassReport(iri);
				report.add(result);
			}
			return result;
			
		}

		private CaseStyle caseStyle(URI id) {
			
			String localName = id.getLocalName();
			String camelCase = StringUtil.camelCase(localName);
			if (camelCase.equals(localName)) {
				return CaseStyle.camelCase;
			}
			String PascalCase = StringUtil.PascalCase(localName);
			if (PascalCase.equals(localName)) {
				return CaseStyle.PascalCase;
			}
			String SNAKE_CASE = StringUtil.SNAKE_CASE(localName);
			if (SNAKE_CASE.equals(localName)) {
				return CaseStyle.UPPER_SNAKE_CASE;
			}
			String snake_case = SNAKE_CASE.toLowerCase();
			if (snake_case.equals(localName)) {
				return CaseStyle.lower_snake_case;
			}
			
			return null;
		}


		
	}

}
