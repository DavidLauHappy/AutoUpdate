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
		//��ʼ��·��
		App.getInstance().setStartPath();
		if(args.length==4){
			Context.VersionServerIp=args[0];
			Context.VersionPort=args[1];
			Context.appID=args[2];
			Context.versionID=args[3];
			Logger.log("�����Ϣ��"+Context.VersionServerIp+"|"+Context.VersionPort+"|"+Context.appID+"|"+Context.versionID);
			App.getInstance().runNetwork();
		}else{
			Logger.log("�Զ��汾���·�����ܵĵ��ò�������ȷ��"+args.toString());
		}
	}
	
	private  void setStartPath(){
		 String path="";
		 try {
				path= System.getProperty("user.dir");//���ֲ�֧������·����
				String pathClass= URLDecoder.decode(this.getClass().getClassLoader().getResource("service/App.class").getPath(),"UTF-8");
				Context.StartPath=path;
			} catch (Exception e) {
				e.printStackTrace();
			}
	 }
	
	//�������ӷ��Ͱ汾��������Ϣ
	private void runNetwork(){
		Socket socket=null;
		DataOutputStream dos=null;
		DataInputStream dis=null;
		FileOutputStream fos=null;
		try{
			int portNo=Integer.parseInt(Context.VersionPort);
			socket=new Socket(Context.VersionServerIp, portNo);
			dos=new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(Context.GFT_CHECKER);//У����
			dos.flush();
			String msg=Context.appID+"|"+Context.versionID;
			dos.writeLong(msg.getBytes().length);//��Ϣ����
			dos.flush();
			dos.writeUTF(msg);//��Ϣ����
			Logger.log("���Ͱ汾����������"+msg);
			//û�г����쳣�Ļ�����ʼ�����ļ�����
			 dis =new DataInputStream(socket.getInputStream());
			  int streamLen=0;
			  //����2���Ӹ��汾��������Ӧ(������������Ӧ���㹻��)
              if((streamLen=dis.available())<=0){
             	  Thread.sleep(2000);
              }
              boolean flag=true;
              boolean reentrantFlag=true;
              long fileCount=0;
              while(flag){
             	 if(dis.available()>0&&reentrantFlag){
             		 reentrantFlag=false;
             		 //У��Э����ļ���Ŀ
             		 String checksum=dis.readUTF();
             		 if(Context.GFT_CHECKER.equals(checksum)){
             			 fileCount=dis.readLong();
             		 }else{//�������Э��Ҳʹ���������˿�
             			 flag=false;
             			 break;
             		 }
             	 }
             	//��ȡ������������ļ�
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
		                 //�ɰ汾�ļ���ɾ��
		                 if(file.exists()){
		                	 file.delete();
		                 }
		                 Logger.log("��ʼ���հ汾�ļ�:"+file.getAbsolutePath());
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
		                 //�����ļ��������
		                 if(fos !=null){
				                fos.close();
			                 }
			             file.setLastModified(time); 
			             String newMd5=FileUtils.getMd5ByPath(file.getAbsolutePath());
		                 if(!md5.equals(newMd5)){
		                	 //��������д��־�ļ���Ӱ�����紫��
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
              //�汾�ļ�����
              App.getInstance().versionProcess();
              //����app�����˳�
              App.getInstance().startApp(Context.appID);
		}catch(Exception e){
			Logger.log("���°汾���紫���ļ������쳣��"+e.toString());
		}
		finally{
			try{
				if(dos !=null)
	                dos.close();
				socket.close();
			}catch(Exception e){
				Logger.log("���°汾���紫���ͷ���Դ�����쳣��"+e.toString());
			}
    	}
	}
	
	private void versionProcess(){
		Logger.log("�汾��"+Context.versionID+"�ļ����ճɹ����");
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
			Logger.log("�汾��"+Context.versionID+"�ļ��滻��Ŀ¼"+Paths.getInstance().getBasePath()+"���");
			//�ļ��滻��ɾ�����µİ汾��(��Ŀ¼)
			FileUtils.deleteFile(dir);
			Logger.log("�汾��"+dir+"�ļ��������");
			LocalDataHelper.updateParameters("CurrentVersion", Context.versionID);
			Logger.log("���±���app�汾��ʶ���");
		}
	}
	
	

	private  void startApp(String exeName){
		try{
			Logger.log("��ʼ����Ӧ��"+exeName);
			Runtime.getRuntime().exec(exeName);
		}catch(Exception e){
			Logger.log("���°汾������"+exeName+"�����쳣��"+e.toString());
		}
	}
	
	private App(){}
	
	public static App getInstance(){
		if(app==null)
			app=new App();
		return app;
	}
	
	
}
