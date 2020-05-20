package com.avereon.xenon.tool;

import com.avereon.undo.BasicUndoScope;
import com.avereon.undo.UndoScope;
import com.avereon.util.FileUtil;
import com.avereon.util.Log;
import com.avereon.util.UriUtil;
import com.avereon.venza.javafx.FxUtil;
import com.avereon.xenon.Action;
import com.avereon.xenon.Program;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.*;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.awt.event.KeyEvent;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

//import org.apache.commons.vfs2.FileSystemException;
//import org.apache.commons.vfs2.FileSystemManager;
//import org.apache.commons.vfs2.VFS;

public class AssetTool extends GuidedTool {

	private enum Mode {
		OPEN,
		SAVE
	}

	private static final System.Logger log = Log.get();

	private Mode mode = Mode.OPEN;

	private final Guide guide;

	private final TextField uriField;

	private final Button goButton;

	private final HBox userNotice;

	private final Label userMessage;

	private final Label closeLabel;

	private final ObservableList<Asset> children;

	private final TableView<Asset> table;

	private final TableColumn<Asset, Node> assetLabel;

	private final Action priorAction;

	private final Action nextAction;

	private final Action parentAction;

	private final UndoScope undoScope;

	private Asset parentAsset;

	private Asset currentAsset;

	public AssetTool( ProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-asset" );

		// URI input bar
		uriField = new TextField();
		HBox.setHgrow( uriField, Priority.ALWAYS );
		goButton = new Button();

		// User notice bar
		userMessage = new Label( "user message" );
		userMessage.setMaxWidth( Double.MAX_VALUE );
		userMessage.setMaxHeight( Double.MAX_VALUE );
		userMessage.setPadding( new Insets( 0, UiFactory.PAD, 0, UiFactory.PAD ) );
		HBox.setHgrow( userMessage, Priority.ALWAYS );
		closeLabel = new Label( "", getProgram().getIconLibrary().getIcon( "workarea-close" ) );
		closeLabel.setPadding( new Insets( UiFactory.PAD ) );
		userNotice = new HBox( UiFactory.PAD, userMessage, closeLabel );
		userNotice.setId( "user-notice" );
		userNotice.setAlignment( Pos.CENTER );
		closeLabel.setOnMousePressed( e -> closeUserNotice() );
		closeUserNotice();

		// Asset table
		table = new TableView<>( children = FXCollections.observableArrayList() );
		VBox.setVgrow( table, Priority.ALWAYS );
		table.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY );
		assetLabel = new TableColumn<>( "Name" );
		assetLabel.setCellValueFactory( new NameValueFactory( getProgram() ) );
		assetLabel.setComparator( new AssetLabelComparator() );
		assetLabel.setSortType( TableColumn.SortType.ASCENDING );
		TableColumn<Asset, String> assetUri = new TableColumn<>( "URI" );
		assetUri.setCellValueFactory( new PropertyValueFactory<>( "uri" ) );
		TableColumn<Asset, String> assetSize = new TableColumn<>( "Size" );
		assetSize.setCellValueFactory( new SizeValueFactory( getProgram() ) );
		assetSize.setStyle( "-fx-alignment: CENTER-RIGHT;" );
		table.getColumns().add( assetLabel );
		table.getColumns().add( assetUri );
		table.getColumns().add( assetSize );

		table.getSortOrder().add( assetLabel );

		// Tool layout
		VBox layout = new VBox( UiFactory.PAD );
		layout.setPadding( new Insets( UiFactory.PAD ) );
		layout.getChildren().add( new HBox( UiFactory.PAD, uriField, goButton ) );
		layout.getChildren().add( userNotice );
		layout.getChildren().add( table );
		getChildren().add( layout );

		// Actions
		priorAction = new PriorAction( getProgram() );
		nextAction = new PriorAction( getProgram() );
		parentAction = new ParentAction( getProgram() );
		undoScope = new BasicUndoScope();

