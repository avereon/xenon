package com.xeomar.xenon.tool.settings;

import com.xeomar.xenon.IconLibrary;
import com.xeomar.xenon.product.Product;
import com.xeomar.xenon.settings.Settings;
import com.xeomar.xenon.tool.GuideNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

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

	public Set<SettingsPage> parse( String path ) throws IOException {
		if( path.startsWith( "/" ) ) path = path.substring( 1 );
		InputStream input = product.getClassLoader().getResourceAsStream( path );
		if( input == null ) log.warn( "Settings page input stream is null: " + path );
		return parse( input );
	}

	public Set<SettingsPage> parse( InputStream input ) throws IOException {
		if( input == null ) return null;

		Set<SettingsPage> pages = new HashSet<>();
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

	private void parse( XMLStreamReader reader, Set<SettingsPage> pages ) throws XMLStreamException {
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
						pages.add( parsePage( reader, pages ) );
					}
				}
			}
		}
	}

	private SettingsPage parsePage( XMLStreamReader reader, Set<SettingsPage> pages ) throws XMLStreamException {
		IconLibrary library = product.getProgram().getIconLibrary();

		SettingsPage page = new SettingsPage();
		page.putResource( PRODUCT, product );
		page.putResource( SETTINGS, settings );
		page.putResource( GuideNode.ICON, library.getIcon( "setting" ) );

		// Read the attributes.
		String key;
		String value;
		int count = reader.getAttributeCount();
		for( int index = 0; index < count; index++ ) {
			key = reader.getAttributeLocalName( index );
			value = reader.getAttributeValue( index );

			switch( key ) {
				case "key": {
					page.setKey( value );
					page.putResource( GuideNode.NAME, product.getResourceBundle().getString( "settings", value ) );
					break;
				}
				case "icon": {
					page.setIcon( value );
					page.putResource( GuideNode.ICON, library.getIcon( value ) );
					break;
				}
				case "title": {
					page.setTitle( value );
					break;
				}
				default: {
					log.warn( "Unknown settings page attribute: " + key + "=" + value );
					break;
				}
			}
		}

		SettingGroup group = null;

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( PAGE ) ) break;

			switch( reader.getEventType() ) {
				case XMLStreamReader.START_ELEMENT: {
					String tagName = reader.getLocalName();
					if( PAGE.equals( tagName ) ) {
						page.addPage( parsePage( reader, pages ) );
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
		SettingGroup group = new SettingGroup( settings );

		// Read the attributes.
		String name;
		String value;
		int count = reader.getAttributeCount();
		for( int index = 0; index < count; index++ ) {
			name = reader.getAttributeLocalName( index );
			value = reader.getAttributeValue( index );

			switch( name ) {
				case "key": {
					group.setKey( value );
					break;
				}
				default: {
					log.warn( "Unknown group attribute: " + name + "=" + value );
					break;
				}
			}
		}

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
		Setting setting = new Setting( settings );

				// Read the attributes.
				String name;
				String value;
				int count = reader.getAttributeCount();
				for( int index = 0; index < count; index++ ) {
					name = reader.getAttributeLocalName( index );
					value = reader.getAttributeValue( index );

					switch( name ) {
						case "key" : {
							setting.setKey( value );
							break;
						}
						case "presentation" : {
							setting.setPresentation( value );
							break;
						}
						case "disabled" : {
							setting.setEnabled( !Boolean.parseBoolean( value ) );
							break;
						}
						case "opaque" : {
							setting.setOpaque( Boolean.parseBoolean( value ) );
							break;
						}
						default : {
							log.warn( "Unknown setting attribute: " + name + "=" + value );
							break;
						}
					}
				}

				while( reader.hasNext() ) {
					reader.next();
					if( reader.isEndElement() && reader.getLocalName().equals( SETTING ) ) break;

					switch( reader.getEventType() ) {
						case XMLStreamReader.START_ELEMENT: {
							String tagName = reader.getLocalName();
							if( OPTION.equals( tagName ) ) {
								setting.addOption( parseOption( product, reader, setting ) );
							} else if( DEPENDENCY.equals( tagName ) ) {
								setting.getDependencies().add( parseDependency( reader ) );
							}
						}
					}
				}

		return setting;
	}

		private  SettingOption parseOption( Product product, XMLStreamReader reader, Setting setting ) throws XMLStreamException {

	//		Map<String, String> attributes = new HashMap<String, String>();
	//
	//		// Read the option attributes.
	//		int count = reader.getAttributeCount();
	//		for( int index = 0; index < count; index++ ) {
	//			attributes.put( reader.getAttributeLocalName( index ), reader.getAttributeValue( index ) );
	//		}
	//
	//		// Collect the option text if it exists.
	//		StringBuilder textBuilder = new StringBuilder();
	//		while( reader.hasNext() ) {
	//			reader.next();
	//			if( reader.isEndElement() && reader.getLocalName().equals( OPTION ) ) break;
	//
	//			int type = reader.getEventType();
	//			if( type == XMLStreamConstants.CHARACTERS ) textBuilder.append( reader.getText() );
	//		}
	//		String text = textBuilder.length() == 0 ? null : textBuilder.toString();
	//
	//		// Determine the option key and name values.
	//		String key = attributes.get( SettingOption.KEY );
	//		if( key == null ) key = attributes.get( SettingOption.VALUE );
	//		if( key == null ) key = text;
	//
	//		// Determine the name.
	//		String prefkey = setting.getBundleName();
	//		String name = text;
	//		if( name == null ) name = ProductUtil.getString( product, BundleKey.SETTINGS, prefkey + "." + key );
	//
	//		// Determine the option value.
	//		String value = attributes.get( SettingOption.VALUE );
	//		if( value == null ) value = text;
	//		if( value == null ) value = attributes.get( SettingOption.KEY );
	//
	//		// Create the option object.
	//		SettingOption option = new SettingOption( key, name, value );
	//
	//		// Copy the attributes to the object.
	//		for( String attributeKey : attributes.keySet() ) {
	//			option.setAttribute( attributeKey, attributes.get( key ) );
	//		}
	//
	//		return option;
			return null;
		}

		private  SettingDependency parseDependency( XMLStreamReader reader ) throws XMLStreamException {
			SettingDependency dependency = new SettingDependency();

			// Read the attributes.
			String key;
			String value;
			int count = reader.getAttributeCount();
			for( int index = 0; index < count; index++ ) {
				key = reader.getAttributeLocalName( index );
				value = reader.getAttributeValue( index );

				switch( key ) {
					case "operator" : {
						dependency.setOperator( SettingDependency.Operator.valueOf( value.toUpperCase() ) );
						break;
					}
					case "key" : {
						break;
					}
					case "value" : {
						break;
					}
					default : {
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
