package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.product.ProductBundle;
import com.xeomar.xenon.IconLibrary;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.tool.guide.Guide;
import com.xeomar.xenon.tool.guide.GuideNode;
import javafx.scene.control.TreeItem;

import java.net.URI;
import java.util.Map;

public class ProgramAboutType extends ResourceType {

	public static final URI uri = URI.create( "program:about" );

	private Map<TreeItem<String>, String> pages;

	public ProgramAboutType( Product product ) {
		super( product, "about" );
	}

	@Override
	public boolean isUserType() {
		return false;
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
		resource.setModel( getProduct().getCard() );
		updateGuide( program, resource );
		return true;
	}

	private void updateGuide( Program program, Resource resource ) {
		Guide guide = resource.getResource( Guide.GUIDE_KEY );
		if( guide != null ) return;

		resource.putResource( Guide.GUIDE_KEY, guide = new Guide() );

		IconLibrary library = program.getIconLibrary();
		ProductBundle rb = getProduct().getResourceBundle();

		GuideNode summaryNode = new GuideNode();
		summaryNode.setId( "summary" );
		summaryNode.setName( rb.getString( "tool", "about-summary" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( summaryNode, library.getIcon( "about" ) ) );

		GuideNode productsNode = new GuideNode();
		productsNode.setId( "products");
		productsNode.setName( rb.getString( "tool", "about-products" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( productsNode, library.getIcon( "about" ) ) );

		GuideNode detailsNode = new GuideNode();
		detailsNode.setId( "details" );
		detailsNode.setName( rb.getString( "tool", "about-details" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( detailsNode, library.getIcon( "about" ) ) );
	}

}
