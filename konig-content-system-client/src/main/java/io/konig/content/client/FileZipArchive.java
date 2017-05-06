package io.konig.content.client;

import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import io.konig.content.ZipArchive;
import io.konig.content.ZipItem;

public class FileZipArchive implements ZipArchive {
	private ZipFile zipFile;
	private Enumeration<? extends ZipEntry> sequence;
	
	

	@Override
	public ZipItem nextItem() {
		if (sequence == null) {
			sequence = zipFile.entries();
		}
		
		ZipEntry entry = sequence.hasMoreElements() ? sequence.nextElement() : null;
		return entry == null ? null : new FileZipItem(zipFile, entry);
	}

}
