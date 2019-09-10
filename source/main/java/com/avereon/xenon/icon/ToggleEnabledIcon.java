package com.avereon.xenon.icon;

public class ToggleEnabledIcon extends ToggleIcon {

	public ToggleEnabledIcon() {
		super( true );
	}

	public static void main( String[] commands ) {
		proof( new ToggleEnabledIcon() );
	}

}
