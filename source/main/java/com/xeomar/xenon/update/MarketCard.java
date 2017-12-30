package com.xeomar.xenon.update;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

@JsonIgnoreProperties( ignoreUnknown = true )
public class MarketCard {

	public static final String CARD = "/META-INF/catalog.card";

	private static final Logger log = LoggerFactory.getLogger( MarketCard.class );

	// TODO Use Lombok when it is supported in Java 9

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
		return new ObjectMapper().readerFor( new TypeReference<MarketCard>() {} ).readValue( input );
	}

	public MarketCard updateWith( MarketCard card, URI source ) {
		this.name = card.name;
		this.iconUri = card.iconUri;
		this.cardUri = card.cardUri;
		this.enabled = card.enabled;
		this.removable = card.removable;
		this.products = card.products;

		if( source != null ) this.cardUri = source.toString();

		return this;
	}

	//	@Deprecated
	//	public static MarketCard loadYaml( InputStream input ) throws IOException {
	//		return loadYaml( input, null );
	//	}
	//
	//	@Deprecated
	//	@SuppressWarnings( "unchecked" )
	//	public static MarketCard loadYaml( InputStream input, URI source ) throws IOException {
	//		Map<String, Object> values;
	//		try( InputStream stream = input ) {
	//			values = (Map<String, Object>)new Yaml().load( stream );
	//		}
	//
	//		MarketCard card = new MarketCard();
	//
	//		card.name = (String)values.get( "name" );
	//		card.iconUri = (String)values.get( "iconUri" );
	//		card.cardUri = source == null ? (String)values.get( "cardUri" ) : source.toString();
	//
	//		Object enabledValue = values.get( "enabled" );
	//		card.enabled = enabledValue != null && Boolean.parseBoolean( enabledValue.toString() );
	//
	//		Object removableValue = values.get( "removable" );
	//		card.removable = removableValue != null && Boolean.parseBoolean( removableValue.toString() );
	//
	//		return card;
	//	}

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
