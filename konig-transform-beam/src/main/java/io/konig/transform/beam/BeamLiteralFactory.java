package io.konig.transform.beam;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JExpr;

public class BeamLiteralFactory {
	
	JCodeModel model;
	

	public BeamLiteralFactory(JCodeModel model) {
		this.model = model;
	}


	public IJExpression javaExpression(Literal literal) throws BeamTransformGenerationException {
		URI datatype = literal.getDatatype();
		if (datatype == null || XMLSchema.STRING.equals(datatype)) {

      return JExpr.lit(literal.stringValue());
		}
		
		if (
				XMLSchema.LONG.equals(datatype) ||
				XMLSchema.INTEGER.equals(datatype) ||
				XMLSchema.INT.equals(datatype) ||
				XMLSchema.NEGATIVE_INTEGER.equals(datatype) ||
				XMLSchema.NON_NEGATIVE_INTEGER.equals(datatype) ||
				XMLSchema.NON_POSITIVE_INTEGER.equals(datatype) ||
				XMLSchema.POSITIVE_INTEGER.equals(datatype) ||
				XMLSchema.UNSIGNED_INT.equals(datatype)
		) {
			long longValue = Long.parseLong(literal.stringValue());
			return JExpr.lit(longValue);
		}
		
		if (XMLSchema.BOOLEAN.equals(datatype)) {
			if ("TRUE".equalsIgnoreCase(literal.stringValue())) {
				return JExpr.TRUE;
			} else {
				return JExpr.FALSE;
			}
		}
		
		throw new BeamTransformGenerationException("Literal not supported: " + literal.toString());
	}

}
