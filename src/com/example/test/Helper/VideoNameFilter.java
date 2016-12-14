package com.example.test.Helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoNameFilter  implements FilenameFilter{

        private String mRegex = null;
        private Pattern mPattern;
        private Matcher mMatcher;
        
        /*public VideoNameFilter(String regex){
                mRegex = regex;
        }*/
        
        @Override
        public boolean accept(File dir, String filename) {
               /* if(mRegex == null)
                        return false;
                mPattern = Pattern.compile(mRegex);
                mMatcher = mPattern.matcher(filename);
                return mMatcher.matches();*/
                return filename.startsWith("VID") && filename.endsWith(".mp4");
        }
}
