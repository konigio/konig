package io.konig.gcp.common;

/*
 * #%L
 * Konig GCP Common
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


import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Reader that replaces all occurrences of a some search string with a given target string
 * while streaming text from another reader.
 * 
 * @author Greg McFall
 *
 */
public class ReplaceStringsReader extends FilterReader {
	
	private List<SearchReplacePair> pairs;
	private char[] buf;
	private int len;
	private int mark;
	private int end;
	private boolean more;

	/**
	 * Create a new ReplaceStringReader
	 * @param in The source reader
	 * @param search The string that is to be replaced
	 * @param replacement The string that will replace the search string
	 */
	public ReplaceStringsReader(Reader in, List<SearchReplacePair> pairs) {
		super(in);
		this.pairs = pairs;
		len = maxLen();
		buf = new char[len*2];
		mark = end = 0;
		more = true;
	}
	
	public ReplaceStringsReader(Reader in, String...data) {
		super(in);
		pairs = new ArrayList<>();
		for (int i=0; i<data.length; i+=2) {
			String search = data[i];
			String replace = data[i+1];
			if (search!=null && replace!=null) {
				pairs.add(new SearchReplacePair(search, replace));
			}
		}
		len = maxLen();
		buf = new char[len*2];
		mark = end = 0;
		more = true;
	}
	
	private int maxLen() {
		int max = 0;
		for (SearchReplacePair pair : pairs) {
			if (pair.getSearch().length > max) {
				max = pair.getSearch().length;
			}
			if (pair.getReplace().length> max) {
				max = pair.getReplace().length;
			}
		}
		return max;
	}

	@Override
	public boolean markSupported() {
		return false;
	}
	
	@Override
	public int read() throws IOException {
		int result = -1;
		if (more) {
			if (mark>=end) {
				fillBuffer();
			}
			if (mark<end) {
				result = buf[mark++];
			}
		}
		return result;
	}
	

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		int result = -1;
		if (more) {
			fillBuffer(len);
			result = Math.min(len, end-mark);
			for (int i=0; i<result; i++) {
				cbuf[i+off] = buf[mark++];
			}
		}
		return result;
	}
	
	@Override
	public long skip(long n) throws IOException {
		int delta = end-mark;
		if (delta >= n) {
			mark+=n;
			return n;
		}
		mark = end = 0;
		
		return delta + in.skip(n-delta);
	}
	
	/**
	 * Load more characters into the buffer, search for match and if a match is found replace it.
	 * @throws IOException
	 */
	private void fillBuffer() throws IOException {
		if (more) {
			resetBuffer();
			int delta = buf.length - end;
			if (delta > 0) {
				int max = Math.min(len, delta);
				int n = in.read(buf, end, max);
				if (n<0) {
					more = false;
				} else {
					scan(n);
				}
			}
		}
	}
	
	private void fillBuffer(int max) throws IOException {
		if (more && max>(end-mark)) {
			
			resetBuffer();
			int extra = max - (end-mark);

			if (end+extra > buf.length) {
				buf = Arrays.copyOf(buf, end + extra);
			} 
			int n = in.read(buf, end, extra);
			if (n < 0) {
				more = false;
			} else {
				scan(n);
			}
			
		}
	}

	private void resetBuffer() {
		if (mark < end && mark>0) {
			// shift characters to the beginning of the buffer
			int k=0;
			for (int i=mark; i<end; i++) {
				buf[k++] = buf[i];
			}
			mark = 0;
			end = k;
		}
	}

	private void scan(int n) throws IOException {
		int start = end;
		end += n;
	
		for (int i=start; i<end; i++) {
			char b = buf[i];
			pair:
			for (SearchReplacePair pair : pairs) {
				char[] search = pair.getSearch();
				char[] replace = pair.getReplace();
				char c = search[0];
				
				if (b == c) {
					// Potential match.
					int length = Math.min(search.length, end-i-1);
					for (int j=1; j<length; j++) {
						if (search[j] != buf[i+j]) {
							continue pair;
						}
					}
					if (length < search.length) {
						int max = search.length - length;
						append(max);
						for (int j = length; j<search.length; j++) {
							if (search[j] != buf[i+j]) {
								continue pair;
							}
						}
					}
					
					// Found a match
					if (replace.length > search.length) {
						shiftRight(pair, i);
					} else {
						shiftLeft(pair, i);
					}
					i += (replace.length-1);
				}
			}
		}
		
	}

	

	private void shiftLeft(SearchReplacePair pair, int i) {
		char[] search = pair.getSearch();
		char[] replace = pair.getReplace();
		interpolate(replace, i);
		if (replace.length < search.length) {
			int delta = search.length - replace.length;
			for (int j=i+replace.length; j<end; j++) {
				buf[j] = buf[j+delta];
			}
			end -= delta;
		}
		
	}

	private void interpolate(char[] replace, int i) {
		for (int j=0; j<replace.length; j++) {
			buf[i+j] = replace[j];
		}
	}

	private void shiftRight(SearchReplacePair pair, int i) {
		char[] search = pair.getSearch();
		char[] replace = pair.getReplace();
		int delta = replace.length-search.length;
		int max = end + delta;
		if (max > buf.length) {
			buf = Arrays.copyOf(buf, max);
		}
		for (int j=end-1; j>=i; j--) {
			buf[j+delta] = buf[j];
		}
		end += delta;
		interpolate(replace, i);
	}

	private void append(int max) throws IOException {
		if (more) {

			if (end+max >= buf.length) {
				buf = Arrays.copyOf(buf, buf.length*2);
			} 
			int n = in.read(buf, end, max);
			if (n < 0) {
				more = false;
			} else {
				end += n;
			}
		}
		
	}
	

}
