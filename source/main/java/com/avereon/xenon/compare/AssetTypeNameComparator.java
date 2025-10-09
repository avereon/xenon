package com.avereon.xenon.compare;

import com.avereon.xenon.resource.ResourceType;

import java.util.Comparator;

public class AssetTypeNameComparator implements Comparator<ResourceType> {

	@Override
	public int compare( ResourceType o1, ResourceType o2 ) {
		return o1.getName().compareTo( o2.getName() );
	}

}
