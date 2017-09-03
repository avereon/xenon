package com.xeomar.xenon.resource.type;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.tool.Guide;

public class ProgramSettingsType extends ResourceType {

	public static final String URI = "program:settings";

	public ProgramSettingsType( Product product ) {
		super( product, "settings" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		resource.setModel( program.getSettingsManager().getProgramSettings() );
		resource.putResource( Guide.GUIDE_KEY, new Guide<>() );
		return true;
	}

}
