package com.avereon.xenon.asset;

import com.avereon.xenon.asset.exception.ResourceException;
import lombok.CustomLog;

@CustomLog
public class AssetTypeAndSizeComparator extends AssetTypeComparator {

	@Override
	public int compare( Asset o1, Asset o2 ) {
		try {
			if( o1.isFolder() && o2.isFolder() ) return Long.compare( o1.getSize(), o2.getSize() );
			if( !o1.isFolder() && !o2.isFolder() ) return Long.compare( o1.getSize(), o2.getSize() );
		} catch( ResourceException exception ) {
			log.atSevere().withCause( exception ).log();
		}
		return super.compare( o1, o2 );
	}

}
