package com.avereon.xenon.tool.settings;

import com.avereon.product.Rb;
import com.avereon.util.TextUtil;
import com.avereon.xenon.RbKey;
import com.avereon.xenon.XenonProgramProduct;
import lombok.CustomLog;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CustomLog
public class SettingsPageParser {

	private static final String PAGES = "pages";

	private static final String PAGE = "page";

	private static final String GROUP = "group";

	private static final String SETTING = "setting";

	private static final String OPTION = "option";

	private static final String DEPENDENCY = "dependency";

	private static final String FAIL_DEPENDENCY_ACTION = "on-dependency-failure";

	private static final String OPERATOR = "operator";

	private static final String ID = "id";

	private static final String KEY = "key";

	private static final String VALUE = "value";

	private static final String ICON = "icon";

	private static final String TITLE = "title";

	private static final String PANEL = "panel";

	private static final String PRODUCT_ICON = "product";

	private static final String SETTING_ICON = "setting";

	private static final String PRODUCT_PAGE = "product-page";

	private static final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

	private final XenonProgramProduct product;

	private SettingsPageParser( XenonProgramProduct product ) {
		this.product = product;
	}

	public static Map<String, SettingsPage> parse( XenonProgramProduct product, String path ) throws IOException {
		return parse( product, path, RbKey.SETTINGS );
	}

	public static Map<String, SettingsPage> parse( XenonProgramProduct product, String path, String rbKey ) throws IOException {
		return new SettingsPageParser( product ).parse( path, rbKey );
	}

	private Map<String, SettingsPage> parse( String path, String rbKey ) throws IOException {
		InputStream input = product.getClass().getResourceAsStream( path );
		if( input == null ) log.atWarn().log( "Settings page input stream is null: %s", path );
		return parse( input, rbKey );
	}

	private Map<String, SettingsPage> parse( InputStream input, String rbKey ) throws IOException {
		if( input == null ) return null;

		Map<String, SettingsPage> pages = new HashMap<>();
		XMLStreamReader reader;
		try( input ) {
			reader = xmlInputFactory.createXMLStreamReader( input );
			parse( reader, rbKey, pages );
			reader.close();
		} catch( XMLStreamException exception ) {
			throw new IOException( exception );
		}

		return pages;
	}

	private void parse( XMLStreamReader reader, String rbKey, Map<String, SettingsPage> pages ) throws XMLStreamException {
		if( !reader.hasNext() ) return;

		reader.next();
		if( !reader.getLocalName().equals( PAGES ) ) return;

		int order = 0;
		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( PAGES ) ) break;

