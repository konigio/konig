package io.konig.core.delta;

/*
 * #%L
 * konig-core
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
import java.io.IOException;

import io.konig.core.Graph;

public interface FileChangeHandler {
	
	/**
	 * Delete a file.
	 * @param file The file that is to be deleted.
	 */
	void deleteFile(File file) throws IOException;
	
	/**
	 * Create a file.
	 * @param file The file that is to be created
	 * @param contents A graph that describes the contents of the file being created
	 * @throws IOException 
	 */
	void createFile(File file, Graph contents) throws IOException;
	
	/**
	 * Modify a file.
	 * @param file The file that is being modified
	 * @param changeSet A graph that describes the changes made to the file. 
	 */
	void modifyFile(File file, Graph changeSet) throws IOException;

}
