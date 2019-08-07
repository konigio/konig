package io.konig.transform.beam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlStructExpression;
import io.konig.core.showl.ShowlUniqueKey;
import io.konig.core.showl.ShowlUniqueKeyCollection;
import io.konig.core.showl.UniqueKeyFactory;

/**
 * A utility for computing the transform for a property whose value consists of multiple
 * BNode resources from multiple data sources.
 * @author Greg McFall
 *
 */
public class BNodeArrayTransform {

	private BeamExpressionTransform etran;

	public BNodeArrayTransform(BeamExpressionTransform etran) {
		this.etran = etran;
	}
	
	public JVar transform(ShowlArrayExpression array) throws BeamTransformGenerationException {
		
		
		
		JCodeModel model = etran.codeModel();
		BlockInfo blockInfo = etran.peekBlockInfo();
		BeamMethod beamMethod = blockInfo.getBeamMethod();
		ShowlPropertyShape targetProperty = beamMethod==null ? null : beamMethod.getTargetProperty();
		
		if (targetProperty == null) {
			throw new BeamTransformGenerationException("Cannot transform array because targetProperty is not defined");
		}
		ShowlNodeShape targetValueNode = targetProperty.getValueShape();
		if (targetValueNode == null) {
			throw new BeamTransformGenerationException("Expected value shape at " + targetProperty.getPath());
		}
		UniqueKeyFactory keyFactory = new UniqueKeyFactory(etran.getOwlReasoner());
		ShowlUniqueKeyCollection keyCollection = keyFactory.createKeyCollection(targetValueNode);
		
		if (keyCollection.isEmpty()) {
			throw new BeamTransformGenerationException("No key found for " + targetValueNode.getPath());
		}
		
		if (keyCollection.size()>1) {
			throw new BeamTransformGenerationException("Cannot handle multiple keys for " + targetValueNode.getPath());
		}
		
		ShowlUniqueKey uniqueKey = keyCollection.get(0);
		
		
		
		
		JBlock block = blockInfo.getBlock();
		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass mapClass = model.ref(Map.class).narrow(objectClass).narrow(tableRowClass);
		AbstractJClass hashMapClass = model.ref(HashMap.class);
		AbstractJClass arrayListClass = model.ref(ArrayList.class).narrow(tableRowClass);

		// ArrayList<TableRow> list = new ArrayList<>();
		// Map<Object, TableRow> map = new HashMap<>();
		
//		JVar list = block.decl(arrayListClass, blockInfo.varName("list")).init(arrayListClass._new());
		JVar map = block.decl(mapClass, blockInfo.varName("map")).init(hashMapClass._new());
		
		int index = 0;
		String baseMemberName = beamMethod.getMethod().name();
		for (ShowlExpression member  : array.getMemberList()) {
			if (member instanceof ShowlStructExpression) {
				String memberMethodName = baseMemberName + (index++);
				BeamMethod memberMethod = memberMethod(map, uniqueKey, memberMethodName, (ShowlStructExpression) member);
			}
		}
		
		
		// TODO: don't return map!
		return map;
	}

	private BeamMethod memberMethod(JVar callerMap, ShowlUniqueKey uniqueKey, String memberMethodName, ShowlStructExpression member) throws BeamTransformGenerationException {

		JCodeModel model = etran.codeModel();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		BlockInfo parentBlock = etran.peekBlockInfo();
		BeamMethod parentMethod = parentBlock.getBeamMethod();
		ShowlPropertyShape targetProperty = parentMethod.getTargetProperty();
		RdfJavaType returnType = etran.getTypeManager().rdfJavaType(member.getPropertyShape());
		
		JDefinedClass targetClass = etran.getTargetClass();
		JMethod method = targetClass.method(JMod.PRIVATE, returnType.getJavaType(), memberMethodName);
		
		
		BeamMethod beamMethod = new BeamMethod(method);
		beamMethod.setReturnType(returnType);
		beamMethod.setTargetProperty(targetProperty);
		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {		
			JVar map = beamMethod.addParameter(BeamParameter.ofMappedValue(callerMap.type(), "map")).getVar();
			parentBlock.putMappedVar(map, callerMap);
			
			JBlock block = blockInfo.getBlock();
			BeamUniqueKeyGenerator keyGenerator = keyGenerator(uniqueKey);
			
			keyGenerator.createKeyVar(member);
			
			JVar row = block.decl(
					tableRowClass, 
					blockInfo.varName(targetProperty.getPredicate().getLocalName() + "Row"))
					.init(tableRowClass._new());
			
			blockInfo.putPropertyValue(targetProperty.asGroup(), row);
			
			if (!(targetProperty instanceof ShowlDirectPropertyShape)) {
				throw new BeamTransformGenerationException("Expected direct property shape: " + targetProperty.getPath());
			}
			etran.processStructPropertyList((ShowlDirectPropertyShape)targetProperty, member);
			
		
			block._return(row);
			
		} finally {
			etran.endBlock();
		}
		
		return beamMethod;
	}

	private BeamUniqueKeyGenerator keyGenerator(ShowlUniqueKey uniqueKey) {
		
		return uniqueKey.size()==1 ? new BeamSingleKeyGenerator(etran, uniqueKey) : new BeamCompositeKeyGenerator(etran, uniqueKey);
	}


}
