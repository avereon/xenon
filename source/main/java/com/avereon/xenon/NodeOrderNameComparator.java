package com.avereon.xenon;

import com.avereon.data.Node;

import java.util.Comparator;

public class NodeOrderNameComparator<T extends Node> implements Comparator<T> {

	public static final String ORDER = "order";

	public static final String NAME = "name";

	Comparator<T> byOrder = Comparator.comparingInt( o -> o.getValue( ORDER ) == null ? -1 : o.getValue( ORDER ) );

	Comparator<T> byName = Comparator.comparing( o -> o.getValue( NAME ) == null ? "" : o.getValue( NAME ) );

	Comparator<T> byOrderThenByName = byOrder.thenComparing( byName );

	@Override
	public int compare( T o1, T o2 ) {
		return byOrderThenByName.compare( o1, o2 );
	}

}
