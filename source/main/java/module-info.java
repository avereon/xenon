import com.avereon.xenon.Mod;

module com.avereon.xenon {
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires com.avereon.rossa;
	requires com.avereon.zenna;
	requires java.logging;
	requires java.management;
	requires java.sql;
	requires jdk.crypto.ec;
	requires miglayout.javafx;
	requires org.controlsfx.controls;
	requires org.slf4j;
	requires org.slf4j.jul;
	requires transitive com.avereon.venza;
	requires transitive com.avereon.zevra;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.fxml;
	requires transitive javafx.swing;
	requires transitive javafx.web;

	exports com.avereon.xenon;
	exports com.avereon.xenon.asset;
	exports com.avereon.xenon.demo;
	exports com.avereon.xenon.notice;
	exports com.avereon.xenon.node;
	exports com.avereon.xenon.product;
	exports com.avereon.xenon.scheme;
	exports com.avereon.xenon.task;
	exports com.avereon.xenon.task.chain;
	exports com.avereon.xenon.tool;
	exports com.avereon.xenon.tool.guide;
	exports com.avereon.xenon.tool.settings;
	exports com.avereon.xenon.transaction;
	exports com.avereon.xenon.util;
	exports com.avereon.xenon.workpane;
	exports com.avereon.xenon.workspace;

	opens com.avereon.xenon.bundles;
	opens com.avereon.xenon.product;
	opens com.avereon.xenon.settings;

	uses Mod;
}
