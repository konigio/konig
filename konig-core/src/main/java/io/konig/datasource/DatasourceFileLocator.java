package io.konig.datasource;

import java.io.File;

/**
 * An interface for locating a file associated with a given DataSource.
 * Different implementations of this interface can be used to locate different
 * types of files.
 * 
 * @author Greg McFall
 *
 */
public interface DatasourceFileLocator {

	/**
	 * Locate a file associated with a given DataSource.
	 * @param ds
	 * @return The file associated with the DataSource, or null if no appropriate file is found.
	 */
	public File locateFile(DataSource ds);
}
