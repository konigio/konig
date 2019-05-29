package io.konig.transform.beam;

import io.konig.core.showl.ShowlPropertyShape;

public interface BeamPropertyManager {


	void add(BeamSourceProperty p);
	
	BeamSourceProperty forPropertyShape(ShowlPropertyShape p) throws BeamTransformGenerationException;

}
