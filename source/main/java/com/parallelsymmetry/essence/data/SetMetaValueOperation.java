package com.parallelsymmetry.essence.data;

public class SetMetaValueOperation extends Operation {

	private String name;

	private Object oldValue;

	private Object newValue;

	public SetMetaValueOperation( DataNode data, String name, Object oldValue, Object newValue ) {
		super( data );
		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	protected OperationResult process() {
		OperationResult result = new OperationResult( this );

		getData().doSetMetaValue( name, oldValue, newValue );

		DataEvent.Action type = DataEvent.Action.MODIFY;
		type = oldValue == null ? DataEvent.Action.INSERT : type;
		type = newValue == null ? DataEvent.Action.REMOVE : type;
		result.addMetaValueEvent( new MetaAttributeEvent( type, data, name, oldValue, newValue ) );

		return result;
	}

}
