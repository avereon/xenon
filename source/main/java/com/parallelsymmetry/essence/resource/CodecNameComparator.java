package com.parallelsymmetry.essence.resource;

import java.util.Comparator;

public class CodecNameComparator implements Comparator<Codec> {

	public CodecNameComparator() {}

	@Override
	public int compare( Codec a, Codec b ) {
		return a.getName().compareTo( b.getName() );
	}

}
