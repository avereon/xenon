package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.util.LogUtil;
import com.avereon.venza.javafx.FxUtil;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.product.DownloadRequest;
import com.avereon.xenon.product.ProductManager;
import com.avereon.xenon.product.ProductStatus;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.util.DialogUtil;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.controlsfx.control.ToggleSwitch;
import org.slf4j.Logger;
import org.tbee.javafx.scene.layout.MigPane;

import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.DoubleConsumer;

class ProductPane extends MigPane {

	private static final Logger log = LogUtil.get( MethodHandles.lookup().lookupClass() );

	private ProductTool tool;

	private Program program;

	private ProductManager manager;

	private ProductCard source;

	private Label iconLabel;

	private Label nameLabel;

	private Label versionLabel;

	private Label summaryLabel;

	private Label hyphenLabel;

	private Label providerLabel;

	private Label releaseLabel;

	private HBox stateContainer;

	private ProgressBar progress;

	private Label stateLabel;

	private ToggleSwitch enableSwitch;

	private Button actionButton1;

	private Button actionButton2;

	ProductPane( ProductTool tool, ProductCard source, ProductCard update ) {
		super( "insets 0, gap " + UiFactory.PAD + ", hidemode 3" );

		this.tool = tool;
		this.program = tool.getProgram();
		this.manager = program.getProductManager();
		this.source = source;
		manager.setProductUpdate( source, update );

		setId( "tool-product-artifact" );

		Node productIcon = program.getIconLibrary().getIcon( source.getIcons(), "product", ProductTool.ICON_SIZE );

		iconLabel = new Label( null, productIcon );
		iconLabel.setId( "tool-product-artifact-icon" );
		nameLabel = new Label( source.getName() );
		nameLabel.setId( "tool-product-artifact-name" );
		versionLabel = new Label( update == null ? source.getRelease().toHumanString( TimeZone.getDefault() ) : update
			.getRelease()
			.toHumanString( TimeZone.getDefault() ) );
		versionLabel.setId( "tool-product-artifact-version" );
		summaryLabel = new Label( source.getSummary() );
		summaryLabel.setId( "tool-product-artifact-summary" );
		hyphenLabel = new Label( "-" );
		providerLabel = new Label( source.getProvider() );
		providerLabel.setId( "tool-product-artifact-provider" );
		releaseLabel = new Label( source.getRelease().toHumanString( TimeZone.getDefault() ) );
		releaseLabel.setId( "tool-product-artifact-release" );
		stateLabel = new Label( "State" );
		stateLabel.setId( "tool-product-artifact-state" );

		progress = new ProgressBar();
		progress.setId( "tool-product-progress" );

		enableSwitch = new ToggleSwitch();
		enableSwitch.selectedProperty().addListener( ( observable, oldValue, newValue ) -> toggleEnabled( newValue ) );

		actionButton1 = new Button( "" );
		actionButton2 = new Button( "" );

		stateContainer = new HBox( stateLabel );

		add( iconLabel, "spany, aligny center" );
		add( nameLabel );
		add( hyphenLabel );
		add( providerLabel, "pushx" );
		add( stateContainer, "tag right" );
		add( enableSwitch, "w min" );
		add( actionButton1 );

		add( summaryLabel, "newline, spanx 3" );
		add( versionLabel, "tag right" );
		add( actionButton2 );
	}

	public ProductCard getSource() {
		return source;
	}

	public ProductCard getUpdate() {
		return manager.getProductUpdate( source );
	}

	boolean isSelected() {
		// TODO Connect this method to an attribute
		return true;
	}

