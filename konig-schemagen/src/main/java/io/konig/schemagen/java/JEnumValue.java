package io.konig.schemagen.java;

import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;

public class JEnumValue extends JExpressionImpl {

	private String name;
	public JEnumValue(String name) {
		this.name = name;
	}

	@Override
	public void generate(JFormatter f) {
		f.p(name);
	}

}
