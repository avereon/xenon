package com.avereon.xenon;

import com.avereon.util.TextUtil;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ThemeWriterTest {

	@Test
	void testWrite() {
		StringWriter text = new StringWriter();

		//FEF7C9
		//ABC6AC
		//657DA0
		//345879
		//1C1E27

		Color a = Color.web( "#FEF7C9" );
		ThemeWriter writer = new ThemeWriter( a );
		writer.write( "test", "Test Theme", text );

		Iterator<String> lines = TextUtil.getLines( text.toString() ).iterator();
		assertThat( lines.next(), is( "/*" ) );
		assertThat( lines.next(), is( " * id=test" ) );
		assertThat( lines.next(), is( " * name=Test Theme" ) );
		assertThat( lines.next(), is( " */" ) );
		assertThat( lines.next(), is( "" ) );
		assertThat( lines.next(), is( ".root {" ) );
		assertThat( lines.next(), is( "  -fx-base: #FEF7C9FF;" ) );
		assertThat( lines.next(), is( "  -fx-color: -fx-base;" ) );
		assertThat( lines.next(), is( "  -fx-background: derive(-fx-base, 25%);" ) );
		assertThat( lines.next(), is( "}" ) );
		assertFalse( lines.hasNext(), "More lines than expected" );

	}

}
