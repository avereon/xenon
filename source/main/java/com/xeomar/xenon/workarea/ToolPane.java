package com.xeomar.xenon.workarea;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Side;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;

import java.util.*;

@DefaultProperty( "tabs" )
public class ToolPane extends Control {

	private ObservableList<ToolTab> tabs = FXCollections.observableArrayList();

	private ObjectProperty<SingleSelectionModel<ToolTab>> selectionModel = new SimpleObjectProperty<>( this, "selectionModel" );

	private static final PseudoClass TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "top" );

	private static final PseudoClass BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "bottom" );

	private static final PseudoClass LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "left" );

	private static final PseudoClass RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "right" );

	public ToolPane() {
		this( (ToolTab[])null );
	}

	public ToolPane( ToolTab... tabs ) {
		getStyleClass().setAll( "tab-pane" );
		setAccessibleRole( AccessibleRole.TAB_PANE );
		setSelectionModel( new ToolPaneSelectionModel( this ) );

		// Add listeners to handle changes to the tabs
		this.tabs.addListener( (ListChangeListener<ToolTab>)listener -> {
			while( listener.next() ) {
				for( ToolTab tab : listener.getRemoved() ) {
					if( tab != null && !getTabs().contains( tab ) ) tab.setToolPane( null );
				}

				for( ToolTab tab : listener.getAddedSubList() ) {
					if( tab != null ) tab.setToolPane( ToolPane.this );
				}
			}
		} );

		// Add all the tabs
		if( tabs != null ) getTabs().addAll( tabs );
	}

	/**
	 * <p>The tool tabs to display in this ToolPane. Changing this ObservableList will
	 * immediately result in the ToolPane updating to display the new contents
	 * of this ObservableList.</p>
	 *
	 * <p>If the tabs ObservableList changes, the selected tab will remain the previously
	 * selected tab, if it remains within this ObservableList. If the previously
	 * selected tab is no longer in the tabs ObservableList, the selected tab will
	 * become the first tab in the ObservableList.</p>
	 *
	 * @return the list of tabs
	 */
	public final ObservableList<ToolTab> getTabs() {
		return tabs;
	}

	/**
	 * <p>Sets the model used for tool tab selection. By changing the model you can
	 * alter how the tabs are selected and which tabs are first or last.</p>
	 *
	 * @param value the selection model
	 */
	public final void setSelectionModel( SingleSelectionModel<ToolTab> value ) {
		selectionModel.set( value );
	}

	/**
	 * <p>Gets the model used for tool tab selection.</p>
	 *
	 * @return the model used for tool tab selection
	 */
	public final SingleSelectionModel<ToolTab> getSelectionModel() {
		return selectionModel.get();
	}

	/**
	 * The selection model used for selecting tool tabs.
	 *
	 * @return selection model property
	 */
	public final ObjectProperty<SingleSelectionModel<ToolTab>> selectionModelProperty() {
		return selectionModel;
	}

	private ObjectProperty<Side> side;

	/**
	 * <p>The position to place the tabs in this ToolPane. Whenever this changes
	 * the ToolPane will immediately update the location of the tabs to reflect
	 * this.</p>
	 *
	 * @param value the side
	 */
	public final void setSide( Side value ) {
		sideProperty().set( value );
	}

	/**
	 * The current position of the tabs in the ToolPane.  The default position
	 * for the tabs is Side.Top.
	 *
	 * @return The current position of the tabs in the ToolPane.
	 */
	public final Side getSide() {
		return side == null ? Side.TOP : side.get();
	}

	/**
	 * The position of the tabs in the ToolPane.
	 *
	 * @return the side property
	 */
	public final ObjectProperty<Side> sideProperty() {
		if( side == null ) {
			side = new ObjectPropertyBase<>( Side.TOP ) {

				private Side oldSide;

				@Override
				protected void invalidated() {
					oldSide = get();

					pseudoClassStateChanged( TOP_PSEUDOCLASS_STATE, (oldSide == Side.TOP || oldSide == null) );
					pseudoClassStateChanged( RIGHT_PSEUDOCLASS_STATE, (oldSide == Side.RIGHT) );
					pseudoClassStateChanged( BOTTOM_PSEUDOCLASS_STATE, (oldSide == Side.BOTTOM) );
					pseudoClassStateChanged( LEFT_PSEUDOCLASS_STATE, (oldSide == Side.LEFT) );
				}

				@Override
				public Object getBean() {
					return ToolPane.this;
				}

				@Override
				public String getName() {
					return "side";
				}
			};
		}
		return side;
	}

	/** {@inheritDoc} */
	@Override
	protected Skin<?> createDefaultSkin() {
		return new ToolPaneSkin( this );
	}

	/** {@inheritDoc} */
	@Override
	public Node lookup( String selector ) {
		Node node = super.lookup( selector );
		if( node == null ) {
			for( ToolTab tab : tabs ) {
				node = tab.lookup( selector );
				if( node != null ) return node;
			}
		}
		return node;
	}

	/** {@inheritDoc} */
	public Set<Node> lookupAll( String selector ) {
		if( selector == null ) return null;

		final List<Node> results = new ArrayList<>();

		results.addAll( super.lookupAll( selector ) );
		for( ToolTab tab : tabs ) {
			results.addAll( tab.lookupAll( selector ) );
		}

		return Collections.unmodifiableSet( new HashSet<>( results ) );
	}

	static class ToolPaneSelectionModel extends SingleSelectionModel<ToolTab> {

		private final ToolPane pane;

		public ToolPaneSelectionModel( final ToolPane pane ) {
			if( pane == null ) throw new NullPointerException( "ToolPane can not be null" );

			this.pane = pane;

			// Watch for changes to the items list content
			final ListChangeListener<ToolTab> itemsContentObserver = change -> {
				while( change.next() ) {
					for( ToolTab tab : change.getRemoved() ) {
						if( tab != null && !this.pane.getTabs().contains( tab ) ) {
							if( tab.isSelected() ) {
								tab.setSelected( false );
								final int tabIndex = change.getFrom();
								findNearestAvailableTab( tabIndex, true );
							}
						}
					}
					if( change.wasAdded() || change.wasRemoved() ) {
						// The selected tab index can be out of sync with the list of tabs
						// if tabs are added or removed before the selected tab.
						if( getSelectedIndex() != this.pane.getTabs().indexOf( getSelectedItem() ) ) {
							clearAndSelect( this.pane.getTabs().indexOf( getSelectedItem() ) );
						}
					}
				}
				if( getSelectedIndex() == -1 && getSelectedItem() == null && this.pane.getTabs().size() > 0 ) {
					findNearestAvailableTab( 0, true );
				} else if( this.pane.getTabs().isEmpty() ) {
					clearSelection();
				}
			};
			if( this.pane.getTabs() != null ) {
				this.pane.getTabs().addListener( itemsContentObserver );
			}
		}

		// API Implementation
		@Override
		public void select( int index ) {
			if( index < 0 || (getItemCount() > 0 && index >= getItemCount()) || (index == getSelectedIndex() && getModelItem( index ).isSelected()) ) {
				return;
			}

			// Unselect the old tab
			if( getSelectedIndex() >= 0 && getSelectedIndex() < pane.getTabs().size() ) {
				pane.getTabs().get( getSelectedIndex() ).setSelected( false );
			}

			setSelectedIndex( index );

			ToolTab tab = getModelItem( index );
			if( tab != null ) {
				setSelectedItem( tab );
			}

			// Select the new tab
			if( getSelectedIndex() >= 0 && getSelectedIndex() < pane.getTabs().size() ) {
				pane.getTabs().get( getSelectedIndex() ).setSelected( true );
			}

			pane.notifyAccessibleAttributeChanged( AccessibleAttribute.FOCUS_ITEM );
		}

		@Override
		public void select( ToolTab tab ) {
			final int itemCount = getItemCount();

			for( int i = 0; i < itemCount; i++ ) {
				final ToolTab value = getModelItem( i );
				if( value != null && value.equals( tab ) ) {
					select( i );
					return;
				}
			}
			if( tab != null ) {
				setSelectedItem( tab );
			}
		}

		@Override
		protected ToolTab getModelItem( int index ) {
			final ObservableList<ToolTab> items = pane.getTabs();
			if( items == null ) return null;
			if( index < 0 || index >= items.size() ) return null;
			return items.get( index );
		}

		@Override
		protected int getItemCount() {
			final ObservableList<ToolTab> items = pane.getTabs();
			return items == null ? 0 : items.size();
		}

		/**
		 * Find the the nearest, non-disabled tab from index.
		 *
		 * @param tabIndex
		 * @param doSelect
		 * @return
		 */
		private ToolTab findNearestAvailableTab( int tabIndex, boolean doSelect ) {
			final int tabCount = getItemCount();

			int index = 1;
			ToolTab bestTab = null;
			while( true ) {
				// Search left
				int leftIndex = tabIndex - index;
				if( leftIndex >= 0 ) {
					ToolTab tab = getModelItem( leftIndex );
					if( tab != null && !tab.isDisable() ) {
						bestTab = tab;
						break;
					}
				}

				// Search right
				int rightIndex = tabIndex + index - 1;
				if( rightIndex < tabCount ) {
					ToolTab tab = getModelItem( rightIndex );
					if( tab != null && !tab.isDisable() ) {
						bestTab = tab;
						break;
					}
				}

				if( leftIndex < 0 && rightIndex >= tabCount ) break;

				index++;
			}

			if( doSelect && bestTab != null ) select( bestTab );

			return bestTab;
		}
	}

}
