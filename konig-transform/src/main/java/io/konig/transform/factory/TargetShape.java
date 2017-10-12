package io.konig.transform.factory;

/*
 * #%L
 * Konig Transform
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


import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;
import io.konig.transform.rule.IdRule;

public class TargetShape extends ShapeNode<TargetProperty> {
	
	public static enum State {
		INITIALIZED,
		FIRST_PASS,
		OK,
		FAILED
	}
	
	public static TargetShape create(Shape shape) {
		return TargetShapeFactory.INSTANCE.createShapeNode(shape);
	}
	
	private List<SourceShape> sourceList = new ArrayList<>();
	private State state = State.INITIALIZED;
	private IdRule idRule;
	
	private List<VariableTargetProperty> variableList = null;

	public TargetShape(Shape shape) {
		super(shape);
	}
	

	public State getState() {
		return state;
	}


	public void setState(State state) {
		this.state = state;
	}


	public List<SourceShape> getSourceList() {
		return sourceList;
	}
	
	/**
	 * Commit to using a given SourceShape.
	 */
	public void commit(SourceShape s) {
		sourceList.add(s);
		s.commit();
	}
	
	/**
	 * Count the total number of direct, leaf level properties contained within this
	 * TargetShape.
	 * @return
	 */
	public int totalPropertyCount() {
		int count = 0;
		
		for (TargetProperty p : getProperties()) {
			count += p.totalPropertyCount();
		}
		
		return count;
	}
	
	
	/**
	 * Count the number of direct, leaf level properties that have a preferred match.
	 * @return
	 */
	public int mappedPropertyCount() {
		int count = 0;

		for (TargetProperty p : getProperties()) {
			count += p.mappedPropertyCount();
		}
		return count;
	}
	
	public List<TargetProperty> getUnmappedProperties() {
		List<TargetProperty> list = new ArrayList<>();
		addUnmappedProperties(list);
		return list;
	}
	
	private void addUnmappedProperties(List<TargetProperty> list) {
		for (TargetProperty tp : getProperties()) {
			if (tp.isDirectProperty()) {
				if (tp.isLeaf() && tp.getPreferredMatch()==null) {
					list.add(tp);
				} else {
					TargetShape nested = tp.getNestedShape();
					if (nested != null) {
						nested.addUnmappedProperties(list);
					}
				}
			}
		}
	}

	
	public TargetProperty getUnmappedProperty() {
		boolean ok = true;
		for (TargetProperty tp : getProperties()) {
			if (tp.isDirectProperty()) {
				if (tp.isLeaf() && tp.getPreferredMatch()==null) {
					return tp;
				} else {
					
					if (tp.getPreferredMatch()==null) {
						return tp;
					}
					
					TargetShape nested = tp.getNestedShape();
					if (nested != null) {
						
						switch (nested.getState()) {
						
						case FAILED:
							ok = false;
							state = State.FAILED;
							break;
							
						case OK :
							break;
							
						default:

							TargetProperty result = nested.getUnmappedProperty();
							if (result != null) {
								return result;
							}
						
						}
					}
				}
			}
		}
		if (ok) {
			state = State.OK;
		}
		return null;
	}
	
	
	
	public void match(SourceShape source) {
		for (TargetProperty tp : getProperties()) {
			SourceProperty sp = source.getProperty(tp.getPredicate());
			if (sp != null) {
				tp.addMatch(sp);

				TargetShape targetNested = tp.getNestedShape();
				if (targetNested != null) {
					SourceShape sourceNested = sp.getNestedShape();
					if (sourceNested != null) {
						targetNested.match(sourceNested);
					}
				}
			}
			
		}
	}
	
	

	public IdRule getIdRule() {
		return idRule;
	}


	public void setIdRule(IdRule idRule) {
		this.idRule = idRule;
	}
	
	public VariableTargetProperty getVariableByName(String name) {
		if (variableList != null) {
			for (VariableTargetProperty vtp : variableList) {
				if (vtp.getPropertyConstraint().getPredicate().getLocalName().equals(name)) {
					return vtp;
				}
			}
		}
		return null;
	}

	public void addVariable(VariableTargetProperty tp) {
		if (variableList == null) {
			variableList = new ArrayList<>();
		}
		variableList.add(tp);
	}
	
	public List<VariableTargetProperty> getVariableList() {
		return variableList;
	}


	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		if (variableList != null) {
			out.beginArray("variableList");
			for (TargetProperty tp : variableList) {
				out.print(tp);
			}
			out.endArray("variableList");
		}
		
	}

}
