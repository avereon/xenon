package com.avereon.xenon.asset.type;

import com.avereon.product.Product;
import com.avereon.util.Log;
import com.avereon.xenon.Program;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.notice.NoticeList;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetType;
import java.lang.System.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;

public class ProgramNoticeType extends AssetType {

	public static final java.net.URI URI = java.net.URI.create( "program:notice" );

	private static final Logger log = Log.log();

	public ProgramNoticeType( Product product ) {
		super( product, "notice" );
		setDefaultCodec( new NoticeCodec() );
	}

	@Override
	public boolean assetInit( Program program, Asset asset ) throws AssetException {
		asset.setModel( new NoticeList() );
		return true;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	private class NoticeCodec extends Codec {

		@Override
		public String getKey() {
			return getClass().getName();
		}

		@Override
		public String getName() {
			return "Program Notice";
		}

		@Override
		public boolean canLoad() {
			return false;
		}

		@Override
		public boolean canSave() {
			return false;
		}

		@Override
		public void load( Asset asset, InputStream input ) throws IOException {
			NoticeList notices = new NoticeList();

			log.log( Log.TRACE,  "Load program notices..." );
			// TODO How do I want to store the notices? In settings? In a folder as separate files? As a single file?
			// TODO Remove old notices...

			notices.setModified( false );
			asset.setModel( notices );
		}

		@Override
		public void save( Asset asset, OutputStream output ) throws IOException {
			NoticeList notices = asset.getModel();

			log.log( Log.TRACE,  "Save program notices..." );

			asset.setModified( false );
		}

	}

}
