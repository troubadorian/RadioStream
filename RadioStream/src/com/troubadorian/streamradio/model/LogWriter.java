package com.troubadorian.streamradio.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import android.os.Environment;

public class LogWriter {
	
	String OUTPUT_FILE = "logData.txt";
	
	public LogWriter() {
		
	}

	public void write(String string) {
		try {
			File file = new File(Environment.getExternalStorageDirectory(), OUTPUT_FILE);
			FileOutputStream fos = new FileOutputStream(file,true);
			OutputStreamWriter writer = new OutputStreamWriter(fos);

			writer.write(string);
			writer.close();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(string);
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
