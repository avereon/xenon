package com.avereon.xenon.product;

import com.avereon.product.ProductCard;

final class ProductState {

	private boolean updatable;

	private boolean removable;

	private ProductCard update;

	private ProductStatus status;

	ProductState() {
		this.updatable = false;
		this.removable = false;
	}

	public boolean isUpdatable() {
		return updatable;
	}

	public void setUpdatable( boolean updatable ) {
		this.updatable = updatable;
	}

	public boolean isRemovable() {
		return removable;
	}

	public void setRemovable( boolean removable ) {
		this.removable = removable;
	}

	public ProductCard getUpdate() {
		return update;
	}

	public void setUpdate( ProductCard update ) {
		this.update = update;
	}

	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus( ProductStatus status ) {
		this.status = status;
	}

}
