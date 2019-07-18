package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.update.ProductManager;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.xenon.util.FxUtil;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.tbee.javafx.scene.layout.MigPane;

import java.util.Optional;
import java.util.TimeZone;

class ProductPane extends MigPane {

	private ProductTool productTool;

	private ProductCard source;

	private ProductCard update;

	private Label iconLabel;

	private Label nameLabel;

	private Label versionLabel;

	private Label summaryLabel;

	private Label hyphenLabel;

	private Label providerLabel;

	private Label releaseLabel;

	private Label stateLabel;

	private Button actionButton1;

	private Button actionButton2;

	ProductPane( ProductTool productTool, ProductCard source, ProductCard update ) {
		super( "insets 0, gap " + UiFactory.PAD );

		this.productTool = productTool;
		this.source = source;
		this.update = update;

		setId( "tool-product-artifact" );

		Program program = productTool.getProgram();

		String iconUri = source.getIconUri();
		Node productIcon = program.getIconLibrary().getIcon( iconUri, "product", ProductTool.ICON_SIZE );

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

		actionButton1 = new Button( "" );
		actionButton2 = new Button( "" );

		add( iconLabel, "spany, aligny center" );
		add( nameLabel );
		add( hyphenLabel );
		add( providerLabel, "pushx" );
		add( stateLabel, "tag right" );
		add( actionButton2 );

		add( summaryLabel, "newline, spanx 3" );
		add( versionLabel, "tag right" );
		add( actionButton1 );
	}

	public ProductCard getSource() {
		return source;
	}

	public ProductCard getUpdate() {
		return update;
	}

	void updateProductState() {
		ProductCard card = source;
		Program program = productTool.getProgram();
		ProductManager manager = program.getProductManager();

		boolean isStaged = update == null ? manager.isStaged( card ) : manager.isReleaseStaged( update );
		boolean isProgram = program.getCard().equals( card );
		boolean isEnabled = manager.isEnabled( card );
		boolean isInstalled = manager.isInstalled( card );
		boolean isInstalledProductsPanel = FxUtil.isChildOf( this, productTool.getInstalledPage() );
		boolean isAvailableProductsPanel = FxUtil.isChildOf( this, productTool.getAvailablePage() );
		boolean isUpdatableProductsPanel = FxUtil.isChildOf( this, productTool.getUpdatesPage() );

		// Determine state string key.
		String stateLabelKey = "not-installed";
		if( isInstalled ) {
			if( !isProgram && !isEnabled ) {
				stateLabelKey = "disabled";
			} else if( isUpdatableProductsPanel ) {
				stateLabelKey = "available";
			} else {
				stateLabelKey = "installed";
			}
		}
		if( isStaged ) stateLabelKey = "downloaded";
		stateLabel.setText( program.getResourceBundle().getString( BundleKey.LABEL, stateLabelKey ) );

		// Configure the action button
		if( isInstalledProductsPanel ) {
			actionButton1.setVisible( true );
			actionButton1.setDisable( isProgram );
			actionButton1.setGraphic( program.getIconLibrary().getIcon( "remove" ) );
			actionButton1.setOnAction( ( event ) -> requestRemoveProduct() );

			actionButton2.setVisible( true );
			actionButton2.setDisable( isProgram );
			actionButton2.setGraphic( program.getIconLibrary().getIcon( isEnabled ? "disable" : "enable" ) );
			actionButton2.setOnAction( ( event ) -> toggleEnabled() );
		} else if( isAvailableProductsPanel ) {
			actionButton1.setVisible( true );
			actionButton1.setDisable( false );
			actionButton1.setGraphic( program.getIconLibrary().getIcon( "download" ) );
			actionButton1.setOnAction( ( event ) -> installProduct() );

			actionButton2.setVisible( false );
			actionButton2.setDisable( true );
		} else if( isUpdatableProductsPanel ) {
			actionButton1.setVisible( true );
			actionButton1.setDisable( false );
			actionButton1.setGraphic( program.getIconLibrary().getIcon( "download" ) );
			actionButton1.setOnAction( ( event ) -> updateProduct() );

			actionButton2.setVisible( false );
			actionButton2.setDisable( true );
		}
	}

	private void toggleEnabled() {
		productTool
			.getProgram()
			.getProductManager()
			.setEnabled( source, !productTool.getProgram().getProductManager().isEnabled( source ) );
		updateProductState();
	}

	private void installProduct() {
		productTool.getProgram().getTaskManager().submit( Task.of( "Install product", () -> {
			try {
				productTool.getProgram().getProductManager().installProducts( source ).get();
				productTool.getSelectedPage().updateState();
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error installing product", exception );
			}
		} ) );
	}

	private void updateProduct() {
		productTool.getProgram().getTaskManager().submit( Task.of( "Update product", () -> {
			try {
				productTool.getProgram().getProductManager().applySelectedUpdates( getUpdate() ).get();
				productTool.getSelectedPage().updateState();
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error updating product", exception );
			}
		} ) );
	}

	private void requestRemoveProduct() {
		String modName = source.getName();
		Program program = productTool.getProgram();

		String title = program.getResourceBundle().getString( BundleKey.PRODUCT, "products" );
		String header = program.getResourceBundle().getString( BundleKey.PRODUCT, "product-remove-header", modName );
		String message = program.getResourceBundle().getString( BundleKey.PRODUCT, "product-remove-message" );

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO );
		alert.setGraphic( program.getIconLibrary().getIcon( source.getIconUri(), 64 ) );
		alert.setTitle( title );
		alert.setHeaderText( header );

		Stage stage = program.getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() && result.get() == ButtonType.YES ) removeProduct();
	}

	private void removeProduct() {
		productTool.getProgram().getTaskManager().submit( Task.of( "Remove product", () -> {
			try {
				productTool.getProgram().getProductManager().uninstallProducts( source ).get();
				productTool.getSelectedPage().updateState();
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error uninstalling product", exception );
			}
		} ) );
	}

}
