package com.avereon.xenon.test.ui.util;

import com.avereon.xenon.ui.util.MenuBarFactory;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuBarFactoryTest extends BaseUiFactoryTest {

	@Test
	void testCreateMenuBar() {
		MenuBar bar = MenuBarFactory.createMenuBar( getProgram(), "file[new,open,save,close],edit[undo,redo],help[about]" );
		assertThat( bar.getMenus().get( 0 ).getItems().size() ).isEqualTo( 4 );
		assertThat( bar.getMenus().get( 1 ).getItems().size() ).isEqualTo( 2 );
		assertThat( bar.getMenus().get( 2 ).getItems().size() ).isEqualTo( 1 );
		assertThat( bar.getMenus().size() ).isEqualTo( 3 );
	}

	@Test
	void testCreateMenu() {
		Menu menu = MenuBarFactory.createMenu( getProgram(), "file[new open save close]", false );
		assertThat( menu.getItems().size() ).isEqualTo( 4 );
	}

	@Test
	void testCreateMenuWithSubmenu() {
		Menu menu = MenuBarFactory.createMenu( getProgram(), "file[new open save[save save-as save-all] close]", false );
		assertThat( menu.getItems().size() ).isEqualTo( 4 );
		assertThat( ((Menu)menu.getItems().get( 2 )).getItems().size() ).isEqualTo( 3 );
	}

}
