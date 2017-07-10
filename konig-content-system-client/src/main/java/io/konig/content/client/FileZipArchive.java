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
