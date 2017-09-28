package com.xeomar.xenon.util;

import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FontUtilTest {

	@Test
	public void testEncode() {
		for( String family : Font.getFamilies() ) {
			System.out.println( family );
			//			Font font = Font.font(family, FontWeight.BLACK, FontPosture.ITALIC, 18);
			//			System.out.println( FontUtil.encode( font ));
		}
		Font font = Font.font( "Harlow Solid Italic", FontWeight.BLACK, FontPosture.ITALIC, 18 );
		System.out.println( FontUtil.encode( font ) );
	}

	@Test
	public void testDecode() {
		assertThat( FontUtil.decode( "SanSerif|18.0" ), is( Font.font( "SanSerif", 18.0 ) ) );
		assertThat( FontUtil.decode( "SanSerif Bold|18.0" ), is( Font.font( "SanSerif Bold", 18.0 ) ) );
		assertThat( FontUtil.decode( "SanSerif Bold Italic|18.0" ), is( Font.font( "SanSerif Bold Italic", 18.0 ) ) );
	}

}
