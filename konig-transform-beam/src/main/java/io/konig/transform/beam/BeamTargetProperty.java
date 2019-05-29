package io.konig.transform.beam;

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
