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

	//xenon-evening-field (light)
	//FEF7C9
	//ABC6AC
	//657DA0
	@Test
	void testWriteLightTheme() {
		StringWriter text = new StringWriter();

		Color a = Color.web( "#FEF7C9" );
		Color b = Color.web( "#ABC6AC" );
		Color c = Color.web( "#657DA0" );
		ThemeWriter writer = new ThemeWriter( a, b, c );
		writer.write( "xenon-evening-field", "Xenon Evening Field", text );

		Iterator<String> lines = TextUtil.getLines( text.toString() ).iterator();
		assertThat( lines.next(), is( "/*" ) );
		assertThat( lines.next(), is( " * id=xenon-evening-field" ) );
		assertThat( lines.next(), is( " * name=Xenon Evening Field" ) );
		assertThat( lines.next(), is( " * base=#FEF7C9FF" ) );
		assertThat( lines.next(), is( " * accent=#ABC6ACFF" ) );
		assertThat( lines.next(), is( " * focus=#657DA0FF" ) );
		assertThat( lines.next(), is( " */" ) );
		assertThat( lines.next(), is( "" ) );
		assertThat( lines.next(), is( ".root {" ) );
		assertThat( lines.next(), is( "  -fx-base: #FEF7C9FF;" ) );
		assertThat( lines.next(), is( "  -fx-background: derive(-fx-base, 25%);" ) );
		assertThat( lines.next(), is( "  -fx-control-inner-background: derive(-fx-base, 80%);" ) );
		assertThat( lines.next(), is( "  -fx-control-inner-background-alt: derive(-fx-control-inner-background, -2%);" ) );
		assertThat( lines.next(), is( "  -fx-color: derive(-fx-base, 0%);" ) );
		assertThat( lines.next(), is( "  -fx-accent: #ABC6ACFF;" ) );
		assertThat( lines.next(), is( "  -fx-default-button: derive(-fx-accent, 40%);" ) );
		assertThat( lines.next(), is( "  -fx-focus-color: #657DA0FF;" ) );
		assertThat( lines.next(), is( "  -fx-faint-focus-color: #657DA040;" ) );
		assertThat( lines.next(), is( "  -fx-selection-bar-non-focused: #D0D0D0;" ) );
		assertThat( lines.next(), is( "  -ex-background-text: #CBC6A180;" ) );
		assertThat( lines.next(), is( "  -ex-background-tabs: #CBC6A199;" ) );
		assertThat( lines.next(), is( "  -ex-background-tags: #CBC6A1B2;" ) );
		assertThat( lines.next(), is( "  -ex-background-note: #CBC6A1CC;" ) );
		assertThat( lines.next(), is( "  -ex-workspace-tint-color: #FEF7C966;" ) );
		assertThat( lines.next(), is( "  -ex-workspace-drop-hint: #00000033;" ) );
		assertThat( lines.next(), is( "}" ) );

		assertFalse( lines.hasNext(), "More lines than expected" );
	}

	//xenon-evening-sky (dark)
	//1A2C3A
	//223854
	//68685E
	@Test
	void testWriteDarkTheme() {
		StringWriter text = new StringWriter();

		Color a = Color.web( "#1A2C3A" );
		Color b = Color.web( "#223854" );
		Color c = Color.web( "#68685E" );
		ThemeWriter writer = new ThemeWriter( a, b, c );
		writer.write( "xenon-evening-sky", "Xenon Evening Sky", text );

		Iterator<String> lines = TextUtil.getLines( text.toString() ).iterator();
		assertThat( lines.next(), is( "/*" ) );
		assertThat( lines.next(), is( " * id=xenon-evening-sky" ) );
		assertThat( lines.next(), is( " * name=Xenon Evening Sky" ) );
		assertThat( lines.next(), is( " * base=#1A2C3AFF" ) );
		assertThat( lines.next(), is( " * accent=#223854FF" ) );
		assertThat( lines.next(), is( " * focus=#68685EFF" ) );
		assertThat( lines.next(), is( " */" ) );
		assertThat( lines.next(), is( "" ) );

		assertThat( lines.next(), is( ".root {" ) );
		assertThat( lines.next(), is( "  -fx-base: #1A2C3AFF;" ) );
		assertThat( lines.next(), is( "  -fx-background: derive(-fx-base, -25%);" ) );
		assertThat( lines.next(), is( "  -fx-control-inner-background: derive(-fx-base, -50%);" ) );
		assertThat( lines.next(), is( "  -fx-control-inner-background-alt: derive(-fx-control-inner-background, 2.5%);" ) );
		assertThat( lines.next(), is( "  -fx-color: derive(-fx-base, 10%);" ) );
		assertThat( lines.next(), is( "  -fx-accent: #223854FF;" ) );
		assertThat( lines.next(), is( "  -fx-default-button: derive(-fx-accent, -40%);" ) );
		assertThat( lines.next(), is( "  -fx-focus-color: #68685EFF;" ) );
		assertThat( lines.next(), is( "  -fx-faint-focus-color: #68685E40;" ) );
		assertThat( lines.next(), is( "  -fx-selection-bar-non-focused: #303030;" ) );
		assertThat( lines.next(), is( "  -ex-background-text: #48566180;" ) );
		assertThat( lines.next(), is( "  -ex-background-tabs: #48566199;" ) );
		assertThat( lines.next(), is( "  -ex-background-tags: #485661B2;" ) );
		assertThat( lines.next(), is( "  -ex-background-note: #485661CC;" ) );
		assertThat( lines.next(), is( "  -ex-workspace-tint-color: #1A2C3A66;" ) );
		assertThat( lines.next(), is( "  -ex-workspace-drop-hint: #FFFFFF33;" ) );
		assertThat( lines.next(), is( "}" ) );

		assertFalse( lines.hasNext(), "More lines than expected" );
	}

}
