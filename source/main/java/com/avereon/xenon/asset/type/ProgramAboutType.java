package com.avereon.xenon.asset.type;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.PlaceholderCodec;
import com.avereon.xenon.scheme.XenonScheme;

public class ProgramAboutType extends AssetType {

	private static final String uriPattern = XenonScheme.ID + ":/about";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramAboutType( XenonProgramProduct product ) {
		super( product, "about" );

		PlaceholderCodec codec = new PlaceholderCodec();
		codec.addSupported( Codec.Pattern.URI, uriPattern );
		setDefaultCodec( codec );
	}

	@Override
	public String getKey() {
		return uriPattern;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	@Override
	public boolean assetOpen( Xenon program, Asset asset ) {
		// Arguably "the program" is the asset model for the about data type. But
		// that is a pretty big model. Not only that but the about tool needs to
		// watch for changes in several things as well as things that do not
		// produce events, like JVM information. In that case a timer will have to
		// be created to update the information at a "regular" rate. If the update
		// rate is sufficiently fast then listening for events on event drive items
		// is "less" helpful even though it is an encouraged pattern. Maybe the
		// answer is use events when possible and use polling when needed. Can
		// polling be setup and used in an event driven manner?
		asset.setModel( program );
		return true;
	}

}
