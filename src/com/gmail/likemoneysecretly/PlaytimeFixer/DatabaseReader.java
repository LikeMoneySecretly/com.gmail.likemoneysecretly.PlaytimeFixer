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
	
	
	
	public void UpdatePlaytime(String uuid,int playtime, String world) throws SQLException{
		String SQL = "SELECT * FROM "+Prefix+"playtime WHERE uuid="+'"'+uuid+'"'+" AND world = "+'"'+world+'"';
		
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs2 = statement.executeQuery(SQL);
		
		while(rs2.next()){
			
	    	String World = rs2.getString("world");
	    	
			if(World.equals(world)){
                rs2.updateInt("value", playtime);
                rs2.updateRow();
                return;
			}
		}
		
		
		statement.execute("INSERT INTO "+Prefix+"playtime (uuid,value,world) VALUES ('"+uuid+"','"+playtime+"','"+world+"')");
		
	}
	
	
	
	
	public int OtherWorldsPlaytime(String UUID, String world) throws SQLException{
		String SQL = "SELECT value, world FROM "+Prefix+"playtime WHERE uuid="+'"'+UUID+'"'+" AND world !="+'"'+world+'"';
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = statement.executeQuery(SQL);
		
		int OtherWorldplaytime = 0;
			
		while(rs.next()){			
	    	if(!(rs.getString("world").equals(world))){
	    		OtherWorldplaytime=OtherWorldplaytime+rs.getInt("value");
	    	}
		}
		return OtherWorldplaytime;
	}
	
	
	public int MainWorldPlaytime(String UUID, String world) throws SQLException{
		int MainWorldplaytime = 0;
		String SQL = "SELECT value, world FROM "+Prefix+"playtime WHERE uuid="+'"'+UUID+'"'+" AND world ="+'"'+world+'"';
		Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
		ResultSet rs = statement.executeQuery(SQL);
		
		while(rs.next()){
			if(rs.getString("world").equals(world)){
				MainWorldplaytime = rs.getInt("value");
				return MainWorldplaytime;
			}
		}
		return MainWorldplaytime;
	}
	
	
	
}