package com.avereon.xenon.asset;

import com.avereon.xenon.asset.exception.AssetException;
import lombok.CustomLog;

@CustomLog
public class AssetTypeAndSizeComparator extends AssetTypeComparator {

	@Override
	public int compare( Asset o1, Asset o2 ) {
		try {
			String t1 = o1.isFolder() ? "d" : "f";
			String t2 = o2.isFolder() ? "d" : "f";
			long z1 = o1.getSize();
			long z2 = o2.getSize();


			if( o1.isFolder() && o2.isFolder() ) return Long.compare( o1.getSize(), o2.getSize() );
			if( !o1.isFolder() && !o2.isFolder() ) return Long.compare( o1.getSize(), o2.getSize() );
			log.atConfig().log( "o1=%s-%s, o2=%s-%s", t1, z1, t2, z2 );
		} catch( AssetException exception ) {
			log.atSevere().withCause( exception ).log();
		}
		return super.compare( o1, o2 );
	}

}
