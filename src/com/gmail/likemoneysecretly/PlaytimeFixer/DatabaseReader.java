package com.gmail.likemoneysecretly.PlaytimeFixer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseReader {
	
	
	public String DatabaseName = "";
	public String Username = "";
	public String Password = "";
	public String Prefix = "";
	Connection con;
	
	
	public DatabaseReader(String host, String username, String password, String prefix) throws SQLException{
		DatabaseName = host;
		Username = username;
		Password = password;
		Prefix = prefix;
		con = DriverManager.getConnection(DatabaseName, Username, Password);
	}
	
	
	
	public void UpdatePlaytime(String uuid,int playtime) throws SQLException{
		String SQL = "SELECT*FROM "+Prefix+"playtime";
		boolean nothalt=true;
		
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs2 = statement.executeQuery(SQL);
		
		while(rs2.next()&&nothalt==true){
			
	    	String unique_id = rs2.getString("uuid");
	    	
			if(unique_id.indexOf(uuid)>-1){
				nothalt=false;
                rs2.updateInt("value", playtime);
                rs2.updateRow();
			}
		}
	}
	
	
	
	
	public int DatabasePlaytime(String UUID) throws SQLException{
		String SQL = "SELECT*FROM "+Prefix+"playtime";
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = statement.executeQuery(SQL);
		
		int playtime = 0;
		boolean nothalt = true;
		
		while(rs.next()&&nothalt==true){			
	    	String unique_id = rs.getString("uuid");
			if(unique_id.indexOf(UUID)>-1){
				nothalt=false;
				playtime = rs.getInt("value");
			}
		}		
		return playtime;
	}
	
	
	
}