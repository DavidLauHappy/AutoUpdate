package util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;


public class Logger {
	//记录通用日志的方法
		public static void log(String info){
			try{
				String logDir=Paths.getInstance().getLogPath()+DateUtil.getCurrentDate("yyyyMMdd");
			    File dir=new File(logDir);
			    if(!dir.exists())
			    	dir.mkdirs();
			    String logpath=logDir+File.separator+DateUtil.getCurrentDate("yyyy-MM-dd")+".log";
			    String currentTime=DateUtil.getCurrentDate("yyyyMMdd-HH:mm:ss.SSS");
				 File file=new File(logpath);
				   FileWriter fw = new FileWriter(file,true);
				   BufferedWriter bw = new BufferedWriter(fw);
				   bw.write(currentTime);
				   bw.write(":");
				   bw.write(info);
				   bw.write("\r\n");
				   bw.close();
				   fw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
}
