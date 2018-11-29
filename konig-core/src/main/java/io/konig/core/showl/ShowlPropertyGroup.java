package io.konig.core.showl;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

public class ShowlPropertyGroup extends HashSet<ShowlPropertyShape>{
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean add(ShowlPropertyShape p) {
		p.group(this);
		return super.add(p);
	}
	
	

}
