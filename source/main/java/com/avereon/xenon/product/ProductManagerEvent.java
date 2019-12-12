package com.avereon.xenon.product;

import com.avereon.product.ProductCard;
import com.avereon.product.ProductEvent;

public class ProductManagerEvent extends ProductEvent {

	public enum Type {
		MOD_INSTALLED,
		MOD_REGISTERED,
		MOD_ENABLED,
		MOD_STARTED,

		MOD_STOPPED,
		MOD_DISABLED,
		MOD_UNREGISTERED,
		MOD_REMOVED,

		PRODUCT_STAGED
	}

	private Type type;

	private ProductCard card;

	public ProductManagerEvent( ProductManager manager, Type type, ProductCard card ) {
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

	public String toString() {
		return super.toString() + ":" + getType() + ":" + getMetadata().toString();
	}

}
