package com.avereon.xenon.tool;

import com.avereon.log.LazyEval;
import com.avereon.product.Rb;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.ThemeMetadata;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.tool.guide.GuidedTool;
import javafx.css.*;
import javafx.css.converter.DeriveColorConverter;
import javafx.css.converter.PaintConverter;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.CustomLog;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CustomLog
@Deprecated
public class ThemeTool extends GuidedTool {

	private final ComboBox<ThemeMetadata> chooser;

	private final Region sample;

	private final VBox layout;

	public ThemeTool( ProgramProduct product, Asset asset ) {
		super( product, asset );

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
		sample.getStylesheets().addAll( Program.STYLESHEET, theme.getUrl() );

		refreshThemeOptions( theme );
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		setTitle( Rb.text( "tool", "themes-name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "themes" ) );
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
			List<Rule> rules = new CssParser().parse( new URL( theme.getUrl() ) ).getRules();
			Map<String, Rule> ruleSelectors = new HashMap<>();
			for( Rule rule : rules ) {

				// NOTE Each rule is a many-to-many mapping of selectors and declarations.
				// Selectors are simple enough and can be easily reproduced
				// Declarations on the other hand are quite a bit more complex and CSS handles them fairly well

				for( Selector selector : rule.getSelectors() ) {
					ruleSelectors.put( selector.toString(), rule );
					log.atInfo().log( "selector=%s", selector );
					for( Declaration declaration : rule.getDeclarations() ) {
						StyleConverter<?, ?> converter = declaration.getParsedValue().getConverter();
						if( converter == null ) {
							Assign assign = Assign.of( declaration );
							log.atInfo().log( "  %s", assign );
						} else if( converter instanceof DeriveColorConverter ) {
							Derive derive = Derive.of( declaration );
							log.atInfo().log( "  %s", derive );
						} else if( converter instanceof PaintConverter.SequenceConverter ) {
							PaintSequence seq = PaintSequence.of( declaration );
							log.atInfo().log( "  %s", seq );
						} else {
							log.atWarn().log( "unknown type=%s", LazyEval.of( () -> converter.getClass().getName() ) );
						}
					}
				}
			}
		} catch( IOException exception ) {
			log.atError( exception ).log( "Unable to parse theme CSS: %s", theme.getId() );
		}
	}

	static abstract class Decl {

		private final String key;

		public Decl( String key ) {
			this.key = key;
		}

		@Override
		public String toString() {
			return key;
		}

	}

	static class Assign extends Decl {

		private final String value;

		public Assign( String key, String value ) {
			super( key );
			this.value = value;
		}

		@Override
		public String toString() {
			return super.toString() + ": " + value;
		}

		public static Assign of( Declaration declaration ) {
			return new Assign( declaration.getProperty(), declaration.getParsedValue().getValue().toString().replace( "0x", "#" ) );
		}

	}

	static class Derive extends Decl {

		private final String source;

		private final String value;

		public Derive( String key, String source, String value ) {
			super( key );
			this.source = source;
			this.value = value;
		}

		@Override
		public String toString() {
			return super.toString() + ": derive(" + source + "," + value + ")";
		}

		public static Derive of( Declaration declaration ) {
			ParsedValue<?, ?>[] values = (ParsedValue<?, ?>[])declaration.getParsedValue().getValue();
			return new Derive( declaration.getProperty(), String.valueOf( values[ 0 ].getValue() ), String.valueOf( values[ 1 ].getValue() ) );
		}

	}

	static class PaintSequence extends Decl {

		private Object[] values;

		public PaintSequence( String key ) {
			super( key );
		}

		@Override
		public String toString() {
			if( values == null ) return "null";
			// -fx-background-color: -fx-mark-highlight-color, derive(-fx-base, -45%);

			StringBuilder builder = new StringBuilder();

			boolean first = false;
			for( Object value : values ) {
				if( !first ) builder.append( "," );
				builder.append( value );
				first = true;
			}

			return builder.toString();
		}

		public static PaintSequence of( Declaration declaration ) {
			log.atInfo().log( "  decl=%s=paint", LazyEval.of( declaration::getProperty ) );
			ParsedValue<?, ?>[] values = (ParsedValue<?, ?>[])declaration.getParsedValue().getValue();
			for( ParsedValue<?, ?> value : values ) {
				log.atInfo().log( "  val=%s", LazyEval.of( value::getValue ) );
			}

			return new PaintSequence( declaration.getProperty() );
		}

	}

}
