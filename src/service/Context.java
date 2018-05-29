package service;

public class Context {
		//版本服务器信息
	   public static String VersionServerIp="";//版本服务器的ip地址
       public static String  VersionPort="";//版本同步的端口
       
       ////////////////////////应用信息
       public static String appID="";
       public static String versionID="";
       
       public static String StartPath="";
       //网络传输校验码
       public static final String GFT_CHECKER="10101010101010101010";
       //网络缓存空间
       public static final int BufferSize=1024;
}
