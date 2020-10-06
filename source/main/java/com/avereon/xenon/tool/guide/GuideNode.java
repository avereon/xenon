package com.avereon.xenon.tool.guide;

import com.avereon.data.Node;
import com.avereon.data.NodeComparator;
import com.avereon.xenon.Program;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.TreeItem;

import java.util.Comparator;

public class GuideNode extends Node {

	public static final String ID = "id";

	public static final String ICON = "icon";

	public static final String NAME = "name";

	public static final String ORDER = "order";

	private static final String TREE_ITEM = "tree-item";

	private final Program program;

	GuideNode( Program program ) {
		this( program, null, null, null );
	}

	GuideNode( Program program, String id, String name ) {
		this( program, id, name, null );
	}

	public GuideNode( Program program, String id, String name, String icon ) {
		this( program, id, name, icon, -1 );
	}

	public GuideNode( Program program, String id, String name, String icon, int order ) {
		this.program = program;
		definePrimaryKey( ID );
		defineNaturalKey( NAME );
		setId( id );
		setName( name );
		setIcon( icon );
		setOrder( order );
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

	public GuideNode setIcon( String icon ) {
		setValue( ICON, icon );
		if( exists( TREE_ITEM ) ) Fx.run( () -> getTreeItem().setGraphic( program.getIconLibrary().getIcon( icon ) ) );
		return this;
	}

	public String getName() {
		return getValue( NAME );
	}

	public GuideNode setName( String name ) {
		setValue( NAME, name );
		return this;
	}

	public int getOrder() {
		return getValue( ORDER );
	}

	public GuideNode setOrder( int order ) {
		setValue( ORDER, order );
		return this;
	}

	public GuideNode reset() {
		if( exists( TREE_ITEM ) ) Fx.run( () -> getTreeItem().getChildren().clear() );
		return this;
	}

	//	public GuideNode add( GuideNode child ) {
	//		Fx.run( () -> getTreeItem().getChildren().add( child.getTreeItem() ) );
	//		return this;
	//	}

	//	public GuideNode remove( GuideNode child ) {
	//		if( !exists( TREE_ITEM ) ) return this;
	//		Fx.run( () -> getTreeItem().getChildren().remove( child.getTreeItem() ) );
	//		return this;
	//	}

	@Override
	public <T extends Node> Comparator<T> getComparator() {
		return new NodeComparator<>( ORDER, NAME );
	}

	@Override
	public String toString() {
		return getName();
	}

	TreeItem<GuideNode> getTreeItem() {
		TreeItem<GuideNode> value = getValue( TREE_ITEM );
		if( value == null ) value = setValue( TREE_ITEM, new TreeItem<>( this, program.getIconLibrary().getIcon( getIcon() ) ) );
		return value;
	}

}
