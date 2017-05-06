package io.konig.content.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.konig.content.ContentAccessException;
import io.konig.content.ZipItem;

public class FileZipItem implements ZipItem {
	private ZipFile zipFile;
	private ZipEntry entry;
	
	

	public FileZipItem(ZipFile zipFile, ZipEntry entry) {
		this.zipFile = zipFile;
		this.entry = entry;
	}

	@Override
	public String getName() {
		return entry.getName();
	}

	@Override
	public InputStream getInputStream() throws ContentAccessException {
		try {
			return zipFile.getInputStream(entry);
		} catch (IOException e) {
			throw new ContentAccessException(e);
		}
	}

}
