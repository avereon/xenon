package com.avereon.xenon.tool;

import com.avereon.util.Log;
import com.avereon.util.TextUtil;
import com.avereon.util.UriUtil;
import com.avereon.venza.javafx.FxUtil;
import com.avereon.xenon.ProgramProduct;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.asset.Asset;
import com.avereon.xenon.asset.AssetException;
import com.avereon.xenon.asset.AssetManager;
import com.avereon.xenon.asset.OpenAssetRequest;
import com.avereon.xenon.notice.Notice;
import com.avereon.xenon.tool.guide.Guide;
import com.avereon.xenon.tool.guide.GuideNode;
import com.avereon.xenon.tool.guide.GuidedTool;
import com.avereon.xenon.workpane.ToolException;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

//import org.apache.commons.vfs2.FileSystemException;
//import org.apache.commons.vfs2.FileSystemManager;
//import org.apache.commons.vfs2.VFS;

public class AssetTool extends GuidedTool {

	private static final System.Logger log = Log.get();

	private final TextField uriField;

	private final Button goButton;

	private final Guide guide;

	private final ObservableList<Asset> children;

	private final TableView<Asset> table;

	public AssetTool( ProgramProduct product, Asset asset ) {
		super( product, asset );

		uriField = new TextField();
		HBox.setHgrow( uriField, Priority.ALWAYS );
		goButton = new Button();

		table = new TableView<>( children = FXCollections.observableArrayList() );
		TableColumn<Asset, String> assetName = new TableColumn<>( "Name" );
		assetName.setCellValueFactory( new PropertyValueFactory<>( "name" ) );
		TableColumn<Asset, String> assetUri = new TableColumn<>( "URI");
		assetUri.setCellValueFactory( new PropertyValueFactory<>( "uri" ) );
		table.getColumns().addAll( assetName, assetUri );

		BorderPane layout = new BorderPane();
		layout.setPadding( new Insets( UiFactory.PAD ) );
		layout.setTop( new HBox( UiFactory.PAD, uriField, goButton ) );
		layout.setCenter( table );
		getChildren().add( layout );

		uriField.setOnAction( e -> selectAsset( uriField.getText() ) );
		goButton.setOnAction( e -> selectAsset( uriField.getText() ) );

		guide = initializeGuide();
	}

	@Override
	protected void open( OpenAssetRequest request ) {
		// Set the title depending on the mode requested
		String action = request.getFragment();
		if( TextUtil.isEmpty( action ) ) action = "open";
		setTitle( getProduct().rb().text( "action", action + ".name" ) );

		goButton.setGraphic( getProgram().getIconLibrary().getIcon( "asset-" + action ) );

		// TODO Select to the current asset
		String current = getProgram().getProgramSettings().get( AssetManager.CURRENT_FOLDER_SETTING_KEY, System.getProperty( "user.dir" ) );
		selectAsset( current );
	}

	@Override
	protected void activate() throws ToolException {
		super.activate();
		activateUriField();
	}

	private void selectAsset( String text ) {
		FxUtil.assertFxThread();
		uriField.setText( text );

		try {
			Asset asset = getProgram().getAssetManager().createAsset( text );
			if( !asset.exists() ) {
				notifyUser( "asset-not-found", text );
			} else if( asset.isFolder() ) {
				loadFolder( asset );
			} else {
				getProgram().getAssetManager().openAssets( asset );
				close();
				return;
			}
			activateUriField();
		} catch( AssetException exception ) {
			log.log( Log.ERROR, exception );
		}
	}

	private void loadFolder( Asset asset ) throws AssetException {
		// NEXT asset.getChildren();
		children.clear();
		children.add( asset );
	}

	private void activateUriField() {
		Platform.runLater( () -> {
			uriField.requestFocus();
			uriField.positionCaret( uriField.getText().length() );
		} );
	}

	private void notifyUser( String messageKey, String uri ) {
		String title = getProduct().rb().text( "label", "asset" );
		String message = getProduct().rb().text( "program", messageKey, uri );
		//Platform.runLater(() -> userMessage.setText( message ));
		getProgram().getNoticeManager().addNotice( new Notice( title, message ) );
	}

	//	private File getFileChooserFolder() {
	//		File folder = new File( getSettings().get( CURRENT_FOLDER_SETTING_KEY, System.getProperty( "user.dir" ) ) );
	//		if( !folder.exists() || !folder.isDirectory() ) folder = new File( System.getProperty( "user.dir" ) );
	//		return folder;
	//	}

	@Override
	protected Guide getGuide() {
		return guide;
	}

	@Override
	protected void guideNodesSelected( Set<GuideNode> oldNodes, Set<GuideNode> newNodes ) {
		if( newNodes.isEmpty() ) return;
		selectAsset( newNodes.stream().findAny().get().getId() );
	}

	private Guide initializeGuide() {
		Guide guide = new Guide();

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

	private Guide testing( Guide guide ) {
		for( Path fsRoot : FileSystems.getDefault().getRootDirectories() ) {
			boolean folder = Files.isDirectory( fsRoot );
			Path filenamePath = fsRoot.getFileName();
			String filename = filenamePath == null ? "" : filenamePath.toString();

			String name = folder ? "/" + filename : filename;
			String icon = folder ? "folder" : "file";

			GuideNode node = new GuideNode( getProgram(), fsRoot.toUri().toString(), name, icon );
			guide.addNode( node );
		}

		return guide;
	}

}
