package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlDerivedPropertyShape;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEnumNodeExpression;
import io.konig.core.showl.ShowlEqualStatement;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlProperty;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlPropertyShapeGroup;
import io.konig.core.showl.ShowlStatement;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;
import io.konig.gcp.datasource.GoogleBigQueryTable;

public class BeamUtil {


	public static String errorTableName(URI shapeId) {
		
		String shapeName = ShowlUtil.shortShapeName(shapeId);
		String prefix = shapeName.contains("_") ?
				"ERROR_" :
				"Error";
				
		StringBuilder builder = new StringBuilder();
		builder.append(prefix);
		builder.append(shapeName);
		
		return builder.toString();
	}
	
	public static String errorTableDataset(ShowlNodeShape node) throws BeamTransformGenerationException {

		ShowlNodeShape targetNode = node.isTargetNode() ? node.getRoot() : node.getTargetNode().getRoot();
		DataSource ds = targetNode.getShapeDataSource().getDataSource();
		if (ds instanceof GoogleBigQueryTable) {
			return ((GoogleBigQueryTable) ds).getTableReference().getDatasetId();
		}
		throw new BeamTransformGenerationException("Expected BigQueryTable data source from " + targetNode.getPath());
	}


	
	private static ShowlNodeShape parentEnumNode(ShowlPropertyShape p, OwlReasoner reasoner) {
		// This is super complicated.  We probably need a major redesign
		// to simplify.
		
		// At a minimum, we ought to apply semantic reasoning to set the OWL class
		// when the ShowlPropertyShape is created, not now.
		
		ShowlNodeShape result = ShowlUtil.parentEnumNode(p);
		if (result == null) {
			while (p != null) {
				ShowlNodeShape node = p.getDeclaringShape();
				URI owlClass = node.getOwlClass().getId();
				if (owlClass.equals(Konig.Undefined)) {
					// Let's try applying semantic reasoning to discover the true OWL class.
					
					if (node.getAccessor() != null) {
						ShowlProperty property = node.getAccessor().getProperty();
						owlClass = property.inferRange(reasoner);
					}
				}
				if (owlClass!=null && reasoner.isEnumerationClass(owlClass)) {
					return node;
				}
				p = node.getAccessor();
			}
		}
		return result;
	}
	
	private static Set<ShowlPropertyShape> nonEnumProperties(ShowlExpression e, OwlReasoner reasoner) throws BeamTransformGenerationException {
		Set<ShowlPropertyShape> set = new HashSet<>();
		e.addProperties(set);
		Iterator<ShowlPropertyShape> sequence = set.iterator();
		
		Set<ShowlPropertyShape> set2 = null;
		while (sequence.hasNext()) {
			ShowlPropertyShape p = sequence.next();
			
			// The following code block is unfortunate.
			// It is an ugly hack.  We probably need a major 
			// redesign to clean this up.
			
			ShowlNodeShape enumNode = parentEnumNode(p, reasoner);
			if (enumNode != null) {
				sequence.remove();
				
				ShowlNodeShape targetEnumNode = enumNode.isTargetNode() ? enumNode : enumNode.getTargetNode();
				if (targetEnumNode!=null) {
					ShowlPropertyShape targetEnumAccessor = targetEnumNode.getAccessor();
					
					if (targetEnumAccessor != null) {
						
						if (targetEnumAccessor instanceof ShowlDerivedPropertyShape) {
							ShowlPropertyShapeGroup group = targetEnumAccessor.asGroup();
							ShowlPropertyShape direct = group.direct();
							if (direct == null) {
								throw new BeamTransformGenerationException("Direct property not found for " + targetEnumAccessor.getPath());
							}
							targetEnumAccessor = direct;
						}
						
						ShowlExpression targetEnumExpression = targetEnumAccessor.getSelectedExpression();
						if (targetEnumExpression instanceof ShowlEnumNodeExpression) {
							ShowlEnumNodeExpression enumNodeExpr = (ShowlEnumNodeExpression) targetEnumExpression;
							ShowlChannel channel = enumNodeExpr.getChannel();
							if (channel != null && channel.getJoinStatement()!=null) {
								ShowlStatement joinStatement = channel.getJoinStatement();
								Set<ShowlPropertyShape> set3 = nonEnumProperties(joinStatement);
								if (set2 == null) {
									set2 = new HashSet<>();
								}
								set2.addAll(set3);
							}
						}
					}
				}
			}
		}
		if (set2!=null) {
			set.addAll(set2);
		}
		return set;
	}
	

	public static Set<ShowlPropertyShape> nonEnumProperties(ShowlStatement statement, OwlReasoner reasoner) throws BeamTransformGenerationException {

		Set<ShowlPropertyShape> result = new HashSet<>();
		if (statement instanceof ShowlEqualStatement) {
			ShowlEqualStatement eq = (ShowlEqualStatement) statement;
			
			result.addAll(nonEnumProperties(eq.getLeft(), reasoner));
			result.addAll(nonEnumProperties(eq.getRight(), reasoner));
		} else {
			throw new BeamTransformGenerationException("Statement type not supported: " + statement.getClass().getSimpleName());
		}
		
		
		return result;
	}
	

