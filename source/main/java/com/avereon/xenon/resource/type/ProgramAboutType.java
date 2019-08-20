package com.avereon.xenon.resource.type;

import com.avereon.product.Product;
import com.avereon.product.ProductBundle;
import com.avereon.xenon.IconLibrary;
import com.avereon.xenon.Program;
import com.avereon.xenon.resource.Codec;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.ResourceException;
import com.avereon.xenon.resource.ResourceType;
import com.avereon.xenon.tool.about.AboutTool;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import javafx.scene.control.TreeItem;

public class ProgramAboutType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:about" );

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
		summaryNode.setId( AboutTool.SUMMARY );
		summaryNode.setName( rb.getString( "tool", "about-summary" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( summaryNode, library.getIcon( "about" ) ) );

		GuideNode detailsNode = new GuideNode();
		detailsNode.setId( AboutTool.DETAILS );
		detailsNode.setName( rb.getString( "tool", "about-details" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( detailsNode, library.getIcon( "about" ) ) );

		GuideNode productsNode = new GuideNode();
		productsNode.setId( AboutTool.MODS );
		productsNode.setName( rb.getString( "tool", "about-mods" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( productsNode, library.getIcon( "about" ) ) );
	}

}
