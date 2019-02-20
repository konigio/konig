package io.konig.spreadsheet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

public class IndividualManager {
	
	private Map<String, Set<URI>> map = new HashMap<>();

	public IndividualManager() {
	}
	
	public void put(String name, URI id) {
		Set<URI> set = map.get(name);
		if (set == null) {
			set = new HashSet<>();
			map.put(name, set);
		}
		set.add(id);
	}
	
	public Set<URI> getIndividualsByName(String name) {
		return map.get(name);
	}

}
