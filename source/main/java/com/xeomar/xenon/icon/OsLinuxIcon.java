package com.xeomar.xenon.icon;

public class OsLinuxIcon extends ImageIcon {

	public OsLinuxIcon() {
		super( "/icons/linux.png" );
	}

	public static void main( String[] commands ) {
		proof( new OsLinuxIcon() );
	}

}
