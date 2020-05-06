package com.avereon.xenon.asset.type;

import com.avereon.util.Log;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.product.ModEvent;

import java.lang.System.Logger;

public class ProgramAboutType extends AssetType {

	public static final String MEDIA_TYPE = "application/vnd.avereon.xenon.program.about";

	public static final java.net.URI URI = java.net.URI.create( "program:about" );

	private static final Logger log = Log.get();

	public ProgramAboutType( ProgramProduct product ) {
		super( product, "about" );
	}

	@Override
	public String getKey() {
		return MEDIA_TYPE;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	/**
	 * There are no codecs for this asset type so this method always returns null.
	 *
	 * @return null
	 */
	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	@Override
	public boolean assetInit( Program program, Asset asset ) {
		// FIXME What is the about type asset model and how is it "updated"?
		// Arguably "the program" is the asset model for the about data type. But
		// that is a pretty big model. Not only that but the about tool needs to
		// watch for changes in several things as well as things that do not
		// produce events, like JVM information. In that case a timer will have to
		// be created to update the information at a "regular" rate. If the update
		// rate is sufficiently fast then listening for events on event drive items
		// is "less" helpful even though it is an encouraged pattern. Maybe the
		// answer is use events when possible and use polling when needed. Can
		// polling be setup and used in an event driven manner?
		asset.setModel( getProduct().getCard() );

		// FIXME These should be moved to the about tool
		program.register( ModEvent.ENABLED, e -> asset.refresh() );
		program.register( ModEvent.DISABLED, e -> asset.refresh() );

		return true;
	}

}
