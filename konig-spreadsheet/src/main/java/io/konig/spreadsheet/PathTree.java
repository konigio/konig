package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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
