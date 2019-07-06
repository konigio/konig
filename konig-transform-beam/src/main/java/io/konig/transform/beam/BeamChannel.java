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


import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JVar;

import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlNodeShape;

@Deprecated
public class BeamChannel implements Comparable<BeamChannel> {

	private ShowlChannel channel;
  private JVar pcollection;
  private JVar tupleTag;
  private JVar sourceRow;
  private JVar sourceRowParam;

  private JDefinedClass readFileFn;
  
	public BeamChannel(ShowlChannel channel) {
		this.channel = channel;
	}

	public JVar getPcollection() {
		return pcollection;
	}

	public void setPcollection(JVar pcollection) {
		this.pcollection = pcollection;
	}

	public JVar getTupleTag() {
		return tupleTag;
	}

	public void setTupleTag(JVar tupleTag) {
		this.tupleTag = tupleTag;
	}

	public JVar getSourceRow() {
		return sourceRow;
	}

	public void setSourceRow(JVar sourceRow) {
		this.sourceRow = sourceRow;
	}

	public JVar getSourceRowParam() {
		return sourceRowParam;
	}

	public void setSourceRowParam(JVar sourceRowParam) {
		this.sourceRowParam = sourceRowParam;
	}

	public ShowlChannel getChannel() {
		return channel;
	}

	public JDefinedClass getReadFileFn() {
		return readFileFn;
	}

	public void setReadFileFn(JDefinedClass readFileFn) {
		this.readFileFn = readFileFn;
	}
  
  public ShowlNodeShape getFocusNode() {
    return channel.getSourceNode();
  }	

  public String toString() {
    return "SourceInfo(" + channel.toString() + ")";
  }

	@Override
	public int compareTo(BeamChannel other) {
		
		return RdfUtil.localName(channel.getSourceNode().getShape().getId())
				.compareTo(RdfUtil.localName(other.getChannel().getSourceNode().getShape().getId()));
	}


}
