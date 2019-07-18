package com.avereon.xenon.update;

import com.avereon.product.ProductCard;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Set;

/**
 * @deprecated In favor of using the product tool to select updates
 */
@Deprecated
public class UpdatesPanel extends Pane {

	private Set<ProductCard> installedPacks;

	private Set<ProductCard> postedUpdates;

	public UpdatesPanel( Set<ProductCard> installedPacks, Set<ProductCard> postedUpdates) {
		this.installedPacks = installedPacks;
		this.postedUpdates = postedUpdates;

		// TODO Generate a layout to select updates
	}


	public Set<ProductCard> getSelectedUpdates() {
		Set<ProductCard> selectedUpdates = new HashSet<>();

		// TODO Determine the selected updates

		return selectedUpdates;
	}

}
