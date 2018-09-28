package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramSettings;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.tool.guide.Guide;

public class ProgramSettingsType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:settings" );

	public static final String KEY = "settings";

	public ProgramSettingsType( Product product ) {
		super( product, KEY );
	}

	/**
	 * @see #isUserType()
	 */
	@Override
	public boolean isUserType() {
		return false;
	}

	/**
	 * @see #getDefaultCodec()
	 */
	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	/**
	 * @see #resourceDefault(Program, Resource)
	 */
	@Override
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		resource.setModel( program.getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		resource.putResource( Guide.GUIDE_KEY, new Guide());
		return true;
	}

}
