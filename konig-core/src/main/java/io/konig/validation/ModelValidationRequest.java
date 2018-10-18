package io.konig.validation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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


import io.konig.core.OwlReasoner;
import io.konig.shacl.ShapeManager;

/**
 * @author Greg McFall
 *
 */
public class ModelValidationRequest {
	
	private ShapeManager shapeManager;
	private OwlReasoner owl;
	private CaseStyleConventions caseStyle = new CaseStyleConventions();
	private CommentConventions commentConventions;
	private Set<String> tabularPropertyNamespaces;
	
	public ModelValidationRequest(OwlReasoner owl, ShapeManager shapeManager) {
		this.owl = owl;
		this.shapeManager = shapeManager;
	}

	public OwlReasoner getOwl() {
		return owl;
	}
	
	public void addTabularPropertyNamespace(String namespace) {
		if (tabularPropertyNamespaces == null) {
			tabularPropertyNamespaces = new HashSet<>();
		}
		tabularPropertyNamespaces.add(namespace);
	}
	
	

	public Set<String> getTabularPropertyNamespaces() {
		return tabularPropertyNamespaces==null ? Collections.emptySet() : tabularPropertyNamespaces;
	}

	public CaseStyleConventions getCaseStyle() {
		return caseStyle;
	}

	public void setCaseStyle(CaseStyleConventions caseStyle) {
		this.caseStyle = caseStyle;
	}

	public ShapeManager getShapeManager() {
		return shapeManager;
	}

	public CommentConventions getCommentConventions() {
		return commentConventions;
	}

	public void setCommentConventions(CommentConventions commentConventions) {
		this.commentConventions = commentConventions;
	}
	
	

}
