package com.avereon.xenon.product;

import com.avereon.product.ProductCard;
import com.avereon.product.RepoCard;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;

/**
 * NOTE: This class is Persistent and changing the package will most likely
 * result in a ClassNotFoundException being thrown at runtime.
 *
 * @author SoderquistMV
 */
@Setter
@Getter
public final class ProductUpdate {

	private ProductCard card;

	private RepoCard repo;

	private Path source;

	private Path target;

	/*
	 * This constructor is used by the settings API via reflection.
	 */
	public ProductUpdate() {}

	public ProductUpdate( RepoCard repo, ProductCard card, Path source, Path target ) {
		this.card = card;
		this.repo = repo;
		this.source = source;
		this.target = target;
	}

	@Override
	public int hashCode() {
		return card.hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !(object instanceof ProductUpdate that) ) return false;
		return this.card.equals( that.card );
	}

}
