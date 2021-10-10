package com.avereon.xenon.test.product;

import com.avereon.product.ProductCard;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

class ProductMatcher extends TypeSafeMatcher<ProductCard> {

	private ProductCard expected;

	private ProductMatcher( ProductCard expected ) {
		this.expected = expected;
	}

	@Override
	protected boolean matchesSafely( ProductCard actual ) {
		return actual.getProductKey().equals( expected.getProductKey() ) && actual.getRelease().compareTo( expected.getRelease() ) == 0;
	}

	@Override
	public void describeTo( Description description ) {
		description.appendValue( expected + ":" + expected.getRelease() );
	}

	@Override
	protected void describeMismatchSafely( ProductCard actual, Description mismatchDescription ) {
		mismatchDescription.appendText( "found " ).appendValue( actual + ":" + actual.getRelease() );
	}

	static ProductMatcher matches( ProductCard card ) {
		return new ProductMatcher( card );
	}

}
