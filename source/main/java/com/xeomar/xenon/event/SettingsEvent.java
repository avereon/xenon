package com.xeomar.xenon.event;

import com.xeomar.product.ProductEvent;

public abstract class SettingsEvent extends ProductEvent {

	public SettingsEvent( Object source ) {
		super( source );
	}

}
