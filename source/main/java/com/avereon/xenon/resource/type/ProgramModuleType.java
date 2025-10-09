package com.avereon.xenon.resource.type;

import com.avereon.xenon.Xenon;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceType;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.PlaceholderCodec;
import com.avereon.xenon.scheme.XenonScheme;

@Deprecated
public class ProgramModuleType extends ResourceType {

	private static final String uriPattern = XenonScheme.ID + ":/modules";

	public static final java.net.URI URI = java.net.URI.create( uriPattern );

	public ProgramModuleType( XenonProgramProduct product ) {
		super( product, "product" );

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
	public boolean assetOpen( Xenon program, Resource resource ) {
		resource.setModel( program.getCard() );
		return true;
	}

}
