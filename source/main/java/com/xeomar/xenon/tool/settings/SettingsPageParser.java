package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class SettingsPageParser {

	private static final Logger log = LoggerFactory.getLogger( SettingsPageParser.class );

	public static final String PRODUCT = "product";

	public static final String SETTINGS = "settings";

	private static final String PAGE = "page";

	private static final String GROUP = "group";

	private static final String SETTING = "setting";

	private static final String OPTION = "option";

	private static final String DEPENDENCY = "dependency";

	private static final String KEY = "key";

	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	private Product product;

	private Settings settings;

	public SettingsPageParser( Product product, Settings settings ) {
		this.product = product;
		this.settings = settings;
	}

	public Map<String, SettingsPage> parse( String path ) throws IOException {
		if( path.startsWith( "/" ) ) path = path.substring( 1 );
		InputStream input = product.getClassLoader().getResourceAsStream( path );
		if( input == null ) log.warn( "Settings page input stream is null: " + path );
		return parse( input );
	}

	public Map<String, SettingsPage> parse( InputStream input ) throws IOException {
		if( input == null ) return null;

		Map<String, SettingsPage> pages = new HashMap<>();
		XMLStreamReader reader;
		try {
			reader = xmlInputFactory.createXMLStreamReader( input );
			parse( reader, pages );
			reader.close();
		} catch( XMLStreamException exception ) {
			throw new IOException( exception );
		} finally {
			input.close();
		}

		return pages;
	}

	private void parse( XMLStreamReader reader, Map<String, SettingsPage> pages ) throws XMLStreamException {
		if( !reader.hasNext() ) return;

		reader.next();
		if( !reader.getLocalName().equals( SETTINGS ) ) return;

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( SETTINGS ) ) break;

			switch( reader.getEventType() ) {
				case XMLStreamReader.START_ELEMENT: {
					String tagName = reader.getLocalName();
					if( PAGE.equals( tagName ) ) {
						SettingsPage page = parsePage( reader );
						pages.put( page.getId(), page );
					}
				}
			}
		}
	}

	private SettingsPage parsePage( XMLStreamReader reader ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );

		String id = attributes.get( "id" );
		String icon = attributes.get( "icon" );
		if( icon == null ) icon = "setting";
		String title = attributes.get( "title" );
		if( title == null ) title = product.getResourceBundle().getString( "settings", id );
		if( title == null ) title = id;

		SettingsPage page = new SettingsPage();
		page.putResource( PRODUCT, product );
		page.putResource( SETTINGS, settings );
		page.setId( id );
		page.setIcon( icon );
		page.setTitle( title );

		SettingGroup group = null;
		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( PAGE ) ) break;

			switch( reader.getEventType() ) {
				case XMLStreamReader.START_ELEMENT: {
					String tagName = reader.getLocalName();
					if( PAGE.equals( tagName ) ) {
						page.addPage( parsePage( reader ) );
					} else if( GROUP.equals( tagName ) ) {
						if( group != null ) {
							page.addGroup( group );
							group = null;
						}
						page.addGroup( parseGroup( reader ) );
					} else if( SETTING.equals( tagName ) ) {
						if( group == null ) group = new SettingGroup( settings );
						group.addSetting( parseSetting( reader ) );
					}
				}
			}
		}

		if( group != null ) page.addGroup( group );

		return page;
	}

	private SettingGroup parseGroup( XMLStreamReader reader ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );

		String id = attributes.get( "id" );

		SettingGroup group = new SettingGroup( settings );
		group.setId( id );

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( GROUP ) ) break;

			switch( reader.getEventType() ) {
				case XMLStreamReader.START_ELEMENT: {
					String tagName = reader.getLocalName();
					if( SETTING.equals( tagName ) ) {
						group.addSetting( parseSetting( reader ) );
					} else if( DEPENDENCY.equals( tagName ) ) {
						group.addDependency( parseDependency( reader ) );
					}
				}
			}
		}

		return group;
	}

	private Setting parseSetting( XMLStreamReader reader ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );

		String key = attributes.get( "key" );
		String presentation = attributes.get( "presentation" );
		String enabled = attributes.get( "disabled" );
		if( enabled == null ) enabled = "false";
		String opaque = attributes.get( "opaque" );
		if( opaque == null ) opaque = "false";

		Setting setting = new Setting( settings );
		setting.setKey( key );
		setting.setPresentation( presentation );
		setting.setEnabled( Boolean.parseBoolean( enabled ) );
		setting.setOpaque( Boolean.parseBoolean( opaque ) );

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( SETTING ) ) break;

			switch( reader.getEventType() ) {
				case XMLStreamReader.START_ELEMENT: {
					String tagName = reader.getLocalName();
					if( OPTION.equals( tagName ) ) {
						setting.addOption( parseOption( reader, setting ) );
					} else if( DEPENDENCY.equals( tagName ) ) {
						setting.addDependency( parseDependency( reader ) );
					}
				}
			}
		}

		return setting;
	}

	private SettingOption parseOption( XMLStreamReader reader, Setting setting ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );

		// Collect the option text if it exists
		StringBuilder textBuilder = new StringBuilder();
		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( OPTION ) ) break;

			int type = reader.getEventType();
			if( type == XMLStreamConstants.CHARACTERS ) textBuilder.append( reader.getText() );
		}
		String text = textBuilder.length() == 0 ? null : textBuilder.toString();

		// Determine the option key
		String key = attributes.get( "key" );
		if( key == null ) key = attributes.get( "value" );
		if( key == null ) key = text;

		// Determine the option name
		String optionName = text;
		String nameRbKey = getBundleName( setting.getKey() ) + "-" + key;
		if( optionName == null ) optionName = product.getResourceBundle().getString( "settings", nameRbKey );

		// Determine the option value
		String optionValue = attributes.get( "value" );
		if( optionValue == null ) optionValue = text;
		if( optionValue == null ) optionValue = attributes.get( "key" );

		// Set the option parameters
		SettingOption option = new SettingOption();
		option.setKey( key );
		option.setName( optionName );
		option.setOptionValue( optionValue );

		return option;
	}

	public static String getBundleName( String key ) {
		if( key == null ) return null;
		if( key.startsWith( "/" ) ) key = key.substring( 1 );
		key = key.replace( '/', '-' );
		return key;
	}

	private Map<String, String> parseAttributes( XMLStreamReader reader ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = new HashMap<>();
		int count = reader.getAttributeCount();
		for( int index = 0; index < count; index++ ) {
			attributes.put( reader.getAttributeLocalName( index ), reader.getAttributeValue( index ) );
		}
		return attributes;
	}

	private SettingDependency parseDependency( XMLStreamReader reader ) throws XMLStreamException {
		SettingDependency dependency = new SettingDependency();

		// Read the attributes.
		String key;
		String value;
		int count = reader.getAttributeCount();
		for( int index = 0; index < count; index++ ) {
			key = reader.getAttributeLocalName( index );
			value = reader.getAttributeValue( index );

			switch( key ) {
				case "operator": {
					dependency.setOperator( SettingDependency.Operator.valueOf( value.toUpperCase() ) );
					break;
				}
				case "key": {
					break;
				}
				case "value": {
					break;
				}
				default: {
					log.warn( "Unknown dependency attribute: " + key + "=" + value );
				}
			}
		}

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( DEPENDENCY ) ) break;

			switch( reader.getEventType() ) {
				case XMLStreamReader.START_ELEMENT: {
					String tagName = reader.getLocalName();
					if( DEPENDENCY.equals( tagName ) ) {
						dependency.addDependency( parseDependency( reader ) );
					}
				}
			}
		}

		return dependency;
	}

	//	private void printTag() {
	//		Log.write( "Tag: " + reader.getEventType() + " > " + reader.getLocalName() );
	//	}

}
