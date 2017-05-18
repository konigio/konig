package io.konig.transform.factory;

import java.util.ArrayList;
import java.util.List;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;

public class TargetShape extends ShapeNode<TargetProperty> {
	
	public static TargetShape create(Shape shape) {
		return TargetShapeFactory.INSTANCE.createShapeNode(shape);
	}
	
	private List<SourceShape> sourceList = new ArrayList<>();

	public TargetShape(Shape shape) {
		super(shape);
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

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		// TODO Auto-generated method stub
		
	}

}
