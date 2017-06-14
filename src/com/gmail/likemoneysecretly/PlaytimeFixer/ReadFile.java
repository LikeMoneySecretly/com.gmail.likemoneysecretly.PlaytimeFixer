package com.gmail.likemoneysecretly.PlaytimeFixer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ReadFile {
private String path;
    
    public ReadFile(String file_path){
        path  =file_path;
    }
    public String[] OpenFile() throws IOException{
        
        FileReader file = new FileReader(path);
        BufferedReader textReader = new BufferedReader(file);
        
        int numberOfLines = readLines();
        String[] textData = new String[numberOfLines];
        
        
        int i;
        
        for(i=0;i<numberOfLines;i++){
            textData[i] = textReader.readLine();
    }
        textReader.close();
        return textData;
        
    }
    public int readLines() throws IOException{
        
        FileReader file = new FileReader(path);
        BufferedReader bf = new BufferedReader(file);
        
        
        int numberOfLines = 0;
        
        @SuppressWarnings("unused")
		String line;
		while((line=bf.readLine())!=null){
            numberOfLines++;
        }
        bf.close();
        
        return numberOfLines;
    }
}
