package util;

public class LocalDataHelper {
	  public static void updateParameters(String id,String value){
			 String updater=" UPDATE PARAMETERS SET VALUE='@VALUE',TIME='@TIME' WHERE NAME='@NAME'";
			 updater=updater.replace("@VALUE", value);
			 updater=updater.replaceAll("@TIME",DateUtil.getCurrentTime());
			 updater=updater.replaceAll("@NAME", id);
			 Logger.log("execute sql:"+updater);
			 LocalDBUtil.update(updater);
		  }
}
