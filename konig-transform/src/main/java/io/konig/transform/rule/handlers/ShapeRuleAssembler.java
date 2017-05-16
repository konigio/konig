package io.konig.transform.rule.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.konig.core.impl.RdfUtil;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.assembly.Blackboard;
import io.konig.transform.assembly.ShapeRankingAgent;
import io.konig.transform.assembly.TransformEvent;
import io.konig.transform.assembly.TransformEventHandler;
import io.konig.transform.assembly.TransformEventType;
import io.konig.transform.rule.AbstractPropertyRule;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RankedVariable;
import io.konig.transform.rule.ShapeRule;

public class ShapeRuleAssembler implements TransformEventHandler {


	@Override
	public void handle(TransformEvent event) throws ShapeTransformException {

		
		
		switch(event.getType()) {
		case PROPERTY_CANDIDATE_COMPLETE :
			
			Blackboard board = event.getBlackboard();
			process(board);
			board.dispatch(TransformEventType.ASSEMBLY_END);
			
			break;
		default:
			throw new UnsupportedEventException(event);
		}

	}

	private void process(Blackboard board) throws ShapeTransformException {

		Shape focusShape = board.getFocusShape();
		ShapeRankingAgent rankingAgent = board.getShapeRankingAgent();
		
		ShapeRule shapeRule = new ShapeRule(focusShape);
		board.setShapeRule(shapeRule);
		List<PropertyConstraint> propertyList = new ArrayList<>(focusShape.getProperty());

		List<RankedVariable<Shape>> shapeStack = shapeStack(board);
		
		int expectedSize = RdfUtil.countDistinctProperties(focusShape);
		
		int currentSize=0;
		while (currentSize < expectedSize && !shapeStack.isEmpty()) {
			if (shapeStack.size()>1) {
				rankingAgent.rank(board);
				Collections.sort(shapeStack);
			}
			
			RankedVariable<Shape> sourceShapeVar = shapeStack.remove(shapeStack.size()-1);
			
			Iterator<PropertyConstraint> sequence = propertyList.iterator();
			while (sequence.hasNext()) {
				PropertyConstraint p = sequence.next();
				Shape childShape = p.getShape();
				PropertyRule propertyRule = board.removePropertyRule(sourceShapeVar, p.getPredicate());
				if (propertyRule != null) {
					shapeRule.addPropertyRule(propertyRule);
					if (childShape == null) {
						sequence.remove();
					}
					
				}

				if (childShape != null) {
					Blackboard childBoard = board.getChild(p.getPredicate());
					if (childBoard != null) {
						process(childBoard);
						
						ShapeRule childRule = childBoard.getShapeRule();
						if (childRule != null) {
							AbstractPropertyRule abstractRule = (AbstractPropertyRule) propertyRule;
							abstractRule.setNestedRule(childRule);
						}
						if (childRule != null && childRule.getPropertyRules().size() == focusShape.getProperty().size()) {
							sequence.remove();
						}
						
					}
				}
			}
			int newSize = board.countDistinctProperties(shapeRule);
			// Abort if we did not add any new PropertyRule instances.
			if (newSize == currentSize) {
				break;
			}
			currentSize = newSize;
			
		}
		
	}

	private List<RankedVariable<Shape>> shapeStack(Blackboard board) {
		Set<RankedVariable<Shape>> set = new HashSet<>();
		addAll(set, board);
		
		return new ArrayList<>(set);
	}

	private void addAll(Set<RankedVariable<Shape>> set, Blackboard board) {
		
		set.addAll(board.getSourceShapeVariables());
		if (board.getParent()!=null) {
			addAll(set, board.getParent());
		}
		
	}

	@Override
	public TransformEventType[] respondsTo() {
		return new TransformEventType[]{
			TransformEventType.PROPERTY_CANDIDATE_COMPLETE
		};
	}

}
