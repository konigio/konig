package io.konig.maven;

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
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtil {
	
	public static void copyDirectory(File source, File target) throws IOException {
		target.mkdirs();
		for (File sourceChild : source.listFiles()) {
			File targetChild = new File(target, sourceChild.getName());
			if (sourceChild.isDirectory()) {
				copyDirectory(sourceChild, targetChild);
			} else {
				copy(sourceChild, targetChild);
			}
		}
	}
	
	public static File leastCommonParent(File a, File b) {
		String aPath = a.getAbsolutePath().replace('\\', '/');
		String bPath = b.getAbsolutePath().replace('\\', '/');
		
		String parent = null;
		int aPoint = aPath.lastIndexOf('/');
		int bPoint = bPath.length();
		while (aPoint > 0) {
			
			bPoint = bPath.lastIndexOf('/', bPoint-1);
			if (bPoint != aPoint) {
				break;
			}
			parent = aPath.substring(0, aPoint);
			String bParent = bPath.substring(0, bPoint);
			if (!parent.equals(bParent)) {
				break;
			}
			
			aPoint = aPath.lastIndexOf('/', aPoint-1);
		}
		
		if (aPoint <= 0) {
			return null;
		}
		
		
		if (File.separator.equals("\\")) {
			parent = parent.replace('/', '\\');
		}
		
		return new File(parent);
	}
	
	public static void copy(File source, File target) throws IOException {
		FileInputStream input = new FileInputStream(source);
		copyAndCloseSource(input, target);
	}
	public static String relativePath(File src, File target) {
		if (src.isDirectory()) {
			src = new File(src, "foo");
		}
		try {
			String srcPath = src.getCanonicalPath();
			String targetPath = target.getCanonicalPath();
			if (srcPath.indexOf('\\') >= 0) {
				srcPath = srcPath.replace('\\', '/');
				targetPath = targetPath.replace('\\', '/');
			}
			return relativePath(srcPath, targetPath);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
	
	public static String readString(File file) throws IOException {
		byte[] encoded = Files.readAllBytes(file.toPath());
		return new String(encoded);
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
