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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlAlternativePathsExpression;
import io.konig.core.showl.ShowlArrayExpression;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlOverlayExpression;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUtil;

public class StructPropertyGenerator extends TargetPropertyGenerator {
	

	public StructPropertyGenerator(BeamExpressionTransform etran) {
		super(etran);
	}
	
	
	/**
	 * Generate and invoke methods for all properties within a given node.
	 * @param beamMethod  The method responsible for constructing the target node.
	 * @param targetNode
	 * @return
	 * @throws BeamTransformGenerationException
	 */
	public StructInfo processNode(BeamMethod beamMethod, ShowlNodeShape targetNode) throws BeamTransformGenerationException {
		
		Set<ShowlEffectiveNodeShape> set = new HashSet<>();
		
		List<BeamMethod> methodList = new ArrayList<>();
		
		Collection<ShowlDirectPropertyShape> list = etran.sortProperties(targetNode);
		
		for (ShowlDirectPropertyShape p : list) {
				
			TargetPropertyGenerator propertyGenerator = propertyGenerator(p);
			BeamMethod propertyMethod = propertyGenerator.generate(beamMethod, p);
			methodList.add(propertyMethod);
			
			for (BeamParameter param : propertyMethod.getParameters()) {
				ShowlEffectiveNodeShape n = param.getNode();
				if (n != null) {
					set.add(n);
				}
			}
		}
		
		List<ShowlEffectiveNodeShape> nodeList = new ArrayList<>(set);
		Collections.sort(nodeList);
	
		
		return new StructInfo(nodeList, methodList);
	}
	

	private TargetPropertyGenerator propertyGenerator(ShowlDirectPropertyShape p) throws BeamTransformGenerationException {
		return TargetPropertyGenerator.create(etran, p);
	}

	@Override
	protected void generateBody(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {

		JVar valueShapeRow = declareValueShapeRow(beamMethod, targetProperty);
		StructInfo structInfo = processNode(beamMethod, targetProperty.getValueShape());

		for (BeamMethod propertyMethod : structInfo.getMethodList()) {
			etran.invoke(propertyMethod);
		}
	
		
		captureValue(targetProperty, valueShapeRow);
		etran.peekBlockInfo().getBlock()._return(valueShapeRow);

	}

	protected JVar declareValueShapeRow(BeamMethod beamMethod, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {
		AbstractJClass tableRowClass = etran.codeModel().ref(TableRow.class);
		String varName = targetProperty.getPredicate().getLocalName() + "Row";
		JVar var = beamMethod.getMethod().body().decl(tableRowClass, varName).init(tableRowClass._new());
		etran.peekBlockInfo().putTableRow(targetProperty.getValueShape().effectiveNode(), var);
		return var;
	}


	@Override
	protected void addParameters(BeamMethod beamMethod, ShowlPropertyShape targetProperty)
			throws BeamTransformGenerationException {
	
		BeamParameter exclude = BeamParameter.pattern(BeamParameterType.TABLE_ROW, targetProperty.getValueShape().effectiveNode());
		beamMethod.excludeParamFor(exclude);
		etran.addOutputRowAndErrorBuilderParams(beamMethod, targetProperty);
		
		ShowlExpression selectedExpression = targetProperty.getSelectedExpression();
		if (selectedExpression instanceof ShowlOverlayExpression) {
			ShowlOverlayExpression overlay = (ShowlOverlayExpression)selectedExpression;
			for (ShowlExpression e : overlay) {
				if (e instanceof ShowlPropertyExpression) {
					ShowlPropertyExpression sourcePropertyExpression = (ShowlPropertyExpression) e;
					declareSourceProperty(beamMethod, sourcePropertyExpression);
				}
			}
		} else if (selectedExpression instanceof ShowlPropertyExpression) {
			declareSourceProperty(beamMethod, (ShowlPropertyExpression)selectedExpression);
		}

	}


	private void declareSourceProperty(BeamMethod beamMethod, ShowlPropertyExpression sourcePropertyExpression) throws BeamTransformGenerationException {

		BlockInfo blockInfo = etran.peekBlockInfo();
		ShowlPropertyShape sourceProperty = sourcePropertyExpression.getSourceProperty();
		if (sourceProperty.getValueShape()!=null) {
			etran.addTableRowParam(beamMethod, sourceProperty.getDeclaringShape().effectiveNode());
			JVar var = etran.declareSourcePropertyValue(sourceProperty);
			blockInfo.putTableRow(sourceProperty.getValueShape().effectiveNode(), var);
		}
		
	}

}
