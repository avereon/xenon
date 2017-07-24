package com.parallelsymmetry.essence.resource.type;

import com.parallelsymmetry.essence.IconLibrary;
import com.parallelsymmetry.essence.Program;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductBundle;
import com.parallelsymmetry.essence.resource.Codec;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceException;
import com.parallelsymmetry.essence.resource.ResourceType;
import com.parallelsymmetry.essence.tool.Guide;
import javafx.scene.control.TreeItem;

public class ProductAboutType extends ResourceType {

	public ProductAboutType( Product product ) {
		super( product, "about" );
	}

	/**
	 * There are no codecs for this resource type so this method always returns null.
	 *
	 * @return null
	 */
	@Override
	public Codec getDefaultCodec() {
		return null;
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		resource.setModel( getProduct().getMetadata() );
		resource.putResource( Guide.GUIDE_KEY, createGuide( program ) );
		return true;
	}

	private Guide createGuide( Program program ) {
		IconLibrary library = program.getIconLibrary();
		ProductBundle rb = getProduct().getResourceBundle();

		TreeItem<String> summaryItem = new TreeItem<String>( rb.getString( "tool", "about-summary" ) );
		summaryItem.setGraphic( library.getIcon( "about" ) );

		TreeItem<String> productsItem = new TreeItem<String>( rb.getString( "tool", "about-products" ) );
		productsItem.setGraphic( library.getIcon( "about" ) );

		TreeItem<String> detailsItem = new TreeItem<String>( rb.getString( "tool", "about-details" ) );
		detailsItem.setGraphic( library.getIcon( "about" ) );

		TreeItem<String> root = new TreeItem<>( "root" );
		root.setGraphic( library.getIcon( "about" ) );
		root.getChildren().add( summaryItem );
		root.getChildren().add( productsItem );
		root.getChildren().add( detailsItem );

		// Create the guide
		Guide guide = new Guide();
		guide.setRoot( root );
		return guide;
	}

}
