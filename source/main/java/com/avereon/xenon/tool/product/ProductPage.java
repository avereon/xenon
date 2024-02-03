package com.avereon.xenon.tool.product;

import com.avereon.product.ProductCard;
import com.avereon.product.Rb;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.Xenon;
import com.avereon.xenon.product.DownloadRequest;
import com.avereon.xenon.product.ProductStatus;
import com.avereon.xenon.task.TaskEvent;
import com.avereon.zarra.javafx.Fx;
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

	private Xenon program;

	private ProductTool productTool;

	private List<ProductPane> sources;

	private Labeled message;

	private String refreshMessage;

	private String missingMessage;

	ProductPage( Xenon program, ProductTool productTool, String productType ) {
		this.program = program;
		this.productTool = productTool;
		sources = new CopyOnWriteArrayList<>();
		setTitle( Rb.text( RbKey.TOOL, "product-" + productType ) );

		this.refreshMessage = Rb.text( RbKey.TOOL, "product-" + productType + "-refresh" );
		this.missingMessage = Rb.text( RbKey.TOOL, "product-" + productType + "-missing" );

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
			//pane.setProductSize( request.getCard().get)
			request
				.register( TaskEvent.START, e -> Fx.run( () -> {
					pane.setSize( e.getTask().getTotal() );
					pane.setStatus( ProductStatus.DOWNLOADING );
				} ) )
				.register( TaskEvent.PROGRESS, e -> Fx.run( () -> pane.setProgress( e.getTask().getPercent() ) ) )
				.register( TaskEvent.CANCEL, e -> Fx.run( () -> pane.setStatus( install ? ProductStatus.NOT_INSTALLED : ProductStatus.AVAILABLE ) ) )
				.register( TaskEvent.FAILURE, e -> Fx.run( () -> pane.setStatus( install ? ProductStatus.NOT_INSTALLED : ProductStatus.AVAILABLE ) ) )
				.register( TaskEvent.SUCCESS, e -> Fx.run( () -> pane.setStatus( install ? ProductStatus.INSTALLED : ProductStatus.DOWNLOADED ) ) );
			return request;
		} ).collect( Collectors.toSet() );
	}

	public Xenon getProgram() {
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
