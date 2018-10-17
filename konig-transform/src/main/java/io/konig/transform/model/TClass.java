package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

/**
 * A representation of an OWL Class that is helpful when generating transforms.
 * It encapsulates information about the targetClass for some shape that is the target of the transform.
 * @author Greg McFall
 *
 */
public class TClass {

	private URI id;
	private TNodeShape targetShape;
	private Set<TNodeShape> sourceShapes = new HashSet<>();
	private Map<URI, TProperty> in = new HashMap<>();
	private Map<URI, TProperty> out = new HashMap<>();
	
	public TClass(URI id, TNodeShape targetShape) {
		this.id = id;
		this.targetShape = targetShape;
	}

	public void putIn(URI predicate, TProperty property) {
		in.put(predicate, property);
	}
	
	public TProperty getIn(URI predicate) {
		return in.get(predicate);
	}
	
	public  void putOut(URI predicate, TProperty property) {
		out.put(predicate, property);
	}
	
	public TProperty getOut(URI predicate) {
		return out.get(predicate);
	}
	
	public TProperty produceOut(URI predicate) {
		TProperty result = out.get(predicate);
		if (result == null) {
			result = new TProperty();
			out.put(predicate, result);
		}
		return result;
	}

	public URI getId() {
		return id;
	}

	public TNodeShape getTargetShape() {
		return targetShape;
	}
	
	public void addSourceShape(TNodeShape candidateSourceShape) {
		sourceShapes.add(candidateSourceShape);
	}

	public Set<TNodeShape> getSourceShapes() {
		return sourceShapes;
	}

}
