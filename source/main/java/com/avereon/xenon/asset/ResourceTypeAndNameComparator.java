package com.avereon.xenon.asset;

import com.avereon.xenon.asset.exception.ResourceException;
import lombok.CustomLog;

@CustomLog
public class ResourceTypeAndNameComparator extends ResourceNameComparator {


	@Override
	public int compare( Resource o1, Resource o2 ) {
		try {
			boolean isFolder1 = o1.isFolder();
			boolean isFolder2 = o2.isFolder();
			if( isFolder1 & !isFolder2 ) {
				return -1;
			} else if( !isFolder1 & isFolder2 ) {
				return 1;
			}
		} catch( ResourceException exception ) {
			log.atWarn().withCause( exception ).log();
		}
		return super.compare( o1, o2 );
	}

}
