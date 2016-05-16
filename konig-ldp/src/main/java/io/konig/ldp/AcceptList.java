package io.konig.ldp;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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

public class AcceptList extends ArrayList<AcceptableMediaType> {
	private static final long serialVersionUID = 1L;
	private MediaType selected;
	
	public MediaType getSelected() {
		if (selected == null) {
			int max = 0;
			for (AcceptableMediaType m : this) {
				if (m.getQValue() > max) {
					max = m.getQValue();
					selected = m.getMediaType();
				}
			}
			if (selected == null) {
				selected = MediaType.TURTLE;
			}
		}
		
		return selected;
	}
	public void setSelected(MediaType selected) {
		this.selected = selected;
	}
	
}
