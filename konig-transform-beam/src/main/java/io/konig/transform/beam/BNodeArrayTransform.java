package io.konig.transform.beam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
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
		System.out.println(uniqueKey);
		
		
		JBlock block = blockInfo.getBlock();
		AbstractJClass objectClass = model.ref(Object.class);
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass mapClass = model.ref(Map.class).narrow(objectClass).narrow(tableRowClass);
		AbstractJClass hashMapClass = model.ref(HashMap.class);
		AbstractJClass arrayListClass = model.ref(ArrayList.class).narrow(tableRowClass);

		// ArrayList<TableRow> list = new ArrayList<>();
		// Map<Object, TableRow> map = new HashMap<>();
		
		JVar list = block.decl(arrayListClass, blockInfo.varName("list")).init(arrayListClass._new());
		JVar map = block.decl(mapClass, blockInfo.varName("map")).init(hashMapClass._new());
		
		
		
		return list;
	}

}
