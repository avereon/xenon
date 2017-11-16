package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.ResourceType;

public class ProgramNoticeType extends ResourceType {

	public static final String URI = "program:notice";

	public ProgramNoticeType( Product product ) {
		super( product, "notice" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	// This resource type does not have a codec
	public Codec getDefaultCodec() {
		return null;
	}

	// This resource type does not have a codec
	public void setDefaultCodec( Codec codec ) {}

}
