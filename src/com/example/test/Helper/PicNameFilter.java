package com.example.test.Helper;

import java.io.File;
import java.io.FilenameFilter;

public class PicNameFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String filename) {
		
		return filename.endsWith("jpg") && filename.startsWith("IMG");
	}

}
