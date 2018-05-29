package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.util.List;

public class FileUtils {
	
	 public static String formatPath(String path){
		  if (path != null && !"".equals(path)) {
				path = path.replace('/', File.separatorChar);
				path = path.replace('\\', File.separatorChar);
				while (path.endsWith(String.valueOf(File.separatorChar))) {
					path = path.substring(0, path.length() - 1);
				}
			} else {
				return "";
			}
			return path;
		}
	 
	 
	  /*递归获取某个目录下的所有文件(不含目录)的清单，支持递归*/
	  public static void getFileList(List<File> result,String startPath){
		  if(result!=null){
			  String path=formatPath(startPath);
			  File dir = new File(path);
			  if (!dir.isDirectory()){
				  result.add(dir);
			  } 
			  else{
				  File[] files=dir.listFiles();
				  for(int i=0; i<files.length; i++){
					  File currentFile=files[i];
					  if(currentFile.isDirectory()){
						  getFileList( result, currentFile.getAbsolutePath());
					  }else{
					  result.add(currentFile);
					  }
				  }
			  }
		  }
	  }
	  
	  public enum FileOperatorType {Move,Copy;}
	  /**
		 * 调用命令行参数移动文件到指定目录下
		 * 非windows系统下调用mv和cp命令移动或拷贝文件到指定目录下
		 * windows系统下要调用move和copy命令移动或拷贝文件到指定目录下
		 * optype 为1是移动文件， 其他是复制文件
		 */
	  public static boolean moveOrCopy(String filePath,String targetPath,FileOperatorType opType){
		  if(filePath==null){
			  filePath = "";
			}
			if(targetPath==null){
				targetPath= "";
			}
			String cmdline = "";
			String OsName = System.getProperty("os.name").toLowerCase();
			if (OsName.indexOf("win") != -1) {
				if(opType.equals(FileOperatorType.Move)){
					cmdline = "cmd.exe /c move /Y \"" + filePath + "\" \"" + targetPath + "\"";
				}else{
					cmdline = "cmd.exe /c copy /Y \"" + filePath + "\" \"" + targetPath + "\"";
				}
			}else{
				if(opType.equals(FileOperatorType.Move)){
					cmdline = "mv " + filePath + " " + targetPath;
				}else{
					cmdline = "cp " + filePath + " " + targetPath;
				}
			}
			Logger.log("执行shell命令："+cmdline);
			return ShellUtil.command(cmdline);
	  }
	  
	  
	  public static void deleteFile(String path){
		  File file=new File(path);
		  if(file.isFile()){
			  file.delete();
		  }else{
			  File[] files=file.listFiles();
			  if(files!=null&&files.length>0){
				 for(File curFile:files){ 
					 deleteFile(curFile.getAbsolutePath());
				 }
			  }
			 file.delete();
		  }
	  }
	  
	  
	 public  static String getMd5ByPath(String filePath){
		  FileInputStream fis = null;
			try {
				MessageDigest md = MessageDigest.getInstance("MD5");
				File file=new File(filePath);
					if(file!=null&&file.isFile()){
						fis = new FileInputStream(file);
						FileChannel fChannel = fis.getChannel();
						ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
						int length = -1;
						while ((length = fChannel.read(buffer)) != -1) {
							buffer.flip();
							md.update(buffer);
							buffer.compact();
						}
						byte[] b = md.digest();
						return byteToHexString(b);
				}else{
					return "";
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
	  }
	 
	 
	 private static char hexdigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8','9', 'a', 'b', 'c', 'd', 'e', 'f' };
	  private static  String byteToHexString(byte[] tmp) {
			String s;// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
			// 所以表示成 16 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节 转换成 16 进制字符的转换
				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexdigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换, >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexdigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串
			return s;
		}
}
