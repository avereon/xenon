package com.parallelsymmetry.essence;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * This class must load the product metadata very quickly.
 */
// TODO Use Lombok when it is supported in Java 9
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

		this.group = (String)values.get( "group" );
		this.artifact = (String)values.get( "artifact" );
		this.version = (String)values.get( "version" );
//		this.timestamp = (String)values.get( "timestamp" );

		this.name = (String)values.get( "name" );
//		this.provider = (String)values.get( "provider" );
//		this.inception = (String)values.get( "inception" );
//
//		this.summary = (String)values.get( "summary" );
//		this.description = (String)values.get( "description" );
	}

	public String getGroup() {
		return group;
	}

	public void setGroup( String group ) {
		this.group = group;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact( String artifact ) {
		this.artifact = artifact;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion( String version ) {
		this.version = version;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp( String timestamp ) {
		this.timestamp = timestamp;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider( String provider ) {
		this.provider = provider;
	}

	public String getInception() {
		return inception;
	}

	public void setInception( String inception ) {
		this.inception = inception;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary( String summary ) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription( String description ) {
		this.description = description;
	}
}
