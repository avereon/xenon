package com.avereon.xenon.tool.settings.editor.paint;

import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.RbKey;
import lombok.Getter;

@Getter
public class PaintMode {

	public static final PaintMode NONE;

	// FIXME This is a Cartesia concept
	public static final PaintMode LAYER;

	public static final PaintMode SOLID;

	public static final PaintMode LINEAR;

	public static final PaintMode RADIAL;

	public static final PaintMode DEFAULT_PAINT_MODE;

	private final String key;

	private final String label;

	static {
		NONE = new PaintMode( "none", Rb.text( RbKey.LABEL, "none" ) );
		LAYER = new PaintMode( "layer", Rb.text( RbKey.LABEL, "layer" ) );
		LINEAR = new PaintMode( "linear", Rb.text( RbKey.LABEL, "linear" ) );
		RADIAL = new PaintMode( "radial", Rb.text( RbKey.LABEL, "radial" ) );
		SOLID = new PaintMode( "solid", Rb.text( RbKey.LABEL, "solid" ) );
		DEFAULT_PAINT_MODE = SOLID;
	}

	public PaintMode( String key, String label ) {
		this.key = key;
		this.label = label;
	}

	public static PaintMode parse(String name ) {
		return switch( name ) {
			case "none" -> NONE;
			case "layer" -> LAYER;
			case "linear" -> LINEAR;
			case "radial" -> RADIAL;
			case "solid" -> SOLID;
			default -> throw new IllegalArgumentException( "Unknown paint mode: " + name );
		};
	}

	public static PaintMode getPaintMode( String paint ) {
		if( TextUtil.isEmpty( paint ) ) return PaintMode.NONE;

		if( PaintMode.LAYER.getKey().equals( paint ) ) return PaintMode.LAYER;
		if( paint.startsWith( "0x" ) ) return SOLID;

		return switch( paint.charAt( 0 ) ) {
			case '#' -> PaintMode.SOLID;
			case '[' -> PaintMode.LINEAR;
			case '(' -> PaintMode.RADIAL;
			default -> throw new IllegalStateException( "Unexpected paint mode: " + paint );
		};
	}

	@Override
	public String toString() {
		return getLabel();
	}

}
