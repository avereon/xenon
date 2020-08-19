package com.avereon.xenon.workpane;

import javafx.event.EventType;
import javafx.geometry.Side;
import javafx.scene.input.TransferMode;

public class DropEvent extends WorkpaneEvent {

	public static final EventType<ToolEvent> DROP = new EventType<>( WorkpaneEvent.ANY, "DROP" );

	public static final EventType<ToolEvent> ANY = DROP;

	public enum Area {
		TAB,
		HEADER,
		TOOL_AREA
	}

	private TransferMode transferMode;

	private Tool source;

	private WorkpaneView target;

	private Area area;

	private int index;

	private Side side;

	private String url;

	public DropEvent(
		Object eventSource,
		EventType<? extends WorkpaneEvent> eventType,
		Workpane workpane,
		TransferMode transferMode,
		Tool source,
		WorkpaneView target,
		Area area,
		int index,
		Side side,
		String url
	) {
		super( eventSource, eventType, workpane );
		this.transferMode = transferMode;
		this.source = source;
		this.target = target;
		this.area = area;
		this.index = index;
		this.side = side;
		this.url = url;
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

	public String getUrl() {
		return url;
	}

}
