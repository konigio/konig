package io.konig.transform.factory;

import java.util.List;

public interface TransformStrategy {
	
	/**
	 * Find shapes that may be used as sources to build a given target shape.
	 * @param target The target shape to be built.
	 * @return The list of candidate source shapes.
	 */
	List<SourceShape> findCandidateSourceShapes(TargetShape target) throws TransformBuildException;
	
	void init(ShapeRuleFactory factory);
	
}
