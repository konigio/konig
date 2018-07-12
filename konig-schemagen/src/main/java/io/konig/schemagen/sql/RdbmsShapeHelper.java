package io.konig.schemagen.sql;

import java.util.Collection;

/*
 * #%L
 * Konig Schema Generator
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
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class RdbmsShapeHelper {
	
	private OwlReasoner owlReasoner;
	private Graph graph;
	private Collection<Shape> shapes;
	private static final Logger LOG = LoggerFactory.getLogger(RdbmsShapeHelper.class);
	public RdbmsShapeHelper(OwlReasoner owlReasoner){
		this.owlReasoner=owlReasoner;
		this.graph=owlReasoner.getGraph();
		ShapeManager shapeManager = new MemoryShapeManager();
		ShapeLoader shapeLoader = new ShapeLoader(null, shapeManager);
		shapeLoader.load(graph);
		this.shapes = shapeManager.listShapes();
	}
	
	public Integer getShMaxCardinality(URI p){
		int max=0;
		for(Shape shape:shapes){
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
	public Integer getOwlMaxCardinality(URI p){
		int max=0;
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
	public Integer getMax(List<Value> valueList){
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
	public Integer getMax(Integer count1, Integer count2) {
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
	
	public RelationshipDegree getRelationshipDegree(PropertyConstraint p){
		if(p.getRelationshipDegree()!=null)
			return p.getRelationshipDegree();
		
		Integer left=0;
		Integer right=0;
		Set<URI> uriSet=owlReasoner.inverseOf(p.getPredicate());
		URI q=null;
		if(uriSet!=null && uriSet.iterator().hasNext()){
			q=uriSet.iterator().next();
			Vertex property=graph.getVertex(q);
			URI relationshipDegree = property.getURI(Konig.relationshipDegree);
				
			if(Konig.OneToOne.equals(relationshipDegree))
				return RelationshipDegree.OneToOne;
			else if(Konig.OneToMany.equals(relationshipDegree))
				return RelationshipDegree.OneToMany;
			else if(Konig.ManyToOne.equals(relationshipDegree))
				return RelationshipDegree.OneToMany;
			else if(Konig.ManyToMany.equals(relationshipDegree))
				return RelationshipDegree.ManyToMany;			
		}
		if(q!=null){
			left = getMax(getOwlMaxCardinality(q), getShMaxCardinality(q));
		}
		right = getMax(getOwlMaxCardinality(p.getPredicate()), getShMaxCardinality(p.getPredicate()));
		
		if(left==1 && right==1)
			return RelationshipDegree.OneToOne;
		else if(left==1 && right!=1)
			return RelationshipDegree.OneToMany;
		else if(left!=1 && right==1)
			return RelationshipDegree.ManyToOne;
		else if(left!=1 && right!=1)
			return RelationshipDegree.ManyToMany;
		return null;
	}
	

}
