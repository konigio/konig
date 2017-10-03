package io.konig.core.pojo;

/*
 * #%L
 * Konig Core
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


import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;

/**
 * A factory that creates Plain-Old Java Objects from RDF Graphs.
 * Properties from an RDF Graph get mapped to properties on a Java Object in accordance with 
 * the conventions described below.
 * <h3> POJO Creation</h3>
 * Each POJO class must support one of the following creation methods:
 * <ol>
 *   <li> An implementation of {@link PojoCreator} with the following characteristics:
 *   	<ul>
 *   		<li> Must be contained in the same package as the POJO class. 
 *          <li> Must have a name of the form <code>{PojoClassSimpleName}Creator</code>.  For
 *               example, if the POJO class name is <code>PropertyPath</code> then the creator
 *               class would be named <code>PropertyPathCreator</code>.
 *          <li> Must implement the <code>create</code> method that takes a <code>Vertex</code> as an argument.
 *      </ul>
 *   <li> Default (no-arg) constructor.
 *   <li> Constructor which takes a string as an argument.  This constructor is used when the class 
 *        is represented as a string literal.
 * </ol>
 * <h3> Simple Properties</h3>
 * For each single-valued property, the POJO should declare a setter method such as:
 * <pre>
 *  	void setEmail(String email) {...}
 * </pre>
 * <p>
 * The name of the property (in this case "email") must match the local name of the 
 * RDF Property.
 * </p>
 * <p>
 * Alternatively, you can use a different name for the setter method and declare the RDF
 * Property explicitly, like this:
 * <pre>
 * 		@RdfProperty("http://schema.org/email")
 * 		void setEmailAddress(String emailAddress);
 * </pre>
 * <p>
 * If the property can have multiple values, then the POJO <em>may</em> declare a setter 
 * method as described above (with or without the annotation.
 * </p>
 * <p> However, that is considered bad practice. In the case of a multi-valued property,
 * it is better to define a collection setter and/or an adder method like this:
 * <pre>
 *  	void setEmail(Collection<String> email);
 * 		void addEmail(String email);
 * </pre>
 * 
 * The Collection argument in the setter may have any type that is assignable to
 *  java.util.Collection (java.util.List, java.util.Set, java.util.LinkedList, etc.).
 *  
 * <h3> RDF List Properties </h3>
 * 
 * Currently, we only support RDF Lists as properties of other entities (not as stand-alone entities).
 * 
 * There are three design patterns for working with RDF Lists in Java:
 * <ol>
 *   <li> Declare a method to append an element to the list.  The method should have a name of 
 *        the form <code>appendToX</code> where <code>X</code> is the capitalized local
 *        name of the property through which the RDF List is accessed.  Consider, for instance,
 *        a property whose local name is <code>path</code>.  Your POJO could declare a method
 *        like this:
 *        <pre>
 *     		void appendToPath(URI pathElement);
 *     	  </pre>
 *        Your POJO is responsible for creating the collection that holds elements that are appended
 *        via this method.  In this example, the argument is a URI.  But it could be a JavaObject
 *        representing the element to be appended, or a literal value if the List contains literals.
 *   </li>
 *   <li> Declare a setter that takes a Java Collection as an argument
 *        <pre>
 *   		void setPath(List<URI> path);
 *        </pre>
 *   </li>
 *   <li> Declare a setter that takes a custom Java class (or interface) as the RDF List.  This option
 *        is similar to the second option (setter that takes a Java Collection), but in this case 
 *        the argument is not a Java Collection.  It is a custom Java class.  The custom Java class 
 *        must have a default (no-arg) constructor, it must contain the @RdfList annotation, 
 *        and it must have an <code>add</code> method for adding elements to the List.  For example, 
 *        you might define the following Java interface to represent an RDF List:
 *        <pre>
 *          @RdfList
 *        	public interface Path {
 *            void add(URI pathElement);
 *            ...
 *          }
 *        </pre>
 *        In this case, your POJO would declare the following setter:
 *        <pre>
 *        	void setPath(Path path);
 *        </pre>
 *	</li>
 * </ol>
 * 
 * 
 * @author Greg McFall
 *
 */
public interface PojoFactory {

	/**
	 * Create a POJO of a given type from data contained in a given vertex.
	 * @param v The vertex containing data for the POJO
	 * @param type The Java Class of the POJO to be created.
	 * @return A POJO of the given type containing data from the given vertex.
	 */
	public <T> T create(Vertex v, Class<T> type) throws KonigException;
	
	/**
	 * Map individuals from a graph into Java objects.
	 * @param graph  The graph containing individuals that are to be mapped into Java objects.
	 * @throws ParseException
	 */
	public void createAll(Graph graph) throws KonigException;
}
