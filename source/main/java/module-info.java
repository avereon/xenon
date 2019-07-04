import com.xeomar.xenon.Mod;

module com.xeomar.xenon {
	requires java.logging;
	requires java.management;
	requires java.sql;
	requires javafx.controls;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires com.xeomar.zenna;
	requires com.xeomar.zevra;
	requires org.slf4j;
	requires miglayout.javafx;

	exports com.xeomar.xenon;
	exports com.xeomar.xenon.demo;
	exports com.xeomar.xenon.notice;
	exports com.xeomar.xenon.resource;
	exports com.xeomar.xenon.task;
	exports com.xeomar.xenon.task.chain;
	exports com.xeomar.xenon.tool;
	exports com.xeomar.xenon.update;
	exports com.xeomar.xenon.util;
	exports com.xeomar.xenon.workarea;
	exports com.xeomar.xenon.workspace;

	opens com.xeomar.xenon.bundles;
	opens settings;

	// WORKAROUND Dev time problem
	opens com.xeomar.xenon.update to com.fasterxml.jackson.databind;

	uses Mod;
}
