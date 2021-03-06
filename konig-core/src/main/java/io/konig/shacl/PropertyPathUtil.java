package io.konig.shacl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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

import io.konig.core.KonigException;
import io.konig.core.Path;
import io.konig.core.path.OutStep;
import io.konig.core.path.Step;

public class PropertyPathUtil {

	public static PropertyPath create(Path path) throws KonigException {
		
		List<Step> stepList = path.asList();
		
		if (stepList.size() == 1) {
			Step step = stepList.get(0);
			return create(step);
			
		} else {
			SequencePath sequence = new SequencePath();
			for (Step step : stepList) {
				sequence.add(create(step));
			}
			return sequence;
		}
	}
	
	public static PropertyPath create(Step step) throws KonigException {
		if (step instanceof OutStep) {
			return new PredicatePath(step.getPredicate());
		} else {
			throw new KonigException("Step type not supported: " + step.getClass().getSimpleName());
		}
	}
}
