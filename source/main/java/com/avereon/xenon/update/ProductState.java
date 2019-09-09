package com.avereon.xenon.update;

final class ProductState {

	private boolean updatable;

	private boolean removable;

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

	public ProductStatus getStatus() {
		return status;
	}

	public void setStatus( ProductStatus status ) {
		this.status = status;
	}

}
