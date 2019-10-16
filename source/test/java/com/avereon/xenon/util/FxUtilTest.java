package com.avereon.xenon.util;

import com.avereon.xenon.FxPlatformTestCase;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class FxUtilTest extends FxPlatformTestCase {

	private TreeItem<String> root = new TreeItem<>( "root" );
	private TreeItem<String> a = new TreeItem<>( "a" );
	private TreeItem<String> a1 = new TreeItem<>( "a1" );
	private TreeItem<String> a2 = new TreeItem<>( "a2" );
	private TreeItem<String> b = new TreeItem<>( "b" );
	private TreeItem<String> b3 = new TreeItem<>( "b3" );
	private TreeItem<String> b4 = new TreeItem<>( "b4" );
	private TreeItem<String> b5 = new TreeItem<>( "b5" );
	private TreeItem<String> b6 = new TreeItem<>( "b6" );
	private TreeItem<String> c = new TreeItem<>( "c" );
	private TreeItem<String> c7 = new TreeItem<>( "c7" );

	@BeforeEach
	@SuppressWarnings( "unchecked" )
	public void setup() {
		a.getChildren().addAll( a1, a2 );
		b.getChildren().addAll( b3, b4, b5, b6 );
		c.getChildren().addAll( c7 );
		root.getChildren().addAll( a, b, c );
	}

	@Test
	public void testIsChildOfFalse() {
		Pane parent = new Pane();
		Node child = new Label();
		assertThat( FxUtil.isChildOf( child, parent ), is( false ) );
	}

	@Test
	public void testIsChildOfWithParent() {
		Pane parent = new Pane();
		Node child = new Label();

		assertThat( FxUtil.isChildOf( child, parent ), is( false ) );

		parent.getChildren().add( child );

		assertThat( FxUtil.isChildOf( child, parent ), is( true ) );
	}

	@Test
	public void testIsChildOfWithGrandParent() {
		Pane grandParent = new Pane();
		Pane parent = new Pane();
		Node child = new Label();

		assertThat( FxUtil.isChildOf( child, grandParent ), is( false ) );

		grandParent.getChildren().add( parent );
		parent.getChildren().add( child );

		assertThat( FxUtil.isChildOf( child, grandParent ), is( true ) );
	}

	@Test
	public void testFlatTree() {
		assertThat( FxUtil.flatTree( root ), contains( a, a1, a2, b, b3, b4, b5, b6, c, c7 ) );
	}

	@Test
	public void testFlatTreeWithRoot() {
		assertThat( FxUtil.flatTree( root, true ), contains( root, a, a1, a2, b, b3, b4, b5, b6, c, c7 ) );
	}

}
