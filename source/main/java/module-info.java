import com.avereon.xenon.Mod;

module com.avereon.xenon {
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires com.avereon.zenna;
	requires com.avereon.zevra;
	requires java.logging;
	requires java.management;
	requires java.sql;
	requires javafx.controls;
	requires jdk.crypto.ec;
	requires miglayout.javafx;
	requires org.controlsfx.controls;
	requires org.slf4j;
	requires org.slf4j.jul;

	exports com.avereon.xenon;
	exports com.avereon.xenon.demo;
	exports com.avereon.xenon.notice;
	exports com.avereon.xenon.resource;
	exports com.avereon.xenon.task;
	exports com.avereon.xenon.task.chain;
	exports com.avereon.xenon.tool;
	exports com.avereon.xenon.product;
	exports com.avereon.xenon.util;
	exports com.avereon.xenon.workarea;
	exports com.avereon.xenon.workspace;

	opens com.avereon.xenon.bundles;
	opens com.avereon.xenon.product;
	opens settings;

	uses Mod;
}
