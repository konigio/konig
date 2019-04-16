package io.konig.core.showl;

import java.util.List;

import io.konig.shacl.Shape;

public interface ShowlTargetNodeSelector {
	
	List<ShowlNodeShape> produceTargetNodes(ShowlFactory factory, Shape shape) throws ShowlProcessingException;

}
