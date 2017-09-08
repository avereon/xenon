package com.xeomar.xenon.util;

import java.util.Comparator;

/**
 * These methods should no longer be needed.
 */
@Deprecated
public class Panels {

//	public static final int PANEL_SPACING = (int)BorderStroke.THICK.getTop();
//
//	public static final String PARAMETERS = "parameters";
//
//	private static final ObjectComparator OBJECT_COMPARATOR = new ObjectComparator();
//
//	private Panels() {}
//
//	public static JPanel createSystemPropertiesPanel() {
//		JPanel panel = new JPanel();
//		panel.setOpaque( false );
//		panel.setLayout( new SpringLayout() );
//
//		int lineCount = 0;
//		Properties properties = System.getProperties();
//		List<Object> keys = new ArrayList<Object>();
//		keys.addAll( properties.keySet() );
//		Collections.sort( keys, OBJECT_COMPARATOR );
//		for( Object keyObject : keys ) {
//			String key = (String)keyObject;
//			String value = properties.getProperty( key );
//
//			if( key.endsWith( "path" ) ) {
//				value = value.replace( ':', '\n' );
//			}
//
//			lineCount += TextUtil.getLineCount( value.trim() );
//
//			panel.add( createTitle( key, key ) );
//			panel.add( createTextArea( key, value.trim() ) );
//			panel.add( Box.createHorizontalGlue() );
//		}
//
//		Layouts.makeGrid( panel, properties.size(), 3, 0, 0 );
//
//		JScrollPane scrollPane = new JScrollPane( panel );
//		scrollPane.getViewport().setOpaque( false );
//		scrollPane.setOpaque( false );
//
//		JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
//		scrollBar.setMaximum( panel.getPreferredSize().height );
//		scrollBar.setUnitIncrement( scrollBar.getMaximum() / lineCount );
//
//		JPanel borderPanel = new JPanel();
//		borderPanel.setOpaque( false );
//		borderPanel.setLayout( new BorderLayout() );
//		borderPanel.setBorder( new TitledBorder( Bundles.getString( BundleKey.LABELS, PARAMETERS ) ) );
//		borderPanel.add( scrollPane, BorderLayout.CENTER );
//
//		panel.validate();
//		panel.scrollRectToVisible( new Rectangle( 0, 0, 1, 1 ) );
//
//		return borderPanel;
//	}
//
//	public static <T extends JComponent> T configure( String key, T component ) {
//		if( key != null ) component.setName( key );
//		component.setOpaque( false );
//		return component;
//	}
//
//	public static JLabel createTitle( String key, String text ) {
//		return configure( key, new XLabel( text ) );
//	}
//
//	public static JLabel createLabel( String key, String text ) {
//		return configure( key, new XLabel( text ) );
//	}
//
//	public static JButton createButton( String key, String text ) {
//		JButton button = new JButton( text );
//		button.setName( key );
//		return button;
//	}
//
//	public static JButton createButton( String key, Action action ) {
//		JButton button = new JButton( action );
//		button.setName( key );
//		return button;
//	}
//
//	public static JButton createIconButton( String key, Icon icon ) {
//		JButton button = new JButton( icon );
//		button.setName( key );
//		button.setPressedIcon( Icons.filter( button.getIcon(), Images.PRESSED_FILTER ) );
//		button.setDisabledIcon( Icons.filter( button.getIcon(), Images.DISABLED_FILTER ) );
//		button.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
//		button.setMargin( new Insets( 0, 0, 0, 0 ) );
//		button.setFocusPainted( false );
//		button.setContentAreaFilled( false );
//		return button;
//	}
//
//	public static JButton createActionIconButton( String key, Action action ) {
//		JButton button = new JButton( action );
//		button.setName( key );
//		button.setPressedIcon( Icons.filter( button.getIcon(), Images.PRESSED_FILTER ) );
//		button.setDisabledIcon( Icons.filter( button.getIcon(), Images.DISABLED_FILTER ) );
//		button.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
//		button.setMargin( new Insets( 0, 0, 0, 0 ) );
//		button.setFocusPainted( false );
//		button.setContentAreaFilled( false );
//		button.setText( "" );
//		return button;
//	}
//
//	public static JRadioButton createRadioButton( String key, String text ) {
//		return configure( key, new JRadioButton( text ) );
//	}
//
//	public static JCheckBox createCheckBox( String key, String text ) {
//		return configure( key, new JCheckBox( text ) );
//	}
//
//	public static <T> JComboBox<T> createComboBox( String key, T[] values ) {
//		JComboBox<T> checkbox = configure( key, new JComboBox<T>( values ) );
//		return checkbox;
//	}
//
//	public static <T> JList<T> createList( String key ) {
//		return configure( key, new JList<T>() );
//	}
//
//	public static JTextField createTextField( String key ) {
//		return createTextField( key, null );
//	}
//
//	public static JTextField createTextField( String key, String value ) {
//		return configure( key, value == null ? new JTextField() : new JTextField( value ) );
//	}
//
//	public static JTextArea createTextArea( String key, String text ) {
//		JTextArea area = configure( key, new JTextArea( text ) );
//		area.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
//		area.setEditable( false );
//		return area;
//	}
//
//	public static JPasswordField createPasswordField( String key, String value ) {
//		return configure( key, value == null ? new JPasswordField() : new JPasswordField( value ) );
//	}
//
//	public static JScrollPane createScrollPane( JComponent component ) {
//		return createScrollPane( component, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
//	}
//
//	public static JScrollPane createScrollPane( JComponent component, int v, int h ) {
//		JScrollPane scroller = new XScrollPane( component, v, h );
//		scroller.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
//		return configure( null, scroller );
//	}

	private static class ObjectComparator implements Comparator<Object> {

		@Override
		public int compare( Object o1, Object o2 ) {
			if( o1 == null & o2 == null ) return 0;
			if( o1 == null & o2 != null ) return -1;
			if( o1 != null & o2 == null ) return 1;
			return o1.toString().compareTo( o2.toString() );
		}

	}

}
