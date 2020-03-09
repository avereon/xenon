package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ThemeMetadata;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.tool.guide.GuidedTool;
import javafx.css.CssParser;
import javafx.css.Declaration;
import javafx.css.Rule;
import javafx.css.Selector;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemesTool extends GuidedTool {

	private static final System.Logger log = Log.get();

	private ComboBox<ThemeMetadata> chooser;

	private Region sample;

	private VBox layout;

	public ThemesTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "themes" ) );
		setTitle( product.rb().text( "tool", "themes-name" ) );

		chooser = new ComboBox<>();

		layout = new VBox( chooser, sample = generateSamplePane() );
		layout.setSpacing( UiFactory.PAD );
		getChildren().addAll( layout );

		refreshThemeChooser();

		ThemeMetadata theme = getProgram().getThemeManager().getMetadata( getProgram().getWorkspaceManager().getTheme() );
		chooser.valueProperty().addListener( ( v, o, n ) -> refreshThemeOptions( n ) );
		chooser.getSelectionModel().select( 1 );
	}

	private Region generateSamplePane() {
		VBox pane = new VBox();
		pane.setSpacing( UiFactory.PAD );

		Button normal = new Button( "Normal" );
		Button cancel = new Button( "Cancel" );
		cancel.setCancelButton( true );
		Button ok = new Button( "Default" );
		ok.setDefaultButton( true );
		HBox buttons = new HBox( normal, ok, cancel );
		buttons.setSpacing( UiFactory.PAD );
		pane.getChildren().add( buttons );

		return pane;
	}

	private void refreshThemeChooser() {
		List<ThemeMetadata> themes = new ArrayList<>( getProgram().getThemeManager().getThemes() );
		themes.sort( null );
		chooser.getItems().clear();
		chooser.getItems().addAll( themes );
	}

	private void refreshThemeOptions( ThemeMetadata theme ) {
		log.log( Log.WARN, "Selected theme: " + theme.getStylesheet() );

		// NOTE This should work but it appears I don't understand the concept yet
		sample.getStylesheets().clear();
		sample.getStylesheets().addAll( theme.getStylesheet() );

		//sample.setStyle( "-fx-background-color: green;" );

		// This only gets the metadata this component uses...which isn't much
		//		Group group = new Group();
		//		group.getStylesheets().addAll( Program.STYLESHEET, theme.getStylesheet() );
		//		group.getCssMetaData().forEach( m -> {
		//			log.log( Log.WARN, "meta=" + m );
		//		} );

		// TODO Hunt down the supported parameters:
		// -fx-base
		// ...
		try {
			List<Rule> rules = new CssParser().parse( new URL( theme.getStylesheet() ) ).getRules();
			Map<String, Rule> ruleSelectors = new HashMap<>();
			for( Rule rule : rules ) {
				for( Selector selector : rule.getSelectors() ) {
					ruleSelectors.put( selector.toString(), rule );
					//log.log( Log.WARN, "selector=" + selector.toString() );
					for( Declaration declaration : rule.getDeclarations() ) {
						//log.log( Log.WARN, "  decl=" + declaration.getProperty() + "=" + declaration.getParsedValue().getValue() );
					}
				}
			}
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Unable to parse theme CSS: " + theme.getId(), exception );
		}
	}

}
