package com.avereon.xenon.tool.guide;

import com.avereon.data.Node;
import com.avereon.xenon.Program;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.TreeItem;

public class GuideNode extends Node {

	public static final String ID = "id";

	public static final String ICON = "icon";

	public static final String NAME = "name";

	private static final String TREE_ITEM = "tree-item";

	private final Program program;

	public GuideNode( Program program ) {
		this( program, null, null, null );
	}

	public GuideNode( Program program, String id, String name ) {
		this( program, id, name, null );
	}

	public GuideNode( Program program, String id, String name, String icon ) {
		this.program = program;
		definePrimaryKey( ID );
		defineNaturalKey( NAME );
		setId( id );
		setName( name );
		setIcon( icon );
	}

	public String getId() {
		return getValue( ID );
	}

	public GuideNode setId( String id ) {
		setValue( ID, id );
		return this;
	}

	public String getIcon() {
		return getValue( ICON );
	}

	public GuideNode setIcon( String name ) {
		setValue( ICON, name );
		if( exists( TREE_ITEM ) ) Fx.run( () -> getTreeItem().setGraphic( program.getIconLibrary().getIcon( name ) ) );
		return this;
	}

	public String getName() {
		return getValue( NAME );
	}

	public GuideNode setName( String name ) {
		setValue( NAME, name );
		return this;
	}

	public GuideNode reset() {
		if( exists( TREE_ITEM ) ) Fx.run( () -> getTreeItem().getChildren().clear() );
		return this;
	}

	public GuideNode add( GuideNode child ) {
		Fx.run( () -> getTreeItem().getChildren().add( child.getTreeItem() ) );
		return this;
	}

	public GuideNode remove( GuideNode child ) {
		if( !exists( TREE_ITEM ) ) return this;
		Fx.run( () -> getTreeItem().getChildren().remove( child.getTreeItem() ) );
		return this;
	}

	TreeItem<GuideNode> getTreeItem() {
		TreeItem<GuideNode> value = getValue( TREE_ITEM );
		if( value == null ) value = setValue( TREE_ITEM, new TreeItem<>( this, program.getIconLibrary().getIcon( getIcon() ) ) );
		return value;
	}

	public String toString() {
		return getName();
	}

}
