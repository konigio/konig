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
 * A BeamMatchMergeGenerator that handles the case where the unique key is a tuple (i.e. not a single value)
 * @author Greg McFall
 *
 */
public class TupleMatchMergeGenerator extends BeamMatchMergeGenerator {

	public TupleMatchMergeGenerator(BeamExpressionTransform etran, List<UniqueKeyElement> uniqueKeyInfo) {
		super(etran, uniqueKeyInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generate() throws BeamTransformGenerationException {
		// TODO Auto-generated method stub

	}

}
