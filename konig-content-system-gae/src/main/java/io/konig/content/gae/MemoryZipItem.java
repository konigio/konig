package io.konig.content.gae;

import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.konig.content.ContentAccessException;
import io.konig.content.ZipItem;

public class MemoryZipItem implements ZipItem {
	
	private ZipInputStream zipInput;
	private ZipEntry entry;

	public MemoryZipItem(ZipInputStream zipInput, ZipEntry entry) {
		this.zipInput = zipInput;
		this.entry = entry;
	}

	@Override
	public String getName() {
		return entry.getName();
	}

	@Override
	public InputStream getInputStream() throws ContentAccessException {
		return zipInput;
	}

}
