package com.avereon.xenon.tool.settings.panel.products;

import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.util.FileUtil;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.UiFactory;
import com.avereon.xenon.XenonProgramProduct;
import com.avereon.xenon.product.ProductStatus;
import com.avereon.xenon.task.Task;
import com.avereon.xenon.util.DialogUtil;
import com.avereon.zarra.javafx.Fx;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import lombok.CustomLog;
import lombok.Getter;
import org.controlsfx.control.ToggleSwitch;

import java.util.Optional;
import java.util.TimeZone;

@CustomLog
public class ProductTile extends BaseTile {

	@Getter
	private final ProductCard source;

	private final DisplayMode displayMode;

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

	private final Label sizeLabel;

	private ToggleSwitch enableSwitch;

	private Button actionButton1;

	private Button actionButton2;

	private BooleanProperty selectedProperty;

	ProductTile( XenonProgramProduct product, ProductsSettingsPanel parent, ProductCard source, ProductCard update, DisplayMode displayMode ) {
		super( product, parent );
		setHgap( UiFactory.PAD );
		setVgap( UiFactory.PAD );

		this.source = source;
		this.displayMode = displayMode;
		this.selectedProperty = new SimpleBooleanProperty( true );
		getProductManager().setProductUpdate( source, update );

		setId( "tool-product-artifact" );

		Node productIcon = getProgram().getIconLibrary().getIcon( source.getIcons(), "module", ProductsSettingsPanel.ICON_SIZE );

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

		sizeLabel = new Label( "" );
		sizeLabel.setId( "tool-product-size" );
		progress = new ProgressBar();
		progress.setId( "tool-product-progress" );

		enableSwitch = new ToggleSwitch();
		enableSwitch.selectedProperty().addListener( ( observable, oldValue, newValue ) -> toggleEnabled( newValue ) );

		actionButton1 = new Button( "" );
		actionButton2 = new Button( "" );

		stateContainer = new HBox( stateLabel );
		stateContainer.setAlignment( Pos.CENTER_RIGHT );

		GridPane.setRowSpan( iconLabel, 2 );
		GridPane.setHgrow( providerLabel, Priority.ALWAYS );
		GridPane.setHalignment( stateContainer, HPos.RIGHT );
		GridPane.setHalignment( enableSwitch, HPos.RIGHT );
		GridPane.setHalignment( actionButton1, HPos.RIGHT );

		GridPane.setColumnSpan( summaryLabel, 4 );
		GridPane.setHgrow( summaryLabel, Priority.ALWAYS );
		GridPane.setHalignment( versionLabel, HPos.RIGHT );
		GridPane.setHalignment( actionButton2, HPos.RIGHT );

		add( iconLabel, 0, 0 );
		add( nameLabel, 1, 0 );
		add( hyphenLabel, 2, 0 );
		add( providerLabel, 3, 0 );
		add( sizeLabel, 4, 0 );
		add( stateContainer, 5, 0 );
		add( enableSwitch, 6, 0 );
		add( actionButton1, 6, 0 );

		add( summaryLabel, 1, 1 );
		add( versionLabel, 5, 1 );
		add( actionButton2, 6, 1 );
	}

	public ProductCard getUpdate() {
		return getProductManager().getProductUpdate( source );
	}

	public boolean isSelected() {
		return true;
	}

	public void setSelected( boolean selected ) {
		selectedProperty.set( selected );
	}

	public BooleanProperty selectedProperty() {
		return selectedProperty;
	}

	void setSize( long size ) {
		if( size >= 0 ) this.sizeLabel.setText( FileUtil.getHumanSizeBase2( size ) );
	}

	void setProgress( double progress ) {
		this.progress.setProgress( progress );
	}

