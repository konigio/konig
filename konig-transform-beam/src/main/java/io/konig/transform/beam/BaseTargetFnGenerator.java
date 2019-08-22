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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.TupleTag;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.fasterxml.uuid.Generators;
import com.google.api.client.util.DateTime;
import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.IJExpression;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;
import io.konig.core.vocab.Konig;

public class BaseTargetFnGenerator {
	
	private String basePackage;
	private NamespaceManager nsManager;
	protected JCodeModel model;
	protected OwlReasoner reasoner;
	protected BeamTypeManager typeManager;

	public BaseTargetFnGenerator(String basePackage, NamespaceManager nsManager, JCodeModel model, OwlReasoner reasoner,
			BeamTypeManager typeManager) {
		this.basePackage = basePackage;
		this.nsManager = nsManager;
		this.model = model;
		this.reasoner = reasoner;
		this.typeManager = typeManager;
	}

	public JDefinedClass generate(ShowlNodeShape targetNode, AbstractJClass inputClass) throws BeamTransformGenerationException {

  	String prefix = namespacePrefix(targetNode.getId());
    String localName = RdfUtil.localName(targetNode.getId());
    String className = className(prefix, "To" + localName + "Fn");

    JDefinedClass theClass;
		try {
			theClass = model._class(className);
	    AbstractJClass tableRowClass = model.ref(TableRow.class);
	    AbstractJClass doFnClass = model.ref(DoFn.class).narrow(inputClass).narrow(tableRowClass);
	    
	    theClass._extends(doFnClass);
	    
	    BeamExpressionTransform etran = new BeamExpressionTransform(reasoner, typeManager, model, theClass);
	    processElementMethod(theClass, targetNode, etran);
	    
		} catch (JClassAlreadyExistsException e) {
			throw new BeamTransformGenerationException("Failed to generate class " + className, e);
		}
		return theClass;
	}

  protected void processElementMethod(JDefinedClass thisClass, ShowlNodeShape targetNode, BeamExpressionTransform etran) 
  		throws BeamTransformGenerationException {
  	
  	AbstractJClass errorBuilderClass = errorBuilderClass();
		AbstractJClass processContextClass = model.ref(ProcessContext.class);
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass pipelineOptionsClass = model.directClass(PipelineOptions.class.getName());
		AbstractJClass customPipelineOptionsClass = typeManager.pipelineOptionsClass(RdfUtil.uri(targetNode.getId()));
		AbstractJClass tupleTagTableRowClass = model.ref(TupleTag.class).narrow(tableRowClass);
		
		JMethod method = thisClass.method(JMod.PUBLIC, model.VOID, "processElement");
		
		BeamMethod beamMethod = new BeamMethod(method);
		JVar c = method.param(processContextClass, "c");
		JVar pipelineOptions = method.param(pipelineOptionsClass, "pipelineOptions");
		
		JVar deadLetterTag = thisClass
				.field(JMod.PUBLIC | JMod.STATIC, tupleTagTableRowClass, "deadLetterTag")
				.init(JExpr.direct("new TupleTag<TableRow>(){}"));

		JVar successTag = thisClass.field(JMod.PUBLIC | JMod.STATIC, tupleTagTableRowClass, "successTag")
				.init(JExpr.direct("new TupleTag<TableRow>(){}"));

		
		JVar options = method.body().decl(customPipelineOptionsClass, "options").init(pipelineOptions.invoke("as").arg(JExpr.dotClass(customPipelineOptionsClass)));
				 
		JVar errorBuilder = method.body().decl(errorBuilderClass, "errorBuilder").init(errorBuilderClass._new());
		
		JTryBlock tryBlock = method.body()._try();
		BlockInfo blockInfo = etran.beginBlock(tryBlock.body());
		try {
			blockInfo.beamMethod(beamMethod);
			method.annotate(ProcessElement.class);
			blockInfo.setOptionsVar(options);

			blockInfo.errorBuilderVar(errorBuilder);

			JVar outputRow = tryBlock.body().decl(tableRowClass, "outputRow").init(tableRowClass._new());

			ShowlEffectiveNodeShape targetEffectiveNode = targetNode.effectiveNode();
			
			blockInfo.putTableRow(targetEffectiveNode, outputRow);
			processElementInternals(etran, targetNode, c, options);
			provideOutput(successTag, deadLetterTag, tryBlock, c, outputRow, errorBuilder, targetNode);
			
			
		} finally {
			etran.endBlock();
		}
		
		// The 'processElement' method should have only two parameters of type ProcessContext and PipelineOptions, respectively.
		// If there are more than two parameters, then something went wrong while propagating parameters
		// required by methods invoked from within 'processElement'.
		
		if (method.params().size()>2) {
			StringBuilder builder = new StringBuilder();
			builder.append("The method ");
			builder.append(thisClass.name());
			builder.append(".processElement contains the following extra parameters: ");
			List<JVar> paramList = method.params();
			String comma = "";
			for (int i=2; i<paramList.size(); i++) {
				JVar var = paramList.get(i);
				builder.append(comma);
				comma = ", ";
				builder.append(var.name());
			}
//			throw new BeamTransformGenerationException(builder.toString());
		}
		
		
	}
  	
