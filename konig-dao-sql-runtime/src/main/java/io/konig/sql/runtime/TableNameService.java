package io.konig.sql.runtime;

public interface TableNameService {

	/**
	 * Get the Table name for a specified SHACL Shape.
	 * @param shapeId The string value of the URI that identifies the SHACL Shape
	 * @return The table name for the Shape.
	 */
	String tableName(String shapeId);
}
