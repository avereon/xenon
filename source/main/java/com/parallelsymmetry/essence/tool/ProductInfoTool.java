package com.parallelsymmetry.essence.tool;

import com.parallelsymmetry.essence.ProductTool;
import com.parallelsymmetry.essence.product.Product;
import com.parallelsymmetry.essence.product.ProductMetadata;
import com.parallelsymmetry.essence.resource.Resource;
import javafx.scene.control.TextArea;

import java.util.*;

public class ProductInfoTool extends ProductTool {

	private String titleSuffix;

	private ProductMetadata metadata;

	private TextArea area;

	public ProductInfoTool( Product product, Resource resource ) {
		super( product, resource );

		area = new TextArea();
		area.setEditable( false );
		getChildren().add( area );

		setTitleSuffix( product.getResourceBundle().getString( "tool", "product-info-suffix" ) );
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

		Properties properties = System.getProperties();
		List<String> keys = new ArrayList<>();
		for( Object object : properties.keySet() ) {
			keys.add( object.toString() );
		}
		Collections.sort( keys );
		for( String key : keys ) {
			String value = properties.getProperty( key );
			System.out.println( key + "=" + value );
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

		area.setText( builder.toString() );
	}

}
