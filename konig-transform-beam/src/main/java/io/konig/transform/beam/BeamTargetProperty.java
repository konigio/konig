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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.datasource.DataSource;
import io.konig.datasource.TableDataSource;

public class BeamTargetProperty {

	private ShowlDirectPropertyShape directProperty;
	private List<BeamChannel> channelList;
	private List<BeamSourceProperty> sourcePropertyList;
	
	public BeamTargetProperty(ShowlDirectPropertyShape directProperty) {
		this.directProperty = directProperty;
	}

	/**
	 * Get the list of source properties that contribute to the target property.
	 */
	public List<BeamSourceProperty> getSourcePropertyList() {
		return sourcePropertyList;
	}

	public void setSourcePropertyList(List<BeamSourceProperty> sourcePropertyList) {
		this.sourcePropertyList = sourcePropertyList;
	}

	public ShowlDirectPropertyShape getDirectProperty() {
		return directProperty;
	}

	/**
	 * Get the set of channels that contribute data to the target property.
	 */
	public List<BeamChannel> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<BeamChannel> channelList) {
		this.channelList = channelList;
	}
	
	public BeamChannel channelFor(ShowlPropertyShape p) throws BeamTransformGenerationException {
			ShowlNodeShape sourceRoot = p.getRootNode();
		
		for (BeamChannel sourceInfo : channelList) {
			if (sourceInfo.getFocusNode().getRoot() == sourceRoot) {
				return sourceInfo;
			}
		}
		
		throw new BeamTransformGenerationException("Failed to get SourceInfo for " + p.getPath());
	}
	
	public String simplePath() throws BeamTransformGenerationException {
		
		List<ShowlPropertyShape> list = new ArrayList<>();
		for (ShowlPropertyShape p=directProperty; p != null; p=p.getDeclaringShape().getAccessor()) {
			list.add(p);
		}
		
		Collections.reverse(list);
		
		
		StringBuilder builder = new StringBuilder();
		String dot = "";
		for (ShowlPropertyShape p : list) {
			builder.append(dot);
			dot = ".";
			builder.append(p.getPredicate().getLocalName());
		}
		return builder.toString();
		
		
	}
	
	
	
}
