package util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
	//yyyyMMddHHmmssSSS
		public static String getCurrentDate(String form){
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat(form);
			String staticsDate= format.format(calendar.getTime());
			return staticsDate;
		}
		
		public static String getCurrentTime(){
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String staticsTime= format.format(calendar.getTime());
			return staticsTime;
		}
}
