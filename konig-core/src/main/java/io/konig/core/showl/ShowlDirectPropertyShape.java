package io.konig.core.showl;

import java.util.Collections;

import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;

public class ShowlDirectPropertyShape extends ShowlPropertyShape {

	public ShowlDirectPropertyShape(ShowlNodeShape declaringShape, ShowlProperty property,
			PropertyConstraint propertyConstraint) {
		super(declaringShape, property, propertyConstraint);
		
		property.addPropertyShape(this);
	}
	
	public boolean isObjectProperty() {
		if (getPredicate().equals(Konig.id)) {
			return true;
		}
		PropertyConstraint p = getPropertyConstraint();
		return p.getNodeKind().equals(NodeKind.IRI) || p.getShape()!=null;
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
