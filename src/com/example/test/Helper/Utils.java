package com.example.test.Helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;

public class Utils {

		public static final String EXTENAL_SD = "/mnt/external_sdio";
		public static final String TAG = "LY_CAMERA_UTIL";
	
		public static int getLineNumber(Exception e){
				StackTraceElement[] trace =e.getStackTrace();
				if(trace==null||trace.length==0) return -1; //
				return trace[0].getLineNumber();
		}
		
		/**
		 * 获取外置SD卡路径
		 * @return	应该就一条记录或空
		 */
		public  static  List getExtSDCardPath()
		{
			List lResult = new ArrayList();
			try {
				Runtime rt = Runtime.getRuntime();
				Process proc = rt.exec("mount");
				InputStream is = proc.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line;
				while ((line = br.readLine()) != null) {
					if (line.contains("extSdCard"))
					{
						String [] arr = line.split(" ");
						String path = arr[1];
						File file = new File(path);
						if (file.isDirectory())
						{
							lResult.add(path);
						}
					}
				}
				isr.close();
			} catch (Exception e) {
			}
			return lResult;
		}
		
		public static List<String> getAllExternalPath(){
			List<String> list = new ArrayList<String>();
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
					String parent = Environment.getExternalStorageDirectory().getParent();
					File fileParent = new File(parent);
					for(String temp:fileParent.list()){
						String x = new File(temp).getAbsolutePath();
						list.add(x);
					}
			}
			return  list;
		}
		
		public static boolean isFileExist(){
				File file = new File("/mnt/external_sdio/DCIM/VID/");
				if(!file.exists()){
						file.mkdirs();
				}
				return file.exists();
		}
		
		public static boolean isFileExist(String path){
				File file = new File("/mnt/external_sdio"+"/"+path);
				return  file.exists();
		}
		
		//向文件中写入每次的初始化记录,包含了时间
		public static void writeInitRecord(String src){
				FileOutputStream fos = null;
				String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				String filePath = sdPath + "/pic/param.txt";
				File file  = new File(filePath);
				if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
						if(!file.exists() ){
								File parent = file.getParentFile();
								if(!parent.exists()){
									parent.mkdirs();
								}
						}					
						try {
								file.createNewFile();
							//表示以追加的方式写数据
								fos = new FileOutputStream(file,true);
								fos.write(src.getBytes());
								//写入换行符
								String newLine = System.getProperty("line.separator");
								fos.write(newLine.getBytes());
								fos.flush();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}finally{
									if(fos != null){
											try {
												fos.close();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
									}		
							}
				}
		}
		
		//判断文件名是否符合.3gp或.mp4格式
		public static boolean isVideoFile(String fileName){
				return (Pattern.compile(".*\\.3gp$").matcher(fileName).matches() || Pattern.compile("^VID.*\\.mp4$").matcher(fileName).matches());
		}
		
		public static void deleteInvalidVideoFiles(String filePath){
				if(TextUtils.isEmpty(filePath))
						return;
				File file = new File(filePath);
				if(needToDelete(file)){
						file.delete();
				}
				if(!file.isDirectory())
						return;
				for(File temp:file.listFiles()){
						if(temp.isDirectory()){
								deleteInvalidVideoFiles(temp.getAbsolutePath());							
						}else{
							if(needToDelete(temp))
										temp.delete();
						}
				}
		}
		
		//判断是否符合删除条件
		public static boolean needToDelete(File file){
				//Log.w("trd_check", "name------"+file.getName()+"-------length-------"+file.length()+"--------------modify-------------"+file.lastModified());
				if(file != null && file.isFile() && isVideoFile(file.getName()) && file.length() <= (2 *1024L*1204L) && (System.currentTimeMillis()-file.lastModified()) >= 6*60*1000)
						return true;
				else
						return false;
		}
		 
	  
	    public static boolean equalRate(Size s, float rate){  
	        float r = (float)(s.width)/(float)(s.height);  
	        if(Math.abs(r - rate) <= 0.03)  
	        {  
	            return true;  
	        }  
	        else{  
	            return false;  
	        }  
	    }  
	    
	    public static  long getSDTotalMemorySize() {
	        //File path = Environment.getDataDirectory();//Gets the Android data directory
	        StatFs stat = new StatFs(EXTENAL_SD);
	        long blockSize = stat.getBlockSize();      //每个block 占字节数
	        long totalBlocks = stat.getBlockCount();   //block总数
	        return totalBlocks * blockSize;
	    }
	    
		public static void launchPackage(Context context, String pkg, String cls) {
			Intent it = new Intent();
			it.setAction(Intent.ACTION_MAIN);
			it.addCategory(Intent.CATEGORY_LAUNCHER);
			it.setClassName(pkg, cls);
			it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			try {
				context.startActivity(it);
			} catch (Throwable t) {
				Log.w("LaunchUtil", "startActivity()", t);
			}
		}
		
	    public static boolean isSDExisted(){
    		boolean re = false;
    		if(Environment.getStorageState(new File(EXTENAL_SD)).equals(Environment.MEDIA_MOUNTED))
    				re = true;
    		return re;
	    }
	    
	    public static void killAppByPackage(final Context context, final String packageName) {
	        Log.d(TAG, "require close " + packageName);
	        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
	        try {
	            activityManager.forceStopPackage(packageName);
	        } catch(Exception e) {
	            Log.e(TAG, "close " + packageName + " error");
	            e.printStackTrace();
	        }
	    }
	    
	    //判断是否要转移
	    public static boolean  isFitTomove(int maxTime,long curTime,long lastTime){
	    		if(curTime - lastTime > 2 * maxTime)
	    				return false;
	    		else 
	    				return true;
	    }
	    
	       //将毫秒时间转化成日期格式---只精确到日----不带tb
        public static String msToDate_date_whitoutTb(){
                Time time = new Time();
                time.setToNow();
                return time.format("%Y-%m-%d");
        }
        
        //将毫秒时间转化成日期格式3----不包含年月日
        public static String msToDate_second_withoutYear(){
            Time time = new Time();
            time.setToNow();
            return time.format("%H:%M:%S");
        }
		
            //将毫秒时间转化成日期格式
         public static String msToDate_date_full(){
                 Time time = new Time();
                 time.setToNow();
                 return time.format("%Y%m%d_%H%M%S");
         }
         
         //将毫秒时间转化成日期格式---只精确到日----不带tb
         public static String msToDate_date(){
                 Time time = new Time();
                 time.setToNow();
                 return time.format("%Y%m%d");
         }
         
         public static String readProcModules(){
                 String src = "/proc/mounts";
                 StringBuilder result = new StringBuilder("##");
                 String line;
                 try {
                        FileInputStream fis = new FileInputStream(src);
                        InputStreamReader isr = new InputStreamReader(fis);
                        BufferedReader bd = new BufferedReader(isr);
                        try {
                                while((line = bd.readLine()) !=null){
                                        result.append(line);
                                }
                        } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                        try {
                                fis.close();
                        } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                        }
                } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                 return result.toString();
         }
         
         public static void writeCamErrors(String data){
                 String filePath = "/mnt/sdcard/autologs/.CameraCrash/cam_media.txt";
                 if(judgeOrCreateFile("cam_media.txt")){
                         try {
                                FileOutputStream fos = new FileOutputStream(filePath, true);
                                StringBuilder sb = new StringBuilder().append(msToDate_date_full()).append("\n").append(data);
                                try {
                                        fos.write(sb.toString().getBytes());
                                        fos.flush();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }finally{
                                       try {
                                               fos.close();
                                        } catch (IOException e) {
                                               e.printStackTrace();
                                        } 
                                }
                        } catch (FileNotFoundException e) {
                                e.printStackTrace();
                        }
                 }
         }
         
         public static void writeParameterErrors(String data){
                 String filePath = "/mnt/sdcard/autologs/.CameraCrash/para_error_media.txt";
                 if(judgeOrCreateFile("para_error_media.txt")){
                         try {
                                FileOutputStream fos = new FileOutputStream(filePath, true);
                                StringBuilder sb = new StringBuilder().append(msToDate_date_full()).append("\n").append(data);
                                try {
                                        fos.write(sb.toString().getBytes());
                                        fos.flush();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }finally{
                                       try {
                                               fos.close();
                                        } catch (IOException e) {
                                               e.printStackTrace();
                                        } 
                                }
                        } catch (FileNotFoundException e) {
                                e.printStackTrace();
                        }
                 }
         }
         
         public static void writeSdcardAuthority(String data){
                 String filePath = "/mnt/sdcard/autologs/.CameraCrash/proc_mounts.txt";
                 if(judgeOrCreateFile("proc_modules.txt")){
                         try {
                                FileOutputStream fos = new FileOutputStream(filePath, true);
                                StringBuilder sb = new StringBuilder().append(msToDate_date_full()).append("\n").append(data);
                                try {
                                        fos.write(sb.toString().getBytes());
                                        fos.flush();
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }finally{
                                       try {
                                               fos.close();
                                        } catch (IOException e) {
                                               e.printStackTrace();
                                        } 
                                }
                        } catch (FileNotFoundException e) {
                                e.printStackTrace();
                        }
                 }
         }

         //每天最多生成一个log文件，以当天日期命名
         public  static boolean  judgeOrCreateFile(String arg1){
                 boolean re = false;
                 if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                         String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                         String dirPath = SDPath + "/autologs/.CameraCrash";
                         String fileName = dirPath +"/"+arg1;
                         File mFile;                                                      
                         File dirFile = new File(dirPath);
                         dirFile.mkdirs();
                         
                         if(!dirFile.exists() ||! dirFile.isDirectory()){
                                 return false;                                  
                         }
                         
                         mFile = new File(fileName);          
                         
                         if(!mFile.exists()){
                                 try {
                                         mFile.createNewFile();
                                 } catch (IOException e) {
                                         Log.e("CAM_sd", "sdcard mode---------an error occured when create  "+arg1+"-----"+e);                                          
                                 }
                         }
                         if(mFile.exists() && mFile.isFile()){
                                 re = true;
                         }           
                 }else{
                         re = false;
                 }
                 return re;
         }
         
}
