package com.xeomar.xenon;

import com.xeomar.xenon.icon.ImageIcon;
import com.xeomar.xenon.icon.OsLinuxIcon;

public class JavaIcon extends ImageIcon {

	public JavaIcon() {
		super( "/icons/java.png" );
	}

	public static void main( String[] commands ) {
		proof( new JavaIcon() );
	}

}
