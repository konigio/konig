package io.konig.pojo.io;

import io.konig.core.Vertex;

public interface PojoFactory {

	/**
	 * Create a POJO of a given type from data contained in a given vertex.
	 * @param v The vertex containing data for the POJO
	 * @param type The Java Class of the POJO to be created.
	 * @return A POJO of the given type containing data from the given vertex.
	 */
	public <T> T create(Vertex v, Class<T> type) throws ParseException;
}
