package io.konig.core.showl;

/*
 * #%L
 * Konig Core
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


import java.util.Set;

import io.konig.shacl.Shape;

/**
 * An entity that selects the set of source shapes for a given target shape.
 * 
 * @author Greg McFall
 *
 */
public interface ShowlSourceNodeSelector {

	/**
	 * Compute the set of potential source node shapes that can be used to 
	 * construct the mapping for a given target node shape.
	 * @param targetShape The target NodeShape.
	 * @return The set of potential source node shapes, or null if there are no restrictions on the set of candidates.
	 */
	public Set<ShowlNodeShape> selectCandidateSources(ShowlService factory, ShowlNodeShape targetShape);
}
