package com.xeomar.xenon.tool.product;

import com.xeomar.product.ProductCard;
import com.xeomar.xenon.BundleKey;
import com.xeomar.xenon.Program;
import com.xeomar.xenon.UiFactory;
import com.xeomar.xenon.update.UpdateManager;
import com.xeomar.xenon.util.FxUtil;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.tbee.javafx.scene.layout.MigPane;

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
		Node productIcon = program.getIconLibrary().getIcon( "module", ProductTool.ICON_SIZE );
		//Node productIcon = program.getIconLibrary().getIcon( iconUri, ProductTool.ICON_SIZE );

		iconLabel = new Label( null, productIcon );
		iconLabel.setId( "tool-product-artifact-icon" );
		nameLabel = new Label( source.getName() );
		nameLabel.setId( "tool-product-artifact-name" );
		versionLabel = new Label( update == null ? source.getRelease().toHumanString( TimeZone.getDefault() ) : update.getRelease().toHumanString( TimeZone.getDefault() ) );
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

		actionButton1 = new Button( "", program.getIconLibrary().getIcon( "remove" ) );
		actionButton2 = new Button( "", program.getIconLibrary().getIcon( "enable" ) );

		add( iconLabel, "spany, aligny center" );
		add( nameLabel );
		add( hyphenLabel );
		add( providerLabel, "pushx" );
		add( versionLabel, "tag right" );
		add( actionButton1 );

		add( summaryLabel, "newline, spanx 3" );
		add( stateLabel, "tag right" );
		add( actionButton2 );

		// Trying to update the product state before being added to a page causes incorrect state
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
		UpdateManager manager = program.getUpdateManager();

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
				stateLabelKey = "enabled";
			}
		}
		if( isStaged ) stateLabelKey = "downloaded";
		stateLabel.setText( program.getResourceBundle().getString( BundleKey.LABEL, stateLabelKey ) );

		// Configure the action button
		if( isInstalledProductsPanel ) {
			actionButton1.setVisible( true );
			actionButton1.setDisable( isProgram );
			actionButton1.setOnAction( ( event ) -> removeProduct() );

			actionButton2.setVisible( true );
			actionButton2.setDisable( isProgram );
			actionButton2.setGraphic( program.getIconLibrary().getIcon( isEnabled ? "disable" : "enable" ) );
			actionButton2.setOnAction( ( event ) -> toggleEnabled() );
		} else if( isAvailableProductsPanel ) {
			actionButton1.setVisible( true );
			actionButton1.setDisable( false );
			actionButton1.setGraphic( program.getIconLibrary().getIcon( "install" ) );
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
		productTool.getProgram().getUpdateManager().setEnabled( source, !productTool.getProgram().getUpdateManager().isEnabled( source ) );
	}

	private void installProduct() {
		productTool.getProgram().getExecutor().submit( () -> {
			try {
				productTool.getProgram().getUpdateManager().installProducts( source );
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error installing product", exception );
			}
		} );
	}

	private void updateProduct() {
		productTool.getProgram().getExecutor().submit( () -> {
			try {
				productTool.getProgram().getUpdateManager().applySelectedUpdates( getUpdate() );
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error updating product", exception );
			}
		} );
	}

	private void removeProduct() {
		productTool.getProgram().getExecutor().submit( () -> {
			try {
				productTool.getProgram().getUpdateManager().uninstallProducts( source );
			} catch( Exception exception ) {
				ProductTool.log.warn( "Error uninstalling product", exception );
			}
		} );
	}

}
