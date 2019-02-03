package io.konig.core.showl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Konig Core
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


public class ShowlJoinCondition {
	
	private static Logger logger = LoggerFactory.getLogger(ShowlJoinCondition.class);

	private ShowlPropertyShape left;
	private ShowlPropertyShape right;
	
	private ShowlJoinCondition previous;

	public ShowlJoinCondition(ShowlPropertyShape left, ShowlPropertyShape right, ShowlJoinCondition previous) {
		this.left = left;
		this.right = right;
		this.previous = previous;
		
		if (logger.isTraceEnabled() && left!=null &&right!=null) {
			logger.trace("new JoinCondition: {} ... {}", left.getPath(), right.getPath());
		}

		if (left != null) {
			left.addJoinCondition(this);
		}
		if (right != null) {
			right.addJoinCondition(this);
		}
		
	}

	
	public ShowlPropertyShape getLeft() {
		return left;
	}

	public ShowlPropertyShape getRight() {
		return right;
	}

	public ShowlJoinCondition getPrevious() {
		return previous;
	}
	
	public ShowlPropertyShape propertyOf(ShowlNodeShape node) {
		return
			node==left.getDeclaringShape() ? left :
			node==right.getDeclaringShape() ? right :
			null;
	}
	
	public ShowlNodeShape otherNode(ShowlNodeShape n) {
		return
			left!=null && n==left.getDeclaringShape() ? right.getDeclaringShape() :
			right!=null && n==right.getDeclaringShape() ? left.getDeclaringShape() :
			null;
	}

	public ShowlPropertyShape otherProperty(ShowlPropertyShape p) {
		
		return 
			p==left ? right :
			p==right ? left : null;
	}
	
	/**
	 * Get the node that is the focus of this join (i.e. the new node being added to the select statement).
	 */
	public ShowlNodeShape focusNode() {
		return right.getDeclaringShape();
	}


	/**
	 * Get an alias for the the focus node as assigned by the given namer.
	 */
	public String focusAlias(NodeNamer namer) {
		return namer.varname(focusNode());
	}
	
	
	
}
