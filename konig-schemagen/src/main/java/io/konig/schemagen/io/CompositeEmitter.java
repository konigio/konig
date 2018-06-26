package io.konig.schemagen.io;

import java.io.IOException;
import java.util.ArrayList;

import io.konig.core.Graph;
import io.konig.core.KonigException;

public class CompositeEmitter extends ArrayList<Emitter> implements Emitter {
	private static final long serialVersionUID = 1L;

	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		for (Emitter emitter : this) {
			emitter.emit(graph);
		}
		
	}


}
