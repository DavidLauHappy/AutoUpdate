package util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalDBUtil {
	private  static Connection conn;
	public static Connection getLocalConnection()
	{
	   if(conn==null)
	   {
		   conn=createConnect();
	   }
	   return conn;
	}
	
	private  static Connection createConnect()
	{
		  // JDBC驱动
		  String classname = "org.sqlite.JDBC";
		  String path=FileUtils.formatPath(Paths.getInstance().getBasePath())+File.separator+"data"+File.separator+"data.db";
		  // 数据库地址
		  String URL = "jdbc:sqlite:"+path;
				try {
					// 加载JDBC驱动
					Class.forName(classname);
					// 连接数据库
					conn = DriverManager.getConnection(URL);
				} 
				catch (Exception e) 
				{
					Logger.log("establish local database connection error:"+e.toString());
					e.printStackTrace();
				}
		  return conn;
	 }
	
	public synchronized static boolean update(String sql){
		Logger.log("execute sequece sql:="+sql);
		int ret =0;
		try {
			if(conn==null){
				conn=	createConnect();
			}
			 Statement stmt=conn.createStatement();
			 ret=stmt.executeUpdate(sql);
			 //conn.commit();
			 if(ret==1 )
			 return true;
		} catch (SQLException e) {
			Logger.log("update local database  error:"+e.toString());
			e.printStackTrace();
		}		
        return false;
	}
}
