package com.xeomar.xenon.util;

import com.xeomar.xenon.FxPlatformTestCase;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class FxUtilTest extends FxPlatformTestCase {

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
		TreeItem<String> root = new TreeItem<>( "root" );
		TreeItem<String> a = new TreeItem<>( "a" );
		TreeItem<String> a1 = new TreeItem<>( "a1" );
		TreeItem<String> a2 = new TreeItem<>( "a2" );
		TreeItem<String> b = new TreeItem<>( "b" );
		TreeItem<String> b3 = new TreeItem<>( "b3" );
		TreeItem<String> b4 = new TreeItem<>( "b4" );
		TreeItem<String> b5 = new TreeItem<>( "b5" );
		TreeItem<String> b6 = new TreeItem<>( "b6" );
		TreeItem<String> c = new TreeItem<>( "c" );
		TreeItem<String> c7 = new TreeItem<>( "c7" );

		a.getChildren().addAll( a1, a2 );
		b.getChildren().addAll( b3, b4, b5, b6 );
		c.getChildren().addAll( c7 );
		root.getChildren().addAll( a, b, c );

		assertThat( FxUtil.flatTree( root ), contains( root, a, a1, a2, b, b3, b4, b5, b6, c, c7 ) );
	}

}
