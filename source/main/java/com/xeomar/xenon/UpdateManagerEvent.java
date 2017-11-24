package com.xeomar.xenon;

import com.xeomar.product.ProductCard;
import com.xeomar.product.ProductEvent;

public class UpdateManagerEvent extends ProductEvent {

	public enum Type {
		PRODUCT_CHANGED, PRODUCT_ENABLED, PRODUCT_DISABLED, PRODUCT_INSTALLED, PRODUCT_REMOVED, PRODUCT_STAGED, PRODUCT_UPDATED
	}

	private Type type;

	private ProductCard card;

	public UpdateManagerEvent( UpdateManager manager, Type type, ProductCard card ) {
		super( manager );
		this.type = type;
		this.card = card;
	}

	public Type getType() {
		return type;
	}

	public ProductCard getMetadata() {
		return card;
	}

}
