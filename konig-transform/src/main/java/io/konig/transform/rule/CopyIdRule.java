package io.konig.transform.rule;

/**
 * A rule to copy the IRI for a resource from the value provided in a given DataChannel.
 * @author Greg McFall
 *
 */
public class CopyIdRule implements IdRule {

	private DataChannel dataChannel;

	public CopyIdRule(DataChannel dataChannel) {
		this.dataChannel = dataChannel;
	}

	public DataChannel getDataChannel() {
		return dataChannel;
	}
}
