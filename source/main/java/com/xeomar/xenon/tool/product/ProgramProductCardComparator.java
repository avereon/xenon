package com.xeomar.xenon.tool.product;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;

public class ProgramProductCardComparator extends ProductCardComparator {

	private Product product;

	public ProgramProductCardComparator( Product product, Field field ) {
		super( field );
		this.product = product;
	}

	@Override
	public int compare( ProductCard card1, ProductCard card2 ) {
		if( card1.equals( product.getCard() ) ) return -1;
		if( card2.equals( product.getCard() ) ) return 1;
		return super.compare( card1, card2 );
	}

}
