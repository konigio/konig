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
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.AbstractJType;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCatchBlock;
import com.helger.jcodemodel.JClassAlreadyExistsException;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JInvocation;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import com.helger.jcodemodel.JTryBlock;
import com.helger.jcodemodel.JVar;

import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlUniqueKey;
import io.konig.core.showl.ShowlUtil;

abstract public class TableRowToKvFnGenerator {

	private ShowlNodeShape sourceNode;
	protected ShowlUniqueKey uniqueKey;
	private String mainPackage;
	protected BeamExpressionTransform etran;
	private SourceRowFilter windowFilter;

	protected JDefinedClass fnClass;
	protected AbstractJType keyType;
	protected AbstractJClass kvClass;
	
	protected TableRowToKvFnGenerator(String mainPackage, BeamExpressionTransform etran, ShowlNodeShape sourceNode,
			ShowlUniqueKey uniqueKey, SourceRowFilter windowFilter) {
		this.mainPackage = mainPackage;
		this.etran = etran;
		this.sourceNode = sourceNode;
		this.uniqueKey = uniqueKey;
		this.windowFilter = windowFilter;
		
	}

	public JDefinedClass generate() throws BeamTransformGenerationException {
		computeKeyType();
		JCodeModel model = etran.codeModel();
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		String fnClassName = fnClassName();
		try {

			fnClass = model._class(JMod.PUBLIC, fnClassName);
			etran.setTargetClass(fnClass);
			AbstractJClass doFnClass = model.ref(DoFn.class).narrow(tableRowClass).narrow(kvClass);

			fnClass._extends(doFnClass);
			processElement(kvClass);


		} catch (JClassAlreadyExistsException e) {
			fail("Failed to create {fnClassName} ", e);
		}
		return fnClass;
	}

  abstract protected void computeKeyType() throws BeamTransformGenerationException;

	abstract protected JVar computeKey(JVar row, JVar processContext) throws BeamTransformGenerationException;

	private BeamTransformGenerationException fail(String pattern, Object...args) throws BeamTransformGenerationException {
    throw new BeamTransformGenerationException(MessageFormat.format(pattern, args));
  }
	
	private void processElement(AbstractJClass outputClass) throws BeamTransformGenerationException {
		JCodeModel model = etran.codeModel();
		AbstractJClass processContextClass = model.ref(ProcessContext.class);
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		AbstractJClass throwableClass = model.ref(Throwable.class);
		AbstractJClass tupleTagClass = model.ref(TupleTag.class);

		JVar deadLetterTag = fnClass.field(JMod.PUBLIC | JMod.STATIC,
				tupleTagClass.narrow(model.ref(String.class)), "deadLetterTag")
				.init(JExpr.direct("new TupleTag<String>(){}"));
		
		JVar successTag = fnClass.field(JMod.PUBLIC | JMod.STATIC , tupleTagClass.narrow(outputClass), 
				"successTag").init(JExpr.direct("new TupleTag<"+outputClass.name()+">(){}"));
		
		JMethod method = fnClass.method(JMod.PUBLIC, model.VOID, "processElement");
		JVar c = method.param(processContextClass, "c");
		method.annotate(model.directClass(ProcessElement.class.getName()));
		BeamMethod beamMethod = new BeamMethod(method);
		BlockInfo blockInfo = etran.beginBlock(beamMethod);
		try {

			JTryBlock tryBlock = method.body()._try();
			JBlock block = tryBlock.body();
			blockInfo.setBlock(block);
			
			JVar row = block.decl(tableRowClass, "row").init(c.invoke("element"));
			blockInfo.putTableRow(sourceNode.effectiveNode(), row);
			
			if (windowFilter!=null) {
				windowFilter.addFilter(etran, sourceNode, c, row);
			}
			
			JVar keyValue = computeKey(row, c);
			AbstractJClass plainKvClass = model.ref(KV.class);

			JInvocation invoke = c.invoke("output").arg(successTag).arg(plainKvClass.staticInvoke("of").arg(keyValue).arg(row));
			block._if(keyValue.neNull())._then().add(invoke);
			JCatchBlock catchBlock = tryBlock._catch(throwableClass);
			JVar oops = catchBlock.param("oops");
			catchBlock.body().add(c.invoke("output").arg(deadLetterTag).arg(oops.invoke("getMessage")));
		} finally {
			etran.endBlock();
		}
		


	}


	private String fnClassName() throws BeamTransformGenerationException {
		String shortName = ShowlUtil.shortShapeName(RdfUtil.uri(sourceNode.getId()));

		return mainPackage + "." + shortName + "ToKvFn";
	}
}
