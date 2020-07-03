package com.avereon.xenon.asset;

import java.io.InputStream;
import java.io.OutputStream;

public class PlaceholderCodec extends Codec {

	@Override
	public String getKey() {
		return "placeholder";
	}

	@Override
	public String getName() {
		return "placeholder";
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
	public void load( Asset asset, InputStream input ) {}

	@Override
	public void save( Asset asset, OutputStream output ) {}

}
