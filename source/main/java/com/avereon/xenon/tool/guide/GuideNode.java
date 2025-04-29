package com.avereon.xenon.tool.guide;

import com.avereon.data.IdNode;
import com.avereon.data.Node;
import com.avereon.xenon.NodeOrderNameComparator;
import com.avereon.xenon.Xenon;
import com.avereon.zerra.javafx.Fx;
import javafx.scene.control.TreeItem;

import java.util.Comparator;

public class GuideNode extends IdNode {

	public static final String ICON = "icon";

	public static final String NAME = "name";

	public static final String ORDER = "order";

	private static final String TREE_ITEM = "tree-item";

	private final Xenon program;

	public GuideNode( Xenon program ) {
		this( program, null, null, null );
	}

	public GuideNode( Xenon program, String id, String name ) {
		this( program, id, name, null );
	}

	public GuideNode( Xenon program, String id, String name, String icon ) {
		this( program, id, name, icon, -1 );
	}

	public GuideNode( Xenon program, String id, String name, String icon, int order ) {
		this.program = program;
		defineNaturalKey( NAME );
		if( id != null ) setId( id );
		setName( name );
		setIcon( icon );
		setOrder( order );
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
		if( exists( TREE_ITEM ) ) {
			Fx.run( () -> {
				// This seems to be the simplest way to update the name on the tree item
				getTreeItem().setValue( null );
				getTreeItem().setValue( this );
			} );
		}
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
	public <T extends Node> Comparator<T> getNaturalComparator() {
		return new NodeOrderNameComparator<>();
	}

	@Override
	public String toString() {
		return getName();
	}

	public TreeItem<GuideNode> getTreeItem() {
		return computeIfAbsent( TREE_ITEM, ( k ) -> new TreeItem<>( this, program.getIconLibrary().getIcon( getIcon() ) ) );
	}

}
