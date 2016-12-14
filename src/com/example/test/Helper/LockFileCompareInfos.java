package com.example.test.Helper;

import java.io.File;

public class LockFileCompareInfos implements Comparable<LockFileCompareInfos> {
        private  File mFile;
        private int mIndexNum;
        private static final int LIMIT_INDEX_NUM = 10000;

        public LockFileCompareInfos(File mFile, int mIndexNum) {
                super();
                this.mFile = mFile;
                this.mIndexNum = mIndexNum;
        } 
        
        public File getmFile() {
                return mFile;
        }

        public void setmFile(File mFile) {
                this.mFile = mFile;
        }

        //经过修正后的索引值
        public int getAmendedIndexNum() {
                if(GlobalUtil.mAddSize_lock && mIndexNum <= LIMIT_INDEX_NUM/2){
                        return mIndexNum+LIMIT_INDEX_NUM;
                }else{
                        return mIndexNum;
                }
        }      

        //原始的索引值，主要用于确定是否要修正索引值
        public int getmIndexNum() {
                return mIndexNum;
        }

        public void setmIndexNum(int mIndexNum) {
                this.mIndexNum = mIndexNum;
        }

        @Override
        public int compareTo(LockFileCompareInfos that) {
                // for descending sort
                if(getAmendedIndexNum() > that.getAmendedIndexNum())
                        return 1;
                else if(getAmendedIndexNum() < that.getAmendedIndexNum())
                        return -1;
                else{
                        if(mFile.lastModified() > that.mFile.lastModified())
                                return 1;
                        else if(mFile.lastModified() < that.mFile.lastModified())
                                return -1;
                        else
                                return 0;
                }
        }

}
