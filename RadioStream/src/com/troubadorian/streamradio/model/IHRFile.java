package com.troubadorian.streamradio.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.StatFs;

public class IHRFile {
	private static final long	serialVersionUID	= 1L;
	
	//**
	public File					_file;
	/*/
	public FileConnection		_file;
	
	public static FileConnection connectionForPath( String inPath ) throws IOException {
		return (FileConnection)Connector.open( "file:///" + inPath );
	}
	
	public static FileConnection connectionForPath( String inPath , int inMode ) throws IOException {
		return (FileConnection)Connector.open( "file:///" + inPath , inMode );
	}
	/**/
	
	public static String separator() {
		//**
		return File.separator;
		/*/
		return System.getProperty( "file.separator" );
//		return "/";
		/**/
	}
	
	public static IHRFile openForPath( String inPath ) {
		return new IHRFile( inPath );
	}
	
	//**
	public static boolean deleteFolder( File inFile , int inStartingDepth ) {
		if ( inFile.isDirectory() ) {
			File[]				list = inFile.listFiles();
			int					index , count = null == list ? 0 : list.length;
			
			for ( index = 0 ; index < count ; ++index ) {
				deleteFolder( list[index] , inStartingDepth - 1 );
			}
		}
		
		return ( inStartingDepth > 0 ) ? true : inFile.delete();
	}
	/*/
	public static boolean deleteFolder( FileConnection inFile , int inStartingDepth ) {
		boolean					result = true;
		
		if ( inFile.isDirectory() ) {
			try {
				Enumeration			list = inFile.list( "*" , true );
				
				while ( list.hasMoreElements() ) {
					inFile.setFileConnection( (String)list.nextElement() );
					
					deleteFolder( inFile , inStartingDepth - 1 );
				}
				
				inFile.setFileConnection( ".." );
			} catch ( Exception e ) {
				result = false;
			}
		}
		
		if ( result && !( inStartingDepth > 0 ) ) {
			try {
				inFile.delete();
			} catch ( Exception e ) {
				result = false;
			}
		}
		
		return result;
	}
	/**/
	
	public static boolean deleteFolder( String inPath , int inStartingDepth ) {
		boolean					result = false;
		
		//**
		result = deleteFolder( new File( inPath ) , inStartingDepth );
		/*/
		IHRFile					file = new IHRFile( inPath );
		
		result = file.deleteFolder( inStartingDepth );
//		result = file.delete();
		
		file.close();
		/**/
		
		return result;
	}
	
	public static long freeSpace( String inPath ) {
		long					result = 0;
		
		//**
		StatFs					stat = new StatFs( inPath );
		
		if ( null != stat ) {
			result = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
		}
		/*/
		try {
			FileConnection		file = connectionForPath( inPath );
			
			result = file.availableSize();
			
			//file.close();
		} catch( Exception e ) {}
		/**/
		
		return result;
	}
	
	public static double freeRatio( String inPath ) {
		double					result = 0;
		
		//**
		StatFs					stat = new StatFs( inPath );
		
		if ( null != stat ) {
			result = (double)stat.getAvailableBlocks() / (double)stat.getBlockCount();
		}
		/*/
		try {
			FileConnection		file = connectionForPath( inPath );
			
			result = (double)file.availableSize() / (double)file.totalSize();
			
			//file.close();
		} catch( Exception e ) {}
		/**/
		
		return result;
	}
	
	//**
	public static File[] listRoots() {
		return File.listRoots();
	}
	/*/
	public static Enumeration listRoots() {
		return FileSystemRegistry.listRoots();
	}
	
	public static void createDirectoriesInPath( String inPath ) throws IOException {
		int						found = inPath.lastIndexOf( '/' );
		
		if ( found > 0 ) {
			String				path = inPath.substring( 0 , found );
			FileConnection		file;
			
			file = connectionForPath( path + seprator() , Connector.READ_WRITE );
			
			if ( !file.isDirectory() ) {
				createDirectoriesInPath( path );
				
				file.mkdir();
			}
			
			file.close();
		}
	}
	/**/
	
