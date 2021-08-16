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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class GuideContext {

	private final GuidedTool tool;

	private final EventHub eventHub;

	private final ObservableList<Guide> guides;

	private final SimpleObjectProperty<Guide> currentGuide;

	private final BooleanProperty focused;

	private final BooleanProperty activeProperty;

	private final BooleanProperty dragAndDropEnabledProperty;

	private final ReadOnlyObjectWrapper<Set<TreeItem<GuideNode>>> expandedItems;

	private final ReadOnlyObjectWrapper<Set<TreeItem<GuideNode>>> selectedItems;

	public GuideContext( GuidedTool tool ) {
		this.tool = tool;
		this.eventHub = new EventHub();
		this.guides = FXCollections.observableArrayList();
		this.currentGuide = new SimpleObjectProperty<>();

		focused = new SimpleBooleanProperty( false );
		activeProperty = new SimpleBooleanProperty( false );
		dragAndDropEnabledProperty = new SimpleBooleanProperty( false );
		expandedItems = new ReadOnlyObjectWrapper<>( this, "expandedItems", new HashSet<>() );
		selectedItems = new ReadOnlyObjectWrapper<>( this, "selectedItems", new HashSet<>() );
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
		return activeProperty.get();
	}

	public void setActive( boolean active ) {
		activeProperty.set( active );
	}

	public BooleanProperty activeProperty() {
		return activeProperty;
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
	final void setExpandedIds( Set<String> ids ) {
		TreeItem<GuideNode> root = getCurrentGuide().getRoot();
		for( TreeItem<GuideNode> item : FxUtil.flatTree( root ) ) {
			if( item == root ) continue;
			item.setExpanded( ids.contains( item.getValue().getId() ) );
		}
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
	final void setSelectedIds( Set<String> ids ) {
		Map<String, TreeItem<GuideNode>> itemMap = getItemMap();

		Set<TreeItem<GuideNode>> newItems = new HashSet<>( ids.size() );
		for( String id : ids ) {
			TreeItem<GuideNode> item = itemMap.get( id );
			if( item != null ) newItems.add( item );
		}

		setSelectedItems( newItems );
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
		TreeItem<GuideNode> root = getCurrentGuide().getRoot();
		return FxUtil.flatTree( root ).stream().collect( Collectors.toMap( item -> item.getValue().getId(), item -> item ) );
	}

}
