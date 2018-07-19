package io.konig.core.reasoners;

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


import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.formula.PathExpression;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.RelationshipDegree;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class RelationshipDegreeReasoner {
	private static final Logger LOG = LoggerFactory.getLogger(RelationshipDegreeReasoner.class);
	public RelationshipDegreeReasoner() {
	}
	
	public void computeRelationshipDegree(Graph graph, ShapeManager shapeManager, Boolean overwriteExisting) {
	
		Worker worker = new Worker(graph, shapeManager,overwriteExisting);
		worker.run();
	}
	
	
	private static class PropertyInfo {
		/**
		 * The property whose relationship degree we wish to compute.
		 */
		private Vertex property;
		
		/**
		 * The inverse of the property whose relationship degree we wish to compute
		 */
		private Vertex inverse;
		
		
		private RelationshipDegree relationshipDegree;
		
		/**
		 * The set of PropertyInfo for other properties that are aliases of
		 * the property whose relationship degree we wish to compute.
		 * 
		 * The target property and all of the alias properties have the same 
		 * relationship degree.
		 */
		private Set<PropertyInfo> alias;
		
		private Set<PropertyConstraint> propertyConstraints = new HashSet<>();
		
		public PropertyInfo(Vertex property, Vertex inverse) {
			this.property = property;
			this.inverse = inverse;
		}
		
		
		public Vertex getProperty() {
			return property;
		}


		public Vertex getInverse() {
			return inverse;
		}


		public Set<PropertyConstraint> getPropertyConstraints() {
			return propertyConstraints;
		}


		public URI getPropertyId() {
			return (URI) property.getId();
		}

		public RelationshipDegree getRelationshipDegree() {
			return relationshipDegree;
		}

		public void setRelationshipDegree(RelationshipDegree relationshipDegree) {
			this.relationshipDegree = relationshipDegree;
		}

		public void addPropertyConstraint(PropertyConstraint p) {
			propertyConstraints.add(p);
		}

		public Set<PropertyInfo> getAlias() {
			return alias;
		}

		public void setAlias(Set<PropertyInfo> set) {
			alias = set;
		}
		
		
		
		
	}
	
	private static class Worker {
		private Graph graph;
		private ShapeManager shapeManager;
		private Boolean overwriteExisting;
		
		private Map<URI, PropertyInfo> propertyInfo = new HashMap<>();
		
		public Worker(Graph graph, ShapeManager shapeManager, Boolean overwriteExisting) {
			this.graph = graph;
			this.shapeManager = shapeManager;
			this.overwriteExisting=overwriteExisting;
		}

		private void run() {

			collectPropertyInfo();
			computeRelationshipDegrees();
			
		}

		private void collectPropertyInfo() {
			
			collectRdfProperties();
			collectPropertyConstraints();
			
			
		}

		private void collectPropertyConstraints() {
			
			for (Shape shape : shapeManager.listShapes()) {
				collectPropertyConstraintsFromShape(shape);
			}
			
		}

		private void collectPropertyConstraintsFromShape(Shape shape) {
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				PropertyInfo info = null;
				
				if (predicate != null) {
					info = producePropertyInfo(predicate);
					info.addPropertyConstraint(p);
				}
				
				PropertyPath path = p.getPath();
				if (path instanceof SequencePath) {
					SequencePath sequence = (SequencePath) path;
					PropertyPath last = sequence.getLast();
					if (last instanceof PredicatePath) {
						URI pathPredicate = ((PredicatePath) last).getPredicate();
						
						PropertyInfo pathInfo = producePropertyInfo(pathPredicate);
						assertAlias(info, pathInfo);
					}
				}
			}
			
		}

		private void assertAlias(PropertyInfo a, PropertyInfo b) {
			
			if (a!=null && b!=null) {
				Set<PropertyInfo> aAlias = a.getAlias();
				Set<PropertyInfo> bAlias = b.getAlias();
				
				if (aAlias==null && bAlias==null) {
					Set<PropertyInfo> set = new HashSet<>();
					set.add(a);
					set.add(b);
					
					a.setAlias(set);
					b.setAlias(set);
				
				} else if (aAlias!=null && bAlias==null) {
					aAlias.add(b);
					b.setAlias(aAlias);
				} else if (aAlias==null && bAlias!=null) {
					bAlias.add(a);
					a.setAlias(bAlias);
				} else if (aAlias != bAlias) {
					// aAlias!=null && bAlias!=null
					
					Set<PropertyInfo> union = aAlias;
					union.addAll(bAlias);
					
					for (PropertyInfo info : union) {
						info.setAlias(union);
					}
					
				}
			}
			
		}

		private PropertyInfo producePropertyInfo(URI predicate) {
			
			PropertyInfo info = propertyInfo.get(predicate);
			if (info == null) {
				info = createPropertyInfo(predicate);
				
			}
			return info;
		}

		private void collectRdfProperties() {
			List<Vertex> propertyList = 
					graph.v(RDF.PROPERTY)
						.union(OWL.OBJECTPROPERTY, OWL.DATATYPEPROPERTY)
						.isIRI()
						.in(RDF.TYPE).toVertexList();
			
			for (Vertex property : propertyList) {

				URI predicate = (URI) property.getId();
				
				createPropertyInfo(predicate);
				
			}
			
		}

		private PropertyInfo createPropertyInfo(URI predicate) {
			Vertex property = graph.getVertex(predicate);
			if (property == null) {
				property = graph.vertex(predicate);
				graph.edge(predicate, RDF.TYPE, RDF.PROPERTY);
			}

			Vertex inverse = property.getVertex(OWL.INVERSEOF);
			if (inverse == null) {
				Set<Edge> inverseSet = property.inProperty(OWL.INVERSEOF);
				if (!inverseSet.isEmpty()) {
					inverse = graph.getVertex(inverseSet.iterator().next().getSubject());
				}
			}
			
			
			PropertyInfo info = new PropertyInfo(property, inverse);
			propertyInfo.put(predicate, info);
			return info;
		}

		private void computeRelationshipDegrees() {
			
			for (PropertyInfo info : propertyInfo.values()) {
				computeRelationshipDegree(info);				
				propertyInfo.put(info.getPropertyId(), info);
				for(Shape shape:shapeManager.listShapes()){
					PropertyConstraint pc=shape.getPropertyConstraint(info.getPropertyId());
					if(pc!=null && (pc.getRelationshipDegree()==null || overwriteExisting==true)){
						pc.setRelationshipDegree(info.getRelationshipDegree());
						shape.updatePropertyConstraint(pc);
						shapeManager.addShape(shape);
					}
				}
			}
			
			
		}
		
		private void computeRelationshipDegree(PropertyInfo info) {
			
			if (info.getRelationshipDegree() == null) {
				/*RelationshipDegree relationshipDegree=getShaclRelationshipDegree(info);
				if(getShaclRelationshipDegree(info)!=null){
					info.setRelationshipDegree(relationshipDegree);
					return;
				}*/
				Integer left=0;
				Integer right=0;
				Vertex inverseProperty=info.getInverse();
				URI inverseRelationshipDegree = null;
				URI q=null;
				if(inverseProperty!=null){
					inverseRelationshipDegree = inverseProperty.getURI(Konig.relationshipDegree);
					q=(URI)inverseProperty.getId();
				}
				if(inverseRelationshipDegree!=null){
				if(Konig.OneToOne.equals(inverseRelationshipDegree))
					info.setRelationshipDegree(RelationshipDegree.OneToOne);
				else if(Konig.OneToMany.equals(inverseRelationshipDegree))
					info.setRelationshipDegree(RelationshipDegree.ManyToOne);
				else if(Konig.ManyToOne.equals(inverseRelationshipDegree))
					info.setRelationshipDegree(RelationshipDegree.OneToMany);
				else if(Konig.ManyToMany.equals(inverseRelationshipDegree))
					info.setRelationshipDegree(RelationshipDegree.ManyToMany);	
				return;
				}
				if(q!=null){
					left = getMax(getOwlMaxCardinality(q), getShMaxCardinality(q));
				}
				right = getMax(getOwlMaxCardinality(info.getPropertyId()), getShMaxCardinality(info.getPropertyId()));
				
				if(left==1 && right==1)
					info.setRelationshipDegree(RelationshipDegree.OneToOne);
				else if(left==1 && right!=1)
					info.setRelationshipDegree(RelationshipDegree.OneToMany);
				else if(left!=1 && right==1)
					info.setRelationshipDegree(RelationshipDegree.ManyToOne);
				else if(left!=1 && right!=1)
					info.setRelationshipDegree(RelationshipDegree.ManyToMany);
			}
			
			
		}
		private Integer getOwlMaxCardinality(URI p){
			int max=0;
			OwlReasoner owlReasoner=new OwlReasoner(graph);
			if(owlReasoner.isTypeOf(p, OWL.FUNCTIONALPROPERTY)){
				return 1;
			}
			Set<URI> uriSet=owlReasoner.inverseOf(p);
			URI q=null;
			if(uriSet!=null && uriSet.iterator().hasNext()){
				q=uriSet.iterator().next();			
			}
			 List<Value> maxCardinalityList=graph.v(p).in(OWL.ONPROPERTY).out(OWL.MAXCARDINALITY).toValueList();
			 List<Value> maxQualifiedCardinalityList=graph.v(p).in(OWL.ONPROPERTY).out(OwlVocab.maxQualifiedCardinality).toValueList();
			 max=getMax(getMax(maxCardinalityList),getMax(maxQualifiedCardinalityList));
			 
			 //TODO: What do we need to compute with the help of inverse of the property?
			return max;
		}
		private Integer getShMaxCardinality(URI p){
			int max=0;
			for(Shape shape:shapeManager.listShapes()){
					if(shape.getProperty()!=null){
						for(PropertyConstraint q:shape.getProperty()){
							PropertyPath path=q.getPath();
							if((path instanceof PredicatePath || path instanceof SequencePath) && q.getFormula()!=null){
								QuantifiedExpression quantifiedExpression =q.getFormula();
								PrimaryExpression primaryExpression=quantifiedExpression.asPrimaryExpression();
								String propertyPath=null;
								if(primaryExpression instanceof PathExpression){
									PathExpression pathExp=(PathExpression) primaryExpression;	
									propertyPath=pathExp.simpleText();
								}
								String propPathToCompare=p.getLocalName();
								if(propertyPath!=null && propertyPath.endsWith(propPathToCompare) && q.getMaxCount()!=null){
									max=getMax(max,q.getMaxCount()); 
								}
							}
						}
					}		
			}	
			
			return max;
		}
		private Integer getMax(List<Value> valueList){
			Integer max=0;
			Integer intValue=0;
			try{
				if(valueList!=null){
					for(Value v:valueList){
						intValue=Integer.parseInt(v.stringValue());
						if(intValue>max)
							max=intValue;
					}
				}
			}
			catch(NumberFormatException e){
				LOG.error("Error while getting maxCardinality value:"+intValue,e);
			}
			return max;
		}
		private Integer getMax(Integer count1, Integer count2) {
			Integer max=null;
			if(count1!=null && count2!=null){
				max=(count1>count2)?count1:count2;
			}
			else if(count1!=null){
				max=count1;
			}
			else if(count2!=null){
				max=count2;
			}
			return max;
		}
		/*private RelationshipDegree getShaclRelationshipDegree(PropertyInfo info) {
			Set<PropertyConstraint> propertyConstraints=info.getPropertyConstraints();
			RelationshipDegree relationshipDegree=null;
			if(propertyConstraints!=null){
				List<RelationshipDegree> list=Arrays.asList(RelationshipDegree.OneToOne,RelationshipDegree.OneToMany,RelationshipDegree.ManyToOne,RelationshipDegree.ManyToMany);
				RelationshipDegreeComparator comparator=new RelationshipDegreeComparator(list);
			
				for(PropertyConstraint pc:propertyConstraints){
					int result=comparator.compare(pc.getRelationshipDegree(), relationshipDegree);
					if(result<0)
						relationshipDegree=pc.getRelationshipDegree();
				}
			}
			return relationshipDegree;
			
		}*/
	}

}
