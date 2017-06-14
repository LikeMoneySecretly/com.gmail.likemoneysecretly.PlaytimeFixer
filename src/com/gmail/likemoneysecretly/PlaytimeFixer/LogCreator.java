package com.gmail.likemoneysecretly.PlaytimeFixer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogCreator {
	public void LogCreator(String directory, String file_name) throws IOException{
        final File parentDir = new File(directory);
        parentDir.mkdir();
        final File file = new File(parentDir, file_name);
        file.createNewFile();
    }
    public boolean DoesFileExist(String file_location){
        try{
            @SuppressWarnings({ "unused", "resource" })
			FileReader file = new FileReader(file_location);
            return true;
        }
        catch(IOException e){
            return false;
        }
    }
}
