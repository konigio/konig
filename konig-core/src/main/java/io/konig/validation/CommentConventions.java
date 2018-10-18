package io.konig.validation;

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


public class CommentConventions {

	private boolean requireClassComments;
	private boolean requirePropertyComments;
	private boolean requireNamedIndividualComments;
	private boolean requireNodeShapeComments;
	private boolean requirePropertyShapeComments;
	
	public boolean getRequireClassComments() {
		return requireClassComments;
	}
	public void setRequireClassComments(boolean requireClassComments) {
		this.requireClassComments = requireClassComments;
	}
	public boolean getRequirePropertyComments() {
		return requirePropertyComments;
	}
	public void setRequirePropertyComments(boolean requirePropertyComments) {
		this.requirePropertyComments = requirePropertyComments;
	}
	public boolean getRequireNamedIndividualComments() {
		return requireNamedIndividualComments;
	}
	public void setRequireNamedIndividualComments(boolean requireNamedIndividualComments) {
		this.requireNamedIndividualComments = requireNamedIndividualComments;
	}
	public boolean getRequireNodeShapeComments() {
		return requireNodeShapeComments;
	}
	public void setRequireNodeShapeComments(boolean requireNodeShapeComments) {
		this.requireNodeShapeComments = requireNodeShapeComments;
	}
	public boolean getRequirePropertyShapeComments() {
		return requirePropertyShapeComments;
	}
	public void setRequirePropertyShapeComments(boolean requirePropertyShapeComments) {
		this.requirePropertyShapeComments = requirePropertyShapeComments;
	}
	
	
}