	public static Set<ShowlPropertyShape> nonEnumProperties(ShowlStatement statement) {

		Set<ShowlPropertyShape> set = new HashSet<>();
		statement.addProperties(set);
		Iterator<ShowlPropertyShape> sequence = set.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShape p = sequence.next();
			if (ShowlUtil.isEnumProperty(p)) {
				sequence.remove();
			}
		}
		return set;
	}
	

	public static void collectSourceProperties(Set<ShowlPropertyShapeGroup> groupSet, ShowlExpression e, OwlReasoner reasoner) throws BeamTransformGenerationException {
	
		Set<ShowlExpression> memory = new HashSet<>();
		doCollectSourceProperties(memory, groupSet, e, reasoner);
	}
	
	private static void doCollectSourceProperties(Set<ShowlExpression> memory, Set<ShowlPropertyShapeGroup> groupSet, ShowlExpression e, OwlReasoner reasoner) throws BeamTransformGenerationException {
		
		if (memory.contains(e)) {
			return;
		}
		
		memory.add(e);
		
		// This method implements the following algorithm:
		
		// 1. Collect the ShowlPropertyShape instances from the given expression.
		// 2. For each ShowlPropertyShape p do:
		// 2.1   If p is an Enum member or  property of an Enum member, then add source properties from the join condition.
		// 2.2   Else if p is a target property, add the source properties from the selected expression or formula of p
		// 2.3   Else if p is a direct source property (or has a peer that is direct) add the group for that property
		// 2.4   Else if p has a formula, add the source properties for the formula
		// 2.5   Else error
		
		Set<ShowlPropertyShape> set = new HashSet<>();
		e.addProperties(set);
	
		for (ShowlPropertyShape p : set) {
			ShowlNodeShape parentEnumNode = parentEnumNode(p, reasoner);
			
			if (parentEnumNode != null) {

				
				ShowlEnumNodeExpression enumNodeExpr = enumNodeExpression(parentEnumNode);
				
				
				ShowlChannel channel = enumNodeExpr.getChannel();
				if (channel == null || channel.getJoinStatement() == null) {
					throw new BeamTransformGenerationException("Join statement not found for " + parentEnumNode.getPath());
				} else {
					ShowlStatement statement = channel.getJoinStatement();
					if (statement instanceof ShowlEqualStatement) {
						ShowlEqualStatement equal = (ShowlEqualStatement) statement;
						doCollectSourceProperties(memory, groupSet, equal.getLeft(), reasoner);
						doCollectSourceProperties(memory, groupSet, equal.getRight(), reasoner);
					} else {
						throw new BeamTransformGenerationException("Statement type not supported: " + statement.getClass().getSimpleName());
					}
				}
			} else if (p.isTargetProperty()) {
				
				ShowlExpression s = p.getSelectedExpression();
				if (s == null) {
					s = p.getFormula();
				}
				if (s == null) {
					
					ShowlDirectPropertyShape direct = p.getDeclaringShape().getProperty(p.getPredicate());
					if (direct!=null) {
						s = direct.getSelectedExpression();
						if (s==null) {
							s = direct.getFormula();
						}
					}
					throw new BeamTransformGenerationException("selectedExpression and formula are null for " + p.getPath());
				}
				doCollectSourceProperties(memory, groupSet, s, reasoner);
				
			} else {
				
				ShowlDirectPropertyShape direct = p.direct();
				if (direct != null) {
					groupSet.add(direct.asGroup());
				} else {
					ShowlExpression s = p.getSelectedExpression();
					if (s == null) {
						s = p.getFormula();
					}
					if (s == null) {
						direct = p.getDeclaringShape().getProperty(p.getPredicate());
						if (direct!=null) {
							groupSet.add(direct.asGroup());
						} else {
							throw new BeamTransformGenerationException("selectedExpression and formula are null for " + p.getPath());
						}
					} else {
						doCollectSourceProperties(memory, groupSet, s, reasoner);
					}
				}
			}
		}
		
	}

	private static ShowlEnumNodeExpression enumNodeExpression(ShowlNodeShape enumNode) throws BeamTransformGenerationException {
		
		ShowlPropertyShape targetProperty = enumNode.getTargetProperty();
		if (targetProperty!=null) {
			ShowlExpression e = targetProperty.getSelectedExpression();
			if (e instanceof ShowlEnumNodeExpression) {
				return (ShowlEnumNodeExpression) e;
			}
		}
		
		
		ShowlPropertyShape accessor = enumNode.getAccessor();
		if (accessor != null) {
			ShowlDirectPropertyShape direct = accessor.direct();
			if (direct == null) {
				direct = accessor.getDeclaringShape().getProperty(accessor.getPredicate());
			}
			if (direct != null) {
				ShowlExpression e = direct.getSelectedExpression();
				if (e instanceof ShowlEnumNodeExpression) {
					return (ShowlEnumNodeExpression) e;
				}
			}
		}
		
		throw new BeamTransformGenerationException("ShowlEnumNodeExpression not found for " + enumNode.getPath());
	}

	public static boolean hasBatchWindow(ShowlNodeShape targetNode, OwlReasoner reasoner) {

		for (ShowlChannel channel : targetNode.nonEnumChannels(reasoner)) {
			if (channel.getSourceNode().getProperty(Konig.modified)==null) {
				return false;
			}
		}
		return true;
	}

}
