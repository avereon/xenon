package com.xeomar.xenon.resource.type;

import com.xeomar.product.Product;
import com.xeomar.product.ProductBundle;
import com.xeomar.xenon.IconLibrary;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.resource.Codec;
import com.xeomar.xenon.resource.Resource;
import com.xeomar.xenon.resource.ResourceException;
import com.xeomar.xenon.resource.ResourceType;
import com.xeomar.xenon.tool.Guide;
import com.xeomar.xenon.tool.GuideNode;
import javafx.scene.control.TreeItem;

public class ProgramProductType extends ResourceType {

	public static final String URI = "program:product";

	public ProgramProductType( Product product ) {
		super( product, "product" );
	}

	@Override
	public boolean isUserType() {
		return false;
	}

	@Override
	public boolean resourceDefault( Program program, Resource resource ) throws ResourceException {
		resource.setModel( program.getCard() );
		resource.putResource( Guide.GUIDE_KEY, new Guide() );
		updateGuide( program, resource );
		return true;
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


	private void updateGuide( Program program, Resource resource ) {
		Guide guide = resource.getResource( Guide.GUIDE_KEY );

		TreeItem<GuideNode> root = guide.getRoot();
		if( root != null ) return;

		IconLibrary library = program.getIconLibrary();
		ProductBundle rb = getProduct().getResourceBundle();

		GuideNode installed = new GuideNode();
		installed.setId( "installed" );
		installed.setName( rb.getString( "tool", "product-installed" ) );

		GuideNode available = new GuideNode();
		available.setId( "available");
		available.setName( rb.getString( "tool", "product-available" ) );

		GuideNode updates = new GuideNode();
		updates.setId( "updates" );
		updates.setName( rb.getString( "tool", "product-updates" ) );

		GuideNode sources = new GuideNode();
		sources.setId( "sources" );
		sources.setName( rb.getString( "tool", "product-sources" ) );

		guide.setRoot( root = new TreeItem<>( new GuideNode(), library.getIcon( "product" ) ) );
		root.getChildren().add( new TreeItem<>( installed, library.getIcon( "product" ) ) );
		root.getChildren().add( new TreeItem<>( available, library.getIcon( "product" ) ) );
		root.getChildren().add( new TreeItem<>( updates, library.getIcon( "product" ) ) );
		root.getChildren().add( new TreeItem<>( sources, library.getIcon( "product" ) ) );
	}
}
