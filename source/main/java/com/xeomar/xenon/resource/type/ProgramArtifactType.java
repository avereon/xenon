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

import java.net.URI;

public class ProgramArtifactType extends ResourceType {

	public static final URI uri = URI.create( "program:artifact" );

	public static final String INSTALLED = "installed";

	public static final String AVAILABLE = "available";

	public static final String UPDATES = "updates";

	public static final String SOURCES = "sources";

	public ProgramArtifactType( Product product ) {
		super( product, "artifact" );
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
		installed.setId( INSTALLED );
		installed.setName( rb.getString( "tool", "artifact-installed" ) );

		GuideNode available = new GuideNode();
		available.setId( AVAILABLE );
		available.setName( rb.getString( "tool", "artifact-available" ) );

		GuideNode updates = new GuideNode();
		updates.setId( UPDATES );
		updates.setName( rb.getString( "tool", "artifact-updates" ) );

		GuideNode sources = new GuideNode();
		sources.setId( SOURCES );
		sources.setName( rb.getString( "tool", "artifact-sources" ) );

		guide.setRoot( root = new TreeItem<>( new GuideNode(), library.getIcon( "artifact" ) ) );
		root.getChildren().add( new TreeItem<>( installed, library.getIcon( "artifact" ) ) );
		root.getChildren().add( new TreeItem<>( available, library.getIcon( "artifact" ) ) );
		root.getChildren().add( new TreeItem<>( updates, library.getIcon( "artifact" ) ) );
		root.getChildren().add( new TreeItem<>( sources, library.getIcon( "artifact" ) ) );
	}
}
