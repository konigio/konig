package io.konig.yaml;

public enum AnchorFeature {
	/**
	 * Don't write any anchors.
	 * This feature should be used only if you know for sure that objects 
	 * do not appear in more than one place in the graph.
	 */
	NONE,
	
	/**
	 * Write anchors for all objects.
	 * This is the default.
	 */
	ALL,
	
	/**
	 * Write anchors only for objects that appear in more than one place
	 * in the graph.
	 */
	SOME
}
