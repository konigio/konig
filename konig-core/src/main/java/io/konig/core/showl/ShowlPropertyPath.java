package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

import io.konig.formula.Direction;

public class ShowlPropertyPath extends ArrayList<ShowlPropertyShape> {
	private static final long serialVersionUID = 1L;
	
	public boolean containsInwardStep() {
		for (ShowlPropertyShape p : this) {
			if (p.getDirection()==Direction.IN) {
				return true;
			}
		}
		return false;
	}
	
	public ShowlPropertyShape first() {
		return isEmpty() ? null : get(0);
	}
	
	public ShowlPropertyShape lastStep() {
		return isEmpty() ? null : get(size()-1);
	}

}
