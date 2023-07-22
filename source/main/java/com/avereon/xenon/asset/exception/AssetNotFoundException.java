package com.avereon.xenon.asset.exception;

import com.avereon.xenon.asset.Asset;

public class AssetNotFoundException extends AssetException {

    public AssetNotFoundException(Asset asset) {
        this(asset, null);
    }

    public AssetNotFoundException(Asset asset, Throwable cause) {
        super(asset, "Asset not found", cause);
    }

}
