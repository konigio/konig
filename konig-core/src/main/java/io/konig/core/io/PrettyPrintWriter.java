package io.konig.core.io;

/*
 * #%L
 * Konig Core
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


import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import io.konig.core.NamespaceManager;

public class PrettyPrintWriter extends PrintWriter {
	
	private boolean prettyPrint = true;
	private int indentLevel;
	private String indentText = "   ";
	
	private NamespaceManager nsManager;

	public PrettyPrintWriter(Writer arg0) {
		super(arg0);
	}

	public PrettyPrintWriter(OutputStream arg0) {
		super(arg0);
	}

	public PrettyPrintWriter(String arg0) throws FileNotFoundException {
		super(arg0);
	}

	public PrettyPrintWriter(File arg0) throws FileNotFoundException {
		super(arg0);
	}

	public PrettyPrintWriter(Writer arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public PrettyPrintWriter(OutputStream arg0, boolean arg1) {
		super(arg0, arg1);
	}

	public PrettyPrintWriter(String arg0, String arg1) throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}

	public PrettyPrintWriter(File arg0, String arg1) throws FileNotFoundException, UnsupportedEncodingException {
		super(arg0, arg1);
	}
	
	
	
	public NamespaceManager getNamespaceManager() {
		return nsManager;
	}

	public void setNamespaceManager(NamespaceManager nsManager) {
		this.nsManager = nsManager;
	}

	public void pushIndent() {
		indentLevel++;
	}
	
	public void popIndent() {
		indentLevel--;
	}
	
	public void indent() {
		if (prettyPrint) {
			for (int i=0; i<indentLevel; i++) {
				print(indentText);
			}
		} else {
			print(' ');
		}
	}

	public String getIndentText() {
		return indentText;
	}

	public void setIndentText(String indentText) {
		this.indentText = indentText;
	}

	public int getIndentLevel() {
		return indentLevel;
	}

	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}
	
	@Override
	public void println() {
		if (prettyPrint) {
			print('\n');
		} else {
			print(' ');
		}
	}
	
	
}
