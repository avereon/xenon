package com.xeomar.xenon.tool;

import com.xeomar.xenon.node.Node;
import com.xeomar.xenon.tool.settings.SettingsPage;

public class GuideNode extends Node {

	public static final String ID = "id";

	public static final String ICON = "icon";

	public static final String NAME = "name";

	public static final String PAGE = "page";

	public GuideNode() {
		definePrimaryKey( ID );
		defineBusinessKey( NAME );
	}

	public String getId() {
		return getValue( ID );
	}

	public void setId( String id ) {
		setValue( ID, id );
	}

	public String getIcon() {
		return getValue( ICON );
	}

	public void setIcon( String name ) {
		setValue( ICON, name );
	}

	public String getName() {
		return getValue( NAME );
	}

	public void setName( String name ) {
		setValue( NAME, name );
	}

	public SettingsPage getPage() {
		return getValue( PAGE );
	}

	public void setPage(SettingsPage page ) {
		setValue( PAGE, page );
	}

	public String toString() {
		return getName();
	}

}
