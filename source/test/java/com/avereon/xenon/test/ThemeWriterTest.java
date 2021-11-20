package com.avereon.xenon.test;

import com.avereon.util.TextUtil;
import com.avereon.xenon.ThemeWriter;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

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
		assertThat( lines.next() ).isEqualTo( "/*" );
		assertThat( lines.next() ).isEqualTo( " * id=xenon-evening-field" );
		assertThat( lines.next() ).isEqualTo( " * name=Xenon Evening Field" );
		assertThat( lines.next() ).isEqualTo( " * base=#FEF7C9FF" );
		assertThat( lines.next() ).isEqualTo( " * accent=#ABC6ACFF" );
		assertThat( lines.next() ).isEqualTo( " * focus=#657DA0FF" );
		assertThat( lines.next() ).isEqualTo( " */" );
		assertThat( lines.next() ).isEqualTo( "" );
		assertThat( lines.next() ).isEqualTo( ".root {" );
		assertThat( lines.next() ).isEqualTo( "  -fx-base: #FEF7C9FF;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-background: derive(-fx-base, 25%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-control-inner-background: derive(-fx-base, 80%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-control-inner-background-alt: derive(-fx-control-inner-background, -2%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-color: derive(-fx-base, 0%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-accent: #ABC6ACFF;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-default-button: derive(-fx-accent, 40%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-focus-color: #657DA0FF;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-faint-focus-color: #657DA040;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-selection-bar-non-focused: #D0D0D0;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-text: #CBC6A166;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-half: #CBC6A180;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-tabs: #CBC6A199;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-tags: #CBC6A1B2;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-note: #CBC6A1CC;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-workspace-tint-color: #FEF7C9E5;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-workspace-drop-hint: #00000033;" );
		assertThat( lines.next() ).isEqualTo( "}" );

		assertThat( lines.hasNext() ).withFailMessage( "More lines than expected" ).isFalse();
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
		assertThat( lines.next() ).isEqualTo( "/*" );
		assertThat( lines.next() ).isEqualTo( " * id=xenon-evening-sky" );
		assertThat( lines.next() ).isEqualTo( " * name=Xenon Evening Sky" );
		assertThat( lines.next() ).isEqualTo( " * base=#1A2C3AFF" );
		assertThat( lines.next() ).isEqualTo( " * accent=#223854FF" );
		assertThat( lines.next() ).isEqualTo( " * focus=#68685EFF" );
		assertThat( lines.next() ).isEqualTo( " */" );
		assertThat( lines.next() ).isEqualTo( "" );

		assertThat( lines.next() ).isEqualTo( ".root {" );
		assertThat( lines.next() ).isEqualTo( "  -fx-base: #1A2C3AFF;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-background: derive(-fx-base, -25%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-control-inner-background: derive(-fx-base, -50%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-control-inner-background-alt: derive(-fx-control-inner-background, 2.5%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-color: derive(-fx-base, 10%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-accent: #223854FF;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-default-button: derive(-fx-accent, -40%);" );
		assertThat( lines.next() ).isEqualTo( "  -fx-focus-color: #68685EFF;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-faint-focus-color: #68685E40;" );
		assertThat( lines.next() ).isEqualTo( "  -fx-selection-bar-non-focused: #303030;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-text: #48566166;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-half: #48566180;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-tabs: #48566199;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-tags: #485661B2;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-background-note: #485661CC;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-workspace-tint-color: #1A2C3AE5;" );
		assertThat( lines.next() ).isEqualTo( "  -ex-workspace-drop-hint: #FFFFFF33;" );
		assertThat( lines.next() ).isEqualTo( "}" );

		assertThat( lines.hasNext() ).withFailMessage( "More lines than expected" ).isFalse();
	}

}
