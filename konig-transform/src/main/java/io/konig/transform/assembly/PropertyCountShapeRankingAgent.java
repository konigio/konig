package io.konig.transform.assembly;

import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RankedVariable;


/**
 * An agent that ranks source shapes by counting the number of properties mapped from the 
 * source shape to the target shape.
 * 
 * @author Greg McFall
 *
 */
public class PropertyCountShapeRankingAgent implements ShapeRankingAgent {



	
	private void doRank(Blackboard board) {
		
		Shape focusShape = board.getFocusShape();
		
		for (PropertyConstraint p : focusShape.getProperty()) {
			URI predicate = p.getPredicate();
			List<PropertyRule> list = board.propertyRuleList(predicate);
			if (list != null) {
				for (PropertyRule rule : list) {
					if (rule.getContainer()==null) {
						RankedVariable<Shape> sourceShape = rule.getSourceShapeVariable();
						sourceShape.plusOne();
					}
				}
			}
			if (p.getShape()!=null) {
				Blackboard childBoard = board.getChild(predicate);
				doRank(childBoard);
			}
		}
		
		
	}
	
	public void rank(Blackboard board) {
		reset(board);
		doRank(board);
	}
	
	private void reset(Blackboard board) {
		Collection<RankedVariable<Shape>> sourceShapeList = board.getSourceShapeVariables();
		for (RankedVariable<Shape> var : sourceShapeList) {
			var.setRanking(0);
		}
		Collection<Blackboard> childList = board.getChildren();
		for (Blackboard child : childList) {
			reset(child);
		}
	}

}
