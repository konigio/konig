package io.konig.content;

import java.io.InputStream;

/**
 * An item (i.e. file) within a zip archive.
 * @author Greg McFall
 *
 */
public interface ZipItem {

	String getName();
	InputStream getInputStream() throws ContentAccessException;
}
