package com.avereon.xenon.ui;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BeanPropertyTest {

	@Getter
	@Setter
	private static class Bean {

		private String name;

		public Bean( String name ) {
			this.name = name;
		}

	}

	@Test
	void testConstructor() {
		BeanProperty<Bean> property = new BeanProperty<>( Bean.class, "name" );
		assertThat( property.getType() ).isEqualTo( Bean.class );
	}

	@Test
	void testGet() {
		BeanProperty<Bean> property = new BeanProperty<>( Bean.class, "name" );
		Bean bean = new Bean( "Frank" );
		assertThat( (String)property.get( bean ) ).isEqualTo( "Frank" );
	}

	@Test
	void testSet() {
		BeanProperty<Bean> property = new BeanProperty<>( Bean.class, "name" );
		Bean bean = new Bean( null );
		property.set( bean, "Jodie" );
		assertThat( bean.getName() ).isEqualTo( "Jodie" );
	}

}
