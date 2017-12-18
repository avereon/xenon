package com.xeomar.xenon.update;

import com.xeomar.product.Product;
import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductCardComparator;
import com.xeomar.xenon.ProgramProduct;

import java.util.Comparator;

public class CatalogCardComparator  implements Comparator<CatalogCard> {

	public enum Field {
		KEY, NAME, GROUP, ARTIFACT, RELEASE
	}

	private ProgramProduct product;

	private ProductCardComparator.Field field;


	@Override
	public int compare( CatalogCard card1, CatalogCard card2 ) {
		// NEXT How to compare with the program catalog
		//
//		product.getProgram().getUpdateManager().getProgramCatalogCard();
//		if( card1.equals( product.getCatalogCard() ) ) return -1;
//		if( card2.equals( product.getCatalogCard() ) ) return 1;

		switch( field ) {
//			case KEY: {
//				return card1.getProductKey().compareTo( card2.getProductKey() );
//			}
//			case RELEASE: {
//				return card1.getRelease().compareTo( card2.getRelease() );
//			}
//			case ARTIFACT: {
//				return card1.getArtifact().compareTo( card2.getArtifact() );
//			}
//			case GROUP: {
//				return card1.getGroup().compareTo( card2.getGroup() );
//			}
			case NAME: {
				return card1.getName().compareTo( card2.getName() );
			}
			default: {
				return card1.getName().compareTo( card2.getName() );
			}
		}
	}

}
