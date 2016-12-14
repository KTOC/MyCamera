package com.example.test.file;

import java.io.File;
import java.io.FileFilter;

import com.example.test.settings.CameraSettings;

public class InvalidFileFilter implements FileFilter{

	@Override
	public boolean accept(File pathFile) {
		if(pathFile != null && pathFile.exists() && pathFile.getName().endsWith(CameraSettings.SUFFIX_TMP))
			return true;
		else
			return false;
	}

}
