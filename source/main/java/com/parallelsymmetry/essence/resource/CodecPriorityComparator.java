package com.parallelsymmetry.essence.resource;

import java.util.Comparator;

public class CodecPriorityComparator implements Comparator<Codec> {

	@Override
	public int compare( Codec codec1, Codec codec2 ) {
		return codec1.getPriority() - codec2.getPriority();
	}

}