	void updateProductState() {
		boolean isProgram = getProgram().getCard().equals( source );
		boolean isEnabled = getProductManager().isEnabled( source ) || getProductManager().getStatus( source ) == ProductStatus.INSTALLED;
		boolean isInstalled = getProductManager().isInstalled( source ) || getProductManager().getStatus( source ) == ProductStatus.INSTALLED;
		boolean inProgress = getProductManager().getStatus( source ) == ProductStatus.DOWNLOADING;
		boolean isDownloaded = getProductManager().getStatus( source ) == ProductStatus.DOWNLOADED;
		boolean isUpdateStaged = getProductManager().isUpdateStaged( source );
		boolean isInstalledProductsPanel = displayMode == DisplayMode.INSTALLED;
		boolean isAvailableProductsPanel = displayMode == DisplayMode.AVAILABLE;
		boolean isUpdatesProductsPanel = displayMode == DisplayMode.UPDATES;

		ProductCard update = getUpdate();
		boolean isSpecificUpdateReleaseStaged = update != null && getProductManager().isSpecificUpdateReleaseStaged( update );

		// Determine state string key
		String stateLabelKey = "not-installed";
		if( isInstalled ) {
			if( !isProgram && !isEnabled ) {
				stateLabelKey = "disabled";
			} else if( isUpdatesProductsPanel ) {
				if( isDownloaded ) {
					stateLabelKey = "restart-required";
				} else {
					stateLabelKey = "available";
				}
			} else if( isAvailableProductsPanel ) {
				stateLabelKey = "installed";
			} else {
				stateLabelKey = "enabled";
			}
		}
		if( isUpdateStaged ) {
			stateLabelKey = "restart-required";
			if( update != null && !isSpecificUpdateReleaseStaged ) stateLabelKey = "update-available";
		}

		stateContainer.getChildren().clear();
		if( inProgress ) {
			stateContainer.getChildren().add( progress );
		} else {
			stateLabel.setText( Rb.text( RbKey.LABEL, stateLabelKey ) );
			stateContainer.getChildren().add( stateLabel );
		}

		// Configure the action buttons
		if( isInstalledProductsPanel ) {
			enableSwitch.setVisible( true );
			enableSwitch.setDisable( isProgram );
			enableSwitch.setSelected( isProgram || getProgram().getProductManager().isEnabled( source ) );

			actionButton1.setVisible( false );

			actionButton2.setVisible( true );
			actionButton2.setDisable( isProgram );
			actionButton2.setGraphic( getProgram().getIconLibrary().getIcon( "remove" ) );
			actionButton2.setOnAction( ( event ) -> requestRemoveProduct() );
		} else if( isAvailableProductsPanel ) {
			enableSwitch.setVisible( false );

			actionButton1.setVisible( true );
			actionButton1.setDisable( isInstalled || inProgress );
			actionButton1.setGraphic( getProgram().getIconLibrary().getIcon( "download" ) );
			actionButton1.setOnAction( ( event ) -> installProduct() );

			actionButton2.setVisible( false );
			actionButton2.setDisable( true );
		} else if( isUpdatesProductsPanel ) {
			enableSwitch.setVisible( false );

			actionButton1.setVisible( true );
			actionButton1.setDisable( inProgress );
			actionButton1.setGraphic( getProgram().getIconLibrary().getIcon( "download" ) );
			actionButton1.setOnAction( ( event ) -> updateProduct() );

			actionButton2.setVisible( false );
			actionButton2.setDisable( true );
		}
	}

	void setStatus( ProductStatus status ) {
		getProductManager().setStatus( getSource(), status );
		updateProductState();
	}

	private void toggleEnabled( boolean enabled ) {
		getProductManager().setModEnabled( getSource(), enabled );
		updateProductState();
	}

	private void installProduct() {
		//tool.getAvailablePage().installProducts( List.of( this ) );
	}

	private void updateProduct() {
		//tool.getUpdatesPage().updateProducts( List.of( this ) );
	}

	private void requestRemoveProduct() {
		String modName = getSource().getName();

		String title = Rb.text( RbKey.PRODUCT, "products" );
		String header = Rb.text( RbKey.PRODUCT, "product-remove-header", modName );
		String message = Rb.text( RbKey.PRODUCT, "product-remove-message" );

		Alert alert = new Alert( Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO );
		alert.setGraphic( getProgram().getIconLibrary().getIcon( source.getIcons(), 64 ) );
		alert.setTitle( title );
		alert.setHeaderText( header );

		Stage stage = getProgram().getWorkspaceManager().getActiveStage();
		Optional<ButtonType> result = DialogUtil.showAndWait( stage, alert );

		if( result.isPresent() && result.get() == ButtonType.YES ) removeProduct();
	}

	private void removeProduct() {
		getProgram().getTaskManager().submit( Task.of( "Remove product", () -> {
			try {
				getProductManager().uninstallProducts( source ).get();
				getProductSettingsPanel().updateState( false );
			} catch( Exception exception ) {
				log.atWarning().withCause(exception).log( "Error uninstalling product", exception );
			}
			Fx.run( () -> setStatus( ProductStatus.NOT_INSTALLED ) );
		} ) );
	}

}
