package com.xeomar.xenon.settings;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Objects;

public class SettingsMatchers {

	public static Matcher<SettingsEvent> eventHas( Object source, SettingsEvent.Type type, String path, String key, String oldValue, String newValue ) {
		return new TypeSafeMatcher<SettingsEvent>() {

			@Override
			public void describeTo( final Description description ) {
				description.appendText( "should be " ).appendValue( format( SettingsEvent.class, source, type, path, key, oldValue, newValue ) );
			}

			@Override
			protected void describeMismatchSafely( final SettingsEvent item, final Description mismatchDescription ) {
				mismatchDescription.appendText( "was " ).appendValue( item );
			}

			@Override
			protected boolean matchesSafely( final SettingsEvent item ) {
				return Objects.equals( item.getSource(), source ) && Objects.equals( item.getKey(), key ) && Objects.equals( item.getType(), type ) && Objects.equals( item.getPath(), path ) && Objects.equals( item.getOldValue(), oldValue ) && Objects.equals( item.getNewValue(), newValue );
			}
		};
	}

	private static String format( Class<? extends SettingsEvent> clazz, Object source, SettingsEvent.Type type, String path, String key, String oldValue, String newValue ) {
		StringBuilder builder = new StringBuilder( );
		builder.append( clazz.getSimpleName() );
		builder.append( ":" );
		builder.append( source.getClass().getSimpleName() );
		builder.append( ":" );
		builder.append( type );
		if( path != null ) {
			builder.append( ":" );
			builder.append( path );
		}
		if( key != null ) {
			builder.append( ":" );
			builder.append( key );
		}
		if( type == SettingsEvent.Type.UPDATED ) {
			builder.append( ":" );
			builder.append( oldValue );
			builder.append( ":" );
			builder.append( newValue );
		}
		return builder.toString();
	}

}
