package com.avereon.xenon.ui.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BarFactoryTest {

	@Test
	void testParseTokensWithSimpleMenu() {
		String descriptor = "file[new,open,save,close,exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId(), is( "file" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId(), is( "new" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId(), is( "open" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId(), is( "save" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId(), is( "close" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId(), is( "exit" ) );
		assertThat( tokens.get( 0 ).getChildren().size(), is( 5 ) );
		assertThat( tokens.size(), is( 1 ) );
	}

	@Test
	void testParseTokensWithSimpleMenuAndSeparator() {
		String descriptor = "file[new,open,save,close|exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId(), is( "file" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId(), is( "new" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId(), is( "open" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId(), is( "save" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId(), is( "close" ) );
		assertTrue( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId(), is( "|" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId(), is( "exit" ) );
		assertThat( tokens.get( 0 ).getChildren().size(), is( 6 ) );
		assertThat( tokens.size(), is( 1 ) );
	}

	@Test
	void testParseTokensWithSimpleMenuAndCommaSeparatedSeparator() {
		String descriptor = "file[new,open,save,close,|,exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId(), is( "file" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId(), is( "new" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId(), is( "open" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId(), is( "save" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId(), is( "close" ) );
		assertTrue( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId(), is( "|" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId(), is( "exit" ) );
		assertThat( tokens.get( 0 ).getChildren().size(), is( 6 ) );
		assertThat( tokens.size(), is( 1 ) );
	}

	@Test
	void testParseTokensWithMultipleMenus() {
		String descriptor = "file[new,open,save,close|exit],edit[undo,redo|cut,copy,paste]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId(), is( "file" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId(), is( "new" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId(), is( "open" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId(), is( "save" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId(), is( "close" ) );
		assertTrue( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId(), is( "|" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId(), is( "exit" ) );
		assertThat( tokens.get( 0 ).getChildren().size(), is( 6 ) );
		assertThat( tokens.get( 1 ).getId(), is( "edit" ) );
		assertThat( tokens.get( 1 ).getChildren().get( 0 ).getId(), is( "undo" ) );
		assertThat( tokens.get( 1 ).getChildren().get( 1 ).getId(), is( "redo" ) );
		assertTrue( tokens.get( 1 ).getChildren().get( 2 ).isSeparator() );
		assertThat( tokens.get( 1 ).getChildren().get( 2 ).getId(), is( "|" ) );
		assertThat( tokens.get( 1 ).getChildren().get( 3 ).getId(), is( "cut" ) );
		assertThat( tokens.get( 1 ).getChildren().get( 4 ).getId(), is( "copy" ) );
		assertThat( tokens.get( 1 ).getChildren().get( 5 ).getId(), is( "paste" ) );
		assertThat( tokens.get( 1 ).getChildren().size(), is( 6 ) );
		assertThat( tokens.size(), is( 2 ) );
	}

	@Test
	void testParseTokensWithNestedMenu() {
		String descriptor = "file[new,open,save,close,|,settings[program,project],|,exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId(), is( "file" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId(), is( "new" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId(), is( "open" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId(), is( "save" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId(), is( "close" ) );
		assertTrue( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId(), is( "|" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId(), is( "settings" ) );
		assertTrue( tokens.get( 0 ).getChildren().get( 6 ).isSeparator() );
		assertThat( tokens.get( 0 ).getChildren().get( 6 ).getId(), is( "|" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 7 ).getId(), is( "exit" ) );
		assertThat( tokens.get( 0 ).getChildren().size(), is( 8 ) );
		assertThat( tokens.size(), is( 1 ) );

		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getChildren().get( 0 ).getId(), is( "program" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getChildren().get( 1 ).getId(), is( "project" ) );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getChildren().size(), is( 2 ) );
	}

}
