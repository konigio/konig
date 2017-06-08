package io.konig.sql.runtime;

import io.konig.dao.core.DaoException;

public interface TableStructureService {

	TableStructure tableStructureForShape(String shapeId) throws DaoException;
}
