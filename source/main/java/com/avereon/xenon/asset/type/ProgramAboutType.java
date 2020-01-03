package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.product.ModEvent;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

public class ProgramAboutType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:about" );

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private static final String WATCHER_KEY = "program.asset.watcher";

	public ProgramAboutType( Product product ) {
		super( product, "about" );
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
	public boolean assetInit( Program program, Asset asset ) throws AssetException {
		asset.setModel( getProduct().getCard() );

		program.getEventBus().register( ModEvent.ENABLED, e -> asset.refresh() );
		program.getEventBus().register( ModEvent.DISABLED, e -> asset.refresh() );

		return true;
	}

}
