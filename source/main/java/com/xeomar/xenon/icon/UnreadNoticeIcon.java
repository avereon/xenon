package com.xeomar.xenon.icon;

import com.xeomar.xenon.ColorTheme;
import javafx.scene.paint.Color;

public class UnreadNoticeIcon extends NoticeIcon {

	public UnreadNoticeIcon() {
		setColorTheme( new ColorTheme( Color.GREEN.brighter() ) );
	}

	public static void main( String[] commands ) {
		proof( new UnreadNoticeIcon() );
	}

}
