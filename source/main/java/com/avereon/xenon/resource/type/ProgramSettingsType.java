package com.avereon.xenon.resource.type;

import com.avereon.product.Product;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramSettings;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceException;
import com.avereon.xenon.resource.ResourceType;

public class ProgramSettingsType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:settings" );

	public ProgramSettingsType( Product product ) {
		super( product, "settings" );
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
		return true;
	}

}
