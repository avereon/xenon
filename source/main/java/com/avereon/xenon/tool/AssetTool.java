package com.avereon.xenon.tool;

import com.avereon.product.Rb;
import com.avereon.util.FileUtil;
import com.avereon.util.UriUtil;
import com.avereon.xenon.*;
import com.avereon.xenon.asset.*;
import com.avereon.xenon.asset.exception.AssetException;
import com.avereon.xenon.scheme.FileScheme;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import com.avereon.zarra.javafx.Fx;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@CustomLog
public class AssetTool extends GuidedTool {

	private enum Mode {
		OPEN,
		SAVE
	}

	private final Callback<AssetWatchEvent, Void> eventCallback;

	private final TextField uriField;

	private final Button goButton;

	private final HBox userNotice;

	private final Label userMessage;

	private final ComboBox<AssetFilter> filters;

	private final TableColumn<Asset, Node> iconColumn;

	private final TableColumn<Asset, Label> nameColumn;

	private final TableColumn<Asset, String> uriColumn;

	private final TableColumn<Asset, Node> sizeColumn;

	private final TableView<Asset> assetTable;

	private final ObservableList<Asset> assets;

	private final FilteredList<Asset> filtered;

	private final SortedList<Asset> sorted;

	private final RefreshAction refreshAction;

	private final ProgramAction priorAction;

	private final ProgramAction nextAction;

	private final ProgramAction parentAction;

	private final NewFolderAction newFolderAction;

	private final DeleteAction deleteAction;

	private final LinkedList<String> history;

	private Mode mode = Mode.OPEN;

	private Asset parentAsset;

	@Getter
	private Asset currentFolder;

	@Getter
	private String currentFilename;

	private int currentIndex;

	@Setter
	private Consumer<Asset> saveActionConsumer;

	public AssetTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-asset" );

		this.eventCallback = this::handleExternalAssetEvent;

		// URI input bar
		uriField = new TextField();
		HBox.setHgrow( uriField, Priority.ALWAYS );
		goButton = new Button();

		// Asset filters
		filters = new ComboBox<>();
		AssetFilter anyAssetFilter = new AnyAssetFilter();
		filters.getItems().add( anyAssetFilter );
		filters.getSelectionModel().select( anyAssetFilter );

		// User notice bar
		userMessage = new Label( "user message" );
		userMessage.setMaxWidth( Double.MAX_VALUE );
		userMessage.setMaxHeight( Double.MAX_VALUE );
		userMessage.setPadding( new Insets( 0, UiFactory.PAD, 0, UiFactory.PAD ) );
		HBox.setHgrow( userMessage, Priority.ALWAYS );
		Label closeLabel = new Label( "", getProgram().getIconLibrary().getIcon( "workarea-close" ) );
		closeLabel.setPadding( new Insets( UiFactory.PAD ) );
		userNotice = new HBox( UiFactory.PAD, userMessage, closeLabel );
		userNotice.setId( "user-notice" );
		userNotice.setAlignment( Pos.CENTER );
		closeLabel.setOnMousePressed( e -> closeUserNotice() );

		String nameColumnHeader = Rb.text( RbKey.LABEL, "name", "Name" );
		String uriColumnHeader = Rb.text( RbKey.LABEL, "uri", "URI" );
		String sizeColumnHeader = Rb.text( RbKey.LABEL, "size", "Size" );

		// The asset collections
		assets = FXCollections.observableArrayList();
		filtered = new FilteredList<>( assets, i -> true );
		sorted = new SortedList<>( filtered );

		// Table columns -----------------------------------------------------------
		Node icon = getProgram().getIconLibrary().getIcon( "asset" );
		double columnWidth = icon.getBoundsInLocal().getWidth() + 8;

		iconColumn = new TableColumn<>( "" );
		iconColumn.setCellValueFactory( new IconValueFactory( getProgram() ) );
		iconColumn.setSortable( false );
		iconColumn.setResizable( false );
		iconColumn.setMaxWidth( columnWidth );
		iconColumn.setMinWidth( iconColumn.getMaxWidth() );

