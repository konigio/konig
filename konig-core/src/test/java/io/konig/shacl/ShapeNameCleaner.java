package io.konig.shacl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class ShapeNameCleaner {

	public static final void main(String[] args) throws Exception {
		File inFile = new File(args[0]);
		File outFile = new File(args[1]);
		
		
		try (BufferedReader reader = new BufferedReader(new FileReader(inFile))) {
			try (PrintWriter out = new PrintWriter(new FileWriter(outFile))) {
				String line = null;
				while ( (line = reader.readLine()) != null) {
					String name = line.trim();
					if (name.length()==0) {
						out.println();
					} else {
						if (name.endsWith("Shape")) {
							out.println(name);
						} else if (allCaps(name)) {
							name = name + "_Shape";
							out.println(name);
						} else {
							
							
							name = name + "Shape";
							out.println(name);
						}
					}
				}
			}
		}
	}

	private static boolean allCaps(String name) {
		int colon = name.indexOf(':');
		
		for (int i=colon+1; i<name.length();) {
			int c = name.codePointAt(i);
			i += Character.charCount(c);
			
			if (Character.isAlphabetic(c) && Character.isLowerCase(c)) {
				return false;
			}
		}
		return true;
	}

}
