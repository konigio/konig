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


import org.apache.beam.sdk.values.KV;

import com.google.api.services.bigquery.model.TableRow;
import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlUniqueKey;

/**
 * A TableRowToKvFnGenerator specialized for the case where the UniqueKey consists of a single, datatype property.
 * @author Greg McFall
 *
 */
public class SimpleKeyFnGenerator extends TableRowToKvFnGenerator {

	public SimpleKeyFnGenerator(String mainPackage, BeamExpressionTransform etran, ShowlNodeShape sourceNode,
			ShowlUniqueKey uniqueKey, SourceRowFilter windowFilter) {
		super(mainPackage, etran, sourceNode, uniqueKey, windowFilter);
	}

	@Override
	protected JVar computeKey(JVar row, JVar processContext) throws BeamTransformGenerationException {

		ShowlPropertyShape keyProperty = uniqueKeyProperty();
		return etran.declareSourcePropertyValue(keyProperty);
	}

	
	private ShowlPropertyShape uniqueKeyProperty() {
		return uniqueKey.get(0).getPropertyShape();
	}

	@Override
	protected void computeKeyType() throws BeamTransformGenerationException {

		JCodeModel model = etran.codeModel();
		ShowlPropertyShape p = uniqueKeyProperty();
		
		RdfJavaType type = etran.getTypeManager().rdfJavaType(p);
		AbstractJClass tableRowClass = model.ref(TableRow.class);
		
		keyType = type.getJavaType();
		kvClass = model.ref(KV.class).narrow(keyType).narrow(tableRowClass);
	}


}
