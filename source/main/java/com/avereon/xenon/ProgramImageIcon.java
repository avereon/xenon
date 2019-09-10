package com.avereon.xenon;

import javafx.scene.image.Image;

public class ProgramImageIcon extends ProgramIcon {

	private Image image;

	public ProgramImageIcon() {}

	public ProgramImageIcon setImage( Image image ) {
		this.image = image;
		fireRender();
		return this;
	}

	@Override
	protected void render() {
		if( image != null ) drawImage( image, 0, 0 );
	}

	public static void main( String[] commands ) {
		ProgramImageIcon icon = new ProgramImageIcon();
		proof( icon );
		icon.setImage( new Image( "https://www.avereon.com/download/latest/xenon/product/icon" ) );
	}

}
