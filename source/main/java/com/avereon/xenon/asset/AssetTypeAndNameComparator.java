package com.avereon.xenon.asset;

import com.avereon.util.Log;

public class AssetTypeAndNameComparator extends AssetNameComparator {

	private static final System.Logger log = Log.get();

	@Override
	public int compare( Asset o1, Asset o2 ) {
		try {
			boolean isFolder1 = o1.isFolder();
			boolean isFolder2 = o2.isFolder();
			if( isFolder1 & !isFolder2 ) {
				return -1;
			} else if( !isFolder1 & isFolder2 ) {
				return 1;
			}
		} catch( AssetException exception ) {
			log.log( Log.ERROR, exception );
		}
		return super.compare( o1, o2 );
	}

}
