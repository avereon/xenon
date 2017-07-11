package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.resource.Resource;
import com.parallelsymmetry.essence.worktool.ToolException;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.TimeZone;

public class ProductInfoTool extends ProductTool {

	private static final Logger log = LoggerFactory.getLogger( ProductInfoTool.class );

	private String titleSuffix;

	private ProductMetadata metadata;

	private TextArea text;

	public ProductInfoTool( Product product, Resource resource ) {
		super( product, resource );
		setId( "tool-product-info" );

		setTitleSuffix( product.getResourceBundle().getString( "tool", "product-info-suffix" ) );

		text = new TextArea();
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
	protected void allocate() throws ToolException {
		log.info( "Tool allocate" );
	}

	@Override
	protected void display() throws ToolException {
		log.info( "Tool display" );
	}

	@Override
	protected void activate() throws ToolException {
		log.info( "Tool activate" );
	}

	@Override
	protected void deactivate() throws ToolException {
		log.info( "Tool deactivate" );
	}

	@Override
	protected void conceal() throws ToolException {
		log.info( "Tool conceal" );
	}

	@Override
	protected void deallocate() throws ToolException {
		log.info( "Tool deallocate" );
	}

}
