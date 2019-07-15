package io.konig.transform.beam;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JVar;

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
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class BeamUtil {



	
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

}
