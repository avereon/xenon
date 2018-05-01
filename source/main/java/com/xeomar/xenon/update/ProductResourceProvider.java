package com.xeomar.xenon.update;

import java.net.URI;
import java.util.Set;

public interface ProductResourceProvider {

	Set<ProductResource> getResources( URI codebase ) throws Exception;

}
