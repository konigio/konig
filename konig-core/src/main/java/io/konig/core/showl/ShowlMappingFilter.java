package io.konig.core.showl;

public interface ShowlMappingFilter {
	
	/**
	 * Determine whether a mapping is allowed from source to target.
	 * @param source The source NodeShape
	 * @param target The target NodeShape
	 * @return true if a mapping is allowed from source to target
	 */
	boolean allowMapping(ShowlNodeShape source, ShowlNodeShape target);

}
