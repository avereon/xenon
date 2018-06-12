package com.xeomar.xenon.icon;

public class OsMacIcon extends  ImageIcon {

	public OsMacIcon() {
		super( "/icons/macosx.png" );
	}

	public static void main( String[] commands ) {
		proof( new OsMacIcon() );
	}

}
