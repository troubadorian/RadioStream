package com.troubadorian.streamradio.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IHRXMLParser extends DefaultHandler {
	protected String			mContents;
	
	public IHRXMLParser() {}
	public IHRXMLParser( byte[] inBytes ) throws IOException, SAXException, ParserConfigurationException { parse( inBytes ); }
	
	public static void parse( InputStream inStream , DefaultHandler inHandler ) throws IOException, SAXException, ParserConfigurationException {
		//**
		SAXParserFactory		factory = SAXParserFactory.newInstance();
		SAXParser				parser = null;
		
			parser = factory.newSAXParser();
			parser.parse( inStream , inHandler );
		/*/
		( new XMLParser() ).parse( inStream , inHandler );
		/**/
	}
	
	public void parse( byte[] inBytes ) throws IOException, SAXException, ParserConfigurationException {
		mContents = "";
		
		parseEnter();
		IHRXMLParser.parse( new ByteArrayInputStream( inBytes ) , this );
		parseLeave();
	}
	
	public void parseEnter() {}
	public void parseLeave() {}
	
	public void endElement( String name ) {}
	public void startElement( String name ) { mContents = ""; }
	
	@Override
	public void characters( char[] ch, int start, int length ) {
		mContents += String.valueOf( ch, start, length );
	}
	
	@Override
	public void endElement( String uri, String localName, String qName ) {
		endElement( localName );
	}
	
	@Override
	public void startElement( String uri, String localName, String qName, Attributes attributes ) {
		startElement( localName );
	}
	
}
