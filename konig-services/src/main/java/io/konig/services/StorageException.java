package io.konig.services;

/**
 * An exception that occurs when the storage or retrieval of a resource failed.
 * @author Greg McFall
 *
 */
public class StorageException extends Exception {
	private static final long serialVersionUID = 1L;

	public StorageException() {
	}

	public StorageException(String message) {
		super(message);
	}

	public StorageException(Throwable cause) {
		super(cause);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
