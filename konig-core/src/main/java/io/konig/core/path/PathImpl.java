package io.konig.core.path;

import java.io.IOException;
import java.io.StringWriter;

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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.SPARQLBuilder;
import io.konig.core.Traverser;
import io.konig.core.Vertex;
import io.konig.core.json.KonigJsonPrettyPrinter;

public class PathImpl implements Path {
	
	private List<Step> stepList;
	private Context context;
	
	public PathImpl() {
		stepList =new ArrayList<>();
	}
	
	public PathImpl(List<Step> list) {
		stepList = list;
	}
	
	public void add(Step step) {
		stepList.add(step);
	}
	
	List<Step> getStepList() {
		return stepList;
	}

	@Override
	public Path out(URI predicate) {
		stepList.add(new OutStep(predicate));
		return this;
	}

	@Override
	public Path in(URI predicate) {
		stepList.add(new InStep(predicate));
		return this;
	}

	@Override
	public List<Step> asList() {
		return stepList;
	}

	@Override
	public Path copy() {
		return new PathImpl(new ArrayList<>(stepList));
	}

	@Override
	public Path has(URI predicate, Value value) {
		Step last = stepList.isEmpty() ? null : stepList.get(stepList.size()-1);
		if (last instanceof HasStep) {
			HasStep step = (HasStep) last;
			step.add(predicate, value);
		} else {
			stepList.add(new HasStep(predicate, value));
		}
		return this;
	}



	@Override
	public Set<Value> traverse(Vertex source) {
		Set<Value> input = new HashSet<>();
		input.add(source.getId());
		
		Graph graph = source.getGraph();
		Traverser traverser = new Traverser(graph, input);
		for (Step step : stepList) {
			traverser.visit(step);
		}
		return traverser.getResultSet();
	}

	@Override
	public Set<Value> traverse(Traverser traverser) {
		for (Step step : stepList) {
			traverser.visit(step);
		}
		return traverser.getResultSet();
	}

	@Override
	public void visit(SPARQLBuilder builder) {
		
		for (Step s : stepList) {
			s.visit(builder);
		}
		
	}
	
	@Override
	public String toString() {
		try {

			StringWriter buffer = new StringWriter();
			JsonFactory factory = new JsonFactory();
			JsonGenerator json = factory.createGenerator(buffer);
			json.setPrettyPrinter(KonigJsonPrettyPrinter.INSTANCE);
			
			
			if (context != null && !context.asList().isEmpty()) {
				buffer.append("@context ");
				context.toJson(json);
				json.flush();
				buffer.append("\n");
			}
			
			for (Step step : stepList) {
				buffer.append(step.toString(context));
			}
			return buffer.toString();
			
		} catch (IOException e) {
			throw new KonigException(e);
		}
	}

	@Override
	public Path subpath(int start, int end) {
		PathImpl result = new PathImpl();
		for (int i=start; i<end; i++) {
			result.add(stepList.get(i));
		}
		return result;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof Path) {
			Path p = (Path) other;
			List<Step> otherList = p.asList();
			
			result = otherList.size() == stepList.size();
			if (result) {
				for (int i=0; i<stepList.size(); i++) {
					Step a = stepList.get(i);
					Step b = otherList.get(i);
					result = a.equals(b);
					if (!result) {
						break;
					}
				}
			}
		}
		
		return result;
	}

	@Override
	public int length() {
		return stepList.size();
	}

	@Override
	public Path subpath(int start) {
		return subpath(start, stepList.size());
	}

	@Override
	public Value toValue() {
		String text = toString();
		return new LiteralImpl(text, XMLSchema.STRING);
	}

	@Override
	public Step remove(int index) {
		return stepList.remove(index);
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void setContext(Context context) {
		this.context = context;
	}


}