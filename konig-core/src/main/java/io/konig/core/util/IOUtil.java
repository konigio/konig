package io.konig.core.util;

import java.io.BufferedReader;

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


import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class IOUtil {
	
	private static Logger logger = LoggerFactory.getLogger(IOUtil.class);
	
	public static void close(Closeable stream, String name) {
		if (stream != null) {
			try {
				stream.close();
			} catch (Throwable oops) {
				logger.warn("Failed to close " + name, oops);
			}
		}
	}

	public static void recursiveDelete(File file) {
		if (file.isDirectory()) {
			File[] array = file.listFiles();
			for (File child : array) {
				recursiveDelete(child);
			}
		}
		file.delete();
	}
	
	public static void replaceAll(File file, List<RewriteRule> rules) throws IOException {
		
		File tmpFile = File.createTempFile(file.getName(), "tmp", file.getParentFile());
		try {
			try (FileWriter out = new FileWriter(tmpFile)) {
				try (BufferedReader buffered = new BufferedReader(new FileReader(file))) {
					String line = null;
					while ( (line = buffered.readLine()) != null) {
						for (RewriteRule rule : rules) {
							line = line.replace(rule.getSourceString(), rule.getTargetString());
						}
						out.write(line);
						out.write('\n');
					}
				}
			}
			Files.copy(tmpFile, file);
			
		} finally {
			tmpFile.delete();
		}
	}
	
	public static String stringContent(File file) throws IOException {
		StringBuilder sb = new StringBuilder();
		InputStream is = new FileInputStream(file);
		BufferedReader buf = new BufferedReader(new InputStreamReader(is));
		try {
			String line = buf.readLine();
			while (line != null) {
				sb.append(line).append("\n");
				line = buf.readLine();
			}
		} finally {
			close(buf, file.getAbsolutePath());
		}
		return sb.toString();
	}

}
