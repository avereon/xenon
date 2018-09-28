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

public class ProgramProductType extends ResourceType {

	public static final java.net.URI URI = java.net.URI.create( "program:product" );

	public static final String INSTALLED = "installed";

	public static final String AVAILABLE = "available";

	public static final String UPDATES = "updates";

	public static final String SOURCES = "sources";

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
		createGuide( program, resource );
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

	private void createGuide( Program program, Resource resource ) {
		Guide guide = resource.getResource( Guide.GUIDE_KEY );
		if( guide != null ) return;

		IconLibrary library = program.getIconLibrary();
		ProductBundle rb = getProduct().getResourceBundle();

		resource.putResource( Guide.GUIDE_KEY, guide = new Guide() );

		GuideNode installed = new GuideNode();
		installed.setId( INSTALLED );
		installed.setName( rb.getString( "tool", "product-installed" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( installed, library.getIcon( "product" ) ) );

		GuideNode available = new GuideNode();
		available.setId( AVAILABLE );
		available.setName( rb.getString( "tool", "product-available" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( available, library.getIcon( "product" ) ) );

		GuideNode updates = new GuideNode();
		updates.setId( UPDATES );
		updates.setName( rb.getString( "tool", "product-updates" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( updates, library.getIcon( "product" ) ) );

		GuideNode sources = new GuideNode();
		sources.setId( SOURCES );
		sources.setName( rb.getString( "tool", "product-sources" ) );
		guide.getRoot().getChildren().add( new TreeItem<>( sources, library.getIcon( "product" ) ) );
	}

}
