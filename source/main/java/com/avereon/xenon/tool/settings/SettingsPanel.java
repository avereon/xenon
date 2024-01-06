package com.avereon.xenon.tool.settings;

import com.avereon.data.NodeEvent;
import com.avereon.event.EventHandler;
import com.avereon.log.LazyEval;
import com.avereon.settings.Settings;
import com.avereon.settings.SettingsEvent;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.zarra.javafx.Fx;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import lombok.CustomLog;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CustomLog
public class SettingsPanel extends VBox {

	private Map<String, SettingOptionProvider> optionProviders;

	protected SettingsPanel() {}

	protected SettingsPanel( Map<String, SettingOptionProvider> optionProviders ) {
		this.optionProviders = optionProviders;
	}

	protected void addTitle( String title ) {
		// Add the title label
		Label titleLabel = new Label( title );
		titleLabel.setFont( Font.font( titleLabel.getFont().getFamily(), 2 * titleLabel.getFont().getSize() ) );
		titleLabel.prefWidthProperty().bind( widthProperty() );
		titleLabel.getStyleClass().add( "setting-title" );
		titleLabel.setAlignment( Pos.CENTER );
		//titleLabel.setBorder( new Border( new BorderStroke( Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		addBlankLine();
		getChildren().add( titleLabel );
		addBlankLine();
	}

	protected void addBlankLine() {
		Label blankLine = new Label( " " );
		blankLine.prefWidthProperty().bind( widthProperty() );
		blankLine.getStyleClass().add( "setting-blank" );
		blankLine.setAlignment( Pos.CENTER );
		getChildren().add( blankLine );
	}

	protected Control createGroupPane( XenonProgramProduct product, String rbKey, SettingsPage page, String name, SettingGroup group ) {
		Pane pane = createSettingsPane( product, rbKey, page, group );

		group.register( NodeEvent.ANY, new GroupChangeHandler( group, pane ) );

		Settings pageSettings = page.getSettings();
		List<SettingDependency> dependencies = group.getDependencies();
		if( !dependencies.isEmpty() ) {
			for( SettingDependency dependency : dependencies ) {
				addGroupDependencyWatchers( pageSettings, group, dependency );
			}
		}

		group.updateState();

		TitledPane groupPane = new TitledPane( name, pane );
		groupPane.setCollapsible( group.isCollapsible() );
		groupPane.setExpanded( group.isExpanded() );
		return groupPane;
	}

	protected Pane createSettingsPane( XenonProgramProduct product, String rbKey, SettingsPage page, SettingGroup group ) {
		GridPane grid = new GridPane();
		grid.setHgap( UiFactory.PAD );
		grid.setVgap( UiFactory.PAD );
		//pane.setBorder( new Border( new BorderStroke( Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.MEDIUM ) ) );

		ColumnConstraints labelColumnConstraints = new ColumnConstraints();
		ColumnConstraints editorColumnConstraints = new ColumnConstraints();
		labelColumnConstraints.setHgrow( Priority.SOMETIMES );
		editorColumnConstraints.setHgrow( Priority.ALWAYS );
		grid.getColumnConstraints().addAll( labelColumnConstraints, editorColumnConstraints );

		int row = 0;
		for( SettingData setting : group.getSettingsList() ) {
			// Get the editor type
			String editorType = setting.getEditor();
			if( editorType == null ) editorType = "textline";

			// Determine the editor class
			Class<? extends SettingEditor> editorClass = SettingEditor.getType( editorType );
			if( editorClass == null ) {
				log.atWarn().log( "Setting editor not registered: %s", editorType );
				editorClass = SettingEditor.getType( "textline" );
			}

			// Determine setting option provider, if any
			String providerId = setting.getProvider();
			if( optionProviders != null ) {
				setting.setOptionProvider( providerId == null ? null : optionProviders.get( providerId ) );
			}

			// Create the editor
			SettingEditor editor = createSettingEditor( product, rbKey, setting, editorClass );
			if( editor != null ) editor.addComponents( grid, row++ );
			if( editor == null ) log.atDebug().log( "Editor not created: %s", LazyEval.of( editorClass::getName ) );

			// Add a watcher to each dependency
			Settings pageSettings = page.getSettings();
			List<SettingDependency> dependencies = setting.getDependencies();
			if( !dependencies.isEmpty() ) {
				for( SettingDependency dependency : dependencies ) {
					addSettingDependencyWatchers( pageSettings, setting, dependency );
				}
			}

			setting.updateState();
		}

		return grid;
	}

