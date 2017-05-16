package io.konig.transform.assembly;

public interface TransformEvent {

	TransformEventType getType();
	Blackboard getBlackboard();

}
