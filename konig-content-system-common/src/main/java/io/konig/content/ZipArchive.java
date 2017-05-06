package io.konig.content;

/**
 * An interface for accessing items in a zip archive.
 * @author Greg McFall
 *
 */
public interface ZipArchive {

	ZipItem nextItem() throws ContentAccessException;
}
