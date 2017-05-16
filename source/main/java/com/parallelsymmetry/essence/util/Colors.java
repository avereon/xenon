package com.parallelsymmetry.essence.util;

import javafx.scene.paint.Color;

public class Colors {

	public static Color mix( Color color, Color mixer, double factor ) {
		if( color == null || mixer == null ) return null;

		double colorR = color.getRed();
		double colorG = color.getGreen();
		double colorB = color.getBlue();
		double colorA = color.getOpacity();

		double mixerR = mixer.getRed();
		double mixerG = mixer.getGreen();
		double mixerB = mixer.getBlue();
		double mixerA = mixer.getOpacity();

		double diffR = mixerR - colorR;
		double diffG = mixerG - colorG;
		double diffB = mixerB - colorB;
		double diffA = mixerA - colorA;

		double r = colorR + (diffR * factor);
		double g = colorG + (diffG * factor);
		double b = colorB + (diffB * factor);
		double a = colorA + (diffA * factor);

		return new Color( r, g, b, a );
	}

	public static Color getShade( Color color, double factor ) {
		if( factor < 0 ) factor = 0;
		if( factor > 1 ) factor = 1;

		double d = Math.abs(0.5-factor )/0.5;

		return mix( color, new Color( factor, factor, factor, color.getOpacity() ), d );
	}

	/**
	 * Get the intensity of the specified color.
	 * <p>
	 * Derived from: http://stackoverflow.com/questions/596216/formula-to-determine-brightness-of-rgb-color
	 *
	 * @param color
	 * @return
	 */
	public static double getLuminance( Color color ) {
		double r = color.getRed();
		double g = color.getGreen();
		double b = color.getBlue();

		//float y = (float)Math.sqrt(0.299 * r*r + 0.587 * g*g + 0.114 * b*b);
		double y = 0.2126f * r + 0.7152f * g + 0.0722f * b;

		return y;
	}

	private static float clamp( float value ) {
		if( value < 0 ) {
			value = 0;
		} else if( value > 1 ) {
			value = 1;
		}
		return value;
	}

	private static int clamp( int value ) {
		if( value < 0 ) {
			value = 0;
		} else if( value > 255 ) {
			value = 255;
		}
		return value;
	}

}

