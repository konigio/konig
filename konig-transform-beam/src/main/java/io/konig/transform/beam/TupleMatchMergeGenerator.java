package io.konig.transform.beam;

import java.util.List;

import io.konig.core.showl.UniqueKeyElement;

/**
 * A BeamMatchMergeGenerator that handles the case where the unique key is a tuple (i.e. not a single value)
 * @author Greg McFall
 *
 */
public class TupleMatchMergeGenerator extends BeamMatchMergeGenerator {

	public TupleMatchMergeGenerator(BeamExpressionTransform etran, List<UniqueKeyElement> uniqueKeyInfo) {
		super(etran, uniqueKeyInfo);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void generate() throws BeamTransformGenerationException {
		// TODO Auto-generated method stub

	}

}
