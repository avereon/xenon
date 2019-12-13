package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.product.ProductEvent;
import com.avereon.product.ProductEventListener;
import com.avereon.util.LogUtil;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.product.ProductManagerEvent;
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
	public boolean assetDefault( Program program, Asset asset ) throws AssetException {
		asset.setModel( getProduct().getCard() );

		// FIXME I'm not sure I like this implementation but it works
		// Not sure if using a listener for this singleton instance is how I
		// want to handle the asset/model/tool relationship.
		ProductEventWatcher watcher = asset.getResource( WATCHER_KEY );
		if( watcher == null ) {
			watcher = new ProductEventWatcher( program, asset );
			asset.putResource( WATCHER_KEY, watcher );
			program.addEventListener( watcher );
		}

		return true;
	}

	private static class ProductEventWatcher implements ProductEventListener<ProductEvent> {

		private Program program;

		private Asset asset;

		private ProductEventWatcher( Program program, Asset asset ) {
			this.program = program;
			this.asset = asset;
		}

		@Override
		public void handleEvent( ProductEvent event ) {
			if( event instanceof ProductManagerEvent  ) {
				asset.refresh( program.getAssetManager() );
			}
		}

	}

}