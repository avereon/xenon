package com.xeomar.xenon.icon;

import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;

public class NoticeUnread extends ExclamationIcon {

	public NoticeUnread() {
		super();
	}

	@Override
	protected void render() {
		Color color = Color.rgb( 192, 0, 0 );
		setFillPaint( linearPaint( 0, 0, 1, 1, new Stop( 0, Color.WHITE ), new Stop( 1, color ) ) );
		fillOval( g(1),g(1), g(30),g(30) );
		setDrawPaint( color );
		drawOval( g(1),g(1), g(30),g(30) );

		setFillPaint( linearPaint( 0, 0, 1, 1, new Stop( 0, Color.WHITE ), new Stop( 1, Color.YELLOW ) ) );
		setDrawPaint( Color.YELLOW );
		super.render();
	}

}
