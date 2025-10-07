package com.avereon.xenon.asset.exception;

import com.avereon.xenon.asset.Resource;

public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException( Resource resource ) {
        this( resource, null);
    }

    public ResourceNotFoundException( Resource resource, Throwable cause) {
        super( resource, "Asset not found", cause);
    }

}
