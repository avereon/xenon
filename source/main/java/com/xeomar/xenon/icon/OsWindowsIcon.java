package com.xeomar.xenon.icon;

public class OsWindowsIcon extends ImageIcon {

	public OsWindowsIcon() {
		super( "/icons/windows.png" );
	}

	public static void main( String[] commands ) {
		proof( new OsWindowsIcon() );
	}

}