		// Basic behavior
		uriField.setOnKeyPressed( e -> {
			if( e.getCode().getCode() == KeyEvent.VK_ESCAPE && userNotice.isVisible() ) closeUserNotice();
		} );
		uriField.setOnAction( e -> selectAsset( uriField.getText() ) );
		goButton.setOnAction( e -> selectAsset( uriField.getText() ) );
		table.setOnMousePressed( e -> {
			int clickCount = Integer.parseInt( getSettings().get( "click-count", "1" ) );
			@SuppressWarnings( "unchecked" ) TableView<Asset> table = (TableView<Asset>)e.getSource();
			Asset item = table.getSelectionModel().getSelectedItem();
			if( item != null && e.getClickCount() >= clickCount ) selectAsset( item.getUri().toString() );
		} );

		guide = initializeGuide();
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		// TODO Put the columns in the preferred order
		// TODO Set the columns to the preferred size
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		mode = resolveMode( request.getFragment() );

		// Set the title depending on the mode requested
		String action = mode.name().toLowerCase();
		setTitle( getProduct().rb().text( "action", action + ".name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "asset-" + action ) );
		goButton.setGraphic( getProgram().getIconLibrary().getIcon( "asset-" + action ) );

		// Select the current asset
		Path currentFolder = FileUtil.findValidFolder( getProgram()
			.getProgramSettings()
			.get( AssetManager.CURRENT_FOLDER_SETTING_KEY, System.getProperty( "user.dir" ) ) );
		selectAsset( currentFolder.toAbsolutePath().toString() );
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();
		pushAction( "prior", priorAction );
		pushAction( "next", nextAction );
		pushAction( "up", parentAction );
		pushToolActions( "prior", "next", "up" );
	}

	@Override
	protected void conceal() throws ToolException {
		super.conceal();
		pullToolActions();
		pullAction( "up", parentAction );
		pullAction( "next", nextAction );
		pullAction( "prior", priorAction );
	}

	@Override
	protected Guide getGuide() {
		return guide;
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( newNodes.isEmpty() ) return;
		selectAsset( newNodes.stream().findAny().get().getId() );
	}

	private static Mode resolveMode( String fragment ) {
		if( fragment == null ) return Mode.OPEN;
		try {
			return Mode.valueOf( fragment.trim().toUpperCase() );
		} catch( IllegalArgumentException exception ) {
			return Mode.OPEN;
		}
	}

	private void selectAsset( Asset asset ) {
		selectAsset( asset.getUri() );
	}

	private void selectAsset( URI uri ) {
		selectAsset( uri.toString() );
	}

	private void selectAsset( String text ) {
		Objects.requireNonNull( text );

		FxUtil.assertFxThread();
		uriField.setText( text );

		try {
			Asset asset = getProgram().getAssetManager().createAsset( text );

			if( asset.isFolder() ) {
				loadFolder( asset );
			} else {
				if( mode == Mode.OPEN ) {
					if( !asset.exists() ) {
						notifyUser( "asset-not-found", text );
					} else {
						getProgram().getAssetManager().openAsset( asset.getUri() );
						close();
						return;
					}
				} else if( mode == Mode.SAVE ) {
					// TODO The save action
					//getProgram().getAssetManager().saveAsAsset( incomingAsset, asset.getUri() );
					close();
					return;
				}
			}
			activateUriField();
		} catch( AssetException exception ) {
			notifyUser( "asset-error", exception.getMessage() );
			log.log( Log.ERROR, exception );
		} finally {
			updateActionState();
		}
	}

	private void updateActionState() {
		priorAction.updateEnabled();
		nextAction.updateEnabled();
		parentAction.updateEnabled();
	}

	private Asset getCurrentAsset() {
		return currentAsset;
	}

	private void loadFolder( Asset asset ) {
		getProgram().getProgramSettings().set( AssetManager.CURRENT_FOLDER_SETTING_KEY, asset.getFile().toString() );
		currentAsset = asset;
		updateActionState();
		closeUserNotice();

		getProgram().getTaskManager().submit( Task.of( "load-folder", () -> {
			try {
				parentAsset = getProgram().getAssetManager().getParent( asset );
				List<Asset> assets = asset.getChildren();
				Platform.runLater( () -> {
					children.clear();
					children.addAll( assets );
					table.sort();
				} );
			} catch( AssetException exception ) {
				notifyUser( "asset-error", exception.getMessage() );
				log.log( Log.ERROR, exception );
			}
		} ) );
	}

	private void activateUriField() {
		Platform.runLater( () -> {
			uriField.requestFocus();
			uriField.positionCaret( uriField.getText().length() );
		} );
	}

	private void notifyUser( String messageKey, String... parameters ) {
		@SuppressWarnings( "ConfusingArgumentToVarargsMethod" ) String message = getProduct().rb().text( "program", messageKey, parameters );

		Platform.runLater( () -> {
			userMessage.setText( message );
			userNotice.setManaged( true );
			userNotice.setVisible( true );
		} );
	}

	private void closeUserNotice() {
		userNotice.setVisible( false );
		userNotice.setManaged( false );
	}

	private Guide initializeGuide() {
		Guide guide = new Guide();

		// TODO Load the asset roots
		// Go through the supported schemes and get the roots
		// or, let the roots be defined in asset manager
		// or, let the roots be defined in asset tool

		try {
			guide.addNode( createGuideNode( "Home", "asset-home", System.getProperty( "user.home" ) ) );

			for( Path path : FileSystems.getDefault().getRootDirectories() ) {
				guide.addNode( createGuideNode( UriUtil.parseName( path.toUri() ), "asset-root", path.toString() ) );
			}
		} catch( AssetException exception ) {
			log.log( Log.ERROR, exception );
		}

		return guide;
	}

	private GuideNode createGuideNode( String name, String icon, String path ) throws AssetException {
		Asset asset = getProgram().getAssetManager().createAsset( path );
		GuideNode node = new GuideNode( getProgram(), asset.getUri().toString(), name, icon );
		asset.register( Asset.ICON_VALUE_KEY, e -> node.setIcon( e.getNewValue() ) );
		return node;
	}

	/**
	 * A table value factory for the asset label.
	 */
	private static class NameValueFactory implements Callback<javafx.scene.control.TableColumn.CellDataFeatures<Asset, Node>, ObservableValue<Node>> {

		private final Program program;

		public NameValueFactory( Program program ) {
			this.program = program;
		}

		@Override
		public ObservableValue<Node> call( TableColumn.CellDataFeatures<Asset, Node> assetStringCellDataFeatures ) {
			Asset asset = assetStringCellDataFeatures.getValue();
			String name = asset.getName();
			Node icon = program.getIconLibrary().getIcon( assetStringCellDataFeatures.getValue().getIcon() );
			Label label = new Label( name, icon );
			label.getProperties().put( "asset", asset );
			return new ReadOnlyObjectWrapper<>( label );
		}

	}

	/**
	 * A table value factory for the asset size.
	 */
	private static class SizeValueFactory implements Callback<TableColumn.CellDataFeatures<Asset, String>, ObservableValue<String>> {

		private final Program program;

		public SizeValueFactory( Program program ) {
			this.program = program;
		}

		@Override
		public ObservableValue<String> call( TableColumn.CellDataFeatures<Asset, String> assetStringCellDataFeatures ) {
			try {
				Asset asset = assetStringCellDataFeatures.getValue();
				long size = asset.getSize();
				if( asset.isFolder() ) return new ReadOnlyObjectWrapper<>( program.rb().text( "asset", "asset-open-folder-size", size ) );
				return new ReadOnlyObjectWrapper<>( FileUtil.getHumanSize( size, false, true ) );
			} catch( AssetException exception ) {
				log.log( Log.ERROR, exception );
				return new ReadOnlyObjectWrapper<>( "" );
			}
		}

	}

	private static final class AssetLabelComparator implements Comparator<Node> {

		private final Comparator<Asset> assetComparator = new AssetTypeAndNameComparator();

		@Override
		public int compare( Node o1, Node o2 ) {
			Asset asset1 = (Asset)o1.getProperties().get( "asset" );
			Asset asset2 = (Asset)o2.getProperties().get( "asset" );
			return assetComparator.compare( asset1, asset2 );
		}
	}

	private final class PriorAction extends Action {

		protected PriorAction( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return undoScope.canUndo();
		}

		@Override
		public void handle( ActionEvent event ) {
			undoScope.undo();
		}

	}

	private final class NextAction extends Action {

		protected NextAction( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return undoScope.canRedo();
		}

		@Override
		public void handle( ActionEvent event ) {
			undoScope.redo();
		}

	}

	private final class ParentAction extends Action {

		protected ParentAction( Program program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return currentAsset != null && UriUtil.hasParent( currentAsset.getUri() );
		}

		@Override
		public void handle( ActionEvent event ) {
			selectAsset( parentAsset );
		}

	}

}
