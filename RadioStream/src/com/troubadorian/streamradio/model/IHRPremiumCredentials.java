package com.troubadorian.streamradio.model;

//**
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpRequest;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHttpRequest;

import com.troubadorian.streamradio.client.model.IHRHashtable;
import com.troubadorian.streamradio.client.services.IHRService;

public class IHRPremiumCredentials {
	public static final long	kPreferenceKeyPremiumCredentials = 0x4948525043726564L;	//	IHRPCred
	
	public static IHRPremiumCredentials	sShared;
	
	public static final String	kSiteMailerDelegate = "mailer_delegate";	//	iheartmailer:4qe892o
	
	public static final String	keyUsername = "username";
	public static final String	keyPassword = "password";
	public static final String	keyProposed = "proposed";
	public static final String	keyExpiring = "expiring";
	public static final String	keyCredentials = "credentials";
	
	public static final String[]kFlattenKeys = { "username" , "password" , "expiring" };
	
	public Pattern 				sPatternDigestNonce = Pattern.compile( "nonce=\"([^\"]*)" );
	public Pattern				sPatternDigestRealm = Pattern.compile( "realm=\"([^\"]*)" );
	
	IHRHashtable				_internals;
	IHRHashtable				_externals;
	
	
	public static IHRPremiumCredentials shared() {
		//**
		return ( null == IHRService.g ) ? null : IHRService.g.mCredentials;
		/*/
		if ( null == sShared ) sShared = new IHRPremiumCredentials();
		
		return sShared;
		/**/
	}
	
	
	private IHRHashtable getInternals() {
		if ( null == _internals ) {
			_internals = new IHRHashtable();
		}
		
		return _internals;
	}
	
	
	private IHRHashtable getExternals() {
		if ( null == _externals ) {
			//**
			String				flat = IHRService.g.preferencesGet( "PremiumCredentials" , "" );
			/*/
			String				flat = IHRPreferences.getString( kPreferenceKeyPremiumCredentials );
			/**/
			
			_externals = new IHRHashtable();
			
			if ( null != flat && 0 != flat.length() ) {
				_externals.restoreDDS( kFlattenKeys , "\t" , flat , 0 );
			}
		}
		
		return _externals;
	}
	
	
	private void save() {
		if ( null != _externals ) {
			String				flat = _externals.flattenDDS( kFlattenKeys , "\t" );
			
			//**
			IHRService.g.preferencesPut( "PremiumCredentials" , flat ).commit();
			/*/
			IHRPreferences.set( kPreferenceKeyPremiumCredentials , flat );
			/**/
		}
	}
	
	
	public boolean hasCredentials( String inSite ) {
		IHRHashtable			internals = getInternals();
		IHRHashtable			internal = ( null == internals ) ? null : (IHRHashtable)internals.get( inSite );
		
		return !( null == internal || null == internal.get( keyCredentials ) );
	}
	
	
	public boolean hasAuthenticated( String inSite ) {
		IHRHashtable			externals = getExternals();
		IHRHashtable			external = ( null == externals ) ? null : (IHRHashtable)externals.get( inSite );
		String					password = ( null == external ) ? null : (String)external.get( keyPassword );
		String					expiring = ( null == external ) ? null : (String)external.get( keyExpiring );
		
		if ( null != external && IHRPremiumChannel.isExpired( expiring ) ) {
			externals.remove( inSite );
			save();
			
			password = null;
		}
		
		return ( null != password && password.length() > 0 );
	}
	
	
	public boolean hasAnyAuthenticated() {
		boolean					result = false;
		
		IHRHashtable			externals = getExternals();
		IHRHashtable			external;
		
		String					password;
		
		//**
		for ( String site : externals.keySet() ) {
		/*/
		for ( Enumeration keys = externals.keys() ; keys.hasMoreElements() ; ) {
			String				site = (String)keys.nextElement();
		/**/
			external = (IHRHashtable)externals.get( site );
			password = ( null == external ) ? null : (String)external.get( keyPassword );
			
			if ( null != password && password.length() > 0 ) {
				if ( IHRPremiumChannel.isExpired( (String)external.get( keyExpiring ) ) ) {
					//	remove from externals
				} else {
					result = true;
					break;
				}
			}
		}
		
		return result;
	}
	
	
	public boolean isUsing( String inSite , String inUsername , String inPassword ) {
		boolean					result = false;
		
		if ( null != inSite && null != inUsername && null != inPassword ) {
			IHRHashtable		internals = getInternals();
			IHRHashtable		internal = ( null == internals ) ? null : (IHRHashtable)internals.get( inSite );
			IHRHashtable		externals = getExternals();
			IHRHashtable		external = ( null == externals ) ? null : (IHRHashtable)externals.get( inSite );
			
			String				username;
			String				password;
			
			if ( null != internal ) {
				username = (String)internal.get( keyUsername );
				password = (String)internal.get( keyPassword );
				
				if ( null != username && null != password && username.equals( inUsername ) && password.equals( inPassword ) ) {
					result = true;
				}
			}
			
			if ( null != external ) {
				username = (String)external.get( keyUsername );
				password = (String)external.get( keyPassword );
				
				if ( null != username && null != password && username.equals( inUsername ) && password.equals( inPassword ) ) {
					result = true;
				}
			}
		}
		
		return result;
	}
	
	
	public String username( String inSite ) {
		IHRHashtable			externals = getExternals();
		IHRHashtable			external = ( null == externals ) ? null : (IHRHashtable)externals.get( inSite );
		
		return ( null == external ) ? null : (String)external.get( keyUsername );
	}
	
