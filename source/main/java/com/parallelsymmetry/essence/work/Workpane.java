package com.parallelsymmetry.essence.work;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.layout.Pane;

public class Workpane extends Pane {

	private ObservableSet<Edge> edges;

	private ObservableSet<View> views;

	public Workpane() {
		edges = FXCollections.observableSet();
		views = FXCollections.observableSet();
	}

	private class Edge {

		private boolean vertical;

		private boolean absolute;

		private double position;
	}

	private class View {

		private Edge n, s, e, w;

	}

}
