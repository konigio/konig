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
import java.io.PrintWriter;

import io.konig.core.Graph;
import io.konig.core.KonigException;

/**
 * A handler that merely prints a report of changes to files.  This handler
 * does not actually make any changes to files.
 * @author Greg McFall
 *
 */
public class FileChangeReporter implements FileChangeHandler {
	private static final String SEPARATOR = "=============================================================================";
	private static final String CREATE = "CREATE ";
	private static final String UPDATE = "UPDATE ";
	private static final String DELETE = "DELETE ";

	private PrintWriter out;
	private String rootDir;
	
	private ChangeSetReportWriter reporter;
	
	

	public FileChangeReporter(File rootDir, PrintWriter out, ChangeSetReportWriter reporter) {
		this.rootDir = rootDir.getAbsolutePath();
		this.out = out;
		this.reporter = reporter;
	}

	@Override
	public void deleteFile(File file) {
		String path = path(file);
		out.println(SEPARATOR);
		out.print(DELETE);
		out.println(path);
		out.println();
	}

	private String path(File file) {
		String full = file.getAbsolutePath();
		if (!full.startsWith(rootDir)) {
			throw new KonigException("Invalid path: " + full);
		}
		
		return full.substring(rootDir.length()+1);
	}

	@Override
	public void createFile(File file, Graph contents) throws IOException {
		
		String path = path(file);

		out.println(SEPARATOR);
		out.print(CREATE);
		out.println(path);
		out.println();
		
		reporter.write(contents, out);
	}

	@Override
	public void modifyFile(File file, Graph changeSet) throws IOException {
		String path = path(file);

		out.println(SEPARATOR);
		out.print(UPDATE);
		out.println(path);
		out.println();
		
		reporter.write(changeSet, out);

	}

}
