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
import com.parallelsymmetry.essence.tool.GuideNode;
import javafx.scene.control.TreeItem;

import java.util.Map;

public class ProgramAboutType extends ResourceType {

	private Map<TreeItem<String>, String> pages;

	public ProgramAboutType( Product product ) {
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
		resource.putResource( Guide.GUIDE_KEY, new Guide<>() );
		updateGuide( program, resource );
		return true;
	}

	private void updateGuide( Program program, Resource resource ) {
		Guide<GuideNode> guide = resource.getResource( Guide.GUIDE_KEY );

		TreeItem<GuideNode> root = guide.getRoot();
		if( root != null ) return;

		IconLibrary library = program.getIconLibrary();
		ProductBundle rb = getProduct().getResourceBundle();

		GuideNode summaryNode = new GuideNode();
		summaryNode.setId( "summary" );
		summaryNode.setName( rb.getString( "tool", "about-summary" ) );

		GuideNode productsNode = new GuideNode();
		productsNode.setId( "products");
		productsNode.setName( rb.getString( "tool", "about-products" ) );

		GuideNode detailsNode = new GuideNode();
		detailsNode.setId( "details" );
		detailsNode.setName( rb.getString( "tool", "about-details" ) );

		guide.setRoot( root = new TreeItem<>( new GuideNode(), library.getIcon( "about" ) ) );
		root.getChildren().add( new TreeItem<>( summaryNode, library.getIcon( "about" ) ) );
		root.getChildren().add( new TreeItem<>( productsNode, library.getIcon( "about" ) ) );
		root.getChildren().add( new TreeItem<>( detailsNode, library.getIcon( "about" ) ) );
	}

}