	protected void processElementInternals(BeamExpressionTransform etran, ShowlNodeShape targetNode, JVar c, JVar options) throws BeamTransformGenerationException {

		BlockInfo blockInfo = etran.peekBlockInfo();
		BeamMethod beamMethod = blockInfo.getBeamMethod();
		JDefinedClass thisClass = etran.getTargetClass();
		
		StructPropertyGenerator struct = new StructPropertyGenerator(etran);
		StructInfo structInfo = struct.processNode(beamMethod, targetNode);
		
		for (ShowlEffectiveNodeShape node : structInfo.getNodeList()) {
			declareTableRow(thisClass, etran, node, c);
		}
		
		abortIfNoData(etran, structInfo.getNodeList(), targetNode);
		
		for (BeamMethod propertyMethod : structInfo.getMethodList()) {
			etran.invoke(propertyMethod);	
		}
		
	}

	protected void abortIfNoData(
			BeamExpressionTransform etran, 
			Collection<ShowlEffectiveNodeShape> nodeList,
			ShowlNodeShape targetNode
	) throws BeamTransformGenerationException {
		
		BlockInfo blockInfo = etran.peekBlockInfo();
		
		if (!requiresEarlyAbort(nodeList, targetNode)) {
			return;
		}
		
		IJExpression condition = null;
		for (ShowlEffectiveNodeShape node : nodeList) {
			if (node.canonicalNode().getId().equals(targetNode.getId())) {
				continue;
			}
			JVar var =  blockInfo.getTableRowVar(node);
			if (var == null) {
				fail("TableRow variable not found for {0}", node.canonicalNode().getPath());
			}
			if (condition == null) {
				condition = var.eqNull();
			} else {
				condition = condition.cand(var.eqNull());
			}
		}
		
		blockInfo.getBlock()._if(condition)._then()._return();
		
		
	}

	private boolean requiresEarlyAbort(Collection<ShowlEffectiveNodeShape> nodeList, ShowlNodeShape targetNode) {
		int count = 0;
		for (ShowlEffectiveNodeShape node : nodeList) {
			if (node.canonicalNode().getId().equals(targetNode.getId())) {
				continue;
			}
			if (++count > 1) {
				return true;
			}
		}
		return false;
	}

  
  	private void provideOutput(
			JVar successTag,
			JVar deadLetterTag,
			JTryBlock tryBlock, 
			JVar c, 
			JVar outputRow, 
			JVar errorBuilder,
			ShowlNodeShape targetNode
	) {
		
	
		tryBlock.body()._if(outputRow.invoke("isEmpty"))
			._then().add(errorBuilder.invoke("addError").arg(JExpr.lit("record is empty")));
		
		JConditional ifStatement = tryBlock.body()._if(errorBuilder.invoke("isEmpty").not());
		
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		JVar errorRow = ifStatement._then().decl(tableRowClass, "errorRow").init(tableRowClass._new());
		JBlock ifBlock = ifStatement._then();
		ifBlock.add(errorRow.invoke("set").arg("errorId").arg(
				model.ref(Generators.class).staticInvoke("timeBasedGenerator").invoke("generate").invoke("toString")));
		ifBlock.add(errorRow.invoke("set").arg("errorCreated").arg(model.ref(Date.class)._new().invoke("getTime").div(1000)));
		ifBlock.add(errorRow.invoke("set").arg("errorMessage").arg(errorBuilder.invoke("toString")));
		ifBlock.add(errorRow.invoke("set").arg("pipelineJobName").arg(JExpr.ref("options").invoke("getJobName")));

		List<ShowlNodeShape> sourceNodeList = listSourceNodes(targetNode);
		for (ShowlNodeShape sourceNode : sourceNodeList) {
			String sourceShapeName = ShowlUtil.shortShapeName(sourceNode);
			String rowName = StringUtil.javaIdentifier(StringUtil.firstLetterLowerCase(sourceShapeName)) + "Row";
			ifBlock.add(errorRow.invoke("set").arg(sourceShapeName).arg(JExpr.ref(rowName)));
		}
		ifBlock.add(c.invoke("output").arg(deadLetterTag).arg(errorRow));
		    
		ifStatement._else().add(c.invoke("output").arg(successTag).arg(outputRow));

		AbstractJClass throwableClass = model.ref(Throwable.class);
		JCatchBlock catchBlock = tryBlock._catch(throwableClass);
		JVar oops = catchBlock.param("oops");
		catchBlock.body().add(oops.invoke("printStackTrace"));
		
	}