		nameColumn = new TableColumn<>( nameColumnHeader );
		nameColumn.setCellValueFactory( new NameValueFactory() );
		nameColumn.setComparator( new AssetLabelComparator() );
		nameColumn.setSortType( TableColumn.SortType.ASCENDING );
		nameColumn.setCellFactory( TextFieldTableCell.forTableColumn( new StringLabelConverter() ) );
		nameColumn.onEditCommitProperty().set( this::doUpdateAssetName );
		nameColumn.setEditable( true );

		uriColumn = new TableColumn<>( uriColumnHeader );
		uriColumn.setCellValueFactory( new PropertyValueFactory<>( "uri" ) );

		sizeColumn = new TableColumn<>( sizeColumnHeader );
		sizeColumn.setCellValueFactory( new SizeValueFactory() );
		sizeColumn.setComparator( new AssetSizeComparator() );
		sizeColumn.setStyle( "-fx-alignment: CENTER-RIGHT;" );

		// Asset table -------------------------------------------------------------
		assetTable = new TableView<>( sorted );
		assetTable.setEditable( true );
		assetTable.getColumns().add( iconColumn );
		assetTable.getColumns().add( nameColumn );
		assetTable.getColumns().add( uriColumn );
		assetTable.getColumns().add( sizeColumn );
		assetTable.setColumnResizePolicy( TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN );
		VBox.setVgrow( assetTable, Priority.ALWAYS );

		sorted.comparatorProperty().bind( assetTable.comparatorProperty() );
		filtered.predicateProperty().bind( filters.getSelectionModel().selectedItemProperty() );

		// Tool layout
		VBox layout = new VBox( UiFactory.PAD );
		layout.setPadding( new Insets( UiFactory.PAD ) );
		layout.getChildren().add( new HBox( UiFactory.PAD, uriField, goButton ) );
		layout.getChildren().add( filters );
		layout.getChildren().add( userNotice );
		layout.getChildren().add( assetTable );
		getChildren().add( layout );

		// Actions
		refreshAction = new RefreshAction( getProgram() );
		priorAction = new PriorAction( getProgram() );
		nextAction = new NextAction( getProgram() );
		parentAction = new ParentAction( getProgram() );
		newFolderAction = new NewFolderAction( getProgram() );
		deleteAction = new DeleteAction( getProgram() );

		history = new LinkedList<>();
		currentIndex = -1;

		// Basic behavior
		uriField.setOnKeyPressed( e -> {
			if( e.getCode().getCode() == KeyEvent.VK_ESCAPE && userNotice.isVisible() ) closeUserNotice();
		} );
		uriField.setOnAction( e -> selectAsset( uriField.getText() ) );
		goButton.setOnAction( this::doGoAction );
		assetTable.setOnMousePressed( this::doMousePressed );
		assetTable.getSelectionModel().selectedItemProperty().addListener( ( p, o, n ) -> updateActionState() );

		Guide guide = createGuide();
		getGuideContext().getGuides().add( guide );
		getGuideContext().setCurrentGuide( guide );

