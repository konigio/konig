package io.konig.core.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import java.util.List;

import io.konig.core.IriRewriter;

public class CompositeIriRewriter implements IriRewriter {
	
	private List<IriRewriter> list = new ArrayList<>();
	
	public void add(IriRewriter rewriter) {
		list.add(rewriter);
	}

	@Override
	public String rewrite(String iri) {
		for (IriRewriter rewriter : list) {
			String  other = rewriter.rewrite(iri);
			if (other != iri) {
				return other;
			}
		}
		return iri;
	}

}
