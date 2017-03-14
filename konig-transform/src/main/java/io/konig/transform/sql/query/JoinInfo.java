package io.konig.transform.sql.query;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.Shape;
import io.konig.transform.MappedProperty;
import io.konig.transform.ShapePath;


public class JoinInfo extends AbstractPrettyPrintable {

	private JoinElement left;
	private JoinElement right;
	
	public JoinInfo(JoinElement left, JoinElement right) {
		this.left = left;
		this.right = right;
	}

	public JoinElement getLeft() {
		return left;
	}

	public JoinElement getRight() {
		return right;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		out.beginObject(this);
		out.field("left", left);
		out.field("right", right);
		out.endObject();
		
	}
	
	
	
}
