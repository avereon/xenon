package com.avereon.xenon.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class Images {

	public static Image resample( Image input, int scale ) {
		int w = (int)input.getWidth();
		int h = (int)input.getHeight();

		WritableImage output = new WritableImage( w * scale, h * scale );

		PixelReader reader = input.getPixelReader();
		PixelWriter writer = output.getPixelWriter();

		for( int y = 0; y < h; y++ ) {
			for( int x = 0; x < w; x++ ) {
				final int argb = reader.getArgb( x, y );
				for( int dy = 0; dy < scale; dy++ ) {
					for( int dx = 0; dx < scale; dx++ ) {
						writer.setArgb( x * scale + dx, y * scale + dy, argb );
					}
				}
			}
		}

		return output;
	}

}
