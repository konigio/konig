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

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;
import org.apache.beam.sdk.transforms.DoFn.ProcessElement;
import org.apache.beam.sdk.values.TupleTag;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JConditional;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;

import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlEffectiveNodeShape;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.util.StringUtil;

public class BaseTargetFnGenerator {
	
	private String basePackage;
	private NamespaceManager nsManager;
	protected JCodeModel model;
	private OwlReasoner reasoner;
	private BeamTypeManager typeManager;

	public BaseTargetFnGenerator(String basePackage, NamespaceManager nsManager, JCodeModel model, OwlReasoner reasoner,
			BeamTypeManager typeManager) {
		this.basePackage = basePackage;
		this.nsManager = nsManager;
		this.model = model;
		this.reasoner = reasoner;
		this.typeManager = typeManager;
	}

	public JDefinedClass generate(ShowlNodeShape targetNode) throws BeamTransformGenerationException {

  	String prefix = namespacePrefix(targetNode.getId());
    String localName = RdfUtil.localName(targetNode.getId());
    String className = className(prefix, "To" + localName + "Fn");

    JDefinedClass theClass;
		try {
			theClass = model._class(className);
	    AbstractJClass tableRowClass = model.ref(TableRow.class);
	    AbstractJClass doFnClass = model.ref(DoFn.class).narrow(tableRowClass).narrow(tableRowClass);
	    
	    theClass._extends(doFnClass);
	    
	    BeamExpressionTransform etran = new BeamExpressionTransform(reasoner, typeManager, model, theClass);
	    processElementMethod(theClass, targetNode, etran);
	    
		} catch (JClassAlreadyExistsException e) {
			throw new BeamTransformGenerationException("Failed to generate class " + className, e);
		}
		return theClass;
	}

  private void processElementMethod(JDefinedClass thisClass, ShowlNodeShape targetNode, BeamExpressionTransform etran) 
  		throws BeamTransformGenerationException {
  	
	  	AbstractJClass errorBuilderClass = errorBuilderClass();
		AbstractJClass processContextClass = model.ref(ProcessContext.class);
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass stringClass = model.ref(String.class);
		
		AbstractJClass tupleTagStringClass = model.ref(TupleTag.class).narrow(stringClass);
		AbstractJClass tupleTagTableRowClass = model.ref(TupleTag.class).narrow(tableRowClass);
		
  	JMethod method = thisClass.method(JMod.PUBLIC, model.VOID, "processElement");
		
		StructPropertyGenerator struct = new StructPropertyGenerator(etran);
		BeamMethod beamMethod = new BeamMethod(method);
		
		JVar deadLetterTag = thisClass
				.field(JMod.PUBLIC | JMod.STATIC, tupleTagStringClass, "deadLetterTag")
				.init(tupleTagStringClass._new());

		JVar successTag = thisClass.field(JMod.PUBLIC | JMod.STATIC, tupleTagTableRowClass, "successTag")
				.init(tupleTagTableRowClass._new());


		JVar errorBuilder = method.body().decl(errorBuilderClass, "errorBuilder").init(errorBuilderClass._new());
		
		JTryBlock tryBlock = method.body()._try();
		BlockInfo blockInfo = etran.beginBlock(tryBlock.body());
		try {
			blockInfo.beamMethod(beamMethod);
			method.annotate(ProcessElement.class);
			JVar c = method.param(processContextClass, "c");
			

			blockInfo.errorBuilderVar(errorBuilder);

			JVar outputRow = tryBlock.body().decl(tableRowClass, "outputRow").init(tableRowClass._new());

			ShowlEffectiveNodeShape targetEffectiveNode = targetNode.effectiveNode();
			
			blockInfo.putTableRow(targetEffectiveNode, outputRow);
			

			StructInfo structInfo = struct.processNode(beamMethod, targetNode);
			
			for (ShowlEffectiveNodeShape node : structInfo.getNodeList()) {
				declareTableRow(thisClass, etran, node, c);
			}
			
			for (BeamMethod propertyMethod : structInfo.getMethodList()) {
				etran.invoke(propertyMethod);
			}
			
			provideOutput(successTag, deadLetterTag, tryBlock, c, outputRow, errorBuilder);
			
			
		} finally {
			etran.endBlock();
		}
		
	}

	private void provideOutput(
			JVar successTag,
			JVar deadLetterTag,
			JTryBlock tryBlock, 
			JVar c, 
			JVar outputRow, 
			JVar errorBuilder
	) {
		
	
		tryBlock.body()._if(outputRow.invoke("isEmpty"))
			._then().add(errorBuilder.invoke("addError").arg(JExpr.lit("record is empty")));
		
		JConditional ifStatement = tryBlock.body()._if(errorBuilder.invoke("isEmpty").not());
		ifStatement._then().add(c.invoke("output").arg(deadLetterTag).arg(errorBuilder.invoke("toString")));
		ifStatement._else().add(c.invoke("output").arg(successTag).arg(outputRow));

		AbstractJClass throwableClass = model.ref(Throwable.class);
		JCatchBlock catchBlock = tryBlock._catch(throwableClass);
		JVar oops = catchBlock.param("oops");
		catchBlock.body().add(errorBuilder.invoke("addError").arg(oops.invoke("getMessage")));
		catchBlock.body().add(c.invoke("output").arg(deadLetterTag).arg(errorBuilder.invoke("toString")));
		
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
