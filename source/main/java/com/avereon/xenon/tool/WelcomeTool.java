package com.avereon.xenon.tool;

import com.avereon.product.Rb;
import com.avereon.xenon.*;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.task.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Ellipse;
import lombok.CustomLog;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@CustomLog
public class WelcomeTool extends ProgramTool {

	private static final double PAD = 2 * UiFactory.PAD;

	private static final double PRODUCT_ICON_SIZE = 96;

	private static final double ICON_SIZE = 64;

	private static final double SLOPE_RADIUS = 5000;

	public WelcomeTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-welcome" );

		Node icon = ((Xenon)product).getIconLibrary().getIcon( "program", PRODUCT_ICON_SIZE );
		Node docsIcon = ((Xenon)product).getIconLibrary().getIcon( "document", ICON_SIZE );
		Node modsIcon = ((Xenon)product).getIconLibrary().getIcon( "product", ICON_SIZE );

		String documentButtonTitle = Rb.text( RbKey.LABEL, "documentation" );
		String documentButtonDescription = Rb.text( RbKey.LABEL, "documentation-desc" );
		String documentButtonUrl = Rb.text( RbKey.LABEL, "documentation-url" );
		String modsButtonTitle = Rb.text( RbKey.LABEL, "mods" );
		String modsButtonDescription = Rb.text( RbKey.LABEL, "mods-desc" );
		String modsButtonUrl = Rb.text( RbKey.LABEL, "mods-url" );

		Label label = new Label( product.getCard().getName(), icon );
		label.getStyleClass().add( "tool-welcome-title" );

		Ellipse accent = new Ellipse( 0, ICON_SIZE + 2 * PAD + SLOPE_RADIUS, SLOPE_RADIUS, SLOPE_RADIUS );
		accent.getStyleClass().add( "accent" );

		Pane accentPane = new Pane();
		accentPane.getChildren().addAll( accent );

		Button docsButton = createButton( docsIcon, documentButtonTitle, documentButtonDescription, documentButtonUrl );
		GridPane.setConstraints( docsButton, 0, 0 );
		Button modsButton = createButton( modsIcon, modsButtonTitle, modsButtonDescription, modsButtonUrl );
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
		button.setMaxWidth( Double.MAX_VALUE );

		button.setOnAction( e -> getProgram().getTaskManager().submit( Task.of( "", () -> {
			try {
				Desktop.getDesktop().browse( new URI( uri ) );
			} catch( IOException | URISyntaxException ioException ) {
				log.atWarn().log( "Unable to open uri=%s", uri );
			}
		} ) ) );

		return button;
	}
}
