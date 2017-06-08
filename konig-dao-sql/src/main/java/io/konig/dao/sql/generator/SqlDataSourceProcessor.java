package io.konig.dao.sql.generator;

import java.util.List;

import io.konig.datasource.DataSource;
import io.konig.shacl.Shape;

public interface SqlDataSourceProcessor {

	/**
	 * List the data sources that have a SQL interface.
	 */
	List<DataSource> findDataSources(Shape shape);
	
	/**
	 * Given a DataSource for a specified Shape, provide the fully-qualified name for 
	 * the Java class of the ShapeReadService associated with that DataSource.
	 */
	String shapeReaderClassName(Shape shape, DataSource dataSource) throws SqlDaoGeneratorException;
}
