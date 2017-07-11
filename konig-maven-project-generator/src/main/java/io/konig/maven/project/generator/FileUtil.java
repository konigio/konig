package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtil {
	
	public static void copy(File source, File target) throws IOException {
		FileInputStream input = new FileInputStream(source);
		copyAndCloseSource(input, target);
	}
	public static String relativePath(File src, File target) {
		if (src.isDirectory()) {
			src = new File(src, "foo");
		}
		String srcPath = src.toPath().normalize().toAbsolutePath().toString();
		String targetPath = target.toPath().normalize().toAbsolutePath().toString();
		if (srcPath.indexOf('\\') >= 0) {
			srcPath = srcPath.replace('\\', '/');
			targetPath = targetPath.replace('\\', '/');
		}
		return relativePath(srcPath, targetPath);
	}
	public static String relativePath(String src, String target) {
		
		int aStart = 0;
		int bStart = 0;
		
		
		StringBuilder builder = new StringBuilder();
		while (aStart >= 0 && bStart>=0) {
			int aEnd = src.indexOf('/', aStart);
			int bEnd = target.indexOf('/', bStart);
			if (aEnd == bEnd && aEnd!=-1) {
				String aPart = src.substring(aStart, aEnd);
				String bPart = target.substring(bStart, bEnd);
				
				if (!aPart.equals(bPart)) {
					break;
				} 
				
				aStart = aEnd + 1;
				bStart = bEnd + 1;
			
			} else {
				break;
			}
		}
		if (aStart>=0) {
			aStart = src.indexOf('/', aStart+1);
			while (aStart>0) {
				builder.append("../");
				aStart = src.indexOf('/', aStart+1);
			}
			if (bStart>=0 && bStart < target.length()) {
				builder.append(target.substring(bStart));
			}
		}
		
		return builder.toString();
	}
	
	public static void copyAndCloseSource(InputStream source, File target) throws IOException {
		
		try (FileOutputStream out = new FileOutputStream(target)) {
			byte[] buffer = new byte[1024];
			int len;
			while ( (len=source.read(buffer)) != -1) {
				out.write(buffer, 0, len);
			}
		} finally {
			source.close();
		}
		
	}

	public static void deleteDir(String path) {
		delete(new File(path));
	}
	public static void delete(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					delete(child);
				}
			}
			file.delete();
		}
	}
}
