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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
