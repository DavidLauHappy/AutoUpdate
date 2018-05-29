package util;

import java.io.InputStream;

public class ShellUtil {
	public static boolean command(String cmdline) {		
		return callCmd(cmdline);
	}

	private static boolean callCmd(String cmdline) {
		Runtime app = Runtime.getRuntime();
		Process proc = null;
		try {
			proc = app.exec(cmdline);
			new Thread(new SubTask(proc.getInputStream())).start();
			new Thread(new SubTask(proc.getErrorStream())).start();			
			proc.waitFor();
			InputStream errStream = proc.getErrorStream();
			int size = errStream.available();
			if (size == 0) {
				app.runFinalization();
				return true;
			} else {
				StringBuffer errMsg = new StringBuffer();
				byte[] bytes = new byte[size];
				while ((errStream.read(bytes, 0, bytes.length)) != -1) {
					errMsg = errMsg.append(new String(bytes));
    			}
				System.out.print("执行命令["+cmdline+"]时捕获到错误流信息："+errMsg);
				Logger.log("执行命令["+cmdline+"]时捕获到错误流信息："+errMsg);
			    return false;
			} 
		} catch (Exception e) {
			System.out.print("执行命令["+cmdline+"]时发生错误："+e.getMessage());
			Logger.log("执行命令["+cmdline+"]时发生错误："+e.getMessage());
			return false;			
		}
	}
}
