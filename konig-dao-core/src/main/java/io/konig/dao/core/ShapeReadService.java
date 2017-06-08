package io.konig.dao.core;

import java.io.Writer;

/**
 * A service for accessing shapes in a particular format
 * @author Greg McFall
 *
 */
public interface ShapeReadService {

	void execute(ShapeQuery query, Writer output, Format format) throws DaoException;
}
