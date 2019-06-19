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


import java.util.Comparator;

import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlNodeShape;

public class BeamNodeComparator implements Comparator<ShowlNodeShape> {

	@Override
	public int compare(ShowlNodeShape a, ShowlNodeShape b) {
		int aKind = nodeKind(a);
		int bKind = nodeKind(b);
		
		return (aKind==bKind) ? RdfUtil.localName(a.getId()).compareTo(RdfUtil.localName(b.getId())) : aKind-bKind;
	}

	private int nodeKind(ShowlNodeShape node) {
		
		return node.isTargetNode() ? 0 : 1;
	}


}
