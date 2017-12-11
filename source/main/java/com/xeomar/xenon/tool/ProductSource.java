package com.xeomar.xenon.tool;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.update.UpdateManager;

/**
 * This class is used for state information in the ArtifactTool class.
 * 
 * @author Mark Soderquist
 */
public class ProductSource {

	private ProductCard card;

	private boolean enabled;

	private boolean removable;

	public ProductSource() {}

	public ProductSource( ProductCard card) {
		this.card = card;
	}

	public ProductCard getCard() {
		return card;
	}

	public void setCard( ProductCard card ) {
		this.card = card;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public boolean isRemovable() {
		return removable;
	}

	public void setRemovable( boolean removable ) {
		this.removable = removable;
	}

	public static ProductSource create( UpdateManager manager, ProductCard card ) {
		ProductSource source = new ProductSource(  );
		source.setCard( card );
		source.setEnabled( manager.isEnabled( card ) );
		source.setRemovable( manager.isRemovable( card ) );
		return source;
	}

}