	void updateProductState() {
		ProductCard update = getUpdate();

		boolean isProgram = program.getCard().equals( source );
		boolean isEnabled = manager.isEnabled( source );
		boolean isInstalled = manager.isInstalled( source );
		boolean inProgress = manager.getStatus( source ) == ProductStatus.DOWNLOADING;
		boolean isAnyUpdateStaged = manager.isUpdateStaged( source );
		boolean isSpecificUpdateReleaseStaged = update != null && manager.isSpecificUpdateReleaseStaged( update );
		boolean isInstalledProductsPanel = FxUtil.isChildOf( this, tool.getInstalledPage() );
		boolean isAvailableProductsPanel = FxUtil.isChildOf( this, tool.getAvailablePage() );
		boolean isUpdatableProductsPanel = FxUtil.isChildOf( this, tool.getUpdatesPage() );

		// Determine state string key
		String stateLabelKey = "not-installed";
		if( isInstalled ) {
			if( !isProgram && !isEnabled ) {
				stateLabelKey = "disabled";
			} else if( isUpdatableProductsPanel ) {
				stateLabelKey = "available";
			} else if( isAvailableProductsPanel ) {
				stateLabelKey = "installed";
			} else {
				stateLabelKey = "enabled";
			}
		}
		if( isAnyUpdateStaged ) {
			stateLabelKey = "restart-required";
			if( update != null && !isSpecificUpdateReleaseStaged ) stateLabelKey = "update-available";
		}

		stateContainer.getChildren().clear();
		if( inProgress ) {
			stateContainer.getChildren().add( progress );
		} else {
			stateLabel.setText( program.rb().text( BundleKey.LABEL, stateLabelKey ) );
			stateContainer.getChildren().add( stateLabel );
		}

		// Configure the action buttons
		if( isInstalledProductsPanel ) {
			enableSwitch.setVisible( true );
			enableSwitch.setDisable( isProgram );
			enableSwitch.setSelected( tool.getProgram().getProductManager().isEnabled( source ) );

			actionButton1.setVisible( false );

			actionButton2.setVisible( true );
			actionButton2.setDisable( isProgram );
			actionButton2.setGraphic( program.getIconLibrary().getIcon( "remove" ) );
			actionButton2.setOnAction( ( event ) -> requestRemoveProduct() );
		} else if( isAvailableProductsPanel ) {
			enableSwitch.setVisible( false );

			actionButton1.setVisible( true );
			actionButton1.setDisable( isInstalled || inProgress );
			actionButton1.setGraphic( program.getIconLibrary().getIcon( "download" ) );
			actionButton1.setOnAction( ( event ) -> installProduct() );

			actionButton2.setVisible( false );
			actionButton2.setDisable( true );
		} else if( isUpdatableProductsPanel ) {
			enableSwitch.setVisible( false );

			actionButton1.setVisible( true );
			actionButton1.setDisable( inProgress );
			actionButton1.setGraphic( program.getIconLibrary().getIcon( "download" ) );
			actionButton1.setOnAction( ( event ) -> updateProduct() );

			actionButton2.setVisible( false );
			actionButton2.setDisable( true );
		}
	}

	private void setStatus( ProductStatus status ) {
		manager.setStatus( getSource(), status );
		updateProductState();
	}

	private void toggleEnabled( boolean enabled ) {
		manager.setModEnabled( source, enabled );
		updateProductState();
	}

	private void installProduct() {
		setStatus( ProductStatus.DOWNLOADING );
		program.getTaskManager().submit( Task.of( "Install product", () -> {
			try {
				DoubleConsumer progressHandler = ( progress ) -> Platform.runLater( () -> this.progress.setProgress( progress ) );
				// TODO Add a way to stop long running download
				manager.installProducts( new DownloadRequest( source, progressHandler ) ).get();
				Platform.runLater( () -> setStatus( ProductStatus.INSTALLED ) );
				tool.getSelectedPage().updateState( false );
			} catch( Exception exception ) {
				Platform.runLater( () -> setStatus( ProductStatus.NOT_INSTALLED ) );
				ProductTool.log.warn( "Error installing product", exception );
			}
		} ) );
	}

	void updateProduct() {
		setStatus( ProductStatus.DOWNLOADING );
		program.getTaskManager().submit( Task.of( "Update product", () -> {
			try {
				DoubleConsumer progressHandler = ( progress ) -> Platform.runLater( () -> this.progress.setProgress( progress ) );
				// TODO Add a way to stop long running download
				manager.updateProducts( new DownloadRequest( getUpdate(), progressHandler ), true ).get();
				Platform.runLater( () -> setStatus( ProductStatus.DOWNLOADED ) );
				tool.getSelectedPage().updateState( false );
			} catch( Exception exception ) {
				Platform.runLater( () -> setStatus( ProductStatus.NOT_INSTALLED ) );
				ProductTool.log.warn( "Error updating product", exception );
			}
		} ) );
	}

	private void requestRemoveProduct() {
		String modName = source.getName();

		String title = program.rb().text( BundleKey.PRODUCT, "products" );
		String header = program.rb().text( BundleKey.PRODUCT, "product-remove-header", modName );
		String message = program.rb().text( BundleKey.PRODUCT, "product-remove-message" );

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO );
		alert.setGraphic( program.getIconLibrary().getIcon( source.getIcons(), 64 ) );
		alert.setTitle( title );
		alert.setHeaderText( header );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() && result.get() == ButtonType.YES ) removeProduct();
	}

	private void removeProduct() {
		program.getTaskManager().submit( Task.of( "Remove product", () -> {
			try {
				manager.uninstallProducts( source ).get();
				tool.getSelectedPage().updateState( false );
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error uninstalling product", exception );
			}
			Platform.runLater( () -> setStatus( ProductStatus.NOT_INSTALLED ) );
		} ) );
	}

}
