import com.avereon.xenon.Mod;

module com.avereon.xenon {
	requires com.avereon.weave;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	//requires commons.vfs2;
	requires java.net.http;
	requires java.logging;
	requires java.management;
	requires java.sql;
	requires jdk.crypto.ec;
	requires miglayout.javafx;
	requires org.controlsfx.controls;
	requires transitive com.avereon.zenna;
	requires transitive com.avereon.zerra;
	requires transitive com.avereon.zevra;
	requires transitive javafx.controls;
	requires transitive javafx.graphics;
	requires transitive javafx.fxml;
	requires transitive javafx.swing;
	requires transitive javafx.web;

	exports com.avereon.xenon;
	exports com.avereon.xenon.action.common;
	exports com.avereon.xenon.asset;
	exports com.avereon.xenon.asset.type;
	exports com.avereon.xenon.demo;
	exports com.avereon.xenon.notice;
	exports com.avereon.xenon.product;
	exports com.avereon.xenon.scheme;
	exports com.avereon.xenon.task;
	exports com.avereon.xenon.task.chain;
	exports com.avereon.xenon.throwable;
	exports com.avereon.xenon.tool;
	exports com.avereon.xenon.tool.guide;
	exports com.avereon.xenon.tool.settings;
	exports com.avereon.xenon.util;
	exports com.avereon.xenon.workpane;
	exports com.avereon.xenon.workspace;

	opens com.avereon.xenon.bundles;
	opens com.avereon.xenon.product;
	opens com.avereon.xenon.settings;

	uses Mod;
}
