package io.konig.core.showl;

import java.util.Set;

public interface ShowlMappingStrategy {
	
	public Set<ShowlPropertyShape> selectMappings(ShowlManager manager, ShowlNodeShape target);
}
