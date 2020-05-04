package com.avereon.xenon.tool.settings;

import com.avereon.product.Product;
import com.avereon.settings.Settings;
import com.avereon.util.Log;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Map;

public class SettingsPageParser {

	private static final Logger log = Log.get();

	private static final String SETTINGS = "settings";

	private static final String PAGE = "page";

	private static final String GROUP = "group";

	private static final String SETTING = "setting";

	private static final String PROVIDER = "provider";

	private static final String OPTION = "option";

	private static final String DEPENDENCY = "dependency";

	private static final String OPERATOR = "operator";

	private static final String ID = "id";

	private static final String KEY = "key";

	private static final String VALUE = "value";

	private static final String ICON = "icon";

	private static final String TITLE = "title";

	private static final String EDITOR = "editor";

	private static final String DISABLE = "disable";

	private static final String EDITABLE = "editable";

	private static final String OPAQUE = "opaque";

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
		if( input == null ) log.log( Log.WARN,  "Settings page input stream is null: " + path );
		return parse( input );
	}

	private Map<String, SettingsPage> parse( InputStream input ) throws IOException {
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

		String id = attributes.get( ID );
		String icon = attributes.get( ICON );
		if( icon == null ) icon = SETTING;
		String title = attributes.get( TITLE );
		if( title == null ) title = product.rb().text( "settings", id );
		if( title == null ) title = id;

		SettingsPage page = new SettingsPage();
		page.setProduct( product );
		page.setSettings( settings );
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

		String id = attributes.get( ID );

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

		String key = attributes.get( KEY );
		String editor = attributes.get( EDITOR );
		String disable = attributes.get( DISABLE );
		if( disable == null ) disable = String.valueOf( false );
		String opaque = attributes.get( OPAQUE );
		if( opaque == null ) opaque = String.valueOf( false );
		String editable = attributes.get( EDITABLE );
		if( editable == null ) editable = String.valueOf( false );
		String provider = attributes.get( PROVIDER );

		Setting setting = new Setting( settings );
		setting.setKey( key );
		setting.setEditor( editor );
		setting.setDisable( Boolean.parseBoolean( disable ) );
		//setting.setEditable( Boolean.parseBoolean( editable ) );
		setting.setOpaque( Boolean.parseBoolean( opaque ) );
		setting.setProvider( provider );

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( SETTING ) ) break;

			if( reader.getEventType() == XMLStreamReader.START_ELEMENT ) {
				String tagName = reader.getLocalName();
				if( OPTION.equals( tagName ) ) {
					setting.addOption( parseOption( reader, setting ) );
				} else if( DEPENDENCY.equals( tagName ) ) {
					setting.addDependency( parseDependency( reader ) );
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
		String key = attributes.get( KEY );
		if( key == null ) key = attributes.get( VALUE );
		if( key == null ) key = text;

		// Determine the option name
		String optionName = text;
		String nameRbKey = getBundleKey( setting.getKey() ) + "-" + key;
		if( optionName == null ) optionName = product.rb().text( "settings", nameRbKey );

		// Determine the option value
		String optionValue = attributes.get( VALUE );
		if( optionValue == null ) optionValue = text;
		if( optionValue == null ) optionValue = attributes.get( KEY );

		// Set the option parameters
		SettingOption option = new SettingOption();
		option.setKey( key );
		option.setName( optionName );
		option.setOptionValue( optionValue );

		return option;
	}

	private SettingDependency parseDependency( XMLStreamReader reader ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );

		String operator = attributes.get( OPERATOR );
		operator = operator != null ? operator.toUpperCase() : SettingDependency.Operator.AND.name();

		SettingDependency dependency = new SettingDependency();
		dependency.setOperator( SettingDependency.Operator.valueOf( operator ) );
		dependency.setKey( attributes.get( KEY ) );
		dependency.setDependencyValue( attributes.get( VALUE ) );

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

	private static String getBundleKey( String key ) {
		if( key == null ) return null;
		if( key.startsWith( "/" ) ) key = key.substring( 1 );
		return key.replace( '/', '-' );
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

	//	private void printTag() {
	//		Log.write( "Tag: " + reader.getEventType() + " > " + reader.getLocalName() );
	//	}

}
