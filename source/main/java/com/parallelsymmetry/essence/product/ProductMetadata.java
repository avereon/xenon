package com.parallelsymmetry.essence.product;

import com.parallelsymmetry.essence.person.Contributor;
import com.parallelsymmetry.essence.person.Maintainer;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * This class must load the product metadata very quickly.
 */
// TODO Use Lombok when it is supported in Java 9
public class ProductMetadata {

	private String group;

	private String artifact;

	private String version;

	private String timestamp;

	private String icon;

	private String name;

	private String provider;

	private int inception;

	private String summary;

	private String description;

	private String copyrightSummary;

	private String licenseSummary;

	private List<Maintainer> maintainers;

	private List<Contributor> contributors;

	public ProductMetadata() throws IOException {
		InputStream stream = getClass().getResourceAsStream( "/META-INF/product.properties" );
		Properties values = new Properties();
		values.load( stream );

		this.group = values.getProperty( "group" );
		this.artifact = values.getProperty( "artifact" );
		this.version = values.getProperty( "version" );
		this.timestamp = values.getProperty( "timestamp" );

		this.icon = values.getProperty( "icon" );
		this.name = values.getProperty( "name" );
		this.provider = values.getProperty( "provider" );
		this.inception = Integer.parseInt( values.getProperty( "inception" ) );

		this.summary = values.getProperty( "summary" );
		this.description = values.getProperty( "description" );
		this.copyrightSummary = values.getProperty( "copyright" );
		this.licenseSummary = values.getProperty( "license" );
	}

	//	public ProductMetadata() {
	//		InputStream stream = getClass().getResourceAsStream( "/META-INF/product.yaml" );
	//		Map<String, Object> values = (Map<String, Object>)new Yaml().load( stream );
	//
	//		this.group = (String)values.get( "group" );
	//		this.artifact = (String)values.get( "artifact" );
	//		this.version = (String)values.get( "version" );
	//		this.timestamp = (String)values.get( "timestamp" );
	//
	//		this.icon = (String)values.get( "icon" );
	//		this.name = (String)values.get( "name" );
	//		this.provider = (String)values.get( "provider" );
	//		this.inception = (Integer)values.get( "inception" );
	//
	//		this.summary = (String)values.get( "summary" );
	//		this.description = (String)values.get( "description" );
	//		this.copyrightSummary = (String)values.get( "copyright.summary" );
	//		this.licenseSummary = (String)values.get( "license.summary" );
	//
	//		this.maintainers = (List<Maintainer>)values.get( "maintainers" );
	//		this.contributors = (List<Contributor>)values.get( "contributors" );
	//	}

	@SuppressWarnings( "unchecked" )
	public void loadContributors() {
		InputStream stream = getClass().getResourceAsStream( "/META-INF/product.yaml" );
		Map<String, Object> values = (Map<String, Object>)new Yaml().load( stream );
		this.maintainers = (List<Maintainer>)values.get( "maintainers" );
		this.contributors = (List<Contributor>)values.get( "contributors" );
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

	public String getIcon() {
		return icon;
	}

	public void setIcon( String icon ) {
		this.icon = icon;
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

	public int getInception() {
		return inception;
	}

	public void setInception( int inception ) {
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

	public String getCopyrightSummary() {
		return copyrightSummary;
	}

	public void setCopyrightSummary( String copyrightSummary ) {
		this.copyrightSummary = copyrightSummary;
	}

	public String getLicenseSummary() {
		return licenseSummary;
	}

	public void setLicenseSummary( String licenseSummary ) {
		this.licenseSummary = licenseSummary;
	}

	public List<Maintainer> getMaintainers() {
		return maintainers;
	}

	public void setMaintainers( List<Maintainer> maintainers ) {
		this.maintainers = maintainers;
	}

	public List<Contributor> getContributors() {
		return contributors;
	}

	public void setContributors( List<Contributor> contributors ) {
		this.contributors = contributors;
	}
}
