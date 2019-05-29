package io.konig.transform.beam;

import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JVar;

import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlChannel;
import io.konig.core.showl.ShowlNodeShape;

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
