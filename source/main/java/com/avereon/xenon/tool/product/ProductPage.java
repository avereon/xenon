package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.xenon.BundleKey;
import com.avereon.xenon.Program;
import com.avereon.xenon.product.DownloadRequest;
import com.avereon.xenon.product.ProductStatus;
import com.avereon.xenon.task.TaskEvent;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

abstract class ProductPage extends ProductToolPage {

	private Program program;

	private ProductTool productTool;

	private List<ProductPane> sources;

	private Labeled message;

	private String refreshMessage;

	private String missingMessage;

	ProductPage( Program program, ProductTool productTool, String productType ) {
		this.program = program;
		this.productTool = productTool;
		sources = new CopyOnWriteArrayList<>();
		setTitle( program.rb().text( BundleKey.TOOL, "product-" + productType ) );

		this.refreshMessage = program.rb().text( BundleKey.TOOL, "product-" + productType + "-refresh" );
		this.missingMessage = program.rb().text( BundleKey.TOOL, "product-" + productType + "-missing" );

		message = new Label();
		message.setPrefWidth( Double.MAX_VALUE );
		message.getStyleClass().addAll( "tool-product-message" );

		showUpdating();
	}

	void installProducts( List<ProductPane> panes ) {
		getProgram().getProductManager().installProducts( getDownloads( panes, true ) );
	}

	void updateProducts( List<ProductPane> panes ) {
		getProgram().getProductManager().updateProducts( getDownloads( panes, false ), true );
	}

	private Set<DownloadRequest> getDownloads( List<ProductPane> panes, boolean install ) {
		return panes.stream().filter( ProductPane::isSelected ).map( pane -> {
			DownloadRequest request = new DownloadRequest( install ? pane.getSource() : pane.getUpdate() );
			request
				.register( TaskEvent.START, e -> Platform.runLater( () -> pane.setStatus( ProductStatus.DOWNLOADING ) ) )
				.register( TaskEvent.PROGRESS, e -> Platform.runLater( () -> pane.setProgress( e.getTask().getPercent() ) ) )
				.register( TaskEvent.FAILURE, e -> Platform.runLater( () -> pane.setStatus( ProductStatus.NOT_INSTALLED ) ) )
				.register( TaskEvent.SUCCESS, e -> Platform.runLater( () -> pane.setStatus( install ? ProductStatus.INSTALLED : ProductStatus.DOWNLOADED ) ) )
				.register( TaskEvent.FINISH, e -> Platform.runLater( pane::updateProductState ) );
			return request;
		} ).collect( Collectors.toSet() );
	}

	public Program getProgram() {
		return program;
	}

	@Override
	protected void showUpdating() {
		showMessage( refreshMessage );
	}

	private void showMissing() {
		showMessage( missingMessage );
	}

	private void showMessage( String messageText ) {
		message.setText( messageText );
		getChildren().clear();
		getChildren().addAll( message );
	}

	List<ProductPane> getSourcePanels() {
		return Collections.unmodifiableList( sources );
	}

	void setProducts( List<ProductCard> cards ) {
		setProducts( cards, Map.of() );
	}

	void setProducts( List<ProductCard> cards, Map<String, ProductCard> productUpdates ) {
		if( cards.size() == 0 ) {
			showMissing();
		} else {
			// Add a product pane for each card
			sources.clear();
			sources.addAll( getTool()
				.createSourceList( cards )
				.stream()
				.map( ( source ) -> new ProductPane( getTool(), source, productUpdates.get( source.getProductKey() ) ) )
				.collect( Collectors.toList() ) );

			getChildren().clear();
			getChildren().addAll( sources );
			updateProductStates();
		}
	}

	private ProductTool getTool() {
		return productTool;
	}

	private void updateProductStates() {
		for( Node node : getChildren() ) {
			((ProductPane)node).updateProductState();
		}
	}

	private void updateProductState( ProductCard card ) {
		for( Node node : getChildren() ) {
			ProductPane panel = (ProductPane)node;
			if( panel.getSource().equals( card ) ) panel.updateProductState();
		}
	}

}
