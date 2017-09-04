package com.xeomar.xenon.resource.type;

import com.xeomar.xenon.Program;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.tool.Guide;
import javafx.scene.control.TreeItem;

public class ProgramSettingsType extends ResourceType {

	public static final String URI = "program:settings";

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
		resource.setModel( program.getSettingsManager().getProgramSettings() );
		Guide guide = new Guide();
		guide.setRoot( new TreeItem<>() );
		resource.putResource( Guide.GUIDE_KEY, guide);
		return true;
	}

}
