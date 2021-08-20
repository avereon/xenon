package com.avereon.xenon.tool.guide;

import com.avereon.event.Event;
import com.avereon.event.EventHandler;
import com.avereon.event.EventHub;
import com.avereon.event.EventType;
import com.avereon.zerra.javafx.FxUtil;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import lombok.CustomLog;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@CustomLog
public class GuideContext {

	private final GuidedTool tool;

	private final EventHub eventHub;

	private final ObservableList<Guide> guides;

	private final SimpleObjectProperty<Guide> currentGuide;

	private final BooleanProperty focused;

	private final BooleanProperty active;

	private final BooleanProperty dragAndDropEnabledProperty;

	private final ReadOnlyObjectWrapper<Set<TreeItem<GuideNode>>> expandedItems;

	private final ReadOnlyObjectWrapper<Set<TreeItem<GuideNode>>> selectedItems;

	public GuideContext( GuidedTool tool ) {
		this.tool = tool;
		this.eventHub = new EventHub();
		this.guides = FXCollections.observableArrayList();
		this.currentGuide = new SimpleObjectProperty<>();

		focused = new SimpleBooleanProperty( false );
		active = new SimpleBooleanProperty( false );
		dragAndDropEnabledProperty = new SimpleBooleanProperty( false );
		expandedItems = new ReadOnlyObjectWrapper<>( this, "expandedItems", new HashSet<>() );
		selectedItems = new ReadOnlyObjectWrapper<>( this, "selectedItems", new HashSet<>() );

		currentGuide.addListener( ( p, o, n ) -> {
			if( o != null ) {
				o.focusedProperty().unbind();
				o.setActive( false );
			}
			if( n != null ) {
				dragAndDropEnabledProperty().bind( n.dragAndDropEnabledProperty() );
				n.focusedProperty().bind( focusedProperty() );
				n.setActive( true );
			}
		} );
	}

	public GuidedTool getTool() {
		return tool;
	}

	public ObservableList<Guide> getGuides() {
		return guides;
	}

	public Guide getCurrentGuide() {
		return currentGuide.get();
	}

	public void setCurrentGuide( Guide guide ) {
		this.currentGuide.set( guide );
	}

	public ReadOnlyObjectProperty<Guide> currentGuideProperty() {
		return currentGuide;
	}

	public boolean isFocused() {
		return focused.get();
	}

	public BooleanProperty focusedProperty() {
		return focused;
	}

	public boolean isActive() {
		return active.get();
	}

	public void setActive( boolean active ) {
		this.active.set( active );
	}

	public BooleanProperty activeProperty() {
		return active;
	}

	public boolean isDragAndDropEnabled() {
		return dragAndDropEnabledProperty.get();
	}

	public void setDragAndDropEnabled( boolean enabled ) {
		dragAndDropEnabledProperty.set( enabled );
	}

	public BooleanProperty dragAndDropEnabledProperty() {
		return dragAndDropEnabledProperty;
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final Set<TreeItem<GuideNode>> getExpandedItems() {
		return expandedItems.get();
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final void setExpandedItems( Set<TreeItem<GuideNode>> items ) {
		expandedItems.set( items );
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	// WORKAROUND This method is public because tests need access
	public final void setExpandedIds( Set<String> ids ) {
		setExpandedItems( ids.stream().map( getItemMap()::get ).filter( Objects::nonNull ).collect( Collectors.toSet() ) );
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final ReadOnlyObjectProperty<Set<TreeItem<GuideNode>>> expandedItemsProperty() {
		return expandedItems.getReadOnlyProperty();
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final Set<TreeItem<GuideNode>> getSelectedItems() {
		return selectedItems.get();
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final void setSelectedItems( Set<TreeItem<GuideNode>> items ) {
		selectedItems.set( items );
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	// WORKAROUND This method is public because tests need access
	public final void setSelectedIds( Set<String> ids ) {
		setSelectedItems( ids.stream().map( getItemMap()::get ).filter( Objects::nonNull ).collect( Collectors.toSet() ) );
	}

	/* Only intended to be used by the GuideTool and GuidedTools */
	final ReadOnlyObjectProperty<Set<TreeItem<GuideNode>>> selectedItemsProperty() {
		return selectedItems.getReadOnlyProperty();
	}

	public <T extends Event> EventHub register( EventType<? super T> type, EventHandler<? super T> handler ) {
		return eventHub.register( type, handler );
	}

	public <T extends Event> EventHub unregister( EventType<? super T> type, EventHandler<? super T> handler ) {
		return eventHub.unregister( type, handler );
	}

	public Event dispatch( Event event ) {
		return eventHub.dispatch( event );
	}

	private Map<String, TreeItem<GuideNode>> getItemMap() {
		return FxUtil.flatTree( getCurrentGuide().getRoot() ).stream().collect( Collectors.toMap( item -> item.getValue().getId(), item -> item ) );
	}

}
