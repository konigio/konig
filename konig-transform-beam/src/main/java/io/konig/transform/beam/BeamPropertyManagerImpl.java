package io.konig.transform.beam;

import java.util.HashMap;
import java.util.Map;

import io.konig.core.showl.ShowlPropertyShape;

public class BeamPropertyManagerImpl implements BeamPropertyManager {
	
	private Map<ShowlPropertyShape, BeamSourceProperty> map = new HashMap<>();

	public BeamPropertyManagerImpl() {
	}

	@Override
	public void add(BeamSourceProperty p) {
		map.put(p.getPropertyShape(), p);

	}

	@Override
	public BeamSourceProperty forPropertyShape(ShowlPropertyShape p) throws BeamTransformGenerationException {
		BeamSourceProperty result = map.get(p);
		if (result == null) {
			throw new BeamTransformGenerationException("Failed to find BeamSourceProperty for " + p.getPath());
		}
		return result;
	}

}
