package io.konig.transform.beam;

import java.text.MessageFormat;

import org.openrdf.model.Literal;
import org.openrdf.model.vocabulary.XMLSchema;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JExpr;

import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.expression.ShowlLiteralExpression;

public class BeamExpressionTransformImpl implements BeamExpressionTransform {

	private BeamPropertyManager manager;
	private JCodeModel model;
	
	public BeamExpressionTransformImpl(BeamPropertyManager manager, JCodeModel model) {
		this.manager = manager;
		this.model = model;
	}

	@Override
	public IJExpression transform(ShowlExpression e) throws BeamTransformGenerationException {
		
		if (e instanceof ShowlLiteralExpression) {

      Literal literal = ((ShowlLiteralExpression) e).getLiteral();
      if (literal.getDatatype().equals(XMLSchema.STRING)) {
        return JExpr.lit(literal.stringValue());
      } else {
        fail("Typed literal not supported in expression: {0}", e.toString());
      }
		} 
		
		if (e instanceof ShowlPropertyExpression) {
			ShowlPropertyExpression p = (ShowlPropertyExpression) e;
			BeamSourceProperty b = manager.forPropertyShape(p.getSourceProperty());
			return b.getVar();
		}
		
		if (e instanceof ShowlStructExpression) {
			return struct((ShowlStructExpression)e);
		}
		
		throw new BeamTransformGenerationException("Failed to tranform " + e.toString());
	}

	private IJExpression struct(ShowlStructExpression e) {
		// TODO Auto-generated method stub
		return null;
	}

	private void fail(String pattern, Object... arg) throws BeamTransformGenerationException {
		String msg = MessageFormat.format(pattern, arg);
		throw new BeamTransformGenerationException(msg);
		
	}

}
