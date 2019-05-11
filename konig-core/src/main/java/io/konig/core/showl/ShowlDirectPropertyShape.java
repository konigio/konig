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

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class ShowlDirectPropertyShape extends ShowlPropertyShape {
	
	private static Logger logger = LoggerFactory.getLogger(ShowlDirectPropertyShape.class);

	
	public ShowlDirectPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property,
			PropertyConstraint propertyConstraint) {
		super(declaringShape, property, propertyConstraint);
	}
	
	public boolean isObjectProperty() {
		if (getPredicate().equals(Konig.id)) {
			return true;
		}
		PropertyConstraint p = getPropertyConstraint();
		return p.getNodeKind().equals(NodeKind.IRI) || p.getShape()!=null;
	}
	
	void setProperty(ShowlProperty p) {
		property = p;
	}
	
	public boolean isIriReference() {

		if (getPredicate().equals(Konig.id)) {
			return true;
		}
		PropertyConstraint p = getPropertyConstraint();
		return p.getNodeKind().equals(NodeKind.IRI) && p.getShape()!=null;
	}

	/**
	 * This property has a formula that links to a property on some other object.
	 */
	public boolean isShortcutProperty() {
		ShowlPropertyShape peer = getPeer();
		if (peer != null) {
			ShowlNodeShape parent = getDeclaringShape();
			int count = 0;
			ShowlNodeShape peerParent = peer.getDeclaringShape();
			while (peerParent.getAccessor() != null && peerParent != parent) {
				count++;
				if (count > 1) {
					return true;
				}
				peerParent = peerParent.getAccessor().getDeclaringShape();
			}
		}
		return false;
	}

	/**
	 * Compute a path from this property's declaring shape to the peer.
	 * @return
	 */
	public ShowlPropertyPath getPeerPath() {
		ShowlPropertyPath path = null;
		ShowlPropertyShape step = getPeer();
		if (step != null) {
			path = new ShowlPropertyPath();
			ShowlNodeShape parent = getDeclaringShape();
			while (step != null) {
				path.add(step);
				ShowlNodeShape stepParent = step.getDeclaringShape();
				step = stepParent==null || stepParent==parent ? null :
					stepParent.getAccessor();
			}
			Collections.reverse(path);
		}
		return path;
	}

}
