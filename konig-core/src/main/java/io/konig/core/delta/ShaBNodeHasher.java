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


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;

/**
 * A utility that creates a hash by sorting the properties of a BNode, serializing them into a string, and
 * then computing the SHA1 hash of the string.
 * @author Greg McFall
 *
 */
public class ShaBNodeHasher implements BNodeHasher {

	@Override
	public String createHash(URI predicate, Vertex object) {
		
		Graph graph = object.getGraph();
		
		Set<Edge> set = object.outEdgeSet();
		List<Pair> list = toPairList(set, graph);
		Collections.sort(list);
		
		StringBuilder builder = new StringBuilder();
		for (Pair pair : list) {
			builder.append(pair.predicate.stringValue());
			builder.append(' ');
			builder.append(pair.value);
			builder.append('|');
		}
		String text = builder.toString();
		try {
			return SHA1(text);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			throw new KonigException(e);
		}
	}
	
	private List<Pair> toPairList(Set<Edge> set, Graph graph) {
		List<Pair> list = new ArrayList<>();
		for (Edge e : set) {
			list.add(toPair(e, graph));
		}
		return list;
	}

	private Pair toPair(Edge e, Graph graph) {
		URI predicate = e.getPredicate();
		Value object = e.getObject();
		String value = null;
		if (object instanceof Literal) {
			Literal literal = (Literal) object;
			String language = literal.getLanguage();
			URI type = literal.getDatatype();
			if (type.equals(XMLSchema.STRING)) {
				type = null;
			}
			StringBuilder builder = new StringBuilder();
			builder.append('"');
			builder.append(literal.getLabel());
			builder.append('"');
			if (language != null) {
				builder.append('@');
				builder.append(language);
			} else if (type != null) {
				builder.append('^');
				builder.append(type.stringValue());
			}
			value = builder.toString();
		} else if (object instanceof URI) {
			value = object.stringValue();
		} else {
			Resource id = (Resource)object;
			Vertex v = graph.getVertex(id);
			value = createHash(predicate, v);
		}
		
		return new Pair(predicate, value);
	}

	static class Pair implements Comparable<Pair> {
		URI predicate;
		String value;
		
		public Pair(URI predicate, String value) {
			this.predicate = predicate;
			this.value = value;
		}

		@Override
		public int compareTo(Pair other) {
			
			int result = predicate.stringValue().compareTo(other.predicate.stringValue());
			if (result == 0) {
				result = value.compareTo(other.value);
			}
			
			return result;
		}
		
		
	}
	
	 private static String convertToHex(byte[] data) { 
	        StringBuffer buf = new StringBuffer();
	        for (int i = 0; i < data.length; i++) { 
	            int halfbyte = (data[i] >>> 4) & 0x0F;
	            int two_halfs = 0;
	            do { 
	                if ((0 <= halfbyte) && (halfbyte <= 9)) 
	                    buf.append((char) ('0' + halfbyte));
	                else 
	                    buf.append((char) ('a' + (halfbyte - 10)));
	                halfbyte = data[i] & 0x0F;
	            } while(two_halfs++ < 1);
	        } 
	        return buf.toString();
	    } 
	 
	    public static String SHA1(String text) 
	    throws NoSuchAlgorithmException, UnsupportedEncodingException  { 
		    MessageDigest md;
		    md = MessageDigest.getInstance("SHA-1");
		    byte[] sha1hash = new byte[40];
		    md.update(text.getBytes("iso-8859-1"), 0, text.length());
		    sha1hash = md.digest();
		    return convertToHex(sha1hash);
	    } 

}
