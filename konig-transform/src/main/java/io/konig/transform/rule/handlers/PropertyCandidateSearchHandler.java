package io.konig.transform.rule.handlers;

import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Path;
import io.konig.core.path.Step;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.assembly.Blackboard;
import io.konig.transform.assembly.EquivalentPathPropertyAccessPoint;
import io.konig.transform.assembly.PropertyAccessPoint;
import io.konig.transform.assembly.TransformEvent;
import io.konig.transform.assembly.TransformEventHandler;
import io.konig.transform.assembly.TransformEventType;
import io.konig.transform.rule.ExactMatchPropertyRule;
import io.konig.transform.rule.PropertyRule;
import io.konig.transform.rule.RankedVariable;
import io.konig.transform.rule.RenamePropertyRule;

/**
 * Search for candidate rules for properties.
 * @author Greg McFall
 *
 */
public class PropertyCandidateSearchHandler implements TransformEventHandler {


	@Override
	public void handle(TransformEvent event) throws ShapeTransformException {
		
		switch (event.getType()) {
		case ASSEMBLY_START :
			
			Blackboard board = event.getBlackboard();
			process(board);
			board.dispatch(TransformEventType.PROPERTY_CANDIDATE_COMPLETE);
			
			
			break;
			
		default :
			throw new UnsupportedEventException(event);
		}
		

	}

	private void process(Blackboard board) throws ShapeTransformException {
		Shape focusShape = board.getFocusShape();
		URI focusClass = focusShape.getTargetClass();
		
		for (PropertyConstraint p : focusShape.getProperty()) {

			URI focusPredicate = p.getPredicate();
			
			List<PropertyAccessPoint> accessPointList =
					board.getPropertyManager().getAccessPoint(focusPredicate);
			
			for (PropertyAccessPoint point : accessPointList) {
				Shape sourceShape = point.getSourceShape();
				if (sourceShape == focusShape) {
					continue;
				}
				
				URI sourceClass = sourceShape.getTargetClass();
				
				URI constraintPredicate = point.getConstraint().getPredicate();
				
				if (focusPredicate.equals(constraintPredicate) && focusClass.equals(sourceClass)) {
					RankedVariable<Shape> sourceShapeVar = board.shapeVariable(sourceShape);
					PropertyRule propertyRule = new ExactMatchPropertyRule(sourceShapeVar, focusPredicate);
					board.addPropertyRule(propertyRule);
					
				} else if (point instanceof EquivalentPathPropertyAccessPoint) {
					EquivalentPathPropertyAccessPoint pathAccess = (EquivalentPathPropertyAccessPoint) point;
					int pathIndex = pathAccess.getPathIndex();
					if (pathIndex == 0) {

						RankedVariable<Shape> sourceShapeVar = board.shapeVariable(sourceShape);
						Path path = pathAccess.getConstraint().getEquivalentPath();
						List<Step> stepList = path.asList();
						Blackboard stepBoard = board;
						PropertyConstraint stepConstraint = p;
						Shape stepShape = focusShape;
						for (int i=0; i<stepList.size(); i++) {
							Step step = stepList.get(i);
							URI stepPredicate = step.getPredicate();
							if (stepPredicate != null) {
								PropertyRule propertyRule = new RenamePropertyRule(stepPredicate, sourceShapeVar, point.getConstraint(), i);
								stepBoard.addPropertyRule(propertyRule);
								
								if (moreSteps(stepConstraint, stepList, i)) {
									stepBoard = stepBoard.provideChild(stepConstraint);
									stepShape = stepBoard.getFocusShape();
									stepConstraint = stepShape.getPropertyConstraint(stepPredicate);
								}
							}
						}
						
					}
				}
			}
			Shape valueShape = p.getShape();
			if (valueShape != null) {
				Blackboard childBoard = board.provideChild(p);
				process(childBoard);
			}
		}
		
	}

	private boolean moreSteps(PropertyConstraint p, List<Step> stepList, int i) {
		if (p!=null && p.getShape()!=null) {
			for (int j=i+1; j<stepList.size(); j++) {
				if (stepList.get(j).getPredicate()!=null) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public TransformEventType[] respondsTo() {
		return new TransformEventType[]{
				TransformEventType.ASSEMBLY_START
		};
	}

}
