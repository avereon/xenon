package com.avereon.xenon.tool.settings;

import com.avereon.event.EventHandler;
import com.avereon.event.EventType;
import com.avereon.product.Product;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.util.Log;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.node.NodeEvent;
import com.avereon.xenon.node.NodeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.List;

public class SettingsPanel extends VBox {

	private static final Logger log = Log.get( MethodHandles.lookup().lookupClass() );

	//	private String[] fontNames;
	//
	//	private String[] fontStyles;
	//
	//	private String[] fontSizes;

	// TODO Add Apply button and change functionality accordingly.
	// TODO Add a default button to set individual setting back to default.
	// TODO Add an undo button to set individual setting back to previous.

	public SettingsPanel( Product product, SettingsPage page ) {
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

	private void addBlankLine( Pane pane ) {
		Label blankLine = new Label( " " );
		blankLine.prefWidthProperty().bind( pane.widthProperty() );
		blankLine.getStyleClass().add( "setting-blank" );
		blankLine.setAlignment( Pos.CENTER );
		pane.getChildren().add( blankLine );
	}

	private Control createGroupPane( Product product, SettingsPage page, String name, SettingGroup group ) {
		Pane pane = createSettingsPane( product, page, group );

		group.addNodeListener( new GroupChangeHandler( group, pane ) );

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

			// Create the editor
			if( editorClass == null ) {
				log.warn( "Setting editor not registered: {}", editorType );
			} else {
				SettingEditor editor = createSettingEditor( product, setting, editorClass );
				if( editor != null ) editor.addComponents( pane, row++ );
				if( editor == null ) log.debug( "Editor not created: {}", editorClass.getName() );
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
			log.error( "Error creating setting editor: " + editorClass.getName(), exception );
		}
		if( editor == null ) return null;

		// Add the change handler
		new EditorChangeHandler( editor, setting );

		return editor;
	}

	private void addGroupDependencyWatchers( Settings settings, SettingGroup group, SettingDependency dependency ) {
		settings.addSettingsListener( new GroupDependencyWatcher( dependency, group ) );

		List<SettingDependency> dependencies = group.getDependencies();
		if( dependencies.size() > 0 ) {
			for( SettingDependency child : dependency.getDependencies() ) {
				addGroupDependencyWatchers( settings, group, child );
			}
		}
	}

	private void addSettingDependencyWatchers( Settings settings, Setting setting, SettingDependency dependency ) {
		settings.addSettingsListener( new SettingDependencyWatcher( dependency, setting ) );

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
			if( this.key == null ) return;
			if( key.equals( event.getKey() ) ) group.updateState();
		}

	}

	private static final class SettingDependencyWatcher implements EventHandler<SettingsEvent> {

		private Setting setting;

		private String key;

		public SettingDependencyWatcher( SettingDependency dependency, Setting setting ) {
			this.setting = setting;
			this.key = dependency.getKey();
		}

		@Override
		public void handle( SettingsEvent event ) {
			if( event.getEventType() != SettingsEvent.CHANGED ) return;
			if( key.equals( event.getKey() ) ) setting.updateState();
		}

	}

	private static class GroupChangeHandler implements NodeListener {

		private SettingGroup group;

		private Pane pane;

		public GroupChangeHandler( SettingGroup group, Pane pane ) {
			this.group = group;
			this.pane = pane;
		}

		@Override
		public void nodeEvent( NodeEvent event ) {
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

	private static class EditorChangeHandler implements NodeListener, EventHandler<SettingsEvent> {

		private SettingEditor editor;

		public EditorChangeHandler( SettingEditor editor, Setting setting ) {
			this.editor = editor;
			setting.addNodeListener( this );
			setting.getSettings().addSettingsListener( this );
		}

		@Override
		public void nodeEvent( NodeEvent event ) {
			EventType<? extends NodeEvent> type = event.getEventType();
			if( type != NodeEvent.VALUE_CHANGED ) return;

			switch( event.getKey() ) {
				case "disable": {
					editor.setDisable( (Boolean)event.getNewValue() );
					break;
				}
				case "visible": {
					editor.setVisible( (Boolean)event.getNewValue() );
					break;
				}
			}
		}

		@Override
		public void handle( SettingsEvent event ) {
			if( event.getEventType() != SettingsEvent.CHANGED ) return;

			// Forward the event to the editor
			editor.handle( event );
		}

	}

}
