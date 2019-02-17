package io.konig.spreadsheet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.konig.cadl.Level;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;

public class PathTree {
	private PathStep step;
	private Level level;
	private List<PathTree> children;
	
	public PathTree() {
		
	}

	public PathTree(PathStep step) {
		this.step = step;
	}
	
	public PathTree append(Level level) {
		QuantifiedExpression formula = level.getFormula();
		if (formula != null) {
			PrimaryExpression primary = formula.asPrimaryExpression();
			if (primary instanceof PathExpression) {
				PathExpression path = (PathExpression) primary;
				PathTree node = this;
				List<PathStep> stepList = path.getStepList();
				
				for (PathStep step : stepList) {
					PathTree next = node.produceChild(step);
				}
			}
		}
		return null;
	}
	
	private PathTree produceChild(PathStep childStep) {
		if (children == null) {
			children = new ArrayList<>();
			
		} else {
			for (PathTree child : children) {
				if (child.step.equals(childStep)) {
					return child;
				}
			}
		}
		
		PathTree result = new PathTree(childStep);
		children.add(result);
		return result;
	}



	public Level getLevel() {
		return level;
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public PathStep getStep() {
		return step;
	}
	
	public void add(PathTree child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	public List<PathTree> getChildren() {
		return children==null ? Collections.emptyList() : children;
	}
	
	

}
