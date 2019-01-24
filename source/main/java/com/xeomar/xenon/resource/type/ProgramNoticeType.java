package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.util.LogUtil;
import com.xeomar.xenon.notice.NoticeList;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceType;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;

public class ProgramNoticeType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:notice" );

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	public ProgramNoticeType( Product product ) {
		super( product, "notice" );
		setDefaultCodec( new NoticeCodec() );
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
			return true;
		}

		@Override
		public boolean canSave() {
			return true;
		}

		@Override
		public void load( Resource resource, InputStream input ) throws IOException {
			NoticeList notices = new NoticeList();

			log.trace( "Load program notices..." );
			// TODO How do I want to store the notices? In settings? In a folder as separate files? As a single file?
			// TODO Remove old notices...

			notices.setModified( false );
			resource.setModel( notices );
		}

		@Override
		public void save( Resource resource, OutputStream output ) throws IOException {
			NoticeList notices = resource.getModel();

			log.trace( "Save program notices..." );

			resource.setModified( false );
		}

	}

}
