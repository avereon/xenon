package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.node.NodeEvent;
import com.xeomar.xenon.node.NodeListener;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.settings.SettingsEvent;
import com.xeomar.xenon.settings.SettingsListener;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

public class SettingsPanel extends VBox {

	private static final Logger log = LoggerFactory.getLogger( SettingsPanel.class );

	public static final String SETTING_KEY = "id";

	public static final String GROUP_KEY = "id";

	public static final String PREFERENCE_KEY = "key";

	public static final String PRESENTATION = "presentation";

	private String[] fontNames;

	private String[] fontStyles;

	private String[] fontSizes;

	// TODO Add Apply button and change functionality accordingly.
	// TODO Add a default button to set individual setting back to default.
	// TODO Add an undo button to set individual setting back to previous.

	public SettingsPanel( Product product, SettingsPage page ) {
		String fontPlain = product.getResourceBundle().getString( "settings", "font-plain" );
		String fontBold = product.getResourceBundle().getString( "settings", "font-bold" );
		String fontItalic = product.getResourceBundle().getString( "settings", "font-italic" );
		String fontBoldItalic = product.getResourceBundle().getString( "settings", "font-bold-italic" );

		List<String> fontFamilies = Font.getFamilies();
		fontNames = fontFamilies.toArray( new String[ fontFamilies.size() ] );
		fontStyles = new String[]{ fontPlain, fontBold, fontItalic, fontBoldItalic };
		fontSizes = new String[]{ "8", "10", "12", "14", "16", "18", "20", "22", "24", "26" };

		//setBorder( new Border( new BorderStroke( Color.BLUE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		// Get the title
		String title = page.getTitle();
		if( title == null ) title = product.getResourceBundle().getString( "settings", page.getId() );

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
			String name = product.getResourceBundle().getString( "settings", group.getId() );
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
		//		VBox box = new VBox();
		//		box.getChildren().add( createSettingsPane( product, page, group ) );

		Pane pane = createSettingsPane( product, page, group );

		// TODO group.addDataListener( new GroupChangeHandler( group, panel, titleBorder, roundedBorder ) );

		Settings pageSettings = page.getSettings();
		List<SettingDependency> dependencies = group.getDependencies();
		if( dependencies.size() > 0 ) {
			for( SettingDependency dependency : dependencies ) {
				// TODO addGroupDependencyWatchers( pageSettings, group, dependency );
			}
		}

		group.updateFlags();

		TitledPane groupPane = new TitledPane( name, pane );
		groupPane.setCollapsible( false );
		groupPane.setExpanded( true );
		return groupPane;
	}

