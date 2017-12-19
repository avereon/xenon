package com.xeomar.xenon.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class MarketCard {

	public static final String CARD = "/META-INF/catalog.card";

	private static final Logger log = LoggerFactory.getLogger( MarketCard.class );

	private String name;

	private String iconUri;

	private String cardUri;

	private boolean enabled;

	private boolean removable;

	private List<String> products;

	public static MarketCard forProduct() throws IOException {
		return loadCard( MarketCard.class.getResourceAsStream( CARD ) );
	}

	public static MarketCard loadCard( InputStream input ) throws IOException {
		return loadCard( input , null );
	}

	@SuppressWarnings( "unchecked" )
	public static MarketCard loadCard( InputStream input, URI source ) throws IOException {
		Map<String, Object> values;
		try( InputStream stream = input ) {
			values = (Map<String, Object>)new Yaml().load( stream );
		}

		MarketCard card = new MarketCard();

		card.name = (String)values.get( "name" );
		card.iconUri = (String)values.get( "icon" );
		card.cardUri = source == null ? (String)values.get( "card" ) : source.toString();

		Object enabledValue = values.get("enabled" );
		card.enabled = enabledValue != null && Boolean.parseBoolean( enabledValue.toString() );

		Object removableValue = values.get( "removable" );
		card.removable = removableValue != null && Boolean.parseBoolean( removableValue.toString() );

		return card;
	}

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}

	public String getIconUri() {
		return iconUri;
	}

	public void setIconUri( String iconUri ) {
		this.iconUri = iconUri;
	}

	public String getCardUri() {
		return cardUri;
	}

	public void setCardUri( String cardUri ) {
		this.cardUri = cardUri;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	public boolean isRemovable() {
		return removable;
	}

	public void setRemovable( boolean removable ) {
		this.removable = removable;
	}

	public List<String> getProducts() {
		return products;
	}

	public void setProducts( List<String> products ) {
		this.products = products;
	}

}
