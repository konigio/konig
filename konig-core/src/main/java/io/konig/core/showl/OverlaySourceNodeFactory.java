package io.konig.core.showl;

import java.util.HashSet;
import java.util.Set;

public class OverlaySourceNodeFactory implements ShowlSourceNodeFactory {
	private Set<ShowlNodeShape> candidates = new HashSet<>();

	private ShowlNodeShape targetNodeShape;
	private ShowlSourceNodeFactory defaultFactory;
	
	
	

	public OverlaySourceNodeFactory(ShowlNodeShape targetNodeShape, ShowlSourceNodeFactory defaultFactory) {
		this.targetNodeShape = targetNodeShape;
		this.defaultFactory = defaultFactory;
	}


	public void setSourceNode(ShowlNodeShape sourceNode) {
		candidates.clear();
		candidates.add(sourceNode);
	}


	@Override
	public Set<ShowlNodeShape> candidateSourceNodes(ShowlNodeShape targetNode) throws ShowlProcessingException {
		
		return targetNodeShape == targetNode ? 
				candidates :
				defaultFactory.candidateSourceNodes(targetNode);
	}

}
