package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.*;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Ellipse;

public class WelcomeTool extends ProgramTool {

	private static final System.Logger log = Log.get();

	private static final double PAD = 2 * UiFactory.PAD;

	private static final double PRODUCT_ICON_SIZE = 96;

	private static final double ICON_SIZE = 64;

	private static final double SLOPE_RADIUS = 5000;

	public WelcomeTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-welcome" );

		Node icon = ((Program)product).getIconLibrary().getIcon( "program", PRODUCT_ICON_SIZE );
		Node docsIcon = ((Program)product).getIconLibrary().getIcon( "document", ICON_SIZE );
		Node modsIcon = ((Program)product).getIconLibrary().getIcon( "product", ICON_SIZE );

		String documentButtonTitle = product.rb().text( BundleKey.LABEL, "documentation" );
		String documentButtonDescription = product.rb().text( BundleKey.LABEL, "documentation-desc" );
		String documentButtonUrl = product.rb().text( BundleKey.LABEL, "documentation-url" );
		String modsButtonTitle = product.rb().text( BundleKey.LABEL, "mods" );
		String modsButtonDescription = product.rb().text( BundleKey.LABEL, "mods-desc" );
		String modsButtonUrl = product.rb().text( BundleKey.LABEL, "mods-url" );

		Label label = new Label( product.getCard().getName(), icon );
		label.getStyleClass().add( "tool-welcome-title" );

		Ellipse accent = new Ellipse( 0, ICON_SIZE + 2 * PAD + SLOPE_RADIUS, SLOPE_RADIUS, SLOPE_RADIUS );
		accent.getStyleClass().add( "accent" );

		Pane accentPane = new Pane();
		accentPane.getChildren().addAll( accent );

		Button docsButton = createButton( docsIcon, documentButtonTitle, documentButtonDescription, documentButtonUrl );
		Button modsButton = createButton( modsIcon, modsButtonTitle, modsButtonDescription, modsButtonUrl );
		VBox buttonBox = new VBox( docsButton, modsButton );
		buttonBox.getStyleClass().addAll( "buttons" );
		buttonBox.setPadding( new Insets( 3 * UiFactory.PAD ) );

		VBox contentPane = new VBox( UiFactory.PAD, label, buttonBox );
		contentPane.setPadding( new Insets( UiFactory.PAD ) );

		getChildren().addAll( accentPane, contentPane );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( getProduct().rb().text( "tool", "welcome-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "welcome" ) );
	}

	@Override
	protected void display() {
		getWorkpane().setMaximizedView( getToolView() );
	}

	@Override
	protected void conceal() {
		if( getToolView().isMaximized() ) getWorkpane().setMaximizedView( null );
	}

	private Button createButton( Node icon, String title, String description, String uri ) {
		Label titleLabel = new Label( title );
		titleLabel.getStyleClass().addAll( "title" );
		Label descriptionLabel = new Label( description );
		descriptionLabel.getStyleClass().addAll( "description" );

		VBox text = new VBox( titleLabel, descriptionLabel );
		BorderPane content = new BorderPane( text, null, null, null, icon );
		Button button = new Button( "", content );
		button.setOnAction( e -> {
			// FIXME This hung the app...probably needs to be on a different thread
//			try {
//				Desktop.getDesktop().browse( new URI( uri ) );
//			} catch( IOException | URISyntaxException ioException ) {
//				log.log( Log.WARN, "Unable to open uri=" + uri );
//			}
		} );
		return button;
	}
}
