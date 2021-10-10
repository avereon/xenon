package com.avereon.xenon.test;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class ObservableWatcher<T> implements ChangeListener<T> {

	private ObservableValue<? extends T> observable;

	private T value;

	private T newValue;

	public ObservableWatcher( ObservableValue<? extends T> observable, T value ) {
		this.observable = observable;
		this.value = value;

		observable.addListener( this );
	}

	@Override
	public synchronized void changed( ObservableValue<? extends T> observable, T oldValue, T newValue ) {
		System.out.println( "New observable value: " + newValue );
		this.newValue = newValue;
		notifyAll();
	}

	public synchronized void waitFor( long timeout ) throws TimeoutException {
		try {
			if( Objects.equals( this.observable.getValue(), this.value ) ) {
				System.out.println( "Observable value already: " + this.value );
				return;
			}

			long stopTime = System.currentTimeMillis() + timeout;
			long remaining = timeout;
			while( remaining > 0 && !Objects.equals( value, newValue ) ) {
				try {
					Thread.sleep( remaining );
				} catch( InterruptedException exception ) {
					// Intentionally ignore exception
				}
				remaining = stopTime - System.currentTimeMillis();
			}
			if( System.currentTimeMillis() >= stopTime ) throw new TimeoutException( "Timeout waiting for value: " + value );
		} finally {
			this.observable.removeListener( this );
		}
	}

}
