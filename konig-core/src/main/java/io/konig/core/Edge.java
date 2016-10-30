package io.konig.core;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

/**
 * A reified statement that can have properties of its own.  These properties are called <em>annotations</em>.
 * @author Greg McFall
 *
 */
public interface Edge extends Statement, Cloneable {

	/**
	 * Get an annotation property of this edge.  
	 * @param predicate The predicate that identifies the annotation whose value is to be returned.
	 * @return The value of the specified annotation property. If the specified annotation property has
	 * multiple values, a {@link ValueSet} that contains all of the values will be returned.  Otherwise,
	 * a single discrete value will be returned, or null if there is no value for the specified annotation.
	 */
	Value getAnnotation(URI predicate);
	
	/**
	 * Set an annotation property of this edge.  This method overwrites any previous value for
	 * the annotation property.  If the previous value was a {@link ValueSet}, then the entire set
	 * is replaced with the supplied value.
	 * @param predicate The predicate that identifies the annotation property to be set
	 * @param value The value of the annotation property to be set.
	 */
	Edge setAnnotation(URI predicate, Value value);
	
	/**
	 * Remove the specified annotation property from the Edge.  If the current value is a {@link ValueSet},
	 * the entire set of values is removed.
	 * @param predicate The predicate that names the annotation to be removed
	 * @return The value of the annotation property that was removed, or null if there was no such 
	 *   annotation property.
	 */
	Value removeAnnotation(URI predicate);
	
	/**
	 * Copy the annotation properties from another edge into this Edge.
	 * @param edge The edge from which annotations will be copied
	 */
	void copyAnnotations(Edge edge);
	
	/**
	 * Get the values of a specific annotation property as a {@link ValueSet}.
	 * @param predicate The predicate that names the annotation property
	 * @return A ValueSet containing all the values of the specified annotation.  If there are no
	 * such values an empty set is returned.
	 */
	ValueSet getAnnotationSet(URI predicate);
	
	/**
	 * Add an annotation value to the Edge.
	 * @param predicate The predicate that names the annotation property
	 * @param value The value of the annotation property.
	 * @return This edge.
	 */
	Edge addAnnotation(URI predicate, Value value);
		
	/**
	 * A convenience function which tests whether two values are equal, or one
	 * is contained in the other.
	 * @param a  A discrete Value (Resource or Literal) or a ValueSet
	 * @param b  Another discrete Value (Resource or Literal) or a ValueSet
	 * @return True if <code>a</code> and <code>b</code> are equal, or <code>a</code>
	 *    is contained in <code>b</code>, or <code>b</code> is contained in <code>a</code>.
	 */
	boolean matches(Value a, Value b);

}
