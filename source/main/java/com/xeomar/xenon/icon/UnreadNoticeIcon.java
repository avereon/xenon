package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;

public class UnreadNoticeIcon extends NoticeIcon {

	public UnreadNoticeIcon() { }

	public void render() {
		setFillPaintColor( Color.GREEN.brighter() );
		super.render();
	}

	public static void main( String[] commands ) {
		proof( new UnreadNoticeIcon() );
	}

}
