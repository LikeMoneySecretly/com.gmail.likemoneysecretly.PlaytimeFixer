package com.gmail.likemoneysecretly.PlaytimeFixer;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

public class PlaytimeFixer extends JavaPlugin implements Listener{
	private Calendar cal = Calendar.getInstance();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd");
	private SimpleDateFormat asdf = new SimpleDateFormat("H:m:s");
	
	@Override
	public void onEnable(){
		this.getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		if(getConfig().getBoolean("Auto-Update")==false){
			console.sendMessage(ChatColor.RED+"WARNING"+ ChatColor.WHITE+": The auto update feature of play time fixer is currently disabled. Please enable it in the config file for maximum effect.");
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void OnJoin(PlayerJoinEvent event){
		if(getConfig().getBoolean("Auto-Update")){
		    UpdatePlaytime(event.getPlayer());
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		String command = cmd.getName();
		
		if(command.equalsIgnoreCase("UpdateAllPlaytimes")){
			
			if(sender.hasPermission("PlaytimeFixer.UpdateAllPlaytimes")){
				for(Player p: Bukkit.getServer().getOnlinePlayers()){
					UpdatePlaytime(p);
				}
				sender.sendMessage(ChatColor.GREEN+"Successfully checked if any online players needed their playtime updated. Check your server command prompt or playtime fixer logs for more info");
				return true;
			}
			else{
				sender.sendMessage(ChatColor.RED+"I'm sorry but you do not have the "+ChatColor.AQUA+"PlaytimeFixer.UpdateAllPlaytimes"+ChatColor.RED+" permission.");
				return true;
			}
			
		}
		
		
		if(command.equalsIgnoreCase("UpdatePlaytime")){
			
			if(sender.hasPermission("PlaytimeFixer.UpdatePlaytime")){
				if(args.length>0){
					String name = args[0];
					
					for(Player g:Bukkit.getServer().getOnlinePlayers()){
					
						if(g.getName().equalsIgnoreCase(name)){
							UpdatePlaytime(g);
							sender.sendMessage(ChatColor.GREEN+"Successfully updated "+g.getName()+"'s playtime. Check the command screen or the PlaytimeFixer logs for the amount.");
							return true;
						}
						
					}
					sender.sendMessage(ChatColor.RED+"I'm sorry but the specified player "+ChatColor.AQUA+ name+ChatColor.RED+" is not currently online at the moment.");
					return true;
				}
				else{
					String name = sender.getName();
					
					for(Player p:Bukkit.getServer().getOnlinePlayers()){
						
						if(p.getName().equalsIgnoreCase(name)){
							UpdatePlaytime(p);
							sender.sendMessage(ChatColor.GREEN+"Successfully updated your own playtime.");
							return true;
						}
					}
				}
			}
			else{
				sender.sendMessage(ChatColor.RED+"I'm sorry but you do not have the "+ChatColor.AQUA+"PlaytimeFixer.UpdatePlaytime"+ChatColor.RED+" permission.");
				return true;
			}
		}
		return false;
	}
	
	
	public void UpdatePlaytime(Player target){
		Player g = target;
		String uuid = g.getUniqueId().toString();
		String slash = "\\";
		String world = getConfig().getString("NameOfWorldFile");
		ReadFile file = new ReadFile(Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", world)+"\\stats"+slash+uuid+".json");
		String[] text;
		String[] data;
		try{
		text = file.OpenFile();
		data = text[0].split(",");
		String PlayOneMinute = "";
		int MojangOneMinute = 0;
		int MainWorldMinute = 0;
		int OtherWorldsMinute =0;
		
		try{
			
			String Username = "";
			String Password = "";
			String Prefix = "";
			String port = "";
			String host = "";
			String database = "";    			
			String[] StatsConfig = getStatsConfig();
			String DatabaseConnectionString = "";
			
			for(int i =0; i<StatsConfig.length;i++){
				if(StatsConfig[i].contains("host")){
					host = StatsConfig[i].replaceAll("host: ", "");
				}
				else if(StatsConfig[i].contains("port")){
					port = StatsConfig[i].replace("port: ","");
				}
				else if(StatsConfig[i].contains("user")){
					Username = StatsConfig[i].replace("user: ", "");
				}
				else if(StatsConfig[i].contains("pass")){
					Password=StatsConfig[i].replace("pass: ", "");
				}
				else if(StatsConfig[i].contains("database")){
					database = StatsConfig[i].replace("database: ", "");
				}
				else if(StatsConfig[i].contains("prefix")){
					Prefix = StatsConfig[i].replace("prefix: ", "");
				}  				
			}
			DatabaseConnectionString = "jdbc:mysql://"+host+":"+port+"/"+database;
			DatabaseReader read = new DatabaseReader(DatabaseConnectionString, Username,Password,Prefix);
			
			for(int i =0; i<data.length;i++){
    			if(data[i].contains("stat.playOneMinute")){
    				PlayOneMinute = data[i].replace('"'+"stat.playOneMinute"+'"'+":", "");
    				
    				if(!(PlayOneMinute.equals(""))){
    					MojangOneMinute = Integer.parseInt(PlayOneMinute);
    					MojangOneMinute = MojangOneMinute/20;
    				}
    				
    			}
    		}
			
			MainWorldMinute = read.MainWorldPlaytime(g.getUniqueId().toString(),world);
			OtherWorldsMinute = read.OtherWorldsPlaytime(g.getUniqueId().toString(), world);
			
			MojangOneMinute = MojangOneMinute-OtherWorldsMinute;
			int difference = MojangOneMinute-MainWorldMinute;
			
			if(difference>60){//THIS IS A VERY IMPORTANT LINE: It basically chooses whether or not the plugin adds playtime or takes it away OR does both.
				
				if(getConfig().getBoolean("ConsoleWarnings")==true){
					ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
					console.sendMessage(ChatColor.RED+"NOTE: "+ChatColor.WHITE+g.getName()+" has had their play time changed with a difference between Mojangs time and the database time of " +difference + " seconds.");
				}
				if(getConfig().getBoolean("PlayerNotification")==true){
					BukkitScheduler scheduler = getServer().getScheduler();
			        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
			            @Override
			            public void run() {
	    					g.sendMessage(ChatColor.GOLD +"["+ChatColor.RED+"PlaytimeFixer"+ChatColor.GOLD+"]"+ChatColor.GREEN+" Good news! A discrepancy between mojang and database play time on your account was noticed and your play time was updated by "+ChatColor.RED+difference+ChatColor.GREEN+" seconds! "+ChatColor.WHITE+" Remember to thank GodsDead for noticing this issue and Silence__ for fixing it!");
			            }
			            },100l);
				}
				if(getConfig().getBoolean("LogCreation")==true){
					LogCreator h = new LogCreator();
					String date = sdf.format(cal.getTime()).toString();
					String time = asdf.format(cal.getTime()).toString();
					String directory = Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", getConfig().getString("NameOfPluginFolder")+"\\PlaytimeFixer");
					String file_location = directory+"\\"+date+"_Log.yml";
					
					if(h.DoesFileExist(file_location)==false){
						h.LogCreator(directory ,sdf.format(cal.getTime()).toString()+"_Log.yml");
						LogWriter write = new LogWriter(file_location, true);
						write.writeToFile(date+" Log:");
						write.writeToFile(" ");
						write.writeToFile("["+time+"] "+g.getName()+"'s (uuid = "+g.getUniqueId()+") play time was changed on "+world+" by "+difference+" seconds.");
					}
					else{
						LogWriter write = new LogWriter(file_location, true);
						write.writeToFile("["+time+"] "+g.getName()+"'s (uuid = "+g.getUniqueId()+") play time was changed on "+world+" by "+difference+" seconds.");
					}
				}
				read.UpdatePlaytime(uuid,MojangOneMinute,world);
			}
		}
		catch (SQLException e){
			System.out.println("[Playtime Fixer Error]: "+e.getMessage());
		}
		}
		catch (IOException e){
			System.out.println("[Playtime Fixer Error]: "+e.getMessage());
		}
	}
	
	public String[] getStatsConfig() throws IOException{
		String file_location = Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", getConfig().getString("NameOfPluginFolder")+"\\Stats\\mysql.yml");
		ReadFile file = new ReadFile(file_location);
		
		String[] data = file.OpenFile();
		return data;
	}
	
}
