package com.xeomar.xenon.update;

import java.util.Objects;

public class ProductCoordinates {

	private String artifact;

	private String platform;

	private String asset;

	private String format;

	public ProductCoordinates( String artifact, String platform, String asset, String format ) {
		this.artifact = artifact;
		this.platform = platform;
		this.asset = asset;
		this.format = format;
	}

	public String getArtifact() {
		return artifact;
	}

	public void setArtifact( String artifact ) {
		this.artifact = artifact;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform( String platform ) {
		this.platform = platform;
	}

	public String getAsset() {
		return asset;
	}

	public void setAsset( String asset ) {
		this.asset = asset;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat( String format ) {
		this.format = format;
	}

	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder( artifact );
		if( platform != null ) builder.append( "/" ).append( platform );
		builder.append( "/" ).append( asset );
		if( format != null ) builder.append( "." ).append( format );
		return builder.toString();
	}

	@Override
	public boolean equals( Object o ) {
		if( this == o ) return true;
		if( o == null || getClass() != o.getClass() ) return false;
		ProductCoordinates that = (ProductCoordinates)o;
		return artifact.equals( that.artifact ) && Objects.equals( platform, that.platform ) && asset.equals( that.asset ) && Objects.equals( format, that.format );
	}

	@Override
	public int hashCode() {
		return Objects.hash( artifact, platform, asset, format );
	}
}
