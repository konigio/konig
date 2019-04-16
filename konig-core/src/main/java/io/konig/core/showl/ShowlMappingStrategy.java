package io.konig.core.showl;

import java.util.List;

public interface ShowlMappingStrategy {
	
	public List<ShowlDirectPropertyShape> selectMappings(ShowlManager manager, ShowlNodeShape target);
}
