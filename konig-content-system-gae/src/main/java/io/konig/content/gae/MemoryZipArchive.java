package io.konig.content.gae;


import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.konig.content.ContentAccessException;
import io.konig.content.ZipArchive;
import io.konig.content.ZipItem;

public class MemoryZipArchive implements ZipArchive {
	
	private ZipInputStream zipInput;

	public MemoryZipArchive(ZipInputStream zipInput) {
		this.zipInput = zipInput;
	}

	@Override
	public ZipItem nextItem() throws ContentAccessException {
		ZipEntry entry;
		try {
			entry = zipInput.getNextEntry();
			return entry==null ? null : new MemoryZipItem(zipInput, entry);
			
		} catch (IOException e) {
			throw new ContentAccessException(e);
		}
	}

}
