package com.xeomar.xenon.transaction;

import java.util.Comparator;

public class TxnEventComparator implements Comparator<TxnEvent> {

	@Override
	public int compare( TxnEvent event1, TxnEvent event2 ) {
		return event1.compareTo( event2 );
	}

}
