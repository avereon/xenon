package com.avereon.xenon.compare;

import com.avereon.xenon.asset.AssetType;

import java.util.Comparator;

public class AssetTypeNameComparator implements Comparator<AssetType> {

	@Override
	public int compare( AssetType o1, AssetType o2 ) {
		return o1.getName().compareTo( o2.getName() );
	}

}
