package com.avereon.xenon.product;

public class DownloadEvent {

	private int progress;

	private int total;

	public DownloadEvent( int progress, int total ) {
		this.progress = progress;
		this.total = total;
	}

	public int getProgress() {
		return progress;
	}

	public int getTotal() {
		return total;
	}

	public double getPercent() {
		return (double)progress / (double)total;
	}

}
