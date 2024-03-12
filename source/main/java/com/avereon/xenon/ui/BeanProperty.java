package com.avereon.xenon.ui;

import lombok.CustomLog;
import lombok.Getter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * A {@link BeanProperty} is a class that represents a property of a JavaBean.
 */
@Getter
@CustomLog
public class BeanProperty<T> {

	private final Class<?> type;

	private final String name;

	private Method getter;

	private Method setter;

	private boolean reflected;

	public BeanProperty( Class<T> type, String name ) {
		if( type == null ) {
			throw new NullPointerException( "Type class must be specified" );
		} else if( name == null || name.trim().isEmpty() ) {
			throw new NullPointerException( "Property name must be specified" );
		} else {
			this.type = type;
			this.name = name;
		}
	}

	public boolean isWritable() {
		this.reflect();
		return this.setter != null;
	}

	public boolean isReadable() {
		this.reflect();
		return this.getter != null;
	}

	@SuppressWarnings( "unchecked" )
	public <S> S get( Object object ) {
		if( !this.isReadable() ) {
			throw new IllegalStateException( "Property not readable: name=" + this.name );
		} else {
			try {
				return (S)this.getter.invoke( object );
			} catch( Exception exception ) {
				throw new RuntimeException( exception );
			}
		}
	}

	public void set( Object object, Object value ) {
		if( !this.isWritable() ) {
			throw new IllegalStateException( "Property not writable: name=" + this.name );
		} else {
			try {
				this.setter.invoke( object, value );
			} catch( Exception exception ) {
				throw new RuntimeException( exception );
			}
		}
	}

	private void reflect() {
		if( !reflected ) {
			reflected = true;
			try {
				// Create first-letter-capitalized version of name
				final String properName = name.length() == 1 ? name.substring( 0, 1 ).toUpperCase() : Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 );

				// Look for the "get" version of the getter
				String getterName = "get" + properName;
				try {
					final Method method = type.getMethod( getterName );
					if( Modifier.isPublic( method.getModifiers() ) ) getter = method;
				} catch( NoSuchMethodException exception ) {
					//log.atWarn().withCause( exception ).log( "Failed to find getter method: %s.%s", type.getName(), getterName );
				}

				// Look for the "is" version of the getter
				if( getter == null ) {
					getterName = "is" + properName;
					try {
						final Method method = type.getMethod( getterName );
						if( Modifier.isPublic( method.getModifiers() ) ) getter = method;
					} catch( NoSuchMethodException exception ) {
						//log.atWarn().withCause( exception ).log( "Failed to find getter method: %s.%s", type.getName(), getterName );
					}
				}

				// Get the setter
				final String setterName = "set" + properName;
				if( getter != null ) {
					// If there is a getter, finding the setter is easier
					try {
						final Method method = type.getMethod( setterName, getter.getReturnType() );
						if( Modifier.isPublic( method.getModifiers() ) ) {
							setter = method;
						}
					} catch( NoSuchMethodException exception ) {
						//log.atWarn().withCause( exception ).log( "Failed to find setter method: %s.%s", type.getName(), getterName );
					}
				} else {
					// If the getter is not found, do it the hard way
					final Method[] methods = type.getMethods();
					for( final Method method : methods ) {
						final Class<?>[] parameters = method.getParameterTypes();
						if( setterName.equals( method.getName() ) && (parameters.length == 1) && Modifier.isPublic( method.getModifiers() ) ) {
							setter = method;
							break;
						}
					}
				}
			} catch( RuntimeException exception ) {
				//log.atWarn().withCause( exception ).log( "Failed to introspect property: %s.%s", type.getName(), name );
			}
		}
	}

}
