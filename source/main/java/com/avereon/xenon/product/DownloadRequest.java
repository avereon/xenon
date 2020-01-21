package com.avereon.xenon.product;

import com.avereon.product.ProductCard;

import java.util.function.DoubleConsumer;

public class DownloadRequest {

	private ProductCard card;

	private DoubleConsumer progressIndicator;

	public DownloadRequest( ProductCard card ) {
		this( card, ( d ) -> {} );
	}

	public DownloadRequest( ProductCard card, DoubleConsumer progressIndicator ) {
		this.card = card;
		this.progressIndicator = progressIndicator;
	}

	public ProductCard getCard() {
		return card;
	}

	public DoubleConsumer getProgressIndicator() {
		return progressIndicator;
	}

}
