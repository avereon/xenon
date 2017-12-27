package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.ProgramSettings;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.tool.guide.Guide;
import javafx.scene.control.TreeItem;

import java.net.URI;

public class ProgramSettingsType extends ResourceType {

	public static final URI uri = URI.create( "program:settings" );

	public static final String KEY = "settings";

	public ProgramSettingsType( Product product ) {
		super( product, KEY );
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
		resource.setModel( program.getSettingsManager().getSettings( ProgramSettings.PROGRAM ) );
		Guide guide = new Guide();
		guide.setRoot( new TreeItem<>() );
		resource.putResource( Guide.GUIDE_KEY, guide);
		return true;
	}

}
