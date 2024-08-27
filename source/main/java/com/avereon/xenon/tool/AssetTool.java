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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.CustomLog;
import lombok.Getter;
import lombok.Setter;

import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

	private Mode mode = Mode.OPEN;

	private final TextField uriField;

	private final Button goButton;

	private final HBox userNotice;

	private final Label userMessage;

	private final ComboBox<AssetFilter> filters;

	private final TableColumn<Asset, Node> iconColumn;

	private final TableColumn<Asset, String> nameColumn;

	private final TableColumn<Asset, String> uriColumn;

	private final TableColumn<Asset, String> sizeColumn;

	private final TableView<Asset> assetTable;

	private final ObservableList<Asset> assets;

	private final FilteredList<Asset> filtered;

	private final SortedList<Asset> sorted;

	private final RefreshAction refreshAction;

	private final ProgramAction priorAction;

	private final ProgramAction nextAction;

	private final ProgramAction parentAction;

	private final NewFolderAction newFolderAction;

	private Asset parentAsset;

	@Getter
	private Asset currentFolder;

	@Getter
	private String currentFilename;

	private final LinkedList<String> history;

	private int currentIndex;

	@Setter
	private Consumer<Asset> saveActionConsumer;

	public AssetTool( XenonProgramProduct product, Asset asset ) {
		super( product, asset );
		setId( "tool-asset" );

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

		// FIXME This column is not editable
		nameColumn = new TableColumn<>( nameColumnHeader );
		nameColumn.setCellValueFactory( new PropertyValueFactory<>( "name" ) );
		//nameColumn.setCellValueFactory( new NameValueFactory( getProgram() ) );
		//nameColumn.setComparator( new AssetLabelComparator() );
		nameColumn.setSortType( TableColumn.SortType.ASCENDING );
		nameColumn.setEditable( true );

		uriColumn = new TableColumn<>( uriColumnHeader );
		uriColumn.setCellValueFactory( new PropertyValueFactory<>( "uri" ) );

		sizeColumn = new TableColumn<>( sizeColumnHeader );
		sizeColumn.setCellValueFactory( new SizeValueFactory() );
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

		history = new LinkedList<>();
		currentIndex = -1;

		// Basic behavior
		uriField.setOnKeyPressed( e -> {
			if( e.getCode().getCode() == KeyEvent.VK_ESCAPE && userNotice.isVisible() ) closeUserNotice();
		} );
		uriField.setOnAction( e -> selectAsset( uriField.getText() ) );
		goButton.setOnAction( this::doGoAction );
		assetTable.setOnMousePressed( this::doMousePressed );

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
		this.filters.getSelectionModel().select( filter );
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
		mode = resolveMode( request.getFragment() );

		// Set the title depending on the mode requested
		String action = mode.name().toLowerCase();
		setTitle( Rb.text( "action", action + ".name" ) );
		setGraphic( getProgram().getIconLibrary().getIcon( "asset-" + action ) );
		goButton.setGraphic( getProgram().getIconLibrary().getIcon( "asset-" + action ) );

		// Determine the current folder
		// The current folder string is in URI format
		String currentFolderString = getProgram().getSettings().get( AssetManager.CURRENT_FOLDER_SETTING_KEY );
		Path currentFolder = FileUtil.findValidFolder( currentFolderString );
		if( currentFolder == null ) currentFolder = FileSystems.getDefault().getPath( System.getProperty( "user.dir" ) );
		getProgram().getSettings().set( AssetManager.CURRENT_FOLDER_SETTING_KEY, currentFolder.toString() );

		// Select the current asset
		URI uri;
		try {
			uri = resolveAsset( request.getQueryParameters() );
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

		pushTools( "refresh | prior next up | new-folder" );
	}

	@Override
	protected void conceal() throws ToolException {
		super.conceal();

		pullTools();

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

	private static URI resolveAsset( Map<String, String> parameters ) throws URISyntaxException {
		if( parameters == null ) return null;
		String asset = parameters.get( "uri" );
		return asset == null ? null : new URI( asset );
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

	private void doGoAction( ActionEvent e ) {
		try {
			selectAsset( uriField.getText() );
			if( mode == Mode.SAVE ) requestSaveAsset();
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}
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
			uriField.setText( URLDecoder.decode( path, StandardCharsets.UTF_8 ) );

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
					// Encode the URI when creating it, to avoid issues with special characters
					String encoded = URLEncoder.encode( currentFilename, StandardCharsets.UTF_8 );
					URI uri = asset.getUri().resolve( encoded );
					// Decode the URI when setting the text, to show the user the actual filename
					uriField.setText( URLDecoder.decode( uri.toString(), StandardCharsets.UTF_8 ) );
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
		// Find the asset in the table, just return if not found
		int index = assetTable.getItems().indexOf( asset );

		log.atConfig().log( "Edit asset from table index=%s", index );

		// Start editing the asset name
		assetTable.edit( index, nameColumn );

		log.atConfig().log( "Edit asset from table index=%s", assetTable.getEditingCell() );

		// Update the asset name and reload the table
	}

	private void requestSaveAsset() throws AssetException {
		if( saveActionConsumer != null ) saveActionConsumer.accept( getProgram().getAssetManager().resolve( getCurrentFolder(), currentFilename ) );
		close();
	}

	private void updateActionState() {
		refreshAction.updateEnabled();
		priorAction.updateEnabled();
		nextAction.updateEnabled();
		parentAction.updateEnabled();
		newFolderAction.updateEnabled();
	}

	private void loadFolder( Asset asset ) {
		if( FileScheme.ID.equals( asset.getUri().getScheme() ) ) getProgram().getSettings().set( AssetManager.CURRENT_FOLDER_SETTING_KEY, asset.getUri().toString() );
		currentFolder = asset;
		updateActionState();
		closeUserNotice();

		// FIXME Also add a folder watcher to update the asset list when the folder changes

		getProgram().getTaskManager().submit( Task.of( "load-asset", () -> {
			try {
				parentAsset = getProgram().getAssetManager().getParent( asset );
				List<Asset> assets = asset.getChildren();
				Fx.run( () -> {
					this.assets.clear();
					this.assets.addAll( assets );
					this.assetTable.sort();
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

	private void createNewFolder() {
		if( currentFolder == null ) return;

		try {
			Scheme scheme = currentFolder.getScheme();

			// Start with the current folder
			String newFolderName = Rb.textOr( RbKey.LABEL, "new-folder", "New Folder" );
			URI newFolderUri = currentFolder.getUri().resolve( URLEncoder.encode( newFolderName, StandardCharsets.UTF_8 ) );
			Asset newFolder = getNextIndexedAsset( getProgram().getAssetManager().createAsset( newFolderUri ) );

			// Get next indexed asset
			scheme.createFolder( newFolder );

			// Select the new folder
			loadFolder( currentFolder );

			// Start editing the new folder name
			//editAssetName( newFolder );
		} catch( AssetException exception ) {
			handleAssetException( exception );
		}
	}

	private Asset getNextIndexedAsset( Asset asset ) throws AssetException {
		Scheme scheme = asset.getScheme();

		// NEXT Continue work on new folder
		while( scheme.exists( asset ) ) {
			String name = FileUtil.removeExtension( asset.getName() );
			//asset = scheme.getNextIndexedAsset( asset );
		}

		return asset;
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

	private void handleAssetException( AssetException exception ) {
		notifyUser( "asset-error", exception.getMessage() );
		log.atSevere().withCause( exception ).log();
	}

	/**
	 * A table value factory for the asset icon.
	 */
	private static class IconValueFactory implements Callback<javafx.scene.control.TableColumn.CellDataFeatures<Asset, Node>, ObservableValue<Node>> {

		private final Xenon program;

		public IconValueFactory( Xenon program ) {
			this.program = program;
		}

		@Override
		public ObservableValue<Node> call( TableColumn.CellDataFeatures<Asset, Node> assetStringCellDataFeatures ) {
			Asset asset = assetStringCellDataFeatures.getValue();
			Node icon = program.getIconLibrary().getIcon( assetStringCellDataFeatures.getValue().getIcon() );
			Label label = new Label( "", icon );
			label.getProperties().put( "asset", asset );
			return new ReadOnlyObjectWrapper<>( label );
		}

	}

//	/**
//	 * A table value factory for the asset label.
//	 */
//	private static class NameValueFactory implements Callback<javafx.scene.control.TableColumn.CellDataFeatures<Asset, Node>, ObservableValue<Node>> {
//
//		private final Xenon program;
//
//		public NameValueFactory( Xenon program ) {
//			this.program = program;
//		}
//
//		@Override
//		public ObservableValue<Node> call( TableColumn.CellDataFeatures<Asset, Node> assetStringCellDataFeatures ) {
//			Asset asset = assetStringCellDataFeatures.getValue();
//			String name = asset.getName();
//			Node icon = program.getIconLibrary().getIcon( assetStringCellDataFeatures.getValue().getIcon() );
//			Label label = new Label( name, icon );
//			label.getProperties().put( "asset", asset );
//			return new ReadOnlyObjectWrapper<>( label );
//		}
//
//	}

	/**
	 * A table value factory for the asset size.
	 */
	private static class SizeValueFactory implements Callback<TableColumn.CellDataFeatures<Asset, String>, ObservableValue<String>> {

		@Override
		public ObservableValue<String> call( TableColumn.CellDataFeatures<Asset, String> assetStringCellDataFeatures ) {
			try {
				Asset asset = assetStringCellDataFeatures.getValue();
				long size = asset.getSize();
				if( asset.isFolder() ) return new ReadOnlyObjectWrapper<>( Rb.text( "asset", "asset-open-folder-size", size ) );
				return new ReadOnlyObjectWrapper<>( FileUtil.getHumanSize( size, false, true ) );
			} catch( AssetException exception ) {
				log.atSevere().withCause( exception ).log();
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
			// TODO Be sure to scroll to the prior location
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
			createNewFolder();
		}

	}

}
