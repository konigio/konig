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

import org.openrdf.model.URI;

import com.helger.jcodemodel.AbstractJClass;
import com.helger.jcodemodel.JBlock;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JExpr;
import com.helger.jcodemodel.JVar;

import io.konig.core.showl.ShowlPropertyShape;

public class BeamSourceProperty implements Comparable<BeamSourceProperty> {

	private BeamChannel beamChannel;
	private ShowlPropertyShape propertyShape;
	private JVar var;

	public BeamSourceProperty(BeamChannel beamChannel, ShowlPropertyShape propertyShape) {
		this.beamChannel = beamChannel;
		this.propertyShape = propertyShape;
	}

	public BeamSourceProperty(ShowlPropertyShape p, JVar var) {
		propertyShape = p;
		this.var = var;
	}

	public BeamChannel getBeamChannel() {
		return beamChannel;
	}
	
	public URI getPredicate() {
		return propertyShape.getPredicate();
	}

	public JVar getVar() {
		return var;
	}

	public void setVar(JVar var) {
		this.var = var;
	}

	public ShowlPropertyShape getPropertyShape() {
		return propertyShape;
	}

	@Override
	public int compareTo(BeamSourceProperty o) {
		return propertyShape.getPredicate().getLocalName().compareTo(o.getPropertyShape().getPredicate().getLocalName());
	}

	public String canonicalPath() {
		return propertyShape.getPath();
	}
	
	public void generateVar(JCodeModel model, JBlock block) {
		
		
		//  Object $sourcePropertyName = $sourceRowParam==null?null:$sourceRowParam.get("$sourcePropertyName");
		
		AbstractJClass objectClass = model.ref(Object.class);
		String sourcePropertyName = getPredicate().getLocalName();
		BeamChannel sourceInfo = getBeamChannel();
		
		JVar sourceRowParam = sourceInfo.getSourceRowParam();
		
		JVar sourcePropertyVar = block.decl(objectClass, sourcePropertyName).init(
				JExpr.cond(sourceRowParam.eqNull(), 
						JExpr._null(), 
						sourceRowParam.invoke("get").arg(JExpr.lit(sourcePropertyName))));
		
		setVar(sourcePropertyVar);
	}
	
}
