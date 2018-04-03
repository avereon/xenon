package com.xeomar.xenon.util;

import com.xeomar.xenon.FxPlatformTestCase;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public abstract class FxUtilTest extends FxPlatformTestCase {

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

}
