package com.avereon.xenon.resource;

import java.util.Comparator;

public class ResourceNameComparator implements Comparator<Resource> {

	@Override
	public int compare( Resource o1, Resource o2 ) {
		return o1.getName().compareTo( o2.getName() );
	}

}
