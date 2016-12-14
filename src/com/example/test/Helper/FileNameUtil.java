package com.example.test.Helper;

import android.util.Log;

public class FileNameUtil {
        
        private static final String TAG = "FileNameUtil";
        public static final String VideoNameFilterRegex = "^VIDU*(_[0-9]{8}){3}\\.mp4";
        public static final String INDEX_TAG_COMMON = "_T";
        public static final String INDEX_TAG_LOCK = "_L";

        //从VIDA,VIDB中视频中获取编号
        public  static int  getIndexStringFromName_Common(String fileName){
                String str = null;
                int result = 0;
                if(isContainIndexNum_Common(fileName)){
                        int first = fileName.indexOf(INDEX_TAG_COMMON)+2;
                        int end = fileName.indexOf("_", first);                        
                        if(first >= 0 && end >= 0 && first < end){
                                str = fileName.substring(first,end);
                                if(str.length() == 4){
                                        try{
                                                result = Integer.valueOf(str);
                                        }catch(NumberFormatException e){
                                                Log.e(TAG, "Parse error when  getIndexNumFromName");
                                        }
                                }
                        }
                }
                return result;
        }
        
      //从LOCK中视频中获取编号
        public  static int  getIndexStringFromName_Lock(String fileName){
                String str = null;
                int result = 0;
                if(isContainIndexNum_Lock(fileName)){
                        int first = fileName.indexOf(INDEX_TAG_LOCK)+2;
                        int end = fileName.indexOf("_", first);                        
                        if(first >= 0 && end >= 0 && first < end){
                                str = fileName.substring(first,end);
                                if(str.length() == 4){
                                        try{
                                                result = Integer.valueOf(str);
                                        }catch(NumberFormatException e){
                                                Log.e(TAG, "Parse error when  getIndexNumFromName");
                                        }
                                }
                        }
                }
                return result;
        }
        
        public static String formatNumToSixByte(int num){
                return String.format("%06d", num);
        }
        
        //兼容以前版本，判断是否是有编号_T的视频(普通视频)
        public static boolean isContainIndexNum_Common(String fileName){
                return fileName.contains(INDEX_TAG_COMMON);
        }
        
      //兼容以前版本，判断是否是有编号_L的视频(上锁视频)
        public static boolean isContainIndexNum_Lock(String fileName){
                return fileName.contains(INDEX_TAG_LOCK);
        }
        
        //转移到上锁目录时，更改视频名称
        public static String commonToLock(String fileName,int lockIndexNum){
                int first = fileName.indexOf(INDEX_TAG_COMMON)+1;
                if(first == 0){
                        //如果本身就是上锁目录的路径，就直接返回
                        return fileName;
                }else{
                        int end = fileName.indexOf("_",first);
                        return fileName.replace(fileName.substring(first, end), String.format("L%04d", lockIndexNum));
                }
        }
}
