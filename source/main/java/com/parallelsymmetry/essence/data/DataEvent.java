package com.parallelsymmetry.essence.data;

public abstract class DataEvent {

	public enum Type {
		DATA_CHANGED, META_ATTRIBUTE, DATA_ATTRIBUTE, DATA_CHILD
	}

	public enum Action {
		MODIFY, INSERT, REMOVE
	}

	private Type type;

	private Action action;

	private boolean isClone;

	private DataNode sender;

	public DataEvent( Type type, Action action, DataNode sender ) {
		this.type = type;
		this.action = action;
		this.sender = sender;
	}

	public Type getType() {
		return type;
	}

	public Action getAction() {
		return action;
	}

	public DataNode getSender() {
		return sender;
	}

	public boolean isClone() {
		return isClone;
	}

	DataEvent setClone( boolean isClone ) {
		this.isClone = isClone;
		return this;
	}

	public abstract DataEvent cloneWithNewSender( DataNode parent );

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append( getClass().getSimpleName() );
		builder.append( "  type: " );
		builder.append( type.name() );
		builder.append( "  action: " );
		builder.append( action.name() );
		builder.append( "  sender: " );
		builder.append( sender.toString() );

		return builder.toString();
	}

}