		closeUserNotice();
	}

	public ObservableList<AssetFilter> getFilters() {
		return filters.getItems();
	}

	public AssetFilter getSelectedFilter() {
		return filters.getSelectionModel().getSelectedItem();
	}

	public void setSelectedFilter( AssetFilter filter ) {
		Fx.run( () -> this.filters.getSelectionModel().select( filter ) );
	}

	public ObservableValue<AssetFilter> selectedFilter() {
		return filters.getSelectionModel().selectedItemProperty();
	}

	@Override
	protected void ready( OpenAssetRequest request ) {
		// TODO Put the columns in the preferred order
		assetTable.getSortOrder().clear();
		assetTable.getSortOrder().add( nameColumn );

		// TODO Set the columns to the preferred size
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		// Update the mode
		// FIXME The tool did not switch modes when the mode was changed
		mode = resolveMode( request.getFragment() );

		// Set the title depending on the mode requested
		String action = mode.name().toLowerCase();
		setTitle( Rb.text( "action", action + ".name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "asset-" + action ) );
		goButton.setGraphic( getProgram().getIconLibrary().getIcon( "asset-" + action ) );

		// Determine the current folder
		Path currentFolder = getProgram().getAssetManager().getCurrentFileFolder();

		// Select the current asset
		URI uri;
		try {
			uri = resolveUri( request.getQueryParameters() );
			if( uri != null && !uri.isAbsolute() ) uri = currentFolder.resolve( uri.getPath() ).toUri();
			if( uri == null ) uri = currentFolder.toUri();
			selectAsset( uri );
		} catch( URISyntaxException exception ) {
			log.atWarn( exception ).log();
		}

		if( mode == Mode.OPEN ) addSupportedFilters();
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();

		pushAction( "refresh", refreshAction );
		pushAction( "prior", priorAction );
		pushAction( "next", nextAction );
		pushAction( "up", parentAction );
		pushAction( "new-folder", newFolderAction );
		pushAction( "delete", deleteAction );

		pushTools( "new-folder | delete | refresh | prior next up" );
	}

	@Override
	protected void conceal() throws ToolException {
		super.conceal();

		pullTools();

		pullAction( "delete", deleteAction );
		pullAction( "new-folder", newFolderAction );
		pullAction( "up", parentAction );
		pullAction( "next", nextAction );
		pullAction( "prior", priorAction );
		pullAction( "refresh", refreshAction );
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

	private static URI resolveUri( Map<String, String> parameters ) throws URISyntaxException {
		if( parameters == null ) return null;
		String uriString = parameters.get( "uri" );
		return uriString == null ? null : URI.create( uriString.replace( " ", "%20" ) );
	}

	private void addSupportedFilters() {
		List<AssetFilter> filters = getProgram()
			.getAssetManager()
			.getAssetTypes()
			.stream()
			.filter( AssetType::isUserType )
			.flatMap( t -> t.getCodecs().stream() )
			.map( CodecAssetFilter::new )
			.sorted()
			.collect( Collectors.toList() );
		getFilters().addAll( 0, filters );
	}

	private void doMousePressed( MouseEvent event ) {
		if( event.isPrimaryButtonDown() ) {
			selectAssetFromTable( event );
		} else if( event.isSecondaryButtonDown() ) {
			editAssetFromTable( event );
		}
	}

	@SuppressWarnings( "unchecked" )
	private void selectAssetFromTable( MouseEvent event ) {
		int clickCount = Integer.parseInt( getSettings().get( "click-count", "2" ) );
		if( event.getClickCount() < clickCount ) return;

		TableView<Asset> table = (TableView<Asset>)event.getSource();
		Asset item = table.getSelectionModel().getSelectedItem();
		if( item == null ) return;

		try {
			if( mode == Mode.OPEN ) {
				selectAsset( item.getUri() );
			} else if( mode == Mode.SAVE ) {
				if( item.isFolder() ) {
					selectAsset( item.getUri().resolve( currentFilename ) );
				} else {
					selectAsset( item.getUri() );
				}
			}
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}
	}

	@SuppressWarnings( "unchecked" )
	private void editAssetFromTable( MouseEvent event ) {
		TableView<Asset> table = (TableView<Asset>)event.getSource();
		Asset item = table.getSelectionModel().getSelectedItem();
		if( item == null ) return;

		editAssetName( item );
	}

	private void doGoAction( ActionEvent event ) {
		selectAsset( uriField.getText() );
		try {
			if( mode == Mode.SAVE ) requestSaveAsset();
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}
		event.consume();
	}

	private void selectAsset( Asset asset ) {
		selectAsset( asset.getUri() );
	}

	private void selectAsset( URI uri ) {
		selectAsset( uri.toString() );
	}

	private void selectAsset( String path ) {
		selectAsset( path, true );
	}

	private void selectAsset( final String path, boolean updateHistory ) {
		Objects.requireNonNull( path );

		if( updateHistory ) {
			while( history.size() - 1 > currentIndex ) {
				history.pop();
			}
			history.add( path );
			currentIndex++;
		}

		try {
			Asset asset = getProgram().getAssetManager().createAsset( path );

			uriField.setText( UriUtil.decode( path ) );

			if( mode == Mode.OPEN ) {
				if( !asset.exists() ) {
					notifyUser( "asset-not-found", path );
				} else {
					if( asset.isFolder() ) {
						loadFolder( asset );
					} else {
						// TODO If the user chose "open with" then allow the user to chose a tool
						//List<Class<? extends ProgramTool>> tools = getProgram().getToolManager().getRegisteredTools( asset.getType() );

						// Use the asset manager to open the asset
						getProgram().getAssetManager().openAsset( asset.getUri() );

						// Close this tool
						close();
						return;
					}
				}
			} else if( mode == Mode.SAVE ) {
				if( asset.isFolder() ) {
					loadFolder( asset );
					uriField.setText( UriUtil.resolveToString( asset.getUri(), currentFilename ) );
				} else {
					currentFilename = asset.getFileName();
					loadFolder( getProgram().getAssetManager().getParent( asset ) );
				}
			}
			activateUriField();
		} catch( AssetException exception ) {
			handleAssetException( exception );
		} finally {
			updateActionState();
		}
	}

	private void editAssetName( Asset asset ) {
		int index = assetTable.getItems().indexOf( asset );
		if( index >= 0 ) assetTable.edit( index, nameColumn );
	}

	private void requestSaveAsset() throws AssetException {
		Asset target = getProgram().getAssetManager().resolve( currentFolder, currentFilename );
		if( saveActionConsumer != null ) saveActionConsumer.accept( target );
		close();
	}

	private void updateActionState() {
		refreshAction.updateEnabled();
		priorAction.updateEnabled();
		nextAction.updateEnabled();
		parentAction.updateEnabled();
		newFolderAction.updateEnabled();
	}

	private Void handleExternalAssetEvent( AssetWatchEvent event ) {
		log.atConfig().log( "External asset event: %s %s", event.type(), event.asset() );
		try {
			Asset folder = event.asset();
			if( !event.asset().isFolder() ) folder = getProgram().getAssetManager().getParent( event.asset() );

			// If the event is for the current folder...reload the folder
			if( folder.equals( currentFolder ) ) loadFolder( currentFolder );
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}

		return null;
	}

	private void loadFolder( Asset asset ) {
		loadFolder( asset, null );
	}

	private void loadFolder( Asset asset, Asset editAsset ) {
		if( isEditing() ) return;

		try {
			// Unregister the asset from the watcher
			if( FileScheme.ID.equals( asset.getScheme().getName() ) ) {
				getProgram().getAssetWatchService().removeWatch( asset, eventCallback );
			}

			currentFolder = asset;

			// Set the current folder with the asset manager
			if( FileScheme.ID.equals( asset.getScheme().getName() ) ) {
				getProgram().getAssetManager().setCurrentFileFolder( asset );
			}

			Fx.run( this::updateActionState );
			Fx.run( this::closeUserNotice );

			// Register the asset with the watcher
			if( FileScheme.ID.equals( asset.getScheme().getName() ) ) {
				getProgram().getAssetWatchService().registerWatch( asset, eventCallback );
			}
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}

		getProgram().getTaskManager().submit( Task.of( "load-asset", () -> {
			try {
				parentAsset = getProgram().getAssetManager().getParent( asset );
				List<Asset> assets = asset.getChildren();
				Fx.run( () -> {
					this.assets.setAll( assets );
					if( editAsset != null ) editAssetName( editAsset );
				} );
			} catch( AssetException exception ) {
				handleAssetException( exception );
			}
		} ) );
	}

	private void activateUriField() {
		Fx.run( () -> {
			uriField.requestFocus();
			uriField.positionCaret( uriField.getText().length() );
		} );
	}

	private void doCreateNewFolder() {
		if( currentFolder == null ) return;

		try {
			Scheme scheme = currentFolder.getScheme();

			// Start with the current folder
			String newFolderName = Rb.textOr( RbKey.LABEL, "new-folder", "New Folder" ) + "/";
			Asset asset = getProgram().getAssetManager().resolve( currentFolder, newFolderName );
			Asset newFolder = getNextIndexedAsset( asset );

			// Get next indexed asset
			scheme.createFolder( newFolder );

			// Reload the current folder, and start editing the new folder name
			loadFolder( currentFolder, newFolder );
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}
	}

	private void doDeleteSelectedFiles() {
		List<Asset> selectedAssets = new ArrayList<>( assetTable.getSelectionModel().getSelectedItems() );
		getProgram().getAssetManager().deleteAssets( selectedAssets );
	}

	private Asset getNextIndexedAsset( Asset asset ) throws AssetException {
		Scheme scheme = asset.getScheme();

		if( !scheme.exists( asset ) ) return asset;

		boolean isFolder = asset.isFolder();
		Asset parent = getProgram().getAssetManager().getParent( asset );
		List<String> children = scheme.listAssets( parent ).stream().map( Asset::getName ).toList();
		String nextName = FileUtil.getNextIndexedName( children, asset.getName() ) + (isFolder ? "/" : "");
		return getProgram().getAssetManager().resolve( parent, nextName );
	}

	private void notifyUser( String messageKey, String... parameters ) {
		String message = Rb.text( "program", messageKey, (Object[])parameters );

		Fx.run( () -> {
			userMessage.setText( message );
			userNotice.setManaged( true );
			userNotice.setVisible( true );
		} );
	}

	private void closeUserNotice() {
		userNotice.setVisible( false );
		userNotice.setManaged( false );
	}

	private Guide createGuide() {
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
			handleAssetException( exception );
		}

		return guide;
	}

	private GuideNode createGuideNode( String name, String icon, String path ) throws AssetException {
		Asset asset = getProgram().getAssetManager().createAsset( path );
		GuideNode node = new GuideNode( getProgram(), asset.getUri().toString(), name, icon );
		asset.register( Asset.ICON, e -> node.setIcon( e.getNewValue() ) );
		return node;
	}

	private boolean isEditing() {
		return assetTable.getEditingCell() != null;
	}

	private void doUpdateAssetName( TableColumn.CellEditEvent<Asset, Label> event ) {
		try {
			Asset asset = event.getRowValue();
			String newName = UriUtil.encode( event.getNewValue().getText() );
			URI parent = UriUtil.getParent( asset.getUri() );
			URI uri = parent.resolve( newName );

			Asset newAsset = getProgram().getAssetManager().createAsset( uri );
			asset.getScheme().rename( asset, newAsset );
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}
	}

	private void handleAssetException( AssetException exception ) {
		notifyUser( "asset-error", exception.getMessage() );
		log.atSevere().withCause( exception ).log();
	}

	/**
	 * A table value factory for the asset icon.
	 */
	private static class IconValueFactory implements Callback<TableColumn.CellDataFeatures<Asset, Node>, ObservableValue<Node>> {

		private final Xenon program;

		public IconValueFactory( Xenon program ) {
			this.program = program;
		}

		@Override
		public ObservableValue<Node> call( TableColumn.CellDataFeatures<Asset, Node> assetStringCellDataFeatures ) {
			Asset asset = assetStringCellDataFeatures.getValue();
			Node icon = program.getIconLibrary().getIcon( assetStringCellDataFeatures.getValue().getIcon() );
			Label label = new Label( "", icon );
			// Add the asset, as a property on the label, so it can be used for sorting
			label.getProperties().put( "asset", asset );
			return new ReadOnlyObjectWrapper<>( label );
		}

	}

	/**
	 * A table value factory for the asset label.
	 */
	private static class NameValueFactory implements Callback<TableColumn.CellDataFeatures<Asset, Label>, ObservableValue<Label>> {

		@Override
		public ObservableValue<Label> call( TableColumn.CellDataFeatures<Asset, Label> assetStringCellDataFeatures ) {
			Asset asset = assetStringCellDataFeatures.getValue();
			String name = asset.getName();
			Label label = new Label( name );
			// Add the asset, as a property on the label, so it can be used for sorting
			label.getProperties().put( "asset", asset );
			return new ReadOnlyObjectWrapper<>( label );
		}

	}

	/**
	 * A table value factory for the asset size.
	 */
	private static class SizeValueFactory implements Callback<TableColumn.CellDataFeatures<Asset, Node>, ObservableValue<Node>> {

		@Override
		public ObservableValue<Node> call( TableColumn.CellDataFeatures<Asset, Node> assetStringCellDataFeatures ) {
			try {
				Asset asset = assetStringCellDataFeatures.getValue();
				long size = asset.getSize();

				String text = Rb.text( "asset", "asset-open-folder-size", size );
				if( !asset.isFolder() ) text = FileUtil.getHumanSize( size, false, true );

				Label label = new Label( text );
				// Add the asset, as a property on the label, so it can be used for sorting
				label.getProperties().put( "asset", asset );
				return new ReadOnlyObjectWrapper<>( label );
			} catch( AssetException exception ) {
				log.atWarn().withCause( exception ).log();
				return new ReadOnlyObjectWrapper<>( null );
			}
		}

	}

	private static final class AssetLabelComparator implements Comparator<Label> {

		private final Comparator<Asset> assetComparator = new AssetTypeAndNameComparator();

		@Override
		public int compare( Label o1, Label o2 ) {
			Asset asset1 = (Asset)o1.getProperties().get( "asset" );
			Asset asset2 = (Asset)o2.getProperties().get( "asset" );
			return assetComparator.compare( asset1, asset2 );
		}

	}

	private static final class AssetSizeComparator implements Comparator<Node> {

		private final Comparator<Asset> assetComparator = new AssetTypeAndSizeComparator();

		@Override
		public int compare( Node o1, Node o2 ) {
			Asset asset1 = (Asset)o1.getProperties().get( "asset" );
			Asset asset2 = (Asset)o2.getProperties().get( "asset" );
			return assetComparator.compare( asset1, asset2 );
		}

	}

	private static final class StringLabelConverter extends StringConverter<Label> {

		@Override
		public String toString( Label object ) {
			if( object == null ) return null;
			return object.getText();
		}

		@Override
		public Label fromString( String string ) {
			if( string == null ) return null;
			return new Label( string );
		}

	}

	private final class RefreshAction extends ProgramAction {

		private RefreshAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return currentFolder != null;
		}

		@Override
		public void handle( ActionEvent event ) {
			loadFolder( currentFolder );
		}

	}

	private final class PriorAction extends ProgramAction {

		private PriorAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return currentIndex > 0;
		}

		@Override
		public void handle( ActionEvent event ) {
			if( currentIndex > 0 ) currentIndex--;
			selectAsset( history.get( currentIndex ), false );
		}

	}

	private final class NextAction extends ProgramAction {

		private NextAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return currentIndex < history.size() - 1;
		}

		@Override
		public void handle( ActionEvent event ) {
			if( currentIndex < history.size() - 1 ) currentIndex++;
			selectAsset( history.get( currentIndex ), false );
		}

	}

	private final class ParentAction extends ProgramAction {

		private ParentAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return currentFolder != null && UriUtil.hasParent( currentFolder.getUri() );
		}

		@Override
		public void handle( ActionEvent event ) {
			if( mode == Mode.OPEN ) {
				selectAsset( parentAsset );
			} else if( mode == Mode.SAVE ) {
				selectAsset( parentAsset.getUri().resolve( currentFilename ) );
			}
		}

	}

	private final class NewFolderAction extends ProgramAction {

		private NewFolderAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return currentFolder != null;
		}

		@Override
		public void handle( ActionEvent event ) {
			doCreateNewFolder();
		}

	}

	private final class DeleteAction extends ProgramAction {

		private DeleteAction( Xenon program ) {
			super( program );
		}

		@Override
		public boolean isEnabled() {
			return !assetTable.getSelectionModel().getSelectedItems().isEmpty();
		}

		@Override
		public void handle( ActionEvent event ) {
			doDeleteSelectedFiles();
		}

	}

}
