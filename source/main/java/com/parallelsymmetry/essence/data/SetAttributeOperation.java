package com.parallelsymmetry.essence.data;

class SetAttributeOperation extends Operation {

	private String name;

	private Object oldValue;

	private Object newValue;

	public SetAttributeOperation( DataNode data, String name, Object oldValue, Object newValue ) {
		super( data );
		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	@Override
	protected OperationResult process() {
		OperationResult result = new OperationResult( this );

		getData().doSetDataValue( name, oldValue, newValue );

		DataEvent.Action type = DataEvent.Action.MODIFY;
		type = oldValue == null ? DataEvent.Action.INSERT : type;
		type = newValue == null ? DataEvent.Action.REMOVE : type;
		//result.addEvent( new DataAttributeEvent( type, data, data, name, oldValue, newValue ) );

		return result;
	}

}
