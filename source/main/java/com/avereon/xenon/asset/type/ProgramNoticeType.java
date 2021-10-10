package com.avereon.xenon.asset.type;

import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetType;
import com.avereon.xenon.asset.Codec;
import com.avereon.xenon.notice.NoticeModel;
import lombok.CustomLog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@CustomLog
public class ProgramNoticeType extends AssetType {

	private static final String mediaTypePattern = "application/vnd.avereon.xenon.program.notice";

	private static final String uriPattern = "program:/notice";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramNoticeType( ProgramProduct product ) {
		super( product, "notice" );
		setDefaultCodec( new ProgramNoticeCodec() );
	}

	@Override
	public boolean assetOpen( Program program, Asset asset ) throws AssetException {
		asset.setModel( new NoticeModel() );
		return true;
	}

	@Override
	public String getKey() {
		return uriPattern;
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	private static class ProgramNoticeCodec extends Codec {

		public ProgramNoticeCodec() {
			addSupported( Pattern.URI, uriPattern );
			addSupported( Pattern.MEDIATYPE, mediaTypePattern );
		}

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
			NoticeModel notices = new NoticeModel();

			log.atTrace().log( "Load program notices..." );
			// TODO How do I want to store the notices? In settings? In a folder as separate files? As a single file?
			// TODO Remove old notices...

			notices.setModified( false );
			asset.setModel( notices );
		}

		@Override
		public void save( Asset asset, OutputStream output ) throws IOException {
			NoticeModel notices = asset.getModel();

			log.atTrace().log( "Save program notices..." );

			asset.setModified( false );
		}

	}

}