	protected SettingEditor createSettingEditor( XenonProgramProduct product, String rbKey, SettingData setting, Class<? extends SettingEditor> editorClass ) {
		// Try loading a class from the type
		SettingEditor editor = null;

		try {
			Constructor<? extends SettingEditor> constructor = editorClass.getConstructor( XenonProgramProduct.class, String.class, SettingData.class );
			editor = constructor.newInstance( product, rbKey, setting );
		} catch( Exception exception ) {
			log.atError( exception ).log( "Error creating setting editor: %s", LazyEval.of( editorClass::getName ) );
		}
		if( editor == null ) return null;

		// Add the change handler
		new EditorChangeHandler( editor );

		return editor;
	}

	private void addGroupDependencyWatchers( Settings settings, SettingGroup group, SettingDependency dependency ) {
		settings.register( SettingsEvent.CHANGED, new GroupDependencyWatcher( dependency, group ) );

		List<SettingDependency> dependencies = group.getDependencies();
		if( !dependencies.isEmpty() ) {
			for( SettingDependency child : dependency.getDependencies() ) {
				addGroupDependencyWatchers( settings, group, child );
			}
		}
	}

	private void addSettingDependencyWatchers( Settings settings, SettingData setting, SettingDependency dependency ) {
		settings.register( SettingsEvent.CHANGED, new SettingDependencyWatcher( dependency, setting ) );

		List<SettingDependency> dependencies = setting.getDependencies();
		if( !dependencies.isEmpty() ) {
			for( SettingDependency child : dependency.getDependencies() ) {
				addSettingDependencyWatchers( settings, setting, child );
			}
		}
	}

	private static final class GroupDependencyWatcher implements EventHandler<SettingsEvent> {

		private final SettingGroup group;

		private final String key;

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

		private final SettingData setting;

		public SettingDependencyWatcher( SettingDependency dependency, SettingData setting ) {
			if( dependency.getKey() == null ) throw new NullPointerException( "Dependency key cannot be null for " + setting.getKey() );
			this.dependencyKey = dependency.getKey();
			this.setting = setting;
		}

		@Override
		public void handle( SettingsEvent event ) {
			if( Objects.equals( event.getKey(), dependencyKey ) ) setting.updateState();
		}

	}

	private record GroupChangeHandler(SettingGroup group, Pane pane) implements EventHandler<NodeEvent> {

		@Override
		public void handle( NodeEvent event ) {
			if( event.getSource() != group || event.getEventType() != NodeEvent.VALUE_CHANGED ) return;

			switch( event.getKey() ) {
				case SettingData.DISABLE -> setDisable( event.getNewValue() );
				case SettingData.VISIBLE -> setVisible( event.getNewValue() );
			}
		}

		private void setDisable( boolean disable ) {
			pane.setDisable( disable );
		}

		private void setVisible( boolean visible ) {
			pane.setVisible( visible );
		}

	}

	private record EditorChangeHandler(SettingEditor editor) {

		private EditorChangeHandler( SettingEditor editor ) {
			this.editor = editor;
			SettingData setting = editor.getSetting();

			// Register a handler when the setting value changes to update the editor
			setting.getSettings().register( SettingsEvent.CHANGED, editor::handle );

			// Register a handler on the setting node to update other setting nodes
			setting.register( NodeEvent.VALUE_CHANGED, this::handleNodeEvent );
		}

		private void handleNodeEvent( NodeEvent event ) {
			switch( event.getKey() ) {
				case SettingData.DISABLE -> Fx.run( () -> editor.setDisable( event.getNewValue() ) );
				case SettingData.VISIBLE -> Fx.run( () -> editor.setVisible( event.getNewValue() ) );
			}
		}

	}

}
