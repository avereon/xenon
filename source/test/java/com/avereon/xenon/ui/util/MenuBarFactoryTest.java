package com.avereon.xenon.ui.util;

import com.avereon.xenon.BaseFullXenonTestCase;
import javafx.scene.control.ContextMenu;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuBarFactoryTest extends BaseFullXenonTestCase {

	@Test
	void testContextMenu() {
		ContextMenu bar = MenuBarFactory.createContextMenu( getProgram(), "file[new,open,save,close],edit[undo,redo],help[about]|exit", false );
		assertThat( bar.getItems().get( 0 ).getId() ).isEqualTo( "menu-file" );
		assertThat( bar.getItems().get( 1 ).getId() ).isEqualTo( "menu-edit" );
		assertThat( bar.getItems().get( 2 ).getId() ).isEqualTo( "menu-help" );
		assertThat( bar.getItems().get( 3 ).getId() ).isEqualTo( "separator" );
		assertThat( bar.getItems().get( 4 ).getId() ).isEqualTo( "menuitem-exit" );
		assertThat( bar.getItems().size() ).isEqualTo( 5 );
	}

	@Test
	void testCreateMenus() {
		//		List<Menu> menus = MenuFactory.createMenus( getProgram(), "file[new,open,save,close],edit[undo,redo],help[about]", false );
		//		assertThat( menus.get( 0 ).getId() ).isEqualTo( "menu-file" );
		//		assertThat( menus.get( 1 ).getId() ).isEqualTo( "menu-edit" );
		//		assertThat( menus.get( 2 ).getId() ).isEqualTo( "menu-help" );
		//		assertThat( menus.size() ).isEqualTo( 3 );
	}

	@Test
	void testCreateMenu() {
		//		Menu menu = MenuFactory.createMenu( getProgram(), "file[new open save close]", false );
		//		assertThat( menu.getItems().size() ).isEqualTo( 4 );
	}

	@Test
	void testCreateMenuWithSubmenu() {
		//		Menu menu = MenuFactory.createMenu( getProgram(), "file[new open save[save save-as save-all] close]", false );
		//		assertThat( menu.getItems().size() ).isEqualTo( 4 );
		//		assertThat( ((Menu)menu.getItems().get( 2 )).getItems().size() ).isEqualTo( 3 );
	}

}
