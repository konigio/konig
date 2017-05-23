package io.konig.transform.factory;

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
			if (p.isDirectProperty()) {
				TargetShape nested = p.getNestedShape();
				if (nested != null) {
					count += nested.totalPropertyCount();
				} else {
					count++;
				}
			}
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
			if (p.isDirectProperty()) {
				TargetShape nested = p.getNestedShape();
				if (nested != null) {
					count += nested.mappedPropertyCount();
				} else if (p.getPreferredMatch()!=null){
					count++;
				}
			}
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


	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		// TODO Auto-generated method stub
		
	}

}
