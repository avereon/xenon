package com.avereon.xenon.ui.util;

import com.avereon.xenon.ui.util.BarFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BarFactoryTest {

	@Test
	void testParseTokensWithSimpleMenu() {
		String descriptor = "file[new,open,save,close,exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId() ).isEqualTo( "file" );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId() ).isEqualTo( "new" );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId() ).isEqualTo( "open" );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId() ).isEqualTo( "save" );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId() ).isEqualTo( "close" );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId() ).isEqualTo( "exit" );
		assertThat( tokens.get( 0 ).getChildren().size() ).isEqualTo( 5 );
		assertThat( tokens.size() ).isEqualTo( 1 );
	}

	@Test
	void testParseTokensWithSimpleMenuAndSeparator() {
		String descriptor = "file[new,open,save,close|exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId() ).isEqualTo( "file" );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId() ).isEqualTo( "new" );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId() ).isEqualTo( "open" );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId() ).isEqualTo( "save" );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId() ).isEqualTo( "close" );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() ).isTrue();
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId() ).isEqualTo( "|" );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId() ).isEqualTo( "exit" );
		assertThat( tokens.get( 0 ).getChildren().size() ).isEqualTo( 6 );
		assertThat( tokens.size() ).isEqualTo( 1 );
	}

	@Test
	void testParseTokensWithSimpleMenuAndCommaSeparatedSeparator() {
		String descriptor = "file[new,open,save,close,|,exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId() ).isEqualTo( "file" );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId() ).isEqualTo( "new" );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId() ).isEqualTo( "open" );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId() ).isEqualTo( "save" );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId() ).isEqualTo( "close" );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() ).isTrue();
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId() ).isEqualTo( "|" );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId() ).isEqualTo( "exit" );
		assertThat( tokens.get( 0 ).getChildren().size() ).isEqualTo( 6 );
		assertThat( tokens.size() ).isEqualTo( 1 );
	}

	@Test
	void testParseTokensWithMultipleMenus() {
		String descriptor = "file[new,open,save,close|exit],edit[undo,redo|cut,copy,paste]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId() ).isEqualTo( "file" );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId() ).isEqualTo( "new" );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId() ).isEqualTo( "open" );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId() ).isEqualTo( "save" );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId() ).isEqualTo( "close" );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() ).isTrue();
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId() ).isEqualTo( "|" );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId() ).isEqualTo( "exit" );
		assertThat( tokens.get( 0 ).getChildren().size() ).isEqualTo( 6 );
		assertThat( tokens.get( 1 ).getId() ).isEqualTo( "edit" );
		assertThat( tokens.get( 1 ).getChildren().get( 0 ).getId() ).isEqualTo( "undo" );
		assertThat( tokens.get( 1 ).getChildren().get( 1 ).getId() ).isEqualTo( "redo" );
		assertThat( tokens.get( 1 ).getChildren().get( 2 ).isSeparator() ).isTrue();
		assertThat( tokens.get( 1 ).getChildren().get( 2 ).getId() ).isEqualTo( "|" );
		assertThat( tokens.get( 1 ).getChildren().get( 3 ).getId() ).isEqualTo( "cut" );
		assertThat( tokens.get( 1 ).getChildren().get( 4 ).getId() ).isEqualTo( "copy" );
		assertThat( tokens.get( 1 ).getChildren().get( 5 ).getId() ).isEqualTo( "paste" );
		assertThat( tokens.get( 1 ).getChildren().size() ).isEqualTo( 6 );
		assertThat( tokens.size() ).isEqualTo( 2 );
	}

	@Test
	void testParseTokensWithNestedMenu() {
		String descriptor = "file[new,open,save,close,|,settings[program,project],|,exit]";
		List<BarFactory.Token> tokens = BarFactory.parseDescriptor( descriptor );
		assertThat( tokens.get( 0 ).getId() ).isEqualTo( "file" );
		assertThat( tokens.get( 0 ).getChildren().get( 0 ).getId() ).isEqualTo( "new" );
		assertThat( tokens.get( 0 ).getChildren().get( 1 ).getId() ).isEqualTo( "open" );
		assertThat( tokens.get( 0 ).getChildren().get( 2 ).getId() ).isEqualTo( "save" );
		assertThat( tokens.get( 0 ).getChildren().get( 3 ).getId() ).isEqualTo( "close" );
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).isSeparator() ).isTrue();
		assertThat( tokens.get( 0 ).getChildren().get( 4 ).getId() ).isEqualTo( "|" );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getId() ).isEqualTo( "settings" );
		assertThat( tokens.get( 0 ).getChildren().get( 6 ).isSeparator() ).isTrue();
		assertThat( tokens.get( 0 ).getChildren().get( 6 ).getId() ).isEqualTo( "|" );
		assertThat( tokens.get( 0 ).getChildren().get( 7 ).getId() ).isEqualTo( "exit" );
		assertThat( tokens.get( 0 ).getChildren().size() ).isEqualTo( 8 );
		assertThat( tokens.size() ).isEqualTo( 1 );

		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getChildren().get( 0 ).getId() ).isEqualTo( "program" );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getChildren().get( 1 ).getId() ).isEqualTo( "project" );
		assertThat( tokens.get( 0 ).getChildren().get( 5 ).getChildren().size() ).isEqualTo( 2 );
	}

}
