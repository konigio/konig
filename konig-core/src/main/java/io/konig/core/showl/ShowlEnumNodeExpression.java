package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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


import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

public class ShowlEnumNodeExpression implements ShowlExpression, HasEnumNode {
	
	private ShowlNodeShape enumNode;
	private ShowlChannel channel;
	private ShowlStatement joinStatement;
	
	public ShowlEnumNodeExpression(ShowlNodeShape enumNode) {
		this.enumNode = enumNode;
	}
	
	
	public ShowlEnumNodeExpression(ShowlNodeShape enumNode, ShowlChannel channel) {
		this.enumNode = enumNode;
		this.channel = channel;
		this.joinStatement = channel.getJoinStatement();
	}


	@Override
	public ShowlNodeShape getEnumNode() {
		return enumNode;
	}



	@Override
	public String displayValue() {
		return enumNode.toString();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {

		ShowlEqualStatement equal = equalStatement();
		if (equal != null) {
			equal.getLeft().addDeclaredProperties(sourceNodeShape, set);
			equal.getRight().addDeclaredProperties(sourceNodeShape, set);
		}
		
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		ShowlEqualStatement equal = equalStatement();
		if (equal != null) {
			equal.getLeft().addProperties(set);
			equal.getRight().addProperties(set);
		}
	}
	
	private ShowlEqualStatement equalStatement() {
		if (channel != null) {
			ShowlStatement statement = channel.getJoinStatement();
			if (statement instanceof ShowlEqualStatement) {
				return (ShowlEqualStatement) statement;
			}
		}
		return null;
	}


	public String toString() {
		return displayValue();
	}



	@Deprecated
	public ShowlChannel getChannel() {
		return channel;
	}


	@Deprecated
	public void setChannel(ShowlChannel channel) {
		this.channel = channel;
	}


	@Override
	public URI valueType(OwlReasoner reasoner) {
		
		return enumNode.getOwlClass().getId();
	}


	@Override
	public ShowlEnumNodeExpression transform() {
		return this;
	}


	public ShowlStatement getJoinStatement() {
		return joinStatement;
	}


	public void setJoinStatement(ShowlStatement joinStatement) {
		this.joinStatement = joinStatement;
	}

}