			if( reader.getEventType() == XMLStreamReader.START_ELEMENT ) {
				String tagName = reader.getLocalName();
				if( PAGE.equals( tagName ) ) {
					SettingsPage page = parsePage( reader, rbKey, null, order++ );
					pages.put( page.getId(), page );
				}
			}
		}
	}

	private SettingsPage parsePage( XMLStreamReader reader, String rbKey, SettingsPage parent, int order ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );
		String id = attributes.get( ID );
		String icon = attributes.getOrDefault( ICON, SETTING_ICON );
		String title = attributes.get( TITLE );
		String panel = attributes.get( PANEL );

		// Special handling of product pages
		if( PRODUCT_PAGE.equals( id ) ) {
			List<String> icons = product.getCard().getIcons();
			id = product.getCard().getProductKey();
			title = product.getCard().getName();
			icon = !icons.isEmpty() ? icons.get( 0 ) : PRODUCT_ICON;
		}

		// Special handling of empty titles
		if( TextUtil.isEmpty( title ) ) title = Rb.textOr( product, rbKey, id, id );

		SettingsPage page = new SettingsPage( parent );
		page.setProduct( product );
		page.setId( id );
		page.setIcon( icon );
		page.setTitle( title );
		page.setRbKey( rbKey );
		page.setPanel( panel );
		page.setOrder( order );

		log.atConfig().log( "Parsing settings page=%s order=%s", page.getTitle(), order );

		int childOrder = 0;
		SettingGroup group = null;
		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( PAGE ) ) break;

			if( reader.getEventType() == XMLStreamReader.START_ELEMENT ) {
				String tagName = reader.getLocalName();
				if( PAGE.equals( tagName ) ) {
					page.addPage( parsePage( reader, rbKey, page, childOrder++ ) );
				} else if( GROUP.equals( tagName ) ) {
					if( group != null ) {
						page.addGroup( group );
						group = null;
					}
					page.addGroup( parseGroup( reader, rbKey, page ) );
				} else if( SETTING.equals( tagName ) ) {
					if( group == null ) group = new SettingGroup( page );
					group.addSetting( parseSetting( reader, rbKey, group ) );
				}
			}
		}

		if( group != null ) page.addGroup( group );

		return page;
	}

	private SettingGroup parseGroup( XMLStreamReader reader, String rbKey, SettingsPage page ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );

		String id = attributes.get( ID );
		String collapsible = attributes.get( SettingData.COLLAPSIBLE );
		String expanded = attributes.get( SettingData.EXPANDED );
		String failDependencyAction = attributes.get( FAIL_DEPENDENCY_ACTION );

		SettingGroup group = new SettingGroup( page );
		group.setId( id );
		group.setFailDependencyAction( failDependencyAction );
		group.setCollapsible( collapsible == null ? null : Boolean.parseBoolean( collapsible ) );
		group.setExpanded( expanded == null ? null : Boolean.parseBoolean( expanded ) );

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( GROUP ) ) break;

			if( reader.getEventType() == XMLStreamReader.START_ELEMENT ) {
				String tagName = reader.getLocalName();
				if( SETTING.equals( tagName ) ) {
					group.addSetting( parseSetting( reader, rbKey, group ) );
				} else if( DEPENDENCY.equals( tagName ) ) {
					group.addDependency( parseDependency( reader ) );
				}
			}
		}

		return group;
	}

	private SettingData parseSetting( XMLStreamReader reader, String rbKey, SettingGroup group ) throws XMLStreamException {
		// Read the attributes.
		Map<String, String> attributes = parseAttributes( reader );

		// The resource bundle key (if not specified the setting key is used)
		String id = attributes.get( ID );
		// The setting key
		String key = attributes.get( KEY );
		String editor = attributes.get( SettingData.EDITOR );
		String disable = attributes.get( SettingData.DISABLE );
		String opaque = attributes.get( SettingData.OPAQUE );
		String rows = attributes.computeIfAbsent( SettingData.ROWS, k -> "10" );
		String provider = attributes.get( SettingData.PROVIDER );
		String failDependencyAction = attributes.get( FAIL_DEPENDENCY_ACTION );

		SettingData setting = new SettingData( group );
		setting.setId( id );
		setting.setKey( key );
		setting.setEditor( editor );
		setting.setDisable( Boolean.parseBoolean( disable ) );
		setting.setOpaque( Boolean.parseBoolean( opaque ) );
		setting.setRows( Integer.parseInt( rows ) );
		setting.setProvider( provider );
		setting.setFailDependencyAction( failDependencyAction );

		while( reader.hasNext() ) {
			reader.next();
			if( reader.isEndElement() && reader.getLocalName().equals( SETTING ) ) break;

			if( reader.getEventType() == XMLStreamReader.START_ELEMENT ) {
				String tagName = reader.getLocalName();
				if( OPTION.equals( tagName ) ) {
					setting.addOption( parseOption( reader, rbKey, setting ) );
				} else if( DEPENDENCY.equals( tagName ) ) {
					setting.addDependency( parseDependency( reader ) );
				}
			}
		}

		return setting;
	}

	private SettingOption parseOption( XMLStreamReader reader, String rbKey, SettingData setting ) throws XMLStreamException {
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
		String text = textBuilder.isEmpty() ? null : textBuilder.toString();

		// Determine the option key
		String key = attributes.get( KEY );
		if( key == null ) key = attributes.get( VALUE );
		if( key == null ) key = text;

		// Determine the option name
		String optionName = text;
		String nameRbKey = toRbKey( setting.getKey() ) + "-" + key;
		if( optionName == null ) optionName = Rb.text( product, rbKey, nameRbKey );

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

			if( reader.getEventType() == XMLStreamReader.START_ELEMENT ) {
				String tagName = reader.getLocalName();
				if( DEPENDENCY.equals( tagName ) ) {
					dependency.addDependency( parseDependency( reader ) );
				}
			}
		}

		return dependency;
	}

	private static String toRbKey( String key ) {
		if( key == null ) return null;
		if( key.startsWith( "/" ) ) key = key.substring( 1 );
		return key.replace( '/', '-' );
	}

	private Map<String, String> parseAttributes( XMLStreamReader reader ) {
		// Read the attributes.
		Map<String, String> attributes = new HashMap<>();
		int count = reader.getAttributeCount();
		for( int index = 0; index < count; index++ ) {
			attributes.put( reader.getAttributeLocalName( index ), reader.getAttributeValue( index ) );
		}
		return attributes;
	}

}
