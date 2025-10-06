package com.avereon.xenon.asset;

import com.avereon.xenon.asset.exception.ResourceException;
import lombok.CustomLog;

@CustomLog
public class AssetTypeComparator extends AssetNameComparator {

	@Override
	public int compare( Asset o1, Asset o2 ) {
		try {
			if( o1.isFolder() && !o2.isFolder() ) return -1;
			if( !o1.isFolder() && o2.isFolder() ) return 1;
		} catch( ResourceException exception ) {
			log.atWarn().withCause( exception ).log();
		}
		return super.compare( o1, o2 );
	}

}
