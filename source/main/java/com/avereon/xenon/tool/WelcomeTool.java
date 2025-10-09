package com.avereon.xenon.tool;

import com.avereon.product.Rb;
import com.avereon.xenon.*;
import com.avereon.xenon.action.DesktopBrowserAction;
import com.avereon.xenon.action.SettingsAction;
import com.avereon.xenon.resource.Resource;
import com.avereon.xenon.resource.OpenAssetRequest;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Ellipse;
import lombok.CustomLog;

import java.net.URI;

@CustomLog
public class WelcomeTool extends ProgramTool {

	private static final double PAD = 2 * UiFactory.PAD;

	private static final double PRODUCT_ICON_SIZE = 96;

	private static final double ICON_SIZE = 64;

	private static final double SLOPE_RADIUS = 5000;

	public WelcomeTool( XenonProgramProduct product, Resource resource ) {
		super( product, resource );
		setId( "tool-welcome" );

		Node icon = product.getProgram().getIconLibrary().getIcon( "program", PRODUCT_ICON_SIZE );
		Node docsIcon = product.getProgram().getIconLibrary().getIcon( "document", ICON_SIZE );
		Node modsIcon = product.getProgram().getIconLibrary().getIcon( "product", ICON_SIZE );

		String documentButtonTitle = Rb.text( RbKey.LABEL, "documentation" );
		String documentButtonDescription = Rb.text( RbKey.LABEL, "documentation-desc" );
		String documentButtonUrl = Rb.text( RbKey.LABEL, "documentation-url" );
		String modsButtonTitle = Rb.text( RbKey.LABEL, "mods" );
		String modsButtonDescription = Rb.text( RbKey.LABEL, "mods-desc" );

		Label label = new Label( product.getCard().getName(), icon );
		label.getStyleClass().add( "tool-welcome-title" );

		Ellipse accent = new Ellipse( 0, ICON_SIZE + 2 * PAD + SLOPE_RADIUS, SLOPE_RADIUS, SLOPE_RADIUS );
		accent.getStyleClass().add( "accent" );

		Pane accentPane = new Pane();
		accentPane.getChildren().addAll( accent );

		DesktopBrowserAction docsAction = new DesktopBrowserAction( product.getProgram(), URI.create( documentButtonUrl ) );
		Button docsButton = createButton( docsIcon, documentButtonTitle, documentButtonDescription, docsAction );
		GridPane.setConstraints( docsButton, 0, 0 );

		SettingsAction modsAction = new SettingsAction( product.getProgram(), "modules-available" );
		Button modsButton = createButton( modsIcon, modsButtonTitle, modsButtonDescription, modsAction );
		GridPane.setConstraints( modsButton, 1, 0 );

		GridPane buttonGrid = new GridPane();
		buttonGrid.getStyleClass().addAll( "buttons" );
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth( 50 );
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth( 50 );
		buttonGrid.getColumnConstraints().addAll( column1, column2 );
		buttonGrid.getChildren().addAll( docsButton, modsButton );

		VBox contentPane = new VBox( UiFactory.PAD, label, buttonGrid );
		contentPane.setPadding( new Insets( UiFactory.PAD ) );

		getChildren().addAll( accentPane, contentPane );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( RbKey.TOOL, "welcome-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "welcome" ) );
	}

	private Button createButton( Node icon, String title, String description, ProgramAction action ) {
		Label titleLabel = new Label( title );
		titleLabel.getStyleClass().addAll( "title" );
		Label descriptionLabel = new Label( description );
		descriptionLabel.getStyleClass().addAll( "description" );

		VBox text = new VBox( titleLabel, descriptionLabel );
		BorderPane content = new BorderPane( text, null, null, null, icon );
		Button button = new Button( "", content );
		button.setMaxWidth( Double.MAX_VALUE );
		button.setOnAction( action );

		return button;
	}
}
