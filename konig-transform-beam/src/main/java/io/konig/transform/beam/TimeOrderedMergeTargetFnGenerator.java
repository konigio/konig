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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.IJClassContainer;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldRef;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JForEach;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JVar;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlOverlayExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class TimeOrderedMergeTargetFnGenerator extends MergeTargetFnGenerator {
	private static final Logger logger = LoggerFactory.getLogger(TimeOrderedMergeTargetFnGenerator.class);

	public TimeOrderedMergeTargetFnGenerator(Map<ShowlEffectiveNodeShape, IJExpression> tupleTagMap, String basePackage,
			NamespaceManager nsManager, JCodeModel model, OwlReasoner reasoner, BeamTypeManager typeManager) {
		super(tupleTagMap, basePackage, nsManager, model, reasoner, typeManager);
	}


	protected void processElementInternals(BeamExpressionTransform etran, ShowlNodeShape targetNode, JVar c, JVar options) throws BeamTransformGenerationException {

		Config config = createConfig(targetNode, options);
		JDefinedClass thisClass = etran.getTargetClass();
		JDefinedClass sourceProcessorClass = declareSourceProcessorClass(thisClass, config);
		Map<ShowlNodeShape, JDefinedClass> processorMap = processorMap(etran, sourceProcessorClass, config);
		
		for (ShowlNodeShape sourceNode : processorMap.keySet()) {
			declareTableRow(thisClass, etran, sourceNode.effectiveNode(), c);
		}

		abortIfNoData(etran, effectiveNodeList(processorMap.keySet()), targetNode);
		
		BlockInfo blockInfo = etran.peekBlockInfo();
		JBlock block = blockInfo.getBlock();
		
		// Build a List of SourceProcessor objects and sort the list
		
		String sourceListName = blockInfo.varName("sourceList");
		
		JCodeModel model = etran.codeModel();
		AbstractJClass listClass = model.ref(List.class).narrow(sourceProcessorClass);
		AbstractJClass arrayListClass = model.ref(ArrayList.class);
		AbstractJClass collectionsClass = model.ref(Collections.class);
		
		JVar sourceList = block.decl(listClass, sourceListName).init(arrayListClass._new());
		
		for (Map.Entry<ShowlNodeShape, JDefinedClass> entry : processorMap.entrySet()) {
			ShowlNodeShape sourceNode = entry.getKey();
			JVar sourceRowVar = blockInfo.getTableRowVar(sourceNode.effectiveNode());
			JDefinedClass processorClass = entry.getValue();
			
			block._if(sourceRowVar.neNull())._then().add(sourceList.invoke("add").arg(processorClass.staticInvoke("instance").arg(sourceRowVar)));
		}
		
		block.add(collectionsClass.staticInvoke("sort").arg(sourceList));
		
		// Invoke each SourceProcessor in the source list.
		
		JForEach forEach = block.forEach(sourceProcessorClass, "processor", sourceList);
		
		JVar processorVar = forEach.var();
		JVar errorBuilder = blockInfo.getErrorBuilderVar();
		JVar targetRow = blockInfo.getTableRowVar(targetNode.effectiveNode());
		forEach.body().add(processorVar.invoke("run").arg(options).arg(errorBuilder).arg(targetRow));
	}


	private Config createConfig(ShowlNodeShape targetNode, JVar options) {
		Config config = new Config(targetNode);
		config.setRequiresOptions(requiresOptions(targetNode));
		config.setOptions(options);
		return config;
	}


	private boolean requiresOptions(ShowlNodeShape targetNode) {
		for (ShowlPropertyShape p : targetNode.getProperties()) {
			if (p.getPredicate().equals(Konig.modified)) {
				return true;
			}
		}
		return false;
	}


	private Collection<ShowlEffectiveNodeShape> effectiveNodeList(Set<ShowlNodeShape> set) {
		List<ShowlEffectiveNodeShape> list = new ArrayList<>();
		for (ShowlNodeShape node : set) {
			list.add(node.effectiveNode());
		}
		return list;
	}


	private JDefinedClass declareSourceProcessorClass(JDefinedClass thisClass, Config config) throws BeamTransformGenerationException {

		try {
			
			ShowlNodeShape targetNode = config.getTargetNode();
			AbstractJClass tableRowClass = model.ref(TableRow.class);
			
			JDefinedClass sourceProcessorClass =  thisClass._class(JMod.ABSTRACT | JMod.PRIVATE, "SourceProcessor");
			

			AbstractJClass comparableClass = model.ref(Comparable.class).narrow(sourceProcessorClass);
			sourceProcessorClass._implements(comparableClass);
			
			JFieldVar modifiedField = sourceProcessorClass.field(JMod.PRIVATE, long.class, "modified");
			JFieldVar sourceRowField = sourceProcessorClass.field(JMod.PROTECTED, tableRowClass, "sourceRow");
			
			JMethod ctor = sourceProcessorClass.constructor(JMod.PROTECTED);
			JVar modifiedParam = ctor.param(long.class, "modified");
			JVar sourceRowParam = ctor.param(tableRowClass, "sourceRow");
			
			JBlock body = ctor.body();
			body.assign(modifiedField, modifiedParam);
			body.assign(sourceRowField, sourceRowParam);
			
			compareToMethod(sourceProcessorClass);
			
			JMethod run = sourceProcessorClass.method(
					JMod.ABSTRACT | JMod.PUBLIC, model.VOID, "run");
			
			String outputRowName = targetRowName(targetNode);
			if (config.isRequiresOptions()) {
				run.param(config.getOptions().type(), "options");
			}
			run.param(errorBuilderClass(), "errorBuilder");
			run.param(tableRowClass, outputRowName);
			
			return sourceProcessorClass;
			
		} catch (JClassAlreadyExistsException e) {
			throw new BeamTransformGenerationException("Failed to declare SourceProcessor class for " + thisClass.name(), e);
		}
		
	}

	private String targetRowName(ShowlNodeShape targetNode) {
		
		return StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(targetNode)) + "Row";
	}

	private Map<ShowlNodeShape, JDefinedClass> processorMap(BeamExpressionTransform etran, JDefinedClass sourceProcessorClass, Config config) throws BeamTransformGenerationException {
		Map<ShowlNodeShape, JDefinedClass> map = new LinkedHashMap<>();
		ShowlNodeShape targetNode = config.getTargetNode();
		List<ShowlChannel> channelList = targetNode.nonEnumChannels(reasoner);
		Collections.sort(channelList, new Comparator<ShowlChannel>() {

			@Override
			public int compare(ShowlChannel a, ShowlChannel b) {
				String aName = RdfUtil.localName(a.getSourceNode().getId());
				String bName = RdfUtil.localName(b.getSourceNode().getId());
				
				return aName.compareTo(bName);
			}
		});
		for (ShowlChannel channel : channelList) {
			ShowlNodeShape sourceNode = channel.getSourceNode();
			
			JDefinedClass processorClass = processorSubclass(etran, sourceProcessorClass, sourceNode, config);
			map.put(sourceNode, processorClass);
		}
		return map;
	}

	private JDefinedClass processorSubclass(BeamExpressionTransform etran, JDefinedClass sourceProcessorClass, ShowlNodeShape sourceNode,
			Config config) throws BeamTransformGenerationException {
		
		String className = ShowlUtil.shortShapeName(sourceNode) + "Processor";
		
		IJClassContainer<?> container = sourceProcessorClass.getOuter();
		try {
			JDefinedClass processorClass = (JDefinedClass) container._class(JMod.PRIVATE, className);
			processorClass._extends(sourceProcessorClass);
			
			
			JMethod ctor = processorClass.constructor(JMod.PRIVATE);
			JVar modifiedParam = ctor.param(long.class, "modified");
			JVar sourceRowParam = ctor.param(TableRow.class, "sourceRow");
			JInvocation invoke = JExpr.invoke("super").arg(modifiedParam).arg(sourceRowParam);
			ctor.body().add(invoke);
			
			instanceMethod(etran, processorClass, sourceNode, config);
			runMethod(processorClass, sourceNode, config);
			
			return processorClass;
			
		} catch (JClassAlreadyExistsException e) {
			ShowlNodeShape targetNode = config.getTargetNode();
			String msg = MessageFormat.format("Failed to declare {0} for {1}", className, targetNode.getPath());
			throw new BeamTransformGenerationException(msg, e);
		}
		
	}

	private void instanceMethod(BeamExpressionTransform etran, JDefinedClass processorClass, ShowlNodeShape sourceNode, Config config) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = etran.peekBlockInfo();
		JCodeModel model = processorClass.owner();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass longClass = model.ref(Long.class);
		JMethod method = processorClass.method(JMod.PRIVATE | JMod.STATIC, processorClass, "instance");
		
		JVar row = method.param(tableRowClass, "row");
		
		
		JBlock block = method.body();
		String fieldName = sourceNode.getPath() + ".modified";
		
		JVar millis = block.decl(longClass, "millis").init(JExpr.invoke(etran.unixTimeMethod()).arg(fieldName).arg(row.invoke("get").arg("modified")).arg(blockInfo.getErrorBuilderVar()));

		block._if(millis.eqNull())._then()._return(JExpr._null());
		
		block._return(processorClass._new().arg(millis).arg(row));
		
	}


	private void runMethod(JDefinedClass processorClass, ShowlNodeShape sourceNode, Config config) throws BeamTransformGenerationException {
		ShowlNodeShape targetNode = config.getTargetNode();
		JCodeModel model = processorClass.owner();
		JMethod run = processorClass.method(JMod.PUBLIC, model.VOID, "run");

    BeamExpressionTransform etran = new BeamExpressionTransform(reasoner, typeManager, model, processorClass);
    etran.setOverlayPattern(true);
		
		BeamMethod beamMethod = new BeamMethod(run);
		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		JVar options = null;
		if (config.isRequiresOptions()) {
			options = run.param(config.getOptions().type(), "options");
		}
		blockInfo.setOptionsVar(options);
		etran.addErrorBuilderParam(beamMethod);
		etran.addTableRowParam(beamMethod, targetNode.effectiveNode());
		
		JDefinedClass superClass = (JDefinedClass) processorClass._extends();
		JVar sourceRow = superClass.fields().get("sourceRow");
		blockInfo.putTableRow(sourceNode.effectiveNode(), sourceRow);

		Collection<ShowlDirectPropertyShape> propertyList = etran.sortProperties(targetNode);
		

		List<BeamMethod> methodList = new ArrayList<>();
		for (ShowlDirectPropertyShape targetProperty : propertyList) {
			ShowlExpression selected = targetProperty.getSelectedExpression();
			if (selected == null) {
				fail("Expression not defined for {0}", targetProperty.getPath());
			}
			
			ShowlExpression sourceExpression = sourceExpression(sourceNode, targetProperty);

			if (sourceExpression != null) {
				
				targetProperty.setSelectedExpression(sourceExpression);

				TargetPropertyGenerator propertyGenerator = TargetPropertyGenerator.create(etran, targetProperty);
				BeamMethod propertyMethod = propertyGenerator.generate(beamMethod, targetProperty);
				
				targetProperty.setSelectedExpression(selected);

				methodList.add(propertyMethod);
			}
		}


		for (BeamMethod propertyMethod : methodList) {
			etran.invoke(propertyMethod);
		}
		
		
	}

	private ShowlExpression sourceExpression(ShowlNodeShape sourceNode, ShowlDirectPropertyShape targetProperty) throws BeamTransformGenerationException {
		ShowlExpression selected = targetProperty.getSelectedExpression();
		
		if (selected instanceof ShowlOverlayExpression) {
			// We only handle the case when there is a single expression
			// based on the given source node and no other source node.
			//
			// For now, if there are two such expressions or if an expression depends on
			// more than one source node, we throw an exception.
			// In the future, we should handle these more complex use cases.
			
			ShowlOverlayExpression overlay = (ShowlOverlayExpression) selected;

			ShowlExpression sourceExpression = null;
			for (ShowlExpression element : overlay) {
				sourceExpression = sourceExpression(sourceExpression, element, sourceNode, targetProperty);
			}
			return sourceExpression;
		} else {
			return sourceExpression(null, selected, sourceNode, targetProperty);
		}
	}

	private ShowlExpression sourceExpression(ShowlExpression sourceExpression, ShowlExpression element,
			ShowlNodeShape sourceNode, ShowlPropertyShape targetProperty) throws BeamTransformGenerationException {

		Set<ShowlPropertyShape> set = new HashSet<>();
		element.addProperties(set);
		
		boolean priorResultExists = sourceExpression!=null;
		boolean basedOnOtherNode = false;
		boolean wanted = false;
		for (ShowlPropertyShape sourceProperty : set) {
			if (sourceProperty.getRootNode() == sourceNode) {
				if (priorResultExists) {
					fail("Cannot handle multiple expressions based on {0} while processing {1}", 
							sourceNode.getPath(), targetProperty.getPath());
				}
				wanted = true;
				sourceExpression = element;
			} else {
				basedOnOtherNode = true;
			}
		}
		if (wanted && basedOnOtherNode) {
			logger.error("Cannot handle expression {} while processing {}.  Please submit a change request to support this use case.", 
					element.displayValue(), targetProperty.getPath());
			return null;
		}
	
		return sourceExpression;
	}

	private void compareToMethod(JDefinedClass sourceProcessorClass) {

		AbstractJType intType = model._ref(int.class);
		AbstractJType longType = model._ref(long.class);

		JMethod compareTo = sourceProcessorClass.method(JMod.PUBLIC, intType, "compareTo");
		JVar other = compareTo.param(sourceProcessorClass, "other");
		JBlock block = compareTo.body();
		
		JVar modified = sourceProcessorClass.fields().get("modified");
		JFieldRef otherModified = other.ref(modified);
		
		
		JVar delta = block.decl(longType, "delta").init(otherModified.minus(modified));
		
		IJExpression result = JExpr.cond(delta.lt0(), JExpr.lit(-1), 
				JExpr.cond(delta.eq0(), JExpr.lit(0), JExpr.lit(1)));
		
		block._return(result);
		
	}
	
	private static class Config {
		private ShowlNodeShape targetNode;
		private JVar options;
		private boolean requiresOptions;

		public Config(ShowlNodeShape targetNode) {
			this.targetNode = targetNode;
		}

		public boolean isRequiresOptions() {
			return requiresOptions;
		}

		public void setRequiresOptions(boolean requiresOptions) {
			this.requiresOptions = requiresOptions;
		}

		public ShowlNodeShape getTargetNode() {
			return targetNode;
		}

		public JVar getOptions() {
			return options;
		}

		public void setOptions(JVar options) {
			this.options = options;
		}
		
		
	}

}
