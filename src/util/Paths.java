package util;

import java.io.File;

import service.Context;

public class Paths {
	 private static Paths uniqueInstance;
	  public static Paths getInstance(){
		  	if(uniqueInstance==null)
		  		uniqueInstance=new Paths();
		  	return uniqueInstance;
	  }
	  
	  private Paths(){
		  this.basePath=FileUtils.formatPath(Context.StartPath)+File.separator;
		  this.init();
	  } 
	  
	  private void init(){
		  this.logPath=this.basePath+"log"+File.separator;
		  File logDir=new File(this.logPath);
		  if(!logDir.exists())
			  logDir.mkdirs();
	  }
	  
	  private String basePath;
	  private String logPath;
	  
	public String getBasePath() {
		return basePath;
	}

	public String getLogPath() {
		return logPath;
	}
	  
}
