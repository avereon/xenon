package com.parallelsymmetry.essence.data;

public class MetaAttributeEvent extends DataEvent {

	private String name;

	private Object newValue;

	private Object oldValue;

	public MetaAttributeEvent( DataEvent.Action action, DataNode sender, String name, Object oldValue, Object newValue ) {
		super( DataEvent.Type.META_ATTRIBUTE, action, sender );
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
		return new MetaAttributeEvent( getAction(), sender, name, oldValue, newValue ).setClone( true );
	}

}
