package io.konig.content.client;

/*
 * #%L
 * Konig Content System, Client Library
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
