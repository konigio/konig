package io.konig.transform.rule;


/**
 * A rule to compute the IRI for a resource from a template provided by some DataChannel
 * @author Greg McFall
 *
 */
public class IriTemplateIdRule implements IdRule {

	private DataChannel dataChannel;

	public IriTemplateIdRule(DataChannel dataChannel) {
		this.dataChannel = dataChannel;
	}

	public DataChannel getDataChannel() {
		return dataChannel;
	}
}