	private Pane createSettingsPane( Product product, SettingsPage page, SettingGroup group ) {
		GridPane pane = new GridPane();
		pane.setBorder( new Border( new BorderStroke( Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		int row = 0;
		for( Setting setting : group.getSettings() ) {
			// Get the presentation attribute.
			String presentation = setting.getPresentation();
			if( presentation == null ) presentation = "text";

			presentation = "text";

			// Determine the editor class.
			Class<? extends SettingEditor> editorClass = SettingEditor.getType( presentation );

			// Create the editor.
			if( editorClass == null ) {
				log.warn( "Setting editor not registered: {}", presentation );
			} else {
				SettingEditor editor = createSettingEditor( product, setting, editorClass );
				if( editor != null ) editor.addComponents( pane, row++ );
				if( editor == null ) log.debug( "Editor not created: ", editorClass.getName() );
			}

			// TODO			// Add a watcher to each dependency.
			//			Settings pageSettings = (Settings)page.getResource( SettingsPageParser.SETTINGS );
			//			DataList<Dependency> dependencies = setting.getDependencies();
			//			if( dependencies.size() > 0 ) {
			//				for( Dependency dependency : dependencies ) {
			//					addSettingDependencyWatchers( pageSettings, setting, dependency );
			//				}

			setting.updateEnabled();
		}

		//pane.add( item, "");

		return pane;
	}

	private SettingEditor createSettingEditor( Product product, Setting setting, Class<? extends SettingEditor> editorClass ) {
		// Try loading a class from the type.
		SettingEditor editor = null;

		try {
			Constructor<? extends SettingEditor> constructor = editorClass.getConstructor( Product.class, Setting.class );
			editor = constructor.newInstance( product, setting );
		} catch( Exception exception ) {
			log.error( "Error creating setting editor: " + editorClass.getName(), exception );
		}
		if( editor == null ) return null;

		// Add the change handler.
		new EditorChangeHandler( setting, editor );

		return editor;
	}

	//	private void addGroupDependencyWatchers( Settings settings, SettingGroup group, Dependency dependency ) {
	//		settings.addSettingListener( (String)dependency.getAttribute( Setting.KEY ), new GroupDependencyWatcher( dependency, group ) );
	//
	//		DataList<Dependency> dependencies = group.getDependencies();
	//		if( dependencies.size() > 0 ) {
	//			for( Dependency child : dependency ) {
	//				addGroupDependencyWatchers( settings, group, child );
	//			}
	//		}
	//	}
	//
	//	private void addSettingDependencyWatchers( Settings settings, Setting setting, Dependency dependency ) {
	//		settings.addSettingListener( (String)dependency.getAttribute( Setting.KEY ), new SettingDependencyWatcher( dependency, setting ) );
	//
	//		DataList<Dependency> dependencies = setting.getDependencies();
	//		if( dependencies.size() > 0 ) {
	//			for( Dependency child : dependency ) {
	//				addSettingDependencyWatchers( settings, setting, child );
	//			}
	//		}
	//	}
	//
	//	private static final class GroupDependencyWatcher implements SettingListener {
	//
	//		private SettingGroup group;
	//
	//		private String key;
	//
	//		public GroupDependencyWatcher( Dependency dependency, SettingGroup setting ) {
	//			this.group = setting;
	//			this.key = (String)dependency.getAttribute( PREFERENCE_KEY );
	//		}
	//
	//		@Override
	//		public void settingChanged( SettingEvent event ) {
	//			if( this.key == null ) return;
	//			if( key.equals( event.getFullPath() ) ) group.updateEnabled();
	//		}
	//
	//	}
	//
	//	private static final class SettingDependencyWatcher implements SettingListener {
	//
	//		private Setting setting;
	//
	//		private String key;
	//
	//		public SettingDependencyWatcher( Dependency dependency, Setting setting ) {
	//			this.setting = setting;
	//			this.key = (String)dependency.getAttribute( PREFERENCE_KEY );
	//		}
	//
	//		@Override
	//		public void settingChanged( SettingEvent event ) {
	//			if( this.key == null ) return;
	//			if( key.equals( event.getFullPath() ) ) setting.updateEnabled();
	//		}
	//
	//	}
	//
	//	private static class GroupChangeHandler extends DataAdapter {
	//
	//		private SettingGroup group;
	//
	//		private JComponent panel;
	//
	//		private TitledBorder title;
	//
	//		private RoundedLineBorder border;
	//
	//		public GroupChangeHandler( SettingGroup group, JComponent panel, TitledBorder title, RoundedLineBorder border ) {
	//			this.group = group;
	//			this.panel = panel;
	//			this.title = title;
	//			this.border = border;
	//		}
	//
	//		@Override
	//		public void dataAttributeChanged( DataAttributeEvent event ) {
	//			if( event.getCause() != group ) return;
	//
	//			switch( event.getAttributeName() ) {
	//				case "enabled": {
	//					setEnabled( (Boolean)event.getNewValue() );
	//					break;
	//				}
	//				case "visible": {
	//					setVisible( (Boolean)event.getNewValue() );
	//					break;
	//				}
	//			}
	//		}
	//
	//		protected final void setEnabled( boolean enabled ) {
	//			java.awt.Color textEnabledColor = UIManager.getColor( "textForeground" );
	//			java.awt.Color textDisabledColor = UIManager.getColor( "textInactiveText" );
	//			java.awt.Color enabledColor = UIManager.getColor( "nimbusSelection" );
	//			java.awt.Color disabledColor = UIManager.getColor( "nimbusBorder" );
	//			if( title != null ) title.setTitleColor( enabled ? textEnabledColor : textDisabledColor );
	//			if( border != null ) border.setColor( enabled ? enabledColor : disabledColor );
	//			panel.setBackground( enabled ? enabledColor : disabledColor );
	//			panel.repaint();
	//		}
	//
	//		protected final void setVisible( boolean visible ) {
	//			panel.setVisible( visible );
	//		}
	//
	//	}

	private static class EditorChangeHandler implements NodeListener, SettingsListener {

		private SettingEditor editor;

		public EditorChangeHandler( Setting setting, SettingEditor editor ) {
			this.editor = editor;
			setting.addNodeListener( this );
			setting.getSettings().addSettingsListener( this );
		}

		@Override
		public void event( SettingsEvent event ) {
			// Forward the event to the editor
			editor.event( event );
		}

		@Override
		public void eventOccurred( NodeEvent event ) {
			NodeEvent.Type type = event.getType();
			if( type != NodeEvent.Type.VALUE_CHANGED ) return;

			switch( event.getKey() ) {
				case "enabled": {
					editor.setEnabled( (Boolean)event.getNewValue() );
					break;
				}
				case "visible": {
					editor.setVisible( (Boolean)event.getNewValue() );
					break;
				}
			}
		}

	}

}
