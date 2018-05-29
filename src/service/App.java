package service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import util.FileUtils;
import util.FileUtils.FileOperatorType;
import util.LocalDataHelper;
import util.Logger;
import util.Paths;

public class App {
	
	
	private  static App app=null;
	public static void main(String[] args){
		//初始化路径
		App.getInstance().setStartPath();
		if(args.length==4){
			Context.VersionServerIp=args[0];
			Context.VersionPort=args[1];
			Context.appID=args[2];
			Context.versionID=args[3];
			Logger.log("入参信息："+Context.VersionServerIp+"|"+Context.VersionPort+"|"+Context.appID+"|"+Context.versionID);
			App.getInstance().runNetwork();
		}else{
			Logger.log("自动版本更新服务接受的调用参数不正确："+args.toString());
		}
	}
	
	private  void setStartPath(){
		 String path="";
		 try {
				path= System.getProperty("user.dir");//这种不支持中文路径吧
				String pathClass= URLDecoder.decode(this.getClass().getClassLoader().getResource("service/App.class").getPath(),"UTF-8");
				Context.StartPath=path;
			} catch (Exception e) {
				e.printStackTrace();
			}
	 }
	
	//建立连接发送版本包请求消息
	private void runNetwork(){
		Socket socket=null;
		DataOutputStream dos=null;
		DataInputStream dis=null;
		FileOutputStream fos=null;
		try{
			int portNo=Integer.parseInt(Context.VersionPort);
			socket=new Socket(Context.VersionServerIp, portNo);
			dos=new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(Context.GFT_CHECKER);//校验码
			dos.flush();
			String msg=Context.appID+"|"+Context.versionID;
			dos.writeLong(msg.getBytes().length);//消息长度
			dos.flush();
			dos.writeUTF(msg);//消息内容
			Logger.log("发送版本包更新请求："+msg);
			//没有出现异常的话，开始网络文件接收
			 dis =new DataInputStream(socket.getInputStream());
			  int streamLen=0;
			  //休眠2秒钟给版本服务器反应(局域网环境下应该足够了)
              if((streamLen=dis.available())<=0){
             	  Thread.sleep(2000);
              }
              boolean flag=true;
              boolean reentrantFlag=true;
              long fileCount=0;
              while(flag){
             	 if(dis.available()>0&&reentrantFlag){
             		 reentrantFlag=false;
             		 //校验协议和文件数目
             		 String checksum=dis.readUTF();
             		 if(Context.GFT_CHECKER.equals(checksum)){
             			 fileCount=dis.readLong();
             		 }else{//别的网络协议也使用这个网络端口
             			 flag=false;
             			 break;
             		 }
             	 }
             	//读取流里面的若干文件
            	 long currFileNum=0;
             	 while((streamLen=dis.available())>0){
		                 String fileName=dis.readUTF();
		                 String path=dis.readUTF();
		                 String md5=dis.readUTF();
		                 path=path.replace("$BASEDIR", Paths.getInstance().getBasePath());
		                 path=FileUtils.formatPath(path);
		                 long time=dis.readLong();
		                 long fileSize=dis.readLong();
		                 File file=new File(path+File.separator+fileName);
		                 File dir=new File(path);
		                 if(!dir.exists()){
		                	 dir.mkdirs();
		                 }
		                 //旧版本文件的删除
		                 if(file.exists()){
		                	 file.delete();
		                 }
		                 Logger.log("开始接收版本文件:"+file.getAbsolutePath());
		                 fos =new FileOutputStream(file);
		                 byte[] sendBytes=new byte[Context.BufferSize];
		                 int read =0;
		                 long recvSize=0;
		                 long leftSize=fileSize;
		                 
		                 while(recvSize<fileSize){
		                	 if(leftSize>=Context.BufferSize){
		                		 read = dis.read(sendBytes);
		                		 fos.write(sendBytes,0, read);
		                         fos.flush();
		                	 }else{
		                		 byte[] leftBytes =new byte[(int)leftSize];
		                		 read = dis.read(leftBytes);
		                		 fos.write(leftBytes,0, read);
		                         fos.flush();
		                	 }
		                	 leftSize-=read;
		                	 recvSize+=read;
		                 } 
		                 //单个文件接收完成
		                 if(fos !=null){
				                fos.close();
			                 }
			             file.setLastModified(time); 
			             String newMd5=FileUtils.getMd5ByPath(file.getAbsolutePath());
		                 if(!md5.equals(newMd5)){
		                	 //网络流中写日志文件会影响网络传输
		                	 System.err.println(" receive file("+fileName+") error the check of md5 is not equal");
		                 }
		                 currFileNum++;  
             	 }
             	 if(fileCount>currFileNum){
             		 Thread.sleep(100);
             		 flag=true;
             	 }else{
             		 if(dis !=null)
   	 	                dis.close();
             		  if(socket!=null){
             			 socket.close();
             		  }
             		 flag=false; 
             	 }
              } 
              //版本文件处理
              App.getInstance().versionProcess();
              //启动app才能退出
              App.getInstance().startApp(Context.appID);
		}catch(Exception e){
			Logger.log("更新版本网络传输文件发生异常："+e.toString());
		}
		finally{
			try{
				if(dos !=null)
	                dos.close();
				socket.close();
			}catch(Exception e){
				Logger.log("更新版本网络传输释放资源发生异常："+e.toString());
			}
    	}
	}
	
	private void versionProcess(){
		Logger.log("版本包"+Context.versionID+"文件接收成功完成");
		String dir= Paths.getInstance().getBasePath()+File.separator+Context.versionID;
		List<File> files=new ArrayList<File>();
		FileUtils.getFileList(files, dir);
		if(files!=null&&files.size()>0){
			for(File file:files){
				String path=file.getAbsolutePath();
				String tempPath=path.replace(File.separatorChar+Context.versionID, "");
				tempPath=FileUtils.formatPath(tempPath);
				tempPath=tempPath.substring(0, tempPath.lastIndexOf(File.separatorChar));
				tempPath=FileUtils.formatPath(tempPath);
				FileUtils.moveOrCopy(file.getAbsolutePath(), tempPath, FileOperatorType.Copy);
			}
			Logger.log("版本包"+Context.versionID+"文件替换到目录"+Paths.getInstance().getBasePath()+"完成");
			//文件替换后，删除最新的版本包(含目录)
			FileUtils.deleteFile(dir);
			Logger.log("版本包"+dir+"文件清理完成");
			LocalDataHelper.updateParameters("CurrentVersion", Context.versionID);
			Logger.log("更新本地app版本标识完成");
		}
	}
	
	

	private  void startApp(String exeName){
		try{
			Logger.log("开始重启应用"+exeName);
			Runtime.getRuntime().exec(exeName);
		}catch(Exception e){
			Logger.log("更新版本后启动"+exeName+"发生异常："+e.toString());
		}
	}
	
	private App(){}
	
	public static App getInstance(){
		if(app==null)
			app=new App();
		return app;
	}
	
	
}
