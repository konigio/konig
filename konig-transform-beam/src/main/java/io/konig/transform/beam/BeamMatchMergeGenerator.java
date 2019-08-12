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


import java.util.List;

import io.konig.core.showl.UniqueKeyElement;

/**
 * A utility that generates code used to match and merge records within a 
 * multi-valued property.
 * @author Greg McFall
 *
 */
abstract public class BeamMatchMergeGenerator {
	
	protected BeamExpressionTransform etran;
	protected List<UniqueKeyElement> uniqueKeyInfo;
	
	

	public BeamMatchMergeGenerator(BeamExpressionTransform etran, List<UniqueKeyElement> uniqueKeyInfo) {
		this.etran = etran;
		this.uniqueKeyInfo = uniqueKeyInfo;
	}

	abstract public void generate() throws BeamTransformGenerationException;

}
