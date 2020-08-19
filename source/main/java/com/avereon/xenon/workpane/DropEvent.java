package com.avereon.xenon.workpane;

import javafx.geometry.Side;
import javafx.scene.input.TransferMode;

public class DropEvent {

	static enum Area {
		TAB,
		HEADER,
		TOOL_AREA
	}

	static final int HEADER = -1;

	static final int TOOL_AREA = -2;

	private TransferMode transferMode;

	private Tool source;

	private WorkpaneView target;

	private Area area;

	private int index;

	private Side side;

	public DropEvent( TransferMode transferMode, Tool source, WorkpaneView target, Area area, int index, Side side ) {
		this.transferMode = transferMode;
		this.source = source;
		this.target = target;
		this.index = index;
		this.side = side;
	}

	public TransferMode getTransferMode() {
		return transferMode;
	}

	public Tool getSource() {
		return source;
	}

	public WorkpaneView getTarget() {
		return target;
	}

	public Area getArea() {
		return area;
	}

	public int getIndex() {
		return index;
	}

	public Side getSide() {
		return side;
	}

}
