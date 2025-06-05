package com.avereon.xenon;

import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.Random;

public class GeomPerfCheck extends Application {

	private static final double WIDTH = 1920;

	private static final double HEIGHT = 1080;

	@Override
	public void start( Stage stage ) throws Exception {
		Pane root = new Pane();
		root.setBackground( Background.EMPTY );

		Layer layer1 = new Layer();
		Layer layer2 = new Layer();
		root.getChildren().add( layer1 );
		root.getChildren().add( layer2 );

		MouseDragBehavior.add( layer2 );

		Scene scene = new Scene( root, WIDTH, HEIGHT, Color.web( "#222222" ) );

		stage.setScene( scene );
		stage.centerOnScreen();
		stage.setMaximized( true );
		stage.show();
	}

	private static Line createLine( double x1, double y1, double x2, double y2, Color color, String unit ) {
		return createLine( x1, y1, x2, y2, color, 1, unit );
	}

	private static Line createLine( double x1, double y1, double x2, double y2, Color color, double size, String unit ) {
		Line line = new Line( x1, y1, x2, y2 );
		line.styleProperty().set( "-fx-stroke-width: " + size + unit + ";" );
		line.setFill( Color.TRANSPARENT );
		line.setStroke( color );
		return line;
	}

	private static Rectangle createVBounds( Node node ) {
		Bounds vBounds = node.getBoundsInParent();
		Rectangle rect = new Rectangle( vBounds.getMinX(), vBounds.getMinY(), vBounds.getWidth(), vBounds.getHeight() );
		rect.setFill( Color.TRANSPARENT );
		rect.setStroke( Color.YELLOW );
		rect.setStrokeWidth( 1 );
		return rect;
	}

	private static class Layer extends Pane {

		public Layer() {
			setBackground( Background.EMPTY );
			generateGeometry();
		}

		private void generateGeometry() {
			// ~50000lines / 250MB
			int n = 25000;
			double w = 0.05;
			Random random = new Random();
			for( int index = 0; index < n; index++ ) {
				double x1 = random.nextDouble() * WIDTH;
				double y1 = random.nextDouble() * HEIGHT;
				double x2 = random.nextDouble() * WIDTH;
				double y2 = random.nextDouble() * HEIGHT;
				double z = random.nextDouble() * w;
				if( z < 0.001 ) z = 0.001;

				Color c = Color.rgb( random.nextInt( 255 ), random.nextInt( 255 ), random.nextInt( 255 ) );

				Line line = createLine( x1, y1, x2, y2, c, z, "mm" );
				getChildren().add( line );
			}
		}

	}

	private static class MouseDragBehavior {

		private Point3D origin;

		private Point3D anchor;

		private MouseDragBehavior( Node node ) {
			node.setOnMousePressed( e -> {
				origin = new Point3D( node.getLayoutX(), node.getLayoutY(), 0 );
				anchor = new Point3D( e.getSceneX(), e.getSceneY(), 0 );
			});

			node.setOnMouseDragged( e -> {
				Point3D offset = new Point3D( e.getSceneX() - anchor.getX(), e.getSceneY() - anchor.getY(), 0 );
				node.setLayoutX( origin.getX() + offset.getX() );
				node.setLayoutY( origin.getY() + offset.getY() );
			});
		}

		public static void add( Node node ) {
			new MouseDragBehavior( node );
		}

	}

}
