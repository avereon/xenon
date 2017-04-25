package com.parallelsymmetry.essence;

import com.parallelsymmetry.essence.product.ProductMetadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith( JavaFxTestRunner.class )
public class ProgramTest {

	Program program = new Program();

	@Before
	public void setup() throws Exception {
		program.init();
	}

	@Test
	public void testProgramMetadata() throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		Document pom = builder.parse( new File( "pom.xml" ) );

		ProductMetadata metadata = program.getMetadata();
		assertNotNull( metadata );
		assertThat( metadata.getGroup(), is( "com.parallelsymmetry" ) );
		assertThat( metadata.getGroup(), is( "essence" ) );
	}

}
