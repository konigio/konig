package io.konig.transform.beam;

import java.util.List;

import io.konig.core.showl.UniqueKeyElement;

/**
 * A utility that generates code used to match and merge records within a 
 * multi-valued property.
 * @author Greg McFall
 *
 */
abstract public class BeamMatchMergeGenerator {
	
	protected BeamExpressionTransform etran;
	protected List<UniqueKeyElement> uniqueKeyInfo;
	
	

	public BeamMatchMergeGenerator(BeamExpressionTransform etran, List<UniqueKeyElement> uniqueKeyInfo) {
		this.etran = etran;
		this.uniqueKeyInfo = uniqueKeyInfo;
	}

	abstract public void generate() throws BeamTransformGenerationException;

}
