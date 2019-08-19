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


/**
 * A structure holding information about a source NodeShape and optionally a statement 
 * for joining the specified source node to some other source node.
 * @author Greg McFall
 *
 */
public class ShowlChannel {
	
	private ShowlNodeShape sourceShape;
	private ShowlStatement joinStatement;
	
	public ShowlChannel(ShowlNodeShape sourceShape, ShowlStatement joinStatement) {
		this.sourceShape = sourceShape;
		this.joinStatement = joinStatement;
	}

	public ShowlNodeShape getSourceNode() {
		return sourceShape;
	}

	public void setJoinStatement(ShowlStatement joinStatement) {
		this.joinStatement = joinStatement;
	}

	public ShowlStatement getJoinStatement() {
		return joinStatement;
	}
	
	public String toString() {
		return "ShowlChannel(" + sourceShape.getPath() + ", " + 
				(joinStatement==null ? "null" : joinStatement.toString() + ")");
	}

	

}
