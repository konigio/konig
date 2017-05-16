package io.konig.transform.assembly;

import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.SuffixVariableNamer;
import io.konig.transform.rule.handlers.PropertyCandidateSearchHandler;
import io.konig.transform.rule.handlers.ShapeRuleAssembler;

public class TransformController {
	
	
	public void assemble(Blackboard board) throws ShapeTransformException {
		
		board.setVariableNamer(new SuffixVariableNamer("a"));
		board.addEventHandler(new PropertyCandidateSearchHandler());
		board.addEventHandler(new ShapeRuleAssembler());
		board.setShapeRankingAgent(new PropertyCountShapeRankingAgent());
		if (board.getFocusShape() == null) {
			throw new ShapeTransformException("targetShape must be defined");
		}
		
		board.dispatch(TransformEventType.ASSEMBLY_START);
		
	}

}
