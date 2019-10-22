package com.avereon.xenon.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

class FontUtilTest {

	@Test
	void testEncode() {
		// Negative checks
		assertThat( Font.font( "SansSerif", 18.0 ), not( is( Font.font( "System", 18.0 ) ) ) );

		// Positive checks
		assertThat( FontUtil.encode( Font.font( "SansSerif", 18.0 ) ), is( "SansSerif|Regular|18.0" ) );
		assertThat( FontUtil.encode( Font.font( "SansSerif", FontWeight.BOLD, 18.0 ) ), is( "SansSerif|Bold|18.0" ) );
		assertThat( FontUtil.encode( Font.font( "SansSerif", FontWeight.BOLD, FontPosture.ITALIC, 18.0 ) ), is( "SansSerif|Bold Italic|18.0" ) );
	}

	@Test
	void testDecode() {
		// Negative checks
		assertThat( FontUtil.decode( "SansSerif|18.0" ), not( is( Font.font( "System", 18.0 ) ) ) );

		// Positive checks
		assertThat( FontUtil.decode( "SansSerif|18.0" ), is( Font.font( "SansSerif", 18.0 ) ) );
		assertThat( FontUtil.decode( "SansSerif|Bold|18.0" ), is( Font.font( "SansSerif", FontWeight.BOLD, 18.0 ) ) );
		assertThat( FontUtil.decode( "SansSerif|Bold Italic|18.0" ), is( Font.font( "SansSerif", FontWeight.BOLD, FontPosture.ITALIC, 18.0 ) ) );
	}

}