  	private List<ShowlNodeShape> listSourceNodes(ShowlNodeShape targetNode) {
  		List<ShowlNodeShape> list = new ArrayList<>();
  		for (ShowlChannel channel : targetNode.getChannels()) {
  			ShowlNodeShape sourceNode = channel.getSourceNode();
  			if (!reasoner.isEnumerationClass(sourceNode.getOwlClass().getId())) {
  				list.add(sourceNode);
  			}
  		}
  		Collections.sort(list, new Comparator<ShowlNodeShape>() {
  			@Override
  			public int compare(ShowlNodeShape a, ShowlNodeShape b) {
  				return a.getId().stringValue().compareTo(b.getId().stringValue());
  			}
  		});
  		return list;
  	}


	protected void declareTableRow(JDefinedClass thisClass, BeamExpressionTransform etran, ShowlEffectiveNodeShape node, JVar c) throws BeamTransformGenerationException {
		
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		BlockInfo blockInfo = etran.peekBlockInfo();
		
		if (blockInfo.getTableRowVar(node) == null) {

			
			JBlock block = blockInfo.getBlock();
			
			String rowName = StringUtil.javaIdentifier(StringUtil.firstLetterLowerCase(ShowlUtil.shortShapeName(node.canonicalNode()))) + "Row";
			
			JVar rowVar = block.decl(tableRowClass, rowName, c.invoke("element").castTo(tableRowClass));
			blockInfo.putTableRow(node, rowVar);
		}
		
		
	}

	private String className(String simpleName) {
    return basePackage + "." + simpleName;
  }
  private String className(String namespacePrefix, String simpleName) throws BeamTransformGenerationException {
    return className(namespacePrefix + "." + simpleName);
  }
  
  private String namespacePrefix(Resource id) throws BeamTransformGenerationException {
    if (id instanceof URI) {
      URI uri = (URI) id;
      Namespace ns = nsManager.findByName(uri.getNamespace());
      if (ns != null) {
        return ns.getPrefix();
      }
      fail("Prefix not found for namespace <{0}>", uri.getNamespace());
    }
    fail("URI expected but id is a BNode");
    return null;
  }
  

  protected BeamTransformGenerationException fail(String pattern, Object...args) throws BeamTransformGenerationException {
    throw new BeamTransformGenerationException(MessageFormat.format(pattern, args));
  }
 
  
  protected JDefinedClass errorBuilderClass() throws BeamTransformGenerationException {
		String errorBuilderClassName = errorBuilderClassName();
		JDefinedClass errorBuilderClass = model._getClass(errorBuilderClassName);
		
		if (errorBuilderClass == null) {
			
			try {
				
				AbstractJClass stringBuilderClass = model.ref(StringBuilder.class);
				AbstractJClass stringClass = model.ref(String.class);
				
				errorBuilderClass = model._class(JMod.PUBLIC, errorBuilderClassName);
				JVar buffer = errorBuilderClass.field(JMod.PRIVATE, stringBuilderClass, "buffer");
				buffer.init(JExpr._new(stringBuilderClass));
				
				JMethod isEmpty = errorBuilderClass.method(JMod.PUBLIC, model._ref(boolean.class), "isEmpty");
				isEmpty.body()._return(buffer.invoke("length").eq(JExpr.lit(0)));
				
				JMethod addError = errorBuilderClass.method(JMod.PUBLIC, model.VOID, "addError");
				JVar text = addError.param(stringClass, "text");
				
				addError.body()._if(JExpr.invoke("isEmpty").not())._then().add(buffer.invoke("append").arg(JExpr.lit("; ")));
				addError.body().add(buffer.invoke("append").arg(text));
				
				JMethod toString = errorBuilderClass.method(JMod.PUBLIC, stringClass, "toString");
				toString.body()._return(buffer.invoke("toString"));
				
				
				
			} catch (JClassAlreadyExistsException e) {
				throw new BeamTransformGenerationException("Failed to create ErrorBuilder class", e);
			}
			
		}
		
		return errorBuilderClass;
		
	}
  
  private String errorBuilderClassName() {
	  	return basePackage + ".common.ErrorBuilder";
	  }
}
