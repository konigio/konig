package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
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


import java.util.Iterator;
import java.util.Map;

import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;
import com.helger.jcodemodel.JWhileLoop;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class MergeTargetFnGenerator extends BaseTargetFnGenerator {
	
	private Map<ShowlEffectiveNodeShape, IJExpression> tupleTagMap;
	
	private JVar keyValueVar = null;
	private JMethod defaultSourceRowMethod = null;
	private JMethod latestSourceRowMethod = null;

	public MergeTargetFnGenerator(Map<ShowlEffectiveNodeShape, IJExpression> tupleTagMap, String basePackage, NamespaceManager nsManager, JCodeModel model, OwlReasoner reasoner,
			BeamTypeManager typeManager) {
		super(basePackage, nsManager, model, reasoner, typeManager);
		this.tupleTagMap = tupleTagMap;
	}


	@Override
	protected void declareTableRow(JDefinedClass thisClass, BeamExpressionTransform etran, ShowlEffectiveNodeShape node, JVar c) throws BeamTransformGenerationException {

		ShowlNodeShape sourceNode = node.canonicalNode();
		if (sourceNode.isTargetNode()) {
			return;
		}
		
		if (sourceNode.getAccessor()!=null) {
			return;
		}
		
		BlockInfo blockInfo = etran.peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		if (keyValueVar == null) {
			// For now, we assume that keys are always strings.
			// We'll add support for other types of keys in the future.
			
			// KV<String, CoGbkResult> e
			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
			AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
			
			keyValueVar = block.decl(kvClass, "e").init(c.invoke("element"));
		}
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		JMethod sourceRowMethod = sourceRowMethod(thisClass, sourceNode);

		IJExpression tupleTagField = tupleTagMap.get(node);
		if (tupleTagField == null) {
			
//			tupleTagField = JExpr.lit("FixMe!");
			
			
			fail("TupleTag not found for {0}", node.canonicalNode().getPath());
		}
		String rowName = StringUtil.javaIdentifier(StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(node.canonicalNode()))) + "Row";
		
		JVar rowVar = block.decl(tableRowClass, rowName, JExpr.invoke(sourceRowMethod).arg(keyValueVar).arg(tupleTagField));
		blockInfo.putTableRow(node, rowVar);
		
		
	}


	private JMethod sourceRowMethod(JDefinedClass thisClass, ShowlNodeShape sourceNode) {
		if (hasModifiedProperty(sourceNode)) {
			return latestSourceRowMethod(thisClass);
		}
		return defaultSourceRowMethod(thisClass);
	}


	private JMethod latestSourceRowMethod(JDefinedClass thisClass) {
		if (latestSourceRowMethod == null) {

			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
			AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(tableRowClass);
			AbstractJClass iteratorClass = model.ref(Iterator.class).narrow(tableRowClass);
			
			latestSourceRowMethod = thisClass.method(JMod.PRIVATE, tableRowClass, "latestSourceRow");
			
			JVar e = latestSourceRowMethod.param(kvClass, "e");
			JVar tag = latestSourceRowMethod.param(tupleTagClass, "tag");
			JBlock body = latestSourceRowMethod.body();
			
			
			JVar latest = body.decl(stringClass, "latest").init(JExpr._null());
			JVar result = body.decl(tableRowClass, "result").init(JExpr._null());
			
			JVar sequence = body.decl(iteratorClass, "sequence").init(e.invoke("getValue").invoke("getAll").arg(tag).invoke("iterator"));
			JWhileLoop loop = body._while(sequence.invoke("hasNext"));
			JBlock block = loop.body();
			
			JVar row = block.decl(tableRowClass, "row").init(sequence.invoke("next"));
			JVar modified = block.decl(stringClass, "modified").init(row.invoke("get").arg("modified").invoke("toString"));
			
			JConditional ifStatement = block._if(latest.eqNull().cor((modified.invoke("compareTo").arg(latest).lt(0))));
			ifStatement._then().assign(latest, modified);
			ifStatement._then().assign(result, row);
			
			
			
			body._return(result);
		}
		return latestSourceRowMethod;
	}


	private boolean hasModifiedProperty(ShowlNodeShape sourceNode) {
		return sourceNode.getProperty(Konig.modified)!=null;
	}


	private JMethod defaultSourceRowMethod(JDefinedClass thisClass) {
		if (defaultSourceRowMethod == null) {

			AbstractJClass stringClass = model.ref(String.class);
			AbstractJClass coGbkResultClass = model.ref(CoGbkResult.class);
			AbstractJClass kvClass = model.ref(KV.class).narrow(stringClass).narrow(coGbkResultClass);
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			AbstractJClass tupleTagClass = model.ref(TupleTag.class).narrow(tableRowClass);
			AbstractJClass iteratorClass = model.ref(Iterator.class).narrow(tableRowClass);
			
			defaultSourceRowMethod = thisClass.method(JMod.PRIVATE, tableRowClass, "sourceRow");
			
			JVar e = defaultSourceRowMethod.param(kvClass, "e");
			JVar tag = defaultSourceRowMethod.param(tupleTagClass, "tag");
			JBlock body = defaultSourceRowMethod.body();
			
			JVar sequence = body.decl(iteratorClass, "sequence").init(e.invoke("getValue").invoke("getAll").arg(tag).invoke("iterator"));
			body._return(JExpr.cond(sequence.invoke("hasNext"), sequence.invoke("next"), JExpr._null()));
			
			
			
		}
		return defaultSourceRowMethod;
	}
}
