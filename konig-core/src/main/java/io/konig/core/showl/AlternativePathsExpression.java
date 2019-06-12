package io.konig.core.showl;

import java.util.List;
import java.util.Set;

/**
 * A structure that is derived from a collection of alternative paths.
 * @author Greg McFall
 *
 */
public class AlternativePathsExpression implements ShowlExpression {

	private List<ShowlAlternativePath> pathList;
	
	public AlternativePathsExpression(List<ShowlAlternativePath> pathList) {
		this.pathList = pathList;
	}

	public List<ShowlAlternativePath> getPathList() {
		return pathList;
	}

	@Override
	public String displayValue() {
		
		return  "AlternativePathsExpression(" + pathList.get(0).getNode().getPath() + ")";
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		
		ShowlNodeShape node = pathList.get(0).getNode();
		if (node.getRoot() == sourceNodeShape.getRoot()) {
			addProperties(set);
		}

	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		
		for (ShowlAlternativePath path : pathList) {
			set.addAll(path.getParameters());
		}

	}

}
