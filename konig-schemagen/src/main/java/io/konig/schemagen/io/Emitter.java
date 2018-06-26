package io.konig.schemagen.io;

import java.io.IOException;

import io.konig.core.Graph;
import io.konig.core.KonigException;

public interface Emitter {
	
	void emit(Graph graph) throws IOException, KonigException;

}
