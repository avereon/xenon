package com.avereon.xenon;

import javafx.scene.image.Image;

public class ProgramImageIcon extends ProgramIcon {

	private Image image;

	public ProgramImageIcon() {}

	public ProgramImageIcon setRenderImage( Image image ) {
		this.image = image;
		fireRender();
		return this;
	}

	public Image getRenderImage() {
		return this.image;
	}

	@Override
	protected void render() {
		if( image != null ) drawImage( image, 0, 0 );
	}

	public static void main( String[] commands ) {
		ProgramImageIcon icon = new ProgramImageIcon();
		proof( icon );
		icon.setRenderImage( new Image( "https://www.avereon.com/download/latest/xenon/product/icon" ) );
	}

}
