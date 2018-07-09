package io.konig.shacl.io;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.io.FileGetter;
import io.konig.core.io.ResourceFilter;

public class FilteringFileGetter implements FileGetter {
	
	private List<FileGetterFilter> list = new ArrayList<>();
	
	public void add(ResourceFilter filter, FileGetter fileGetter) {
		list.add(new FileGetterFilter(filter, fileGetter));
	}
	
	@Override
	public File getFile(URI resourceId) {
		for (FileGetterFilter e : list) {
			if (e.filter.accept(resourceId)) {
				return e.getter.getFile(resourceId);
			}
		}
		return null;
	}
	
	private static class FileGetterFilter {
		private ResourceFilter filter;
		private FileGetter getter;
		
		public FileGetterFilter(ResourceFilter filter, FileGetter getter) {
			this.filter = filter;
			this.getter = getter;
		}
		
		
	}

}
