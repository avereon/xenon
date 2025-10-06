package com.avereon.xenon.asset.exception;

import com.avereon.xenon.asset.Asset;

public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException( Asset asset) {
        this(asset, null);
    }

    public ResourceNotFoundException( Asset asset, Throwable cause) {
        super(asset, "Asset not found", cause);
    }

}
