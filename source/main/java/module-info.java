import java.util.spi.ResourceBundleProvider;

module com.xeomar.xenon {

	requires java.logging;
	requires java.management;
	requires java.sql; // Wish this weren't required
	requires javafx.controls;
	requires javafx.swing;
	requires commons.io;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.xeomar.annex;
	requires com.xeomar.razor;
	requires jackson.annotations;
	requires org.slf4j;

	exports com.xeomar.xenon;

	opens bundles;
	opens settings;
	opens com.xeomar.xenon;
	opens com.xeomar.xenon.update;

	//provides ResourceBundleProvider with com.xeomar.xenon.ProgramResourceBundleProvider;

}
