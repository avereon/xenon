package com.xeomar.xenon;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public final class FileUtil {

	public static final long KB = 1000;

	public static final long MB = KB * 1000;

	public static final long GB = MB * 1000;

	public static final long TB = GB * 1000;

	public static final long PB = TB * 1000;

	public static final long EB = PB * 1000;

	public static final long KiB = 1L << 10;

	public static final long MiB = 1L << 20;

	public static final long GiB = 1L << 30;

	public static final long TiB = 1L << 40;

	public static final long PiB = 1L << 50;

	public static final long EiB = 1L << 60;

	public static final File TEMP_FOLDER = new File( System.getProperty( "java.io.tmpdir" ) );

	public static final FileFilter FOLDER_FILTER = new FolderFilter();

	public static final FileFilter JAR_FILE_FILTER = new JarFileFilter();

	/**
	 * @deprecated Just use FilenameUtils.getExtension()
	 */
	@Deprecated
	public static String getExtension( File file ) {
		return file == null ? null : getExtension( file.getName() );
	}

	/**
	 * @deprecated Just use FilenameUtils.getExtension()
	 */
	@Deprecated
	public static String getExtension( String name ) {
		return FilenameUtils.getExtension( name );
	}

	/**
	 * Get a human readable string using orders of magnitude in base-10.
	 *
	 * @param size
	 * @return
	 */
	public static String getHumanSize( long size ) {
		int exponent = 0;
		long coefficient = size;
		while( coefficient >= KB ) {
			coefficient /= KB;
			exponent++;
		}

		String unit = "B";
		switch( (int)exponent ) {
			case 1: {
				unit = "KB";
				break;
			}
			case 2: {
				unit = "MB";
				break;
			}
			case 3: {
				unit = "GB";
				break;
			}
			case 4: {
				unit = "TB";
				break;
			}
			case 5: {
				unit = "PB";
				break;
			}
			case 6: {
				unit = "EB";
				break;
			}
		}

		// Should be, at most, five characters long; three numbers, two units.
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= MB ) {
				precise /= KB;
			}
			return String.format( "%3.1f", (float)precise / KB ) + unit;
		}

		return String.valueOf( coefficient ) + unit;
	}

	/**
	 * Get a human readable string using orders of magnitude in base-2.
	 *
	 * @param size
	 * @return
	 */
	public static String getHumanBinSize( long size ) {
		int exponent = 0;
		long coefficient = size;
		while( coefficient >= KiB ) {
			coefficient /= KiB;
			exponent++;
		}

		String unit = "B";
		switch( (int)exponent ) {
			case 1: {
				unit = "KiB";
				break;
			}
			case 2: {
				unit = "MiB";
				break;
			}
			case 3: {
				unit = "GiB";
				break;
			}
			case 4: {
				unit = "TiB";
				break;
			}
			case 5: {
				unit = "PiB";
				break;
			}
			case 6: {
				unit = "EiB";
				break;
			}
		}

		// Should be, at most, seven characters long; four numbers, three units.
		if( exponent > 0 && coefficient < 10 ) {
			long precise = size;
			while( precise >= MiB ) {
				precise /= KiB;
			}
			return String.format( "%3.1f", (float)precise / KiB ) + unit;
		}

		return String.valueOf( coefficient ) + unit;
	}

	public static File removeExtension( File file ) {
		if( file == null ) return null;
		return new File( removeExtension( file.toString() ) );
	}

	public static String removeExtension( String name ) {
		if( name == null ) return null;
		int index = name.lastIndexOf( '.' );
		if( index < 0 ) return name;
		return name.substring( 0, index );
	}

	/**
	 * Create a temporary folder. If there is a problem creating the folder this
	 * method will return null.
	 *
	 * @param prefix
	 * @param suffix
	 * @return
	 * @throws IOException
	 */
	public static File createTempFolder( String prefix, String suffix ) throws IOException {
		File file = File.createTempFile( prefix, suffix );
		if( !file.delete() ) return null;
		if( !file.mkdir() ) return null;
		return file;
	}

	/**
	 * Create a temporary folder. If there is a problem creating the folder this
	 * method will return null.
	 *
	 * @param prefix
	 * @param suffix
	 * @param parent
	 * @return
	 * @throws IOException
	 */
	public static File createTempFolder( String prefix, String suffix, File parent ) throws IOException {
		File file = File.createTempFile( prefix, suffix, parent );
		if( !file.delete() ) return null;
		if( !file.mkdir() ) return null;
		return file;
	}

	public static boolean isWritable( File file ) {
		if( !file.isDirectory() ) return file.canWrite();

		try {
			File privilegeCheckFile = new File( file, "privilege.check.txt" );
			return privilegeCheckFile.createNewFile() && privilegeCheckFile.delete();
		} catch( IOException exception ) {
			return false;
		}
	}

}
