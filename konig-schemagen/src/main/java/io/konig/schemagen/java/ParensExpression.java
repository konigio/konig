package io.konig.schemagen.java;

import com.sun.codemodel.JExpression;
import com.sun.codemodel.JExpressionImpl;
import com.sun.codemodel.JFormatter;

public class ParensExpression extends JExpressionImpl {

	 private JExpression expression;

	    public ParensExpression(JExpression expression) {
	        this.expression = expression;
	    }

	    @Override
	    public void generate(JFormatter formatter) {
	        formatter.p('(').g(expression).p(')');
	    }
	

}