	//**
	public IHRFile( File inFile ) { _file = inFile; }
	/*/
	public IHRFile( FileConnection inFile ) { _file = inFile; }
	/**/
	
	public IHRFile() {}
	public IHRFile( String inPath ) { open( inPath ); }
	public IHRFile( String inPath , boolean inWritable ) { open( inPath , inWritable ); }
	
	public boolean open( String inPath ) {
		return open( inPath , false );
	}
	
	public boolean open( String inPath , boolean inWritable ) {
		close();
		
		//**
		_file = new File( inPath );
		/*/
		try { _file = inWritable ? connectionForPath( inPath , Connector.READ_WRITE ) : connectionForPath( inPath ); } catch ( Exception e ) {}
		/**/
		
		return ( null != _file );
	}
	
	public boolean openCreatingParents( String inPath ) {
		close();
		
		//**
		File					file = new File( inPath );
		File					parent = file.getParentFile();
		
		if ( null != parent ) {
			parent.mkdirs();
		}
		
		_file = file;
		/*/
		try {
			createDirectoriesInPath( inPath );
			
			_file = connectionForPath( inPath , Connector.READ_WRITE );
		} catch ( Exception e ) {}
		/**/
		
		return ( null != _file );
	}
	
	public boolean openParent() {
		//**
		if ( null != _file ) _file = _file.getParentFile();
		/*/
		if ( null != _file ) {
			if ( _file.isDirectory() ) {
				_file.setFileConnection( ".." );
			} else {
				String			path = _file.getAbsolutePath();
				int				last = path.lastIndexOf( '/' );
				
				if ( last > 0 && last == path.length() - 1 ) {
					last = path.lastIndexOf( '/' , last - 1 );
				}
				
				if ( last < 0 ) close();
				else open( path.substring( 0 , last + 1 ) );
			}
		}
		/**/
		
		return ( null != _file );
	}
	
	public boolean openChild( String inName ) {
		boolean					result = false;
		
		if ( null != _file ) {
			//**
			_file = new File( _file , inName );
			/*/
			_file.setFileConnection( inName );
			/**/
			
			result = ( null != _file );
		}
		
		return result;
	}
	
	public void close() {
		//**
		
		/*/
		if ( null != _file ) {
			try { _file.close(); } catch( Exception e ) {}
		}
		/**/
		
		_file = null;
	}
	
	public String getName() {
		return _file.getName();
	}
	
	public String getPath() {
		return _file.getPath();
	}
	
	public boolean isDirectory() {
		return _file.isDirectory();
	}
	
	public boolean isDirectoryEmpty() {
		//**
		String[]				list = ( null == _file ) ? null : _file.list();
		
		return ( null == list || 0 == list.length );
		/*/
		Enumeration				list = ( null == _file ) ? null : _file.list();
		
		return ( null == list || !list.hasMoreElements() );
		/**/
	}
	
	public boolean canRead() {
		return _file.canRead();
	}
	
	public boolean canWrite() {
		return _file.canWrite();
	}
	
	public boolean exists() {
		return _file.exists();
	}
	
	public boolean mkdir() {
		//**
		return _file.mkdir();
		/*/
		boolean					result = true;
		
		try {
			_file.mkdir();
		} catch ( Exception e ) {
			result = false;
		}
		
		return result;
		/**/
	}
	
	public boolean delete() {
		//**
		return _file.delete();
		/*/
		boolean					result = true;
		
		try {
			_file.delete();
		} catch ( Exception e ) {
			result = false;
		}
		
		return result;
		/**/
	}
	
	public boolean deleteFolder( int inStartingDepth ) {
		return deleteFolder( _file , inStartingDepth );
	}
	
	public boolean deleteWithEmptyParent() {
		boolean					result = delete();
		
		//**
		File					parent = _file.getParentFile();
		String[]				list = ( null == parent ) ? null : parent.list();
		
		if ( null != list && 0 == list.length ) {
			parent.delete();
		}
		/*/
		try {
			_file.setFileConnection( ".." );
			
			if ( !_file.list().hasMoreElements() ) {
				_file.delete();
			}
		} catch ( Exception e ) {}
		/**/
		
		return result;
	}
	
