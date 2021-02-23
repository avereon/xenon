package com.avereon.xenon.workspace;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;

public class FpsMonitor extends AbstractMonitor {

	private final FpsCounter counter;

	private final Label label;

	public FpsMonitor() {
		getStyleClass().add( "fps-monitor" );

		counter = new FpsCounter();

		label = new Label();
		label.getStyleClass().add( "fps-monitor-label" );

		getChildren().addAll( label );
		update();
	}

	@Override
	protected void start() {
		super.start();
		counter.start();
	}

	@Override
	protected void update() {
		label.setText( "FPS: " + counter.getFps() );
	}

	@Override
	public void close() {
		counter.stop();
		super.close();
	}

	private static class FpsCounter extends AnimationTimer {

		private static final int BUFFER_SIZE = 60;

		private final int[] rates;

		private long priorTime;

		private int rateIndex;

		public FpsCounter() {
			this.rates = new int[ BUFFER_SIZE ];
		}

		public int getFps() {
			int count = 0;
			int total = 0;
			for( int index = 0; index < BUFFER_SIZE; index++ ) {
				long value = rates[ index ];
				if( value != 0 ) {
					total += value;
					count++;
				}
			}

			return count == 0 ? 0 : total / count;
		}

		@Override
		public void handle( long now ) {
			now = System.nanoTime();

			// Calc rate
			addRate( (int)(1e9 / (now - priorTime)) );

			priorTime = now;
		}

		private void addRate( int rate ) {
			rates[ rateIndex++ ] = rate;
			rateIndex %= BUFFER_SIZE;
		}

	}

}
