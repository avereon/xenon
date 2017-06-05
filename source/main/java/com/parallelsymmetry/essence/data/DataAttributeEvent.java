package com.parallelsymmetry.essence.data;

public class DataAttributeEvent extends DataValueEvent {

	private String name;

	private Object newValue;

	private Object oldValue;

	public DataAttributeEvent( DataEvent.Action action, DataNode sender, DataNode cause, String name, Object oldValue, Object newValue ) {
		super( DataEvent.Type.DATA_ATTRIBUTE, action, sender, cause );
		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public String getAttributeName() {
		return name;
	}

	public Object getOldValue() {
		return oldValue;
	}

	public Object getNewValue() {
		return newValue;
	}

	@Override
	public DataEvent cloneWithNewSender( DataNode sender ) {
		return new DataAttributeEvent( getAction(), sender, getCause(), name, oldValue, newValue ).setClone( true );
	}

}
