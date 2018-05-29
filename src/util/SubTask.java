package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SubTask implements Runnable {
	  public SubTask(InputStream istream)  {
		  if (istream == null)
				inputStream=System.in;
			else
				inputStream=istream;
		  iReader = new BufferedReader(new InputStreamReader(inputStream));

	  }
	  
	  private String input;
	  public void run() {
		try {
			 String line = null;;
			  while ((line=iReader.readLine())!= null)
			  {
				  input =input+ line;
			  }
		}
		catch (IOException ioe) {
			Logger.log("SubTask run error:"+ioe.getMessage());
		}
	  }
	  public String getInput() {
			return input;
		 }
	  public InputStream getInputStream() {
			return inputStream;
		}
	  private BufferedReader iReader;
	  private InputStream inputStream;
}
