package com.avereon.xenon.product;

import com.avereon.product.ProductCard;
import org.assertj.core.api.AbstractAssert;

public class ProductCardAssert extends AbstractAssert<ProductCardAssert, ProductCard> {

	public ProductCardAssert( ProductCard product ) {
		super( product, ProductCardAssert.class );
	}

	public static ProductCardAssert assertThat( ProductCard product ) {
		return new ProductCardAssert( product );
	}

	public ProductCardAssert matches( ProductCard expected ) {
		if( !actual.getProductKey().equals( expected.getProductKey() ) ) failWithMessage( "Expected productKey to be %s but was %s", expected.getProductKey(), actual.getProductKey() );
		if( actual.getRelease().compareTo( expected.getRelease() ) != 0 ) failWithMessage( "Expected release to be %s but was %s", expected.getRelease(), actual.getRelease() );
		return this;
	}

}
