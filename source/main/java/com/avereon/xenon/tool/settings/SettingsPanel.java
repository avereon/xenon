package com.avereon.xenon.tool.settings;

import com.avereon.data.NodeEvent;
import com.avereon.event.EventHandler;
import com.avereon.product.Product;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.zerra.javafx.Fx;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.lang.System.Logger;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SettingsPanel extends VBox {

	private static final Logger log = Log.get();

	private final SettingsPage page;

	private final Map<String, SettingOptionProvider> optionProviders;

	//	private String[] fontNames;
	//
	//	private String[] fontStyles;
	//
	//	private String[] fontSizes;

	// TODO Add Apply button and change functionality accordingly.
	// TODO Add a default button to set individual setting back to default.
	// TODO Add an undo button to set individual setting back to previous.

	/**
	 * @param page
	 * @param optionProviders The map of available option providers
	 */
	public SettingsPanel( SettingsPage page, Map<String, SettingOptionProvider> optionProviders ) {
		this.page = page;
		this.optionProviders = optionProviders;

		//		String fontPlain = product.getResourceBundle().getString( "settings", "font-plain" );
		//		String fontBold = product.getResourceBundle().getString( "settings", "font-bold" );
		//		String fontItalic = product.getResourceBundle().getString( "settings", "font-italic" );
		//		String fontBoldItalic = product.getResourceBundle().getString( "settings", "font-bold-italic" );
		//
		//		List<String> fontFamilies = Font.getFamilies();
		//		fontNames = fontFamilies.toArray( new String[ fontFamilies.size() ] );
		//		fontStyles = new String[]{ fontPlain, fontBold, fontItalic, fontBoldItalic };
		//		fontSizes = new String[]{ "8", "10", "12", "14", "16", "18", "20", "22", "24", "26" };

		//setBorder( new Border( new BorderStroke( Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		// Get the title
		String title = page.getTitle();
		Product product = page.getProduct();
		if( title == null ) title = product.rb().text( "settings", page.getId() );

		// Add the title label
		Label titleLabel = new Label( title );
		titleLabel.setFont( Font.font( titleLabel.getFont().getFamily(), 2 * titleLabel.getFont().getSize() ) );
		titleLabel.prefWidthProperty().bind( widthProperty() );
		titleLabel.getStyleClass().add( "setting-title" );
		titleLabel.setAlignment( Pos.CENTER );
		//titleLabel.setBorder( new Border( new BorderStroke( Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		getChildren().add( titleLabel );
		addBlankLine( this );

		// Add the groups
		for( SettingGroup group : page.getGroups() ) {
			String name = product.rb().text( "settings", group.getId() );
			Control pane = createGroupPane( product, page, name, group );
			pane.setBorder( new Border( new BorderStroke( Color.RED, BorderStrokeStyle.NONE, CornerRadii.EMPTY, BorderStroke.THICK ) ) );
			getChildren().add( pane );
		}
	}

	public SettingsPage getPage() {
		return this.page;
	}

	private void addBlankLine( Pane pane ) {
		Label blankLine = new Label( " " );
		blankLine.prefWidthProperty().bind( pane.widthProperty() );
		blankLine.getStyleClass().add( "setting-blank" );
		blankLine.setAlignment( Pos.CENTER );
		pane.getChildren().add( blankLine );
	}

	private Control createGroupPane( Product product, SettingsPage page, String name, SettingGroup group ) {
		Pane pane = createSettingsPane( product, page, group );

		group.register( NodeEvent.ANY, new GroupChangeHandler( group, pane ) );

		Settings pageSettings = page.getSettings();
		List<SettingDependency> dependencies = group.getDependencies();
		if( dependencies.size() > 0 ) {
			for( SettingDependency dependency : dependencies ) {
				addGroupDependencyWatchers( pageSettings, group, dependency );
			}
		}

		group.updateState();

		TitledPane groupPane = new TitledPane( name, pane );
		groupPane.setCollapsible( false );
		groupPane.setExpanded( true );
		return groupPane;
	}

	private Pane createSettingsPane( Product product, SettingsPage page, SettingGroup group ) {
		GridPane pane = new GridPane();
		pane.setHgap( UiFactory.PAD );
		pane.setVgap( UiFactory.PAD );
		//pane.setBorder( new Border( new BorderStroke( Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		int row = 0;
		for( Setting setting : group.getSettings() ) {
			// Get the editor type
			String editorType = setting.getEditor();
			if( editorType == null ) editorType = "textline";

			// Determine the editor class
			Class<? extends SettingEditor> editorClass = SettingEditor.getType( editorType );
			if( editorClass == null ) editorClass = SettingEditor.getType( "textline" );

			// Determine setting option provider, if any
			String providerId = setting.getProvider();
			setting.setOptionProvider( providerId == null ? null : optionProviders.get( providerId ) );

			// Create the editor
			if( editorClass == null ) {
				log.log( Log.WARN, "Setting editor not registered: {0}", editorType );
			} else {
				SettingEditor editor = createSettingEditor( product, setting, editorClass );
				if( editor != null ) editor.addComponents( pane, row++ );
				if( editor == null ) log.log( Log.DEBUG, "Editor not created: {0}", editorClass.getName() );
			}

			// Add a watcher to each dependency
			Settings pageSettings = page.getSettings();
			List<SettingDependency> dependencies = setting.getDependencies();
			if( dependencies.size() > 0 ) {
				for( SettingDependency dependency : dependencies ) {
					addSettingDependencyWatchers( pageSettings, setting, dependency );
				}
			}

			setting.updateState();
		}

		return pane;
	}

	private SettingEditor createSettingEditor( Product product, Setting setting, Class<? extends SettingEditor> editorClass ) {
		// Try loading a class from the type
		SettingEditor editor = null;

		try {
			Constructor<? extends SettingEditor> constructor = editorClass.getConstructor( ProgramProduct.class, Setting.class );
			editor = constructor.newInstance( (ProgramProduct)product, setting );
		} catch( Exception exception ) {
			log.log( Log.ERROR, "Error creating setting editor: " + editorClass.getName(), exception );
		}
		if( editor == null ) return null;

		// Add the change handler
		new EditorChangeHandler( editor );

		return editor;
	}

	private void addGroupDependencyWatchers( Settings settings, SettingGroup group, SettingDependency dependency ) {
		settings.register( SettingsEvent.CHANGED, new GroupDependencyWatcher( dependency, group ) );

		List<SettingDependency> dependencies = group.getDependencies();
		if( dependencies.size() > 0 ) {
			for( SettingDependency child : dependency.getDependencies() ) {
				addGroupDependencyWatchers( settings, group, child );
			}
		}
	}

	private void addSettingDependencyWatchers( Settings settings, Setting setting, SettingDependency dependency ) {
		settings.register( SettingsEvent.CHANGED, new SettingDependencyWatcher( dependency, setting ) );

		List<SettingDependency> dependencies = setting.getDependencies();
		if( dependencies.size() > 0 ) {
			for( SettingDependency child : dependency.getDependencies() ) {
				addSettingDependencyWatchers( settings, setting, child );
			}
		}
	}

	private static final class GroupDependencyWatcher implements EventHandler<SettingsEvent> {

		private SettingGroup group;

		private String key;

		public GroupDependencyWatcher( SettingDependency dependency, SettingGroup setting ) {
			this.group = setting;
			this.key = dependency.getKey();
		}

		@Override
		public void handle( SettingsEvent event ) {
			if( key == null ) return;
			if( key.equals( event.getKey() ) ) group.updateState();
		}

	}

	private static final class SettingDependencyWatcher implements EventHandler<SettingsEvent> {

		private final String dependencyKey;

		private final Setting setting;

		public SettingDependencyWatcher( SettingDependency dependency, Setting setting ) {
			if( dependency.getKey() == null ) throw new NullPointerException( "Dependency key cannot be null for " + setting.getKey() );
			this.dependencyKey = dependency.getKey();
			this.setting = setting;
		}

		@Override
		public void handle( SettingsEvent event ) {
			if( Objects.equals( event.getKey(), dependencyKey ) ) setting.updateState();
		}

	}

	private static class GroupChangeHandler implements EventHandler<NodeEvent> {

		private SettingGroup group;

		private Pane pane;

		public GroupChangeHandler( SettingGroup group, Pane pane ) {
			this.group = group;
			this.pane = pane;
		}

		@Override
		public void handle( NodeEvent event ) {
			if( event.getSource() != group || event.getEventType() != NodeEvent.VALUE_CHANGED ) return;

			switch( event.getKey() ) {
				case "disable": {
					setDisable( (Boolean)event.getNewValue() );
					break;
				}
				case "visible": {
					setVisible( (Boolean)event.getNewValue() );
					break;
				}
			}
		}

		protected final void setDisable( boolean disable ) {
			pane.setDisable( disable );
		}

		protected final void setVisible( boolean visible ) {
			pane.setVisible( visible );
		}

	}

	private static class EditorChangeHandler {

		private final SettingEditor editor;

		private EditorChangeHandler( SettingEditor editor ) {
			this.editor = editor;
			Setting setting = editor.getSetting();

			// Register a handler when the setting value changes to update the editor
			setting.getSettings().register( SettingsEvent.CHANGED, editor );

			// Register a handler on the setting node to update other setting nodes
			setting.register( NodeEvent.VALUE_CHANGED, this::handleNodeEvent );
		}

		private void handleNodeEvent( NodeEvent event ) {
			switch( event.getKey() ) {
				case Setting.DISABLE: {
					Fx.run( () -> editor.setDisable( (Boolean)event.getNewValue() ) );
					break;
				}
				case Setting.VISIBLE: {
					Fx.run( () -> editor.setVisible( (Boolean)event.getNewValue() ) );
					break;
				}
			}
		}

	}

}
