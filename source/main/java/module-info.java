import com.avereon.xenon.Mod;

module com.avereon.xenon {

	// Compile-time only
	requires static lombok;

	// Both compile-time and run-time
	requires transitive com.avereon.zenna;
	requires transitive com.avereon.zarra;
	requires transitive com.avereon.zevra;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.fxml;
	requires transitive javafx.swing;
	requires transitive javafx.web;
	requires transitive org.fxmisc.undo;
	requires com.avereon.weave;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires java.net.http;
	requires java.logging;
	requires java.management;
	requires java.sql;
	requires jdk.crypto.ec;
	requires org.controlsfx.controls;
	requires reactfx;

	// This is to provide some base classes for testing the program and mods
	//requires org.junit.jupiter;
	//requires org.junit.jupiter.api;
	//requires org.junit.jupiter.engine;
	//requires org.junit.jupiter.params;
	//requires org.junit.platform.commons;
	//requires org.junit.platform.engine;
	requires static org.testfx.junit5;
	//requires static org.assertj.core;

	exports com.avereon.xenon;
	exports com.avereon.xenon.action.common;
	exports com.avereon.xenon.asset;
	exports com.avereon.xenon.asset.type;
	exports com.avereon.xenon.demo;
	exports com.avereon.xenon.index;
	exports com.avereon.xenon.notice;
	exports com.avereon.xenon.product;
	exports com.avereon.xenon.scheme;
	exports com.avereon.xenon.task;
	exports com.avereon.xenon.test;
	exports com.avereon.xenon.throwable;
	exports com.avereon.xenon.tool;
	exports com.avereon.xenon.tool.guide;
	exports com.avereon.xenon.tool.settings;
	exports com.avereon.xenon.undo;
	exports com.avereon.xenon.util;
	exports com.avereon.xenon.ui;
	exports com.avereon.xenon.ui.util;
	exports com.avereon.xenon.workpane;
	exports com.avereon.xenon.workspace;

	opens com.avereon.xenon;
	opens com.avereon.xenon.bundles;
	opens com.avereon.xenon.product;
	opens com.avereon.xenon.settings;
	opens com.avereon.xenon.undo;

	uses Mod;

}
