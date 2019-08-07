package io.konig.transform.beam;

import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlUniqueKey;
import io.konig.core.showl.UniqueKeyElement;

abstract public class BeamUniqueKeyGenerator {

	protected BeamExpressionTransform etran;
	protected ShowlUniqueKey uniqueKey;
	
	public BeamUniqueKeyGenerator(BeamExpressionTransform etran, ShowlUniqueKey uniqueKey) {
		this.etran = etran;
		this.uniqueKey = uniqueKey;
	}
	
	protected void createValues(ShowlStructExpression struct) throws BeamTransformGenerationException {
		BlockInfo blockInfo = etran.peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		IJExpression condition = null;
		
		for (UniqueKeyElement element : uniqueKey) {
			
			ShowlPropertyShape p = element.getPropertyShape();
			ShowlExpression e = struct.get(p.getPredicate());
			
			if (e == null) {
				throw new BeamTransformGenerationException("Property not found in struct: " + p.getPath());
			}
			
			String varName = blockInfo.varName(p.getPredicate().getLocalName());
			RdfJavaType varType = etran.getTypeManager().rdfJavaType(p);
			
			IJExpression init = etran.transform(e);
			
			JVar var = block.decl(varType.getJavaType(),varName).init(init);
			blockInfo.putPropertyValue(p.asGroup(),  var);
			
			if (condition == null) {
				condition = var.eqNull();
			} else {
				condition = condition.cor(var.eqNull());
			}
			
		}
		
		block._if(condition)._then()._return(JExpr._null());
	}

	abstract public JVar createKeyVar(ShowlStructExpression member) throws BeamTransformGenerationException;
	

}
