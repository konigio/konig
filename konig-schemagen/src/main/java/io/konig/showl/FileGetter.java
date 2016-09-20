package io.konig.showl;

import java.io.File;

import org.openrdf.model.URI;

/**
 * A utility for getting the file that holds the description of a given resource.
 * @author Greg McFall
 *
 */
public interface FileGetter {

	/**
	 * Get the File where the description of a given resource is stored.
	 * @param resourceId  The URI for the resource
	 * @return The File where a description of the resource is to be stored.
	 */
	File getFile(URI resourceId);
}
