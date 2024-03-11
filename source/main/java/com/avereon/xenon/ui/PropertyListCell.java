package com.avereon.xenon.ui;

import javafx.beans.NamedArg;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

public class PropertyListCell<T> implements Callback<ListView<T>, ListCell<T>> {

	private final String property;

	private final BeanProperty<?> beanProperty;

	public PropertyListCell( Class<?> type, @NamedArg( "property" ) String property ) {
		this.property = property;
		this.beanProperty = new BeanProperty<>( type, property );
	}

	public final String getProperty() {return property;}

	@Override
	public ListCell<T> call( ListView<T> param ) {
		return new ListCell<>() {

			@Override
			protected void updateItem( T item, boolean empty ) {
				super.updateItem( item, empty );
				if( empty || item == null ) {
					setText( null );
				} else {
					setText( beanProperty.get( item ) );
				}
			}

		};
	}

}
