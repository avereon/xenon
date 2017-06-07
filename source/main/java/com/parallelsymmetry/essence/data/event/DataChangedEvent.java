package com.parallelsymmetry.essence.data.event;

import com.parallelsymmetry.essence.data.DataEvent;
import com.parallelsymmetry.essence.data.DataNode;

public class DataChangedEvent extends DataEvent {

	public DataChangedEvent( Action action, DataNode sender ) {
		super( DataEvent.Type.DATA_CHANGED, action, sender );
	}

	@Override
	public DataEvent cloneWithNewSender( DataNode sender ) {
		return new DataChangedEvent( getAction(), sender ).setClone( true );
	}

}
