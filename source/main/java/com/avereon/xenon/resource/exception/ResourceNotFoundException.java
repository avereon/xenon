package com.avereon.xenon.resource.exception;

import com.avereon.xenon.resource.Resource;

public class ResourceNotFoundException extends ResourceException {

    public ResourceNotFoundException( Resource resource ) {
        this( resource, null);
    }

    public ResourceNotFoundException( Resource resource, Throwable cause) {
        super( resource, "Asset not found", cause);
    }

}