	/*
	 * HTTP/1.0 401 Authorization Required
	 * WWW-Authenticate: Basic realm="Secure Area"
	 * 
	 * HTTP:1.0 GET
	 * Authorization: Basic xxxxxxxxxxxxxxxxxxx==
	 * xxxxxxxxxxxxxxxxxxx== base64 encoding of username:password
	 * 
	 * returns "Basic xxxxxxxxxxxxxxxxxxx=="
	 * 
	 * 
	 * Android
	 * HttpURLConnection http = new HttpURLConnection( url );
	 * if ( HttpURLConnection.HTTP_UNAUTHORIZED == http.getResponseCode() ) {
	 *   http.addRequestProperty( "Authorization" , credentials.authorizationBasic( "site" ) );
	 * }
	 * 
	 * also Authenticator/PasswordAuthentication
	 * 
	 * 
	 * BlackBerry
	 * HttpConnection http = (HttpConnection)Connector.open( url );
	 * if ( HttpConnection.HTTP_UNAUTHORIZED == http.getResponseCode() ) {
	 *   http.setRequestProperty( "Authorization" , credentials.authorizationBasic( "site" ) );
	 * }
	 * 
	 * 
	 * */
	public static String authorizationBasic( String inUsername , String inPassword ) {
		String					result = null;
		byte[]					bytes;
		
		bytes = ( inUsername + ":" + inPassword ).getBytes();
		bytes = IHRBase64.encode( bytes , 0 , bytes.length , IHRBase64.kBase64EncodingDefault );
		
		try {
			result = "Basic " + new String( bytes , "US-ASCII" );
		} catch ( Exception e ) {}
		
		return result;
	}
	

	public String digestCredentials( String inSite, String challenge, String method, String uri ) {
		IHRHashtable						external;
		IHRHashtable						externals;
		String								ha1, ha2, ha3, nonce, password, realm, response, username;
		Matcher								match;
		
		if ( ( externals = getExternals() ) == null ) return null;
		if ( ( external = (IHRHashtable) externals.get( inSite ) ) == null ) return null;

		if ( ( username = (String) external.get( keyUsername ) ) == null || username.length() == 0 ) return null;
		if ( ( password = (String) external.get( keyPassword ) ) == null || password.length() == 0 ) return null;

		if ( ( match = sPatternDigestNonce.matcher( challenge ) ) == null || ! match.find() ) return null;
		if ( ( nonce = match.group( 1 ) ) == null ) return null;
		
		if ( ( match = sPatternDigestRealm.matcher( challenge ) ) == null || ! match.find() ) return null;
		if ( ( realm = match.group( 1 ) ) == null ) return null;

		ha1 = IHRUtilities.MD5( username + ":" + realm + ":" + password );
		ha2 = IHRUtilities.MD5( method + ":" + uri );
		ha3 = IHRUtilities.MD5( ha1 + ":" + nonce + ":" + ha2 );
		
		response =  "Digest " +
					"username=\"" + username + "\", " +
					"realm=\"" + realm + "\", " +
					"nonce=\"" + nonce + "\", " +
					"uri=\"" + uri + "\", " +
					"response=\"" + ha3.toString() + "\"";
		
		return response;
	}
	
