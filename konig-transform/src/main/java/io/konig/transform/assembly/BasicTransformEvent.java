package io.konig.transform.assembly;

public class BasicTransformEvent implements TransformEvent {
	
	private Blackboard board;
	private TransformEventType eventType;
	
	

	public BasicTransformEvent(TransformEventType eventType, Blackboard board) {
		this.eventType = eventType;
		this.board = board;
	}

	@Override
	public TransformEventType getType() {
		return eventType;
	}

	@Override
	public Blackboard getBlackboard() {
		return board;
	}

}
