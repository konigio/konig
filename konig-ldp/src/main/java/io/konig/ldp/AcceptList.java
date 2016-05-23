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
import java.util.Collections;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AcceptList extends ArrayList<AcceptableMediaType> {
	private static final long serialVersionUID = 1L;
	private MediaType selected;
	
	
	
	public AcceptList() {
		
	}
	
	public void parse(String acceptHeader) {
		if (acceptHeader != null) {
			Pattern pattern = Pattern.compile("q=([.\\d]+)");
			String[] array = acceptHeader.split(",");
			for (int i=0; i<array.length; i++) {
				String value = array[i];
				int semiColon = value.indexOf(';');
				String mediaType = null;
				
				
				float qValue = 1;
				
				if (semiColon > 0) {
					mediaType = value.substring(0, semiColon).trim();
					String qValueText = value.substring(semiColon).trim();
					
					Matcher matcher = pattern.matcher(qValueText);
					if (matcher.find()) {
						qValueText = matcher.group(1);
						qValue = Float.parseFloat(qValueText);
					}
				} else {
					mediaType = value.trim();
				}
				
				MediaType type = MediaType.instance(mediaType);
				AcceptableMediaType accept = new AcceptableMediaType(type, qValue);
				add(accept);
			}
			
		}
		
	}
	
	public MediaType getSelected() {
		if (selected == null) {
			float max = 0;
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
	
	/**
	 * Sort this list by q value, descending.
	 */
	public void sort() {
		Collections.sort(this, new Comparator<AcceptableMediaType>() {

			@Override
			public int compare(AcceptableMediaType a, AcceptableMediaType b) {
				float delta = b.getQValue() - a.getQValue();
				return delta < 0 ? -1 : delta>0 ? 1 : 
					a.getMediaType().getFullName().compareTo(b.getMediaType().getFullName());
			}
		});
	}
	
	
	
	public String toString() {
		if (isEmpty()) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		String comma = "";
		for (AcceptableMediaType m : this) {
			builder.append(comma);
			m.append(builder);
			comma = ", ";
		}
		return builder.toString();
	}
	
}
