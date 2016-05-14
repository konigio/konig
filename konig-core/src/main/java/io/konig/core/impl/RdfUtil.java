package io.konig.core.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.DC;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;

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


public class RdfUtil {
	
	public static String curie(NamespaceManager nsManager, URI uri) {
		Namespace ns = nsManager.findByName(uri.getNamespace());
		if (ns != null) {
			StringBuilder builder = new StringBuilder();
			builder.append(ns.getPrefix());
			builder.append(':');
			builder.append(uri.getLocalName());
			return builder.toString();
		}
		
		return uri.stringValue();
	}

	public static String getDescription(Vertex subject) {
		
		Value value =null;
		return 
			((value=subject.getValue(RDFS.COMMENT))!=null) 			? value.stringValue() :
			((value=subject.getValue(DC.DESCRIPTION)) != null)		? value.stringValue() :
			((value=subject.getValue(Schema.description)) != null)	? value.stringValue() :
			null;
	}
	
	public static List<Vertex> listSubtypes(List<Vertex> typeList) {
		Graph graph = null;
		if (!typeList.isEmpty()) {
			graph = typeList.get(0).getGraph();
		}
		Set<Resource> set = new HashSet<>();
		for (Vertex v : typeList) {
			set.add(v.getId());
			Set<Resource> subTypes = RdfUtil.subTypes(v);
			set.addAll(subTypes);
		}
		List<Vertex> result = new ArrayList<>();
		for (Resource r : set) {
			result.add(graph.getVertex(r));
		}
		
		return result;
	}
	
	public static String normalize(String value) {
		if (value != null) {
			StringTokenizer tokens = new StringTokenizer(value, "\r\n\t ");
			StringBuilder builder = new StringBuilder();
			while (tokens.hasMoreTokens()) {
				builder.append(tokens.nextToken());
				if (tokens.hasMoreTokens()) {
					builder.append(' ');
				}
			}
			value = builder.toString();
		}
		
		return value;
	}
	
	/**
	 * Find the super types of a given OWL Class.
	 * @param owlClass The OWL class whose super types are to be found.
	 * @return The set of super types of the given class.
	 */
	public static Set<Resource> superTypes(Vertex owlClass) {
		
		Set<Resource> result = new HashSet<>();
		
		List<Vertex> stack = new ArrayList<>();
		stack.add(owlClass);
		for (int i=0; i<stack.size(); i++) {
			Vertex target = stack.get(i);
			List<Vertex> list = target.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
			for (Vertex v : list) {
				Resource id = v.getId();
				if (!id.equals(owlClass.getId()) && !result.contains(id)) {
					result.add(id);
					stack.add(v);
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Compute the transitive closure of the subtypes of a given OWL Class.
	 * @param owlClass The OWL Class whose transitive closure is to be computed.
	 * @return The transitive closure of the given OWL Class.
	 */
	public static List<Vertex> subtypeList(Vertex owlClass) {
		return owlClass.asTraversal().inTransitive(RDFS.SUBCLASSOF).toVertexList();
	}
	
	public static Set<Resource> subTypes(Vertex owlClass) {
		
		Set<Resource> result = new HashSet<>();
		
		List<Vertex> stack = new ArrayList<>();
		stack.add(owlClass);
		for (int i=0; i<stack.size(); i++) {
			Vertex target = stack.get(i);
			List<Vertex> list = target.asTraversal().in(RDFS.SUBCLASSOF).toVertexList();
			for (Vertex v : list) {
				Resource id = v.getId();
				if (!id.equals(owlClass.getId()) && !result.contains(id)) {
					result.add(id);
					stack.add(v);
				}
			}
		}
		
		return result;
	}
	
	public static boolean isSubClassOf(Vertex subject, Resource target) {
		
		List<Vertex> list = subject.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex v : list) {
			if (v.getId().equals(target)) {
				return true;
			}
		}
		for (Vertex v : list) {
			if (isSubClassOf(v, target)) {
				return true;
			}
		}
		
		return false;
	}
}
