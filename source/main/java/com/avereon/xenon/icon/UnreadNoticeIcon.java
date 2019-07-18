package com.avereon.xenon.icon;

import com.avereon.xenon.ColorTheme;
import javafx.scene.paint.Color;

public class UnreadNoticeIcon extends NoticeIcon {

	public UnreadNoticeIcon() {
		setColorTheme( new ColorTheme( Color.GREEN.brighter() ) );
	}

	public static void main( String[] commands ) {
		proof( new UnreadNoticeIcon() );
	}

}