	public long fileSize() throws IOException {
		//**
		return _file.length();
		/*/
		return _file.fileSize();
		/**/
	}
	
	public InputStream openInputStream() throws FileNotFoundException {
		InputStream				result = null;
		
		if ( null != _file && !_file.isDirectory() ) {
			//**
			result = new FileInputStream( _file );
			/*/
			try { result = _file.openInputStream(); } catch ( Exception e ) {}
			/**/
		}
		
		return result;
	}
	
	public OutputStream openOutputStream( boolean inAppend ) throws FileNotFoundException {
		OutputStream			result = null;
		
		if ( null != _file && !_file.isDirectory() ) {
			//**
			result = ( inAppend && _file.exists() ) ? new FileOutputStream( _file , inAppend ) : new FileOutputStream( _file );
			/*/
			try {
				if ( !_file.exists() ) {
					_file.create();
				}
				
				result = ( inAppend && _file.exists() ) ? _file.openOutputStream( _file.fileSize() ) : _file.openOutputStream();
			} catch ( Exception e ) {}
			/**/
		}
		
		return result;
	}
	
	public OutputStream openOutputStream( int inOffset ) throws IOException {
		//**
		FileOutputStream		result = new FileOutputStream( _file , inOffset > 0 );
		
		if ( inOffset != result.getChannel().position() ) {
			result.getChannel().position( inOffset );
		}
		
		return result;
		/*/
		if ( null != _file && !_file.exists() ) {
			_file.create();
		}
		
		return _file.openOutputStream( inOffset );
		/**/
	}
	
	public void setFileConnection( String inPath ) {
		//**
		if ( inPath.equals( ".." ) ) {
			_file = _file.getParentFile();
		} else if ( inPath.startsWith( File.separator ) ) {
			_file = new File( inPath );
		} else {
			_file = new File( _file.getAbsolutePath() + inPath );
		}
		/*/
		if ( inPath.indexOf( "/" ) < 0 ) {
			_file.setFileConnection( inPath );
		} else {
			//_file.close();
			_file = connectionForPath( inPath );
		}
		/**/
	}
	
	public byte[] data() {
		byte[]					result = null;
		
		if ( null != _file && _file.exists() ) try {
			long				length = fileSize();
			InputStream			stream = null;
			
			if ( length > 0 && length < Integer.MAX_VALUE ) {
				stream = openInputStream();
			}
			
			if ( null != stream ) {
				result = new byte[(int)length];
				
				stream.read( result );
				stream.close();
			}
		} catch ( Exception e ) { result = null; }
		
		return result;
	}
	
	public boolean write( byte[] inContents , boolean inAppend ) {
		boolean					result = false;
		
		if ( null != _file && !_file.isDirectory() ) try {
			OutputStream		stream = openOutputStream( inAppend );
			
			if ( null != stream ) {
				stream.write( inContents );
				stream.close();
				
				result = true;
			}
		} catch ( Exception e ) {}
		
		return result;
	}
	
	public String stringContents( String inEncoding ) {
		String					result = null;
		byte[]					data = data();
		
		if ( null == data ) result = null;
		else if ( null == inEncoding ) result = new String( data );
		else try { result = new String( data , inEncoding ); } catch( Exception e ) {}
		
		return result;
	}
	
	public boolean renameToPathInSameDirectory( String inPath ) {
		boolean					result = true;
		
		//**
		File					file = new File( inPath );
		
		if ( file.exists() ) file.delete();
		
		result = _file.renameTo( file );
		/*/
		try {
			FileConnection			file = connectionForPath( inPath , Connector.READ_WRITE );
			String					name = file.getName();
			
			if ( file.exists() ) file.delete();
			
			file.close();
			
			_file.rename( name );
		} catch ( Exception e ) {
			result = false;
		}
		/**/
		
		return result;
	}
	
}
