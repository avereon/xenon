package com.parallelsymmetry.essence.work;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.scene.control.Control;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Workpane extends Pane {

	private ObservableSet<Edge> edges;

	private ObservableSet<View> views;

	public Workpane() {
		edges = FXCollections.observableSet();
		views = FXCollections.observableSet();
	}

	@Override
	protected void layoutChildren() {
		getLayoutBounds();
		//super.layoutChildren();
		for( Edge edge : edges ) {

		}

	}

	private class Edge extends Control {

		private boolean vertical;

		private boolean absolute;

		private double position;

		public Edge() {
			setBackground( new Background( new BackgroundFill( Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY ) ) );
		}

	}

	private class View {

		private Edge n, s, e, w;

	}

}
