package com.xeomar.xenon.update;

import com.xeomar.xenon.ProgramProduct;

import java.util.Comparator;

public class CatalogCardComparator  implements Comparator<MarketCard> {

	public enum Field {
		NAME
	}

	private ProgramProduct product;

	private Field field;

	public CatalogCardComparator( ProgramProduct product, Field field  ) {
		this.product = product;
		this.field = field;
	}

	@Override
	public int compare( MarketCard card1, MarketCard card2 ) {
		if( card1.equals( product.getProgram().getMarket() ) ) return -1;
		if( card2.equals( product.getProgram().getMarket() ) ) return 1;

		switch( field ) {
			case NAME: {
				return card1.getName().compareTo( card2.getName() );
			}
			default: {
				return card1.getName().compareTo( card2.getName() );
			}
		}
	}

}