	public String credentials( String inSite ) {
		if ( inSite == null ) return null;
		
		String					result = null;
		
		IHRHashtable			internals = getInternals();
		IHRHashtable			internal = ( null == internals ) ? null : (IHRHashtable)internals.get( inSite );
		
		if ( null != internal ) {
			result = (String)internal.get( keyCredentials );
		}
		
		if ( null == result ) {
			IHRHashtable		externals = getExternals();
			IHRHashtable		external = ( null == externals ) ? null : (IHRHashtable)externals.get( inSite );
			String				username = null;
			String				password = null;
			String				expiring = null;
			
			if ( null != external ) {
				username = (String)external.get( keyUsername );
				password = (String)external.get( keyPassword );
				expiring = (String)external.get( keyExpiring );
			}
			
			if ( null != external && IHRPremiumChannel.isExpired( expiring ) ) {
				externals.remove( inSite );
				save();
				
				password = null;
			}
			
			if ( null != username && username.length() > 0 && null != password && password.length() > 0 ) {
				result = authorizationBasic( username , password );
				
				if ( null != result ) {
					if ( null == internal ) internal = new IHRHashtable();
					
					internal.remove( keyProposed );
					internal.put( keyCredentials , result );
					internal.put( keyUsername , username );
					internal.put( keyPassword , password );
				}
			}
		}
		
		if ( null == result && inSite == kSiteMailerDelegate ) {
			//	credentials for mailing through channel delegate url
			result = authorizationBasic( "iheartmailer" , "4qe892o" );
		}
		
		return result;
	}
	
	
	public void propose( String inSite , String inUsername , String inPassword , String inExpiring ) {
		IHRHashtable			internals = getInternals();
		IHRHashtable			externals = getExternals();
		IHRHashtable			internal = ( null == internals ) ? null : (IHRHashtable)internals.get( inSite );
		IHRHashtable			external = ( null == externals ) ? null : (IHRHashtable)externals.get( inSite );
		
		String					password = null;
		
		if ( null != inExpiring && 0 != inExpiring.length() ) {
			if ( IHRPremiumChannel.isExpired( inExpiring ) ) return;	//	already expired
			
			password = ( null == external ) ? null : (String)external.get( keyPassword );
			if ( null != password && password.length() > 0 && null == external.get( keyExpiring ) ) return;
			
			password = ( null == internal ) ? null : (String)internal.get( keyPassword );
			if ( null != password && password.length() > 0 && null == internal.get( keyExpiring ) ) return;
		}
		
		if ( null == external ) {
			external = new IHRHashtable();
			externals.put( inSite , external );
		}
		
		if ( null != inExpiring && null != inUsername && null != inPassword && 0 != inExpiring.length() && 0 != inUsername.length() && 0 != inPassword.length() ) {
			external.put( keyUsername , inUsername );
			external.put( keyPassword , inPassword );
			external.put( keyExpiring , inExpiring );
			externals.put( inSite , external );
			internals.remove( inSite );
			
			save();
			return;
		}
		
		if ( null == internal ) {
			internal = new IHRHashtable();
			internals.put( inSite , internal );
		}
		
		if ( null != inUsername && null != inPassword && 0 != inUsername.length() && 0 != inPassword.length() ) {
			internal.put( keyProposed , "1" );
			internal.put( keyPassword , inPassword );
			internal.put( keyCredentials , authorizationBasic( inUsername , inPassword ) );
		} else {
			internal.remove( keyProposed );
			internal.remove( keyPassword );
			internal.remove( keyCredentials );
		}
		
		if ( null != inUsername ) {
			internal.put( keyUsername , inUsername );
			external.put( keyUsername , inUsername );
		} else {
			internal.remove( keyUsername );
			external.remove( keyUsername );
		}
		
		if ( null != external.get( keyPassword ) ) {
			external.remove( keyPassword );
			external.remove( keyExpiring );
			save();
		}
	}
	
	
	public void accept( String inSite , boolean inAccepted ) {
		IHRHashtable			internals = getInternals();
		IHRHashtable			internal = ( null == internals ) ? null : (IHRHashtable)internals.get( inSite );
		String					proposed = ( null == internal ) ? null : (String)internal.get(  keyProposed );
		
		if ( !inAccepted || ( null != proposed && proposed.equals( "1" ) ) ) {
			IHRHashtable		externals = getExternals();
			IHRHashtable		external = ( null == externals ) ? null : (IHRHashtable)externals.get( inSite );
			
			String				username = null;
			String				password = null;
			
			if ( null != internal && inAccepted ) {
				username = (String)internal.get( keyUsername );
				password = (String)internal.get( keyPassword );
			}
			
			if ( null == external ) {
				external = new IHRHashtable();
			}
			
			if ( null == username || 0 == username.length() ) {
				external.remove( keyUsername );
			} else {
				external.put( keyUsername , username );
			}
			
			if ( null == password || 0 == password.length() ) {
				external.remove( keyPassword );
				external.remove( keyExpiring );
			} else {
				external.put( keyPassword , password );
			}
			
			externals.put( inSite , external );
			internals.remove( inSite );
			save();
		}
	}
	
	
	public void onCreate( Object inOwner ) {}
	public void onDestroy( Object inOwner ) { save(); }
	
}
