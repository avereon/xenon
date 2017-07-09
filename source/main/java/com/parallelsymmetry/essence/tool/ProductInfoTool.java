package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.resource.ResourceAdapter;
import com.parallelsymmetry.essence.resource.ResourceEvent;
import com.parallelsymmetry.essence.resource.ResourceListener;
import com.parallelsymmetry.essence.workspace.ToolInstanceMode;
import com.parallelsymmetry.essence.worktool.ToolException;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;

import java.util.Calendar;
import java.util.TimeZone;

public class ProductInfoTool extends ProductTool {

	private String titleSuffix;

	private ProductMetadata metadata;

	private TextArea text;

	private ResourceListener watcher;

	public ProductInfoTool( Product product, Resource resource ) {
		super( product, resource );

		setTitleSuffix( product.getResourceBundle().getString( "tool", "product-info-suffix" ) );

		text = new TextArea();
		text.setBackground( Background.EMPTY );
		text.setEditable( false );

		BorderPane border = new BorderPane();
		border.setPadding( new Insets( 10 ) );
		border.setCenter( text );
		getChildren().add( border );

		// FIXME The resource may not be loaded at this point
		setMetadata( resource.getModel() );
	}

	public String getTitleSuffix() {
		return titleSuffix;
	}

	public void setTitleSuffix( String titleSuffix ) {
		this.titleSuffix = titleSuffix;
	}

	public ProductMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata( ProductMetadata metadata ) {
		this.metadata = metadata;
		if( titleSuffix == null ) {
			setTitle( metadata.getName() );
		} else {
			setTitle( metadata.getName() + " - " + titleSuffix );
		}

		StringBuilder builder = new StringBuilder();
		builder.append( metadata.getName() );
		builder.append( " " );
		builder.append( metadata.getVersion() );
		builder.append( "\n" );
		builder.append( "  build " );
		builder.append( metadata.getTimestamp() );
		builder.append( " UTC\n" );
		builder.append( "  by " );
		builder.append( metadata.getProvider() );
		builder.append( "\n" );

		builder.append( "\n" );

		builder.append( System.getProperty( "java.vm.name" ) );
		builder.append( " (" );
		builder.append( System.getProperty( "java.vm.version" ) );
		builder.append( ", " );
		builder.append( System.getProperty( "java.vm.info" ) );
		builder.append( ")\n" );
		builder.append( "  by " );
		builder.append( System.getProperty( "java.vm.vendor" ) );
		builder.append( "\n" );

		builder.append( "\n" );

		builder.append( System.getProperty( "os.name" ) );
		builder.append( " " );
		builder.append( System.getProperty( "os.version" ) );
		builder.append( " " );
		builder.append( System.getProperty( "os.arch" ) );
		builder.append( "\n" );

		builder.append( "\n" );
		builder.append( "\n" );

		int year = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) ).get( Calendar.YEAR );

		builder.append( "\u00A9" );
		if( year == metadata.getInception() ) {
			builder.append( metadata.getInception() );
		} else {
			builder.append( metadata.getInception() );
			builder.append( "-" );
			builder.append( year );
		}
		builder.append( " " );
		builder.append( metadata.getProvider() );
		builder.append( " " );
		builder.append( metadata.getCopyrightSummary() );
		builder.append( "\n" );

		text.setText( builder.toString().trim() );
	}

	@Override
	public ToolInstanceMode getInstanceMode() {
		return ToolInstanceMode.SINGLETON;
	}

	@Override
	protected void allocate() throws ToolException {
		super.allocate();
		getResource().addResourceListener( watcher = new ResourceWatcher() );
	}

	@Override
	protected void deallocate() throws ToolException {
		getResource().removeResourceListener( watcher );
		super.deallocate();
	}

	private class ResourceWatcher extends ResourceAdapter {

		@Override
		public void resourceClosed( ResourceEvent event ) {
			ProductInfoTool.this.close();
		}

	}

}
