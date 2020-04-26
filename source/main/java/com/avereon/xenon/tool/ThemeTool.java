package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ThemeMetadata;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.tool.guide.GuidedTool;
import javafx.css.*;
import javafx.css.converter.DeriveColorConverter;
import javafx.css.converter.PaintConverter;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ThemeTool extends GuidedTool {

	private static final System.Logger log = Log.get();

	private ComboBox<ThemeMetadata> chooser;

	private Region sample;

	private VBox layout;

	public ThemeTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setGraphic( product.getProgram().getIconLibrary().getIcon( "themes" ) );
		setTitle( product.rb().text( "tool", "themes-name" ) );

		chooser = new ComboBox<>();
		sample = generateSamplePane();

		layout = new VBox( chooser, sample );
		layout.setSpacing( UiFactory.PAD );
		layout.setBorder( new Border( new BorderStroke( Color.TRANSPARENT, BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths( UiFactory.PAD ) ) ) );
		getChildren().addAll( layout );

		refreshThemeChooser();

		chooser.valueProperty().addListener( ( v, o, n ) -> setSampleTheme( n ) );
		chooser.getSelectionModel().select( 1 );
	}

	private void refreshThemeChooser() {
		chooser.getItems().clear();
		chooser.getItems().addAll( getProgram().getThemeManager().getThemes().stream().sorted().collect( Collectors.toList() ) );
	}

	private void setSampleTheme( ThemeMetadata theme ) {
		sample.getStylesheets().clear();
		sample.getStylesheets().addAll( Program.STYLESHEET, theme.getStylesheet() );

		refreshThemeOptions( theme );
	}

	private Region generateSamplePane() {
		VBox pane = new VBox();
		pane.setSpacing( UiFactory.PAD );
		pane.getStyleClass().add( "root" );
		pane.setBorder( new Border( new BorderStroke( Color.TRANSPARENT, BorderStrokeStyle.NONE, CornerRadii.EMPTY, new BorderWidths( UiFactory.PAD ) ) ) );

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

	private void refreshThemeOptions( ThemeMetadata theme ) {

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
					log.log( Log.INFO, "selector=" + selector.toString() );
					for( Declaration declaration : rule.getDeclarations() ) {
						StyleConverter<?,?> converter = declaration.getParsedValue().getConverter();
						if( converter == null ) {
							log.log( Log.INFO, "  decl=" + declaration.getProperty() + "=" + declaration.getParsedValue().getValue() );
						} else if( converter instanceof DeriveColorConverter ) {
							log.log( Log.INFO, "  decl=" + declaration.getProperty() + "=derive" );
							ParsedValue<?, ?>[] values = (ParsedValue<?, ?>[])declaration.getParsedValue().getValue();
							for( ParsedValue<?, ?> value : values ) {
								log.log( Log.INFO, "  val=" + value.getValue() );
							}
						} else if( converter instanceof PaintConverter.SequenceConverter ) {
							log.log( Log.INFO, "  decl=" + declaration.getProperty() + "=paint" );
							ParsedValue<?, ?>[] values = (ParsedValue<?, ?>[])declaration.getParsedValue().getValue();
							for( ParsedValue<?, ?> value : values ) {
								log.log( Log.INFO, "  val=" + value.getValue() );
							}
						} else {
							log.log( Log.WARN, "unknown type=" + converter.getClass().getName() );
						}
					}
				}
			}
		} catch( IOException exception ) {
			log.log( Log.ERROR, "Unable to parse theme CSS: " + theme.getId(), exception );
		}
	}

}
