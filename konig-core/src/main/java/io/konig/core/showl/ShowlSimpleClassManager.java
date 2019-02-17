package io.konig.core.showl;

import io.konig.core.OwlReasoner;
import io.konig.shacl.ShapeManager;

/**
 * A ShowlManager that does not infer inverses or build mappings
 * @author Greg McFall
 *
 */
public class ShowlSimpleClassManager extends ShowlManager {

	public ShowlSimpleClassManager(ShapeManager shapeManager, OwlReasoner reasoner) {
		super(shapeManager, reasoner);
	}

	
	public void build() {
		clear();
		loadShapes();
		inferTargetClasses();
	}
	

}
