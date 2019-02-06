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


/**
 * A filter that allows a mapping only if all of the encapsulated filters are allowed.
 * @author Greg McFall
 *
 */
public class AndShowlMappingFilter extends CompositeShowlMappingFilter {
	

	@Override
	public boolean allowMapping(ShowlNodeShape source, ShowlNodeShape target) {

		for (ShowlMappingFilter e : elements) {
			if (!e.allowMapping(source, target)) {
				return false;
			}
		}
		return true;
	}

}
