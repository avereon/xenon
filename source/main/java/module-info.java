module com.xeomar.xenon {

	requires java.logging;
	requires java.management;
	requires java.sql;
	requires javafx.controls;
	requires org.apache.commons.io;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires com.xeomar.xevra;
	requires com.xeomar.zenna;
	requires org.slf4j;
	requires miglayout.javafx;

	exports com.xeomar.xenon;
	exports com.xeomar.xenon.mod;
	exports com.xeomar.xenon.resource;
	exports com.xeomar.xenon.task;
	exports com.xeomar.xenon.tool;
	exports com.xeomar.xenon.update;

	opens bundles;
	opens settings;
	opens com.xeomar.xenon;
	opens com.xeomar.xenon.update;

	uses com.xeomar.xenon.mod.Mod;
}
