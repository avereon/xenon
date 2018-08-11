package com.xeomar.xenon.workarea;

import com.xeomar.util.OperatingSystem;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ToolPaneSkin extends SkinBase<ToolPane> {

	private static final int SPACER = 10;

	private static int CLOSE_BTN_SIZE = 16;

	private static final double ANIMATION_SPEED = 150;

	private TabHeaderArea tabHeaderArea;

	private ObservableList<TabContentRegion> tabContentRegions;

	private Rectangle tabHeaderAreaClipRect;

	private Rectangle clipRect;

	private boolean isSelectingTab;

	private ToolTab selectedTab;

	private final ToolPaneBehavior behavior;

	private static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "selected" );

	private static final PseudoClass DISABLED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "disabled" );

	private static final PseudoClass TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "top" );

	private static final PseudoClass BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "bottom" );

	private static final PseudoClass LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "left" );

	private static final PseudoClass RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass( "right" );

	/**
	 * Constructor for all SkinBase instances.
	 *
	 * @param control The control for which this Skin should attach to.
	 */
	protected ToolPaneSkin( ToolPane control ) {
		super( control );
		this.behavior = new ToolPaneBehavior( control );

		clipRect = new Rectangle( control.getWidth(), control.getHeight() );
		getSkinnable().setClip( clipRect );

		tabContentRegions = FXCollections.observableArrayList();

		for( ToolTab tab : getSkinnable().getTabs() ) {
			addTabContent( tab );
		}

		tabHeaderAreaClipRect = new Rectangle();
		tabHeaderArea = new TabHeaderArea();
		tabHeaderArea.setClip( tabHeaderAreaClipRect );
		getChildren().add( tabHeaderArea );
		if( getSkinnable().getTabs().size() == 0 ) {
			tabHeaderArea.setVisible( false );
		}

		initializeTabListener();

		//		registerChangeListener(control.getSelectionModel().selectedItemProperty(), e -> {
		//			isSelectingTab = true;
		//			selectedTab = getSkinnable().getSelectionModel().getSelectedItem();
		//			getSkinnable().requestLayout();
		//		});
		//		registerChangeListener(control.sideProperty(), e -> updateTabPosition());
		//		registerChangeListener(control.widthProperty(), e -> clipRect.setWidth(getSkinnable().getWidth()));
		//		registerChangeListener(control.heightProperty(), e -> clipRect.setHeight(getSkinnable().getHeight()));
		//
		//		selectedTab = getSkinnable().getSelectionModel().getSelectedItem();
		//		// Could not find the selected tab try and get the selected tab using the selected index
		//		if (selectedTab == null && getSkinnable().getSelectionModel().getSelectedIndex() != -1) {
		//			getSkinnable().getSelectionModel().select(getSkinnable().getSelectionModel().getSelectedIndex());
		//			selectedTab = getSkinnable().getSelectionModel().getSelectedItem();
		//		}
		//		if (selectedTab == null) {
		//			// getSelectedItem and getSelectedIndex failed select the first.
		//			getSkinnable().getSelectionModel().selectFirst();
		//		}
		//		selectedTab = getSkinnable().getSelectionModel().getSelectedItem();
		//		isSelectingTab = false;
		//
		//		initializeSwipeHandlers();
	}

	private void initializeTabListener() {
		getSkinnable().getTabs().addListener( (ListChangeListener<ToolTab>)c -> {
			List<ToolTab> tabsToRemove = new ArrayList<>();
			List<ToolTab> tabsToAdd = new ArrayList<>();
			int insertPos = -1;

			while( c.next() ) {
				if( c.wasPermutated() ) {
					ToolPane tabPane = getSkinnable();
					List<ToolTab> tabs = tabPane.getTabs();

					// tabs sorted : create list of permutated tabs.
					// clear selection, set tab animation to NONE
					// remove permutated tabs, add them back in correct order.
					// restore old selection, and old tab animation states.
					int size = c.getTo() - c.getFrom();
					ToolTab selTab = tabPane.getSelectionModel().getSelectedItem();
					List<ToolTab> permutatedTabs = new ArrayList<>( size );
					getSkinnable().getSelectionModel().clearSelection();

					//					// save and set tab animation to none - as it is not a good idea
					//					// to animate on the same data for open and close.
					//					TabAnimation prevOpenAnimation = openTabAnimation.get();
					//					TabAnimation prevCloseAnimation = closeTabAnimation.get();
					//					openTabAnimation.set( TabAnimation.NONE );
					//					closeTabAnimation.set( TabAnimation.NONE );
					//					for( int i = c.getFrom(); i < c.getTo(); i++ ) {
					//						permutatedTabs.add( tabs.get( i ) );
					//					}
					//					removeTabs( permutatedTabs );
					//					addTabs( permutatedTabs, c.getFrom() );
					//					openTabAnimation.set( prevOpenAnimation );
					//					closeTabAnimation.set( prevCloseAnimation );

					getSkinnable().getSelectionModel().select( selTab );
				}

				if( c.wasRemoved() ) {
					tabsToRemove.addAll( c.getRemoved() );
				}

				if( c.wasAdded() ) {
					tabsToAdd.addAll( c.getAddedSubList() );
					insertPos = c.getFrom();
				}
			}

			//			// now only remove the tabs that are not in the tabsToAdd list
			//			tabsToRemove.removeAll( tabsToAdd );
			//			removeTabs( tabsToRemove );
			//
			//			// and add in any new tabs (that we don't already have showing)
			//			if( !tabsToAdd.isEmpty() ) {
			//				for( TabContentRegion tabContentRegion : tabContentRegions ) {
			//					ToolTab tab = tabContentRegion.getTab();
			//					TabHeaderSkin tabHeader = tabHeaderArea.getTabHeaderSkin( tab );
			//					if( !tabHeader.isClosing && tabsToAdd.contains( tabContentRegion.getTab() ) ) {
			//						tabsToAdd.remove( tabContentRegion.getTab() );
			//					}
			//				}
			//
			//				addTabs( tabsToAdd, insertPos == -1 ? tabContentRegions.size() : insertPos );
			//			}

			getSkinnable().requestLayout();
		} );
	}

	private void addTabContent( ToolTab tab ) {
		TabContentRegion tabContentRegion = new TabContentRegion( tab );
		tabContentRegion.setClip( new Rectangle() );
		tabContentRegions.add( tabContentRegion );
		// We want the tab content to always sit below the tab headers
		getChildren().add( 0, tabContentRegion );
	}

	private void removeTabContent( ToolTab tab ) {
		for( TabContentRegion contentRegion : tabContentRegions ) {
			if( contentRegion.getTab().equals( tab ) ) {
				contentRegion.removeListeners( tab );
				getChildren().remove( contentRegion );
				tabContentRegions.remove( contentRegion );
				break;
			}
		}
	}

	/**
	 * "Duplicate" Label and ImageView nodes to be used in a
	 * ToolTab and the tool tabs menu at the same time.
	 */
	private static Node clone( Node node ) {
		if( node == null ) return null;

		if( node instanceof ImageView ) {
			ImageView oldView = (ImageView)node;
			ImageView newView = new ImageView();
			newView.imageProperty().bind( oldView.imageProperty() );
			return newView;
		}

		if( node instanceof Label ) {
			Label oldLabel = (Label)node;
			Label newLabel = new Label( oldLabel.getText(), clone( oldLabel.getGraphic() ) );
			newLabel.textProperty().bind( oldLabel.textProperty() );
			return newLabel;
		}

		return null;
	}

	@Override
	public Object queryAccessibleAttribute( AccessibleAttribute attribute, Object... parameters ) {
		switch( attribute ) {
			// TODO case FOCUS_ITEM: return tabHeaderArea.getTabHeaderSkin(selectedTab);
			// TODO case ITEM_COUNT: return tabHeaderArea.headersRegion.getChildren().size();
			// TODO case ITEM_AT_INDEX: {
			//				Integer index = (Integer)parameters[0];
			//				if (index == null) return null;
			//				return tabHeaderArea.headersRegion.getChildren().get(index);
			//			}
			default:
				return super.queryAccessibleAttribute( attribute, parameters );
		}
	}

	class TabHeaderArea extends StackPane {

		private Rectangle headerClip;

		private StackPane headersRegion;

		private StackPane headerBackground;

		private TabControlButtons controlButtons;

		private boolean measureClosingTabs = false;

		private double scrollOffset;

		public TabHeaderArea() {
			getStyleClass().setAll( "tool-tab-header-area" );
			setManaged( false );
			final ToolPane tabPane = getSkinnable();

			headerClip = new Rectangle();

			headersRegion = new TabHeadersRegion( this );

			// NEXT Keep implementing
		}

		public boolean isMeasureClosingTabs() {
			return measureClosingTabs;
		}

		private boolean tabsFit() {
			double headerPrefWidth = snapSizeX( headersRegion.prefWidth( -1 ) );
			double controlTabWidth = snapSizeX( controlButtons.prefWidth( -1 ) );
			double visibleWidth = headerPrefWidth + controlTabWidth + firstTabIndent() + SPACER;
			return visibleWidth < getWidth();
		}

		private double firstTabIndent() {
			switch( getSkinnable().getSide() ) {
				case TOP:
				case BOTTOM:
					return snappedLeftInset();
				case RIGHT:
				case LEFT:
					return snappedTopInset();
				default:
					return 0;
			}
		}

	}

	private class TabHeadersRegion extends StackPane {

		private TabHeaderArea tabHeaderArea;

		public TabHeadersRegion( TabHeaderArea tabHeaderArea ) {
			this.tabHeaderArea = tabHeaderArea;
		}

		@Override
		protected double computePrefWidth( double height ) {
			double width = 0.0F;
			for( Node child : getChildren() ) {
				TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)child;
				if( tabHeaderSkin.isVisible() && (tabHeaderArea.isMeasureClosingTabs() || !tabHeaderSkin.isClosing) ) {
					width += tabHeaderSkin.prefWidth( height );
				}
			}
			return snapSizeX( width ) + snappedLeftInset() + snappedRightInset();
		}

		@Override
		protected double computePrefHeight( double width ) {
			double height = 0.0F;
			for( Node child : getChildren() ) {
				TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)child;
				height = Math.max( height, tabHeaderSkin.prefHeight( width ) );
			}
			return snapSizeY( height ) + snappedTopInset() + snappedBottomInset();
		}

		@Override
		protected void layoutChildren() {
			if( tabHeaderArea.tabsFit() ) {
				setScrollOffset( 0 );
			} else {
				if( !removeTab.isEmpty() ) {
					double offset = 0;
					double w = tabHeaderArea.getWidth() - snapSizeX( controlButtons.prefWidth( -1 ) ) - firstTabIndent() - SPACER;
					Iterator<Node> nodes = getChildren().iterator();
					while( nodes.hasNext() ) {
						TabHeaderSkin tabHeader = (TabHeaderSkin)nodes.next();
						double tabHeaderPrefWidth = snapSizeX( tabHeader.prefWidth( -1 ) );
						if( removeTab.contains( tabHeader ) ) {
							if( offset < w ) {
								isSelectingTab = true;
							}
							nodes.remove();
							removeTab.remove( tabHeader );
							if( removeTab.isEmpty() ) break;
						}
						offset += tabHeaderPrefWidth;
					}
					//                        } else {
					//                            isSelectingTab = true;
				}
			}

			if( isSelectingTab ) {
				ensureSelectedTabIsVisible();
				isSelectingTab = false;
			} else {
				validateScrollOffset();
			}

			Side tabPosition = getSkinnable().getSide();
			double tabBackgroundHeight = snapSizeY( prefHeight( -1 ) );
			double tabX = (tabPosition.equals( Side.LEFT ) || tabPosition.equals( Side.BOTTOM )) ? snapSizeX( getWidth() ) - getScrollOffset() : getScrollOffset();

			updateHeaderClip();
			for( Node node : getChildren() ) {
				TabHeaderSkin tabHeader = (TabHeaderSkin)node;

				// size and position the header relative to the other headers
				double tabHeaderPrefWidth = snapSizeX( tabHeader.prefWidth( -1 ) * tabHeader.animationTransition.get() );
				double tabHeaderPrefHeight = snapSizeY( tabHeader.prefHeight( -1 ) );
				tabHeader.resize( tabHeaderPrefWidth, tabHeaderPrefHeight );

				// This ensures that the tabs are located in the correct position
				// when there are tabs of differing heights.
				double startY = tabPosition.equals( Side.BOTTOM ) ? 0 : tabBackgroundHeight - tabHeaderPrefHeight - snappedBottomInset();
				if( tabPosition.equals( Side.LEFT ) || tabPosition.equals( Side.BOTTOM ) ) {
					// build from the right
					tabX -= tabHeaderPrefWidth;
					tabHeader.relocate( tabX, startY );
				} else {
					// build from the left
					tabHeader.relocate( tabX, startY );
					tabX += tabHeaderPrefWidth;
				}
			}
		}

	}

	private class TabHeaderSkin extends StackPane {

		private final ToolTab tab;

		private Label label;

		private StackPane closeBtn;

		private StackPane inner;

		private Tooltip oldTooltip;

		private Tooltip tooltip;

		private Rectangle clip;

		private boolean isClosing = false;

		//private LambdaMultiplePropertyChangeListenerHandler listener = new LambdaMultiplePropertyChangeListenerHandler();

		private final ListChangeListener<String> styleClassListener = new ListChangeListener<String>() {

			@Override
			public void onChanged( Change<? extends String> c ) {
				getStyleClass().setAll( tab.getStyleClass() );
			}
		};

		private final WeakListChangeListener<String> weakStyleClassListener = new WeakListChangeListener<>( styleClassListener );

		public TabHeaderSkin( final ToolTab tab ) {
			getStyleClass().setAll( tab.getStyleClass() );
			setId( tab.getId() );
			setStyle( tab.getStyle() );
			setAccessibleRole( AccessibleRole.TAB_ITEM );

			this.tab = tab;
			clip = new Rectangle();
			setClip( clip );

			label = new Label( tab.getTool().getTitle(), tab.getTool().getGraphic() );
			label.getStyleClass().setAll( "tool-tab-label" );

			closeBtn = new StackPane() {

				@Override
				protected double computePrefWidth( double height ) {
					return CLOSE_BTN_SIZE;
				}

				@Override
				protected double computePrefHeight( double width ) {
					return CLOSE_BTN_SIZE;
				}

				@Override
				public void executeAccessibleAction( AccessibleAction action, Object... parameters ) {
					switch( action ) {
						case FIRE: {
							ToolTab tab = getTab();
							if( behavior.canCloseTab( tab ) ) {
								behavior.closeTab( tab );
								setOnMousePressed( null );
							}
							break;
						}
						default:
							super.executeAccessibleAction( action, parameters );
					}
				}
			};

			closeBtn.setAccessibleRole( AccessibleRole.BUTTON );
			////closeBtn.setAccessibleText( getString( "Accessibility.title.TabPane.CloseButton" ) );
			closeBtn.getStyleClass().setAll( "tool-tab-close-button" );
			closeBtn.setOnMousePressed( event -> {
				ToolTab tabToClose = getTab();
				if( behavior.canCloseTab( tabToClose ) ) {
					behavior.closeTab( tabToClose );
					setOnMousePressed( null );
				}
			} );

			updateGraphicRotation();

			final Region focusIndicator = new Region();
			focusIndicator.setMouseTransparent( true );
			focusIndicator.getStyleClass().add( "focus-indicator" );

			inner = new StackPane() {

				@Override
				protected void layoutChildren() {
					final ToolPane skinnable = getSkinnable();

					final double paddingTop = snappedTopInset();
					final double paddingRight = snappedRightInset();
					final double paddingBottom = snappedBottomInset();
					final double paddingLeft = snappedLeftInset();
					final double w = getWidth() - (paddingLeft + paddingRight);
					final double h = getHeight() - (paddingTop + paddingBottom);

					final double prefLabelWidth = snapSizeX( label.prefWidth( -1 ) );
					final double prefLabelHeight = snapSizeY( label.prefHeight( -1 ) );

					final double closeBtnWidth = showCloseButton() ? snapSizeX( closeBtn.prefWidth( -1 ) ) : 0;
					final double closeBtnHeight = showCloseButton() ? snapSizeY( closeBtn.prefHeight( -1 ) ) : 0;
					final double minWidth = snapSizeX( skinnable.getTabMinWidth() );
					final double maxWidth = snapSizeX( skinnable.getTabMaxWidth() );
					final double maxHeight = snapSizeY( skinnable.getTabMaxHeight() );

					double labelAreaWidth = prefLabelWidth;
					double labelWidth = prefLabelWidth;
					double labelHeight = prefLabelHeight;

					final double childrenWidth = labelAreaWidth + closeBtnWidth;
					final double childrenHeight = Math.max( labelHeight, closeBtnHeight );

					if( childrenWidth > maxWidth && maxWidth != Double.MAX_VALUE ) {
						labelAreaWidth = maxWidth - closeBtnWidth;
						labelWidth = maxWidth - closeBtnWidth;
					} else if( childrenWidth < minWidth ) {
						labelAreaWidth = minWidth - closeBtnWidth;
					}

					if( childrenHeight > maxHeight && maxHeight != Double.MAX_VALUE ) {
						labelHeight = maxHeight;
					}

					//					if( animationState != TabAnimationState.NONE ) {
					//						//// if (prefWidth.getValue() < labelAreaWidth) labelAreaWidth = prefWidth.getValue();
					//						labelAreaWidth *= animationTransition.get();
					//						closeBtn.setVisible( false );
					//					} else {
					//						closeBtn.setVisible( showCloseButton() );
					//					}

					label.resize( labelWidth, labelHeight );

					double labelStartX = paddingLeft;

					// If maxWidth is less than Double.MAX_VALUE, the user has clamped
					// the max width. Regardless, the close button should be positioned
					// at the end of the tab, which may not necessarily be the entire
					// width of the provided max width.
					double closeBtnStartX = (maxWidth < Double.MAX_VALUE ? Math.min( w, maxWidth ) : w) - paddingRight - closeBtnWidth;

					positionInArea( label, labelStartX, paddingTop, labelAreaWidth, h, 0, HPos.CENTER, VPos.CENTER );

					if( closeBtn.isVisible() ) {
						closeBtn.resize( closeBtnWidth, closeBtnHeight );
						positionInArea( closeBtn, closeBtnStartX, paddingTop, closeBtnWidth, h, 0, HPos.CENTER, VPos.CENTER );
					}

					// Magic numbers regretfully introduced for RT-28944 (so that
					// the focus rect appears as expected on Windows and Mac).
					// In short we use the vPadding to shift the focus rect down
					// into the content area (whereas previously it was being clipped
					// on Windows, whilst it still looked fine on Mac). In the
					// future we may want to improve this code to remove the
					// magic number. Similarly, the hPadding differs on Mac.
					final int vPadding = OperatingSystem.isMac() ? 2 : 3;
					final int hPadding = OperatingSystem.isMac() ? 2 : 1;
					focusIndicator.resizeRelocate( paddingLeft - hPadding, paddingTop + vPadding, w + 2 * hPadding, h - 2 * vPadding );
				}
			};
			inner.getStyleClass().add( "tab-container" );
			inner.setRotate( getSkinnable().getSide().equals( Side.BOTTOM ) ? 180.0F : 0.0F );
			inner.getChildren().addAll( label, closeBtn, focusIndicator );

			getChildren().addAll( inner );

			//			tooltip = tab.getTooltip();
			//			if( tooltip != null ) {
			//				Tooltip.install( this, tooltip );
			//				oldTooltip = tooltip;
			//			}
			//
			//			listener.registerChangeListener( tab.closableProperty(), e -> {
			//				inner.requestLayout();
			//				requestLayout();
			//			} );
			//			listener.registerChangeListener( tab.selectedProperty(), e -> {
			//				pseudoClassStateChanged( SELECTED_PSEUDOCLASS_STATE, tab.isSelected() );
			//				// Need to request a layout pass for inner because if the width
			//				// and height didn't not change the label or close button may have
			//				// changed.
			//				inner.requestLayout();
			//				requestLayout();
			//			} );
			//			listener.registerChangeListener( tab.textProperty(), e -> label.setText( getTab().getText() ) );
			//			listener.registerChangeListener( tab.graphicProperty(), e -> label.setGraphic( getTab().getGraphic() ) );
			//			listener.registerChangeListener( tab.tooltipProperty(), e -> {
			//				// uninstall the old tooltip
			//				if( oldTooltip != null ) {
			//					Tooltip.uninstall( this, oldTooltip );
			//				}
			//				tooltip = tab.getTooltip();
			//				if( tooltip != null ) {
			//					// install new tooltip and save as old tooltip.
			//					Tooltip.install( this, tooltip );
			//					oldTooltip = tooltip;
			//				}
			//			} );
			//			listener.registerChangeListener( tab.disableProperty(), e -> {
			//				pseudoClassStateChanged( DISABLED_PSEUDOCLASS_STATE, tab.isDisable() );
			//				inner.requestLayout();
			//				requestLayout();
			//			} );
			//			listener.registerChangeListener( tab.styleProperty(), e -> setStyle( tab.getStyle() ) );
			//
			//			tab.getStyleClass().addListener( weakStyleClassListener );
			//
			//			listener.registerChangeListener( getSkinnable().tabClosingPolicyProperty(), e -> {
			//				inner.requestLayout();
			//				requestLayout();
			//			} );
			//			listener.registerChangeListener( getSkinnable().sideProperty(), e -> {
			//				final Side side = getSkinnable().getSide();
			//				pseudoClassStateChanged( TOP_PSEUDOCLASS_STATE, (side == Side.TOP) );
			//				pseudoClassStateChanged( RIGHT_PSEUDOCLASS_STATE, (side == Side.RIGHT) );
			//				pseudoClassStateChanged( BOTTOM_PSEUDOCLASS_STATE, (side == Side.BOTTOM) );
			//				pseudoClassStateChanged( LEFT_PSEUDOCLASS_STATE, (side == Side.LEFT) );
			//				inner.setRotate( side == Side.BOTTOM ? 180.0F : 0.0F );
			//				if( getSkinnable().isRotateGraphic() ) {
			//					updateGraphicRotation();
			//				}
			//			} );
			//			listener.registerChangeListener( getSkinnable().rotateGraphicProperty(), e -> updateGraphicRotation() );
			//			listener.registerChangeListener( getSkinnable().tabMinWidthProperty(), e -> {
			//				requestLayout();
			//				getSkinnable().requestLayout();
			//			} );
			//			listener.registerChangeListener( getSkinnable().tabMaxWidthProperty(), e -> {
			//				requestLayout();
			//				getSkinnable().requestLayout();
			//			} );
			//			listener.registerChangeListener( getSkinnable().tabMinHeightProperty(), e -> {
			//				requestLayout();
			//				getSkinnable().requestLayout();
			//			} );
			//			listener.registerChangeListener( getSkinnable().tabMaxHeightProperty(), e -> {
			//				requestLayout();
			//				getSkinnable().requestLayout();
			//			} );
			//
			//			getProperties().put( Tab.class, tab );
			//			getProperties().put( ContextMenu.class, tab.getContextMenu() );
			//
			//			setOnContextMenuRequested( ( ContextMenuEvent me ) -> {
			//				if( getTab().getContextMenu() != null ) {
			//					getTab().getContextMenu().show( inner, me.getScreenX(), me.getScreenY() );
			//					me.consume();
			//				}
			//			} );
			//			setOnMousePressed( new EventHandler<MouseEvent>() {
			//
			//				@Override
			//				public void handle( MouseEvent me ) {
			//					if( getTab().isDisable() ) {
			//						return;
			//					}
			//					if( me.getButton().equals( MouseButton.MIDDLE ) ) {
			//						if( showCloseButton() ) {
			//							Tab tab = getTab();
			//							if( behavior.canCloseTab( tab ) ) {
			//								removeListeners( tab );
			//								behavior.closeTab( tab );
			//							}
			//						}
			//					} else if( me.getButton().equals( MouseButton.PRIMARY ) ) {
			//						behavior.selectTab( getTab() );
			//					}
			//				}
			//			} );

			// initialize pseudo-class state
			pseudoClassStateChanged( SELECTED_PSEUDOCLASS_STATE, tab.isSelected() );
			pseudoClassStateChanged( DISABLED_PSEUDOCLASS_STATE, tab.isDisable() );
			final Side side = getSkinnable().getSide();
			pseudoClassStateChanged( TOP_PSEUDOCLASS_STATE, (side == Side.TOP) );
			pseudoClassStateChanged( RIGHT_PSEUDOCLASS_STATE, (side == Side.RIGHT) );
			pseudoClassStateChanged( BOTTOM_PSEUDOCLASS_STATE, (side == Side.BOTTOM) );
			pseudoClassStateChanged( LEFT_PSEUDOCLASS_STATE, (side == Side.LEFT) );
		}

		public ToolTab getTab() {
			return tab;
		}

		//		private void updateGraphicRotation() {
		//			if( label.getGraphic() != null ) {
		//				label.getGraphic().setRotate( getSkinnable().isRotateGraphic() ? 0.0F : (getSkinnable().getSide().equals( Side.RIGHT ) ? -90.0F : (getSkinnable().getSide().equals( Side.LEFT ) ? 90.0F : 0.0F)) );
		//			}
		//		}
		//
		//		private boolean showCloseButton() {
		//			return tab.isClosable() && (getSkinnable().getTabClosingPolicy().equals( TabClosingPolicy.ALL_TABS ) || getSkinnable().getTabClosingPolicy().equals( TabClosingPolicy.SELECTED_TAB ) && tab.isSelected());
		//		}

		private final DoubleProperty animationTransition = new SimpleDoubleProperty( this, "animationTransition", 1.0 ) {

			@Override
			protected void invalidated() {
				requestLayout();
			}

		};

		//		private void removeListeners( ToolTab tab ) {
		//			listener.dispose();
		//			inner.getChildren().clear();
		//			getChildren().clear();
		//		}
		//
		//		private TabPaneSkin.TabAnimationState animationState = TabPaneSkin.TabAnimationState.NONE;
		//
		//		private Timeline currentAnimation;
		//
		//		@Override
		//		protected double computePrefWidth( double height ) {
		//			//            if (animating) {
		//			//                return prefWidth.getValue();
		//			//            }
		//			double minWidth = snapSize( getSkinnable().getTabMinWidth() );
		//			double maxWidth = snapSize( getSkinnable().getTabMaxWidth() );
		//			double paddingRight = snappedRightInset();
		//			double paddingLeft = snappedLeftInset();
		//			double tmpPrefWidth = snapSize( label.prefWidth( -1 ) );
		//
		//			// only include the close button width if it is relevant
		//			if( showCloseButton() ) {
		//				tmpPrefWidth += snapSize( closeBtn.prefWidth( -1 ) );
		//			}
		//
		//			if( tmpPrefWidth > maxWidth ) {
		//				tmpPrefWidth = maxWidth;
		//			} else if( tmpPrefWidth < minWidth ) {
		//				tmpPrefWidth = minWidth;
		//			}
		//			tmpPrefWidth += paddingRight + paddingLeft;
		//			//            prefWidth.setValue(tmpPrefWidth);
		//			return tmpPrefWidth;
		//		}
		//
		//		@Override
		//		protected double computePrefHeight( double width ) {
		//			double minHeight = snapSize( getSkinnable().getTabMinHeight() );
		//			double maxHeight = snapSize( getSkinnable().getTabMaxHeight() );
		//			double paddingTop = snappedTopInset();
		//			double paddingBottom = snappedBottomInset();
		//			double tmpPrefHeight = snapSize( label.prefHeight( width ) );
		//
		//			if( tmpPrefHeight > maxHeight ) {
		//				tmpPrefHeight = maxHeight;
		//			} else if( tmpPrefHeight < minHeight ) {
		//				tmpPrefHeight = minHeight;
		//			}
		//			tmpPrefHeight += paddingTop + paddingBottom;
		//			return tmpPrefHeight;
		//		}

		@Override
		protected void layoutChildren() {
			double w = (snapSizeX( getWidth() ) - snappedRightInset() - snappedLeftInset()) * animationTransition.getValue();
			inner.resize( w, snapSizeY( getHeight() ) - snappedTopInset() - snappedBottomInset() );
			inner.relocate( snappedLeftInset(), snappedTopInset() );
		}

		@Override
		protected void setWidth( double value ) {
			super.setWidth( value );
			clip.setWidth( value );
		}

		@Override
		protected void setHeight( double value ) {
			super.setHeight( value );
			clip.setHeight( value );
		}

		/** {@inheritDoc} */
		@Override
		public Object queryAccessibleAttribute( AccessibleAttribute attribute, Object... parameters ) {
			switch( attribute ) {
				case TEXT:
					return getTab().getTool().getTitle();
				case SELECTED:
					return selectedTab == getTab();
				default:
					return super.queryAccessibleAttribute( attribute, parameters );
			}
		}

		/** {@inheritDoc} */
		@Override
		public void executeAccessibleAction( AccessibleAction action, Object... parameters ) {
			switch( action ) {
				case REQUEST_FOCUS:
					getSkinnable().getSelectionModel().select( getTab() );
					break;
				default:
					super.executeAccessibleAction( action, parameters );
			}
		}

	}

	class TabControlButtons extends StackPane {

		private StackPane inner;

		private StackPane downArrow;

		private Pane downArrowBtn;

		private boolean showControlButtons;

		private ContextMenu popup;

		public TabControlButtons() {
			getStyleClass().setAll( "control-buttons-tab" );

			ToolPane pane = getSkinnable();

			downArrowBtn = new Pane();
			downArrowBtn.getStyleClass().setAll( "tab-down-button" );
			downArrowBtn.setVisible( isShowTabsMenu() );
			downArrow = new StackPane();
			downArrow.setManaged( false );
			downArrow.getStyleClass().setAll( "arrow" );
			downArrow.setRotate( pane.getSide().equals( Side.BOTTOM ) ? 180.0F : 0.0F );
			downArrowBtn.getChildren().add( downArrow );
			downArrowBtn.setOnMouseClicked( me -> {
				showPopupMenu();
			} );

			setupPopupMenu();

			inner = new StackPane() {

				@Override
				protected double computePrefWidth( double height ) {
					double pw;
					double maxArrowWidth = !isShowTabsMenu() ? 0 : snapSizeX( downArrow.prefWidth( getHeight() ) ) + snapSize( downArrowBtn.prefWidth( getHeight() ) );
					pw = 0.0F;
					if( isShowTabsMenu() ) {
						pw += maxArrowWidth;
					}
					if( pw > 0 ) {
						pw += snappedLeftInset() + snappedRightInset();
					}
					return pw;
				}

				@Override
				protected double computePrefHeight( double width ) {
					double height = 0.0F;
					if( isShowTabsMenu() ) {
						height = Math.max( height, snapSizeY( downArrowBtn.prefHeight( width ) ) );
					}
					if( height > 0 ) {
						height += snappedTopInset() + snappedBottomInset();
					}
					return height;
				}

				@Override
				protected void layoutChildren() {
					if( isShowTabsMenu() ) {
						double x = 0;
						double y = snappedTopInset();
						double w = snapSizeX( getWidth() ) - x + snappedLeftInset();
						double h = snapSizeY( getHeight() ) - y + snappedBottomInset();
						positionArrow( downArrowBtn, downArrow, x, y, w, h );
					}
				}

				private void positionArrow( Pane btn, StackPane arrow, double x, double y, double width, double height ) {
					btn.resize( width, height );
					positionInArea( btn, x, y, width, height, 0, HPos.CENTER, VPos.CENTER );
					// center arrow region within arrow button
					double arrowWidth = snapSizeX( arrow.prefWidth( -1 ) );
					double arrowHeight = snapSizeY( arrow.prefHeight( -1 ) );
					arrow.resize( arrowWidth, arrowHeight );
					positionInArea( arrow, btn.snappedLeftInset(), btn.snappedTopInset(), width - btn.snappedLeftInset() - btn.snappedRightInset(), height - btn.snappedTopInset() - btn.snappedBottomInset(), 0, HPos.CENTER, VPos.CENTER );
				}
			};
			inner.getStyleClass().add( "container" );
			inner.getChildren().add( downArrowBtn );

			getChildren().add( inner );

			pane.sideProperty().addListener( valueModel -> {
				Side tabPosition = getSkinnable().getSide();
				downArrow.setRotate( tabPosition.equals( Side.BOTTOM ) ? 180.0F : 0.0F );
			} );
			pane.getTabs().addListener( (ListChangeListener<ToolTab>)change -> setupPopupMenu() );
			showControlButtons = false;
			if( isShowTabsMenu() ) {
				showControlButtons = true;
				requestLayout();
			}
			getProperties().put( ContextMenu.class, popup );
		}

		private boolean showTabsMenu = false;

		private void showTabsMenu( boolean value ) {
			final boolean wasTabsMenuShowing = isShowTabsMenu();
			this.showTabsMenu = value;

			if( showTabsMenu && !wasTabsMenuShowing ) {
				downArrowBtn.setVisible( true );
				showControlButtons = true;
				inner.requestLayout();
				tabHeaderArea.requestLayout();
			} else if( !showTabsMenu && wasTabsMenuShowing ) {
				hideControlButtons();
			}
		}

		private boolean isShowTabsMenu() {
			return showTabsMenu;
		}

		@Override
		protected double computePrefWidth( double height ) {
			double pw = snapSizeX( inner.prefWidth( height ) );
			if( pw > 0 ) {
				pw += snappedLeftInset() + snappedRightInset();
			}
			return pw;
		}

		@Override
		protected double computePrefHeight( double width ) {
			return Math.max( getSkinnable().getTabMinHeight(), snapSizeY( inner.prefHeight( width ) ) ) + snappedTopInset() + snappedBottomInset();
		}

		@Override
		protected void layoutChildren() {
			double x = snappedLeftInset();
			double y = snappedTopInset();
			double w = snapSizeX( getWidth() ) - x + snappedRightInset();
			double h = snapSizeY( getHeight() ) - y + snappedBottomInset();

			if( showControlButtons ) {
				showControlButtons();
				showControlButtons = false;
			}

			inner.resize( w, h );
			positionInArea( inner, x, y, w, h, /*baseline ignored*/0, HPos.CENTER, VPos.BOTTOM );
		}

		private void showControlButtons() {
			setVisible( true );
			if( popup == null ) {
				setupPopupMenu();
			}
		}

		private void hideControlButtons() {
			// If the scroll arrows or tab menu is still visible we don't want
			// to hide it animate it back it.
			if( isShowTabsMenu() ) {
				showControlButtons = true;
			} else {
				setVisible( false );
				popup.getItems().clear();
				popup = null;
			}

			// This needs to be called when we are in the left tabPosition
			// to allow for the clip offset to move properly (otherwise
			// it jumps too early - before the animation is done).
			requestLayout();
		}

		private void setupPopupMenu() {
			if( popup == null ) {
				popup = new ContextMenu();
			}
			popup.getItems().clear();
			ToggleGroup group = new ToggleGroup();
			ObservableList<RadioMenuItem> menuitems = FXCollections.<RadioMenuItem> observableArrayList();
			for( final ToolTab tab : getSkinnable().getTabs() ) {
				TabMenuItem item = new TabMenuItem( tab );
				item.setToggleGroup( group );
				item.setOnAction( t -> getSkinnable().getSelectionModel().select( tab ) );
				menuitems.add( item );
			}
			popup.getItems().addAll( menuitems );
		}

		private void showPopupMenu() {
			for( MenuItem mi : popup.getItems() ) {
				TabMenuItem tmi = (TabMenuItem)mi;
				if( selectedTab.equals( tmi.getTab() ) ) {
					tmi.setSelected( true );
					break;
				}
			}
			popup.show( downArrowBtn, Side.BOTTOM, 0, 0 );
		}
	} /* End TabControlButtons*/

	private static class TabMenuItem extends RadioMenuItem {

		ToolTab tab;

		private InvalidationListener disableListener = new InvalidationListener() {

			@Override
			public void invalidated( Observable o ) {
				setDisable( tab.isDisable() );
			}
		};

		private WeakInvalidationListener weakDisableListener = new WeakInvalidationListener( disableListener );

		public TabMenuItem( final ToolTab tab ) {
			super( tab.getTool().getTitle(), ToolPaneSkin.clone( tab.getTool().getGraphic() ) );
			this.tab = tab;
			setDisable( tab.isDisable() );
			tab.disableProperty().addListener( weakDisableListener );
			textProperty().bind( tab.getTool().titleProperty() );
		}

		public ToolTab getTab() {
			return tab;
		}

		public void dispose() {
			tab.disableProperty().removeListener( weakDisableListener );
		}
	}

	private static class TabContentRegion extends StackPane {

		private ToolTab tab;

		private InvalidationListener tabContentListener = valueModel -> updateContent();

		private InvalidationListener tabSelectedListener = valueModel -> setVisible( tab.isSelected() );

		private WeakInvalidationListener weakTabContentListener = new WeakInvalidationListener( tabContentListener );

		private WeakInvalidationListener weakTabSelectedListener = new WeakInvalidationListener( tabSelectedListener );

		public ToolTab getTab() {
			return tab;
		}

		public TabContentRegion( ToolTab tab ) {
			getStyleClass().setAll( "tool-tab-content-area" );
			setManaged( false );
			this.tab = tab;
			updateContent();
			setVisible( tab.isSelected() );

			tab.selectedProperty().addListener( weakTabSelectedListener );
			tab.toolProperty().addListener( weakTabContentListener );
		}

		private void updateContent() {
			Tool tool = getTab().getTool();
			if( tool == null ) {
				getChildren().clear();
			} else {
				getChildren().setAll( tool );
			}
		}

		private void removeListeners( ToolTab tab ) {
			tab.selectedProperty().removeListener( weakTabSelectedListener );
			tab.toolProperty().removeListener( weakTabContentListener );
		}

	}

}
