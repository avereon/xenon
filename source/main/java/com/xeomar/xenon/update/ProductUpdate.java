package com.xeomar.xenon.update;

import com.xeomar.product.ProductCard;
import com.xeomar.settings.Settings;
import com.xeomar.util.Configurable;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * NOTE: This class is Persistent and changing the package will most likely
 * result in a ClassNotFoundException being thrown at runtime.
 * 
 * @author SoderquistMV
 */
public final class ProductUpdate implements Configurable {

	private ProductCard card;

	private Path source;

	private Path target;

	private Settings settings;

	/*
	 * This constructor is used by the settings API via reflection.
	 */
	public ProductUpdate() {}

	public ProductUpdate( ProductCard card, Path source, Path target ) {
		this.card = card;
		this.source = source;
		this.target = target;
	}

	public ProductCard getCard() {
		return card;
	}

	public Path getSource() {
		return source;
	}

	public Path getTarget() {
		return target;
	}

	@Override
	public void setSettings( Settings settings ) {
		this.settings = settings;
//		card = new ProductCard( settings.getNode( "card" ) );
		String sourcePath = settings.get( "source", null );
		String targetPath = settings.get( "target", null );
		source = sourcePath == null ? null : Paths.get( sourcePath );
		target = targetPath == null ? null : Paths.get( targetPath );
	}

	@Override
	public Settings getSettings(  ) {
		return settings;
//		card.saveSettings( settings.getNode( "card" ) );
//		settings.put( "source", source.getPath() );
//		settings.put( "target", target.getPath() );
	}

	@Override
	public int hashCode() {
		return card.hashCode();
	}

	@Override
	public boolean equals( Object object ) {
		if( !( object instanceof ProductUpdate ) ) return false;
		ProductUpdate that = (ProductUpdate)object;
		return this.card.equals( that.card );
	}

}
