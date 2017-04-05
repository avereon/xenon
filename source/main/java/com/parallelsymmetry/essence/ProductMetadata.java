package com.parallelsymmetry.essence;

import lombok.Data;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * This class must load the product metadata very quickly.
 */
@Data
public class ProductMetadata {

	private String group;

	private String artifact;

	private String version;

	private String timestamp;

	private String name;

	private String provider;

	private String inception;

	private String summary;

	private String description;

	public ProductMetadata() {
		InputStream stream = getClass().getResourceAsStream( "/META-INF/product.yaml" );
		Map<String, Object> values = (Map<String, Object>)new Yaml().load( stream );

//		this.group = (String)values.get( "group" );
//		this.artifact = (String)values.get( "artifact" );
//		this.version = (String)values.get( "version" );
//		this.timestamp = (String)values.get( "timestamp" );

		this.name = (String)values.get( "name" );
//		this.provider = (String)values.get( "provider" );
//		this.inception = (String)values.get( "inception" );
//
//		this.summary = (String)values.get( "summary" );
//		this.description = (String)values.get( "description" );
	}

}
