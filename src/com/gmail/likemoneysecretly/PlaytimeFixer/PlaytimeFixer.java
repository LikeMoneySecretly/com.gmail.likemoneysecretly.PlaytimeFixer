package com.gmail.likemoneysecretly.PlaytimeFixer;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
	
	public String Username = "";
	public String Password = "";
	public String Prefix = "";
	public String port = "";
	public String host = "";
	public String database = ""; 
	
	public int MojangOneMinute = 0;
	
	
	@Override
	public void onEnable(){
		
		this.getServer().getPluginManager().registerEvents(this, this);
		getConfig().options().copyDefaults(true);
		saveConfig();
			
		ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
		if(getConfig().getBoolean("Auto-Update")==false){
			console.sendMessage(ChatColor.RED+"WARNING"+ ChatColor.WHITE+": The auto update feature of play time fixer is currently disabled. Please enable it in the config file for maximum effect.");
		}
		try{
		String[] StatsConfig = getStatsConfig();
		
		
		for(int i =0; i<StatsConfig.length;i++){
			if(StatsConfig[i].contains("host")){
				host = StatsConfig[i].replaceAll("host: ", "");
				host = host.replace("'", "");
			}
			else if(StatsConfig[i].contains("port")){
				port = StatsConfig[i].replace("port: ","");
				port = port.replace("'", "");
			}
			else if(StatsConfig[i].contains("user")){
				Username = StatsConfig[i].replace("user: ", "");
				Username = Username.replace("'", "");
			}
			else if(StatsConfig[i].contains("pass")){
				Password=StatsConfig[i].replace("pass: ", "");
				Password = Password.replace("'", "");
			}
			else if(StatsConfig[i].contains("database")){
				database = StatsConfig[i].replace("database: ", "");
				database =database.replace("'", "");
			}
			else if(StatsConfig[i].contains("prefix")){
				Prefix = StatsConfig[i].replace("prefix: ", "");
				Prefix = Prefix.replace("'", "");
			}
		}
		}
		catch(IOException v){
			System.out.println("[Playtime Fixer Error]: "+v.getMessage());
		}
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	public void OnJoin(PlayerJoinEvent event){
		if(getConfig().getBoolean("Auto-Update")&&GetDifference(event.getPlayer())>0){
		    UpdatePlaytime(event.getPlayer(), GetDifference(event.getPlayer()));
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		String command = cmd.getName();
		
		if(command.equalsIgnoreCase("UpdateAllPlaytimes")){
			
			if(sender.hasPermission("PlaytimeFixer.UpdateAllPlaytimes")){
				String changedPlayers= "";
				for(Player p: Bukkit.getServer().getOnlinePlayers()){
					
					int difference = GetDifference(p);
					
					if(difference>60){
						
						UpdatePlaytime(p,difference);
						
						if(changedPlayers.equals("")){
							changedPlayers= p.getName();
						}
						else{
							changedPlayers= changedPlayers+", "+p.getName();
						}
					}
				}
				if(!(changedPlayers.equals(""))){
					sender.sendMessage(ChatColor.GREEN+"The following players have had their playtime updated: "+ChatColor.GOLD+changedPlayers);
				}
				else{
					sender.sendMessage(ChatColor.GREEN+"No online players needed their playtime updated.");
				}
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
							int difference = GetDifference(g);
							if(difference>60){
								sender.sendMessage(ChatColor.GREEN+"Successfully updated "+g.getName()+"'s playtime by "+ChatColor.RED+GetDifference(g)+ChatColor.GREEN+" seconds.");
								UpdatePlaytime(g, difference);								
								return true;
							}
							else{
								sender.sendMessage(ChatColor.RED+"Sorry but the difference in "+g.getName()+"'s play time between mojang and stats3 is neglible.");
								return true;
							}
						}
						
					}
					OfflinePlayer offlineplayer = Bukkit.getOfflinePlayer(name);
					
					if(offlineplayer!=null){
						int difference = GetDifference(offlineplayer);
						if(difference>60){
							int change = GetDifference(offlineplayer);
							UpdatePlaytime(offlineplayer, difference);
							sender.sendMessage(ChatColor.GREEN+"Successfully updated "+name+"'s playtime by "+ChatColor.RED+change+ChatColor.GREEN+" seconds.");
							return true;
						}
						else{
							sender.sendMessage(ChatColor.RED+"Sorry but the difference in "+offlineplayer.getName()+"'s play time between mojang and stats3 is neglible.");
							return true;
					}
				}
					else{
						sender.sendMessage(ChatColor.RED+"Sorry but this player has never played on this server before.");
					}
				}
				else{
					String name = sender.getName();
					
					for(Player p:Bukkit.getServer().getOnlinePlayers()){
						
						if(p.getName().equalsIgnoreCase(name)&&GetDifference(p)>60){
							UpdatePlaytime(p, GetDifference(p));
							System.out.println(p.getUniqueId());
							return true;
						}
						else{
							sender.sendMessage(ChatColor.RED+"I'm sorry but your own playtime does not require updating.");
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
	
	
	public void UpdatePlaytime(Player target, int difference){
			String world = getConfig().getString("NameOfWorldFile");
			try{
			String DatabaseConnectionString = "jdbc:mysql://"+host+":"+port+"/"+database;
			DatabaseReader read = new DatabaseReader(DatabaseConnectionString, Username,Password,Prefix);
			if(difference>60){//THIS IS A VERY IMPORTANT LINE: It basically chooses whether or not the plugin adds playtime or takes it away OR does both.
				
				if(getConfig().getBoolean("ConsoleWarnings")==true){
					ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
					console.sendMessage(ChatColor.RED+"NOTE: "+ChatColor.WHITE+target.getName()+" has had their play time changed with a difference between Mojangs time and the database time of " +difference + " seconds.");
					
				}
				if(getConfig().getBoolean("PlayerNotification")==true){
					BukkitScheduler scheduler = getServer().getScheduler();
			        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
			            @Override
			            public void run() {
	    					target.sendMessage(ChatColor.GOLD +"["+ChatColor.RED+"PlaytimeFixer"+ChatColor.GOLD+"]"+ChatColor.GREEN+" Good news! A discrepancy between mojang and database play time on your account was noticed and your play time was updated by "+ChatColor.RED+difference+ChatColor.GREEN+" seconds! "+ChatColor.WHITE+" Remember to thank GodsDead for noticing this issue and Silence__ for fixing it!"+ChatColor.DARK_RED+" Please re-log for these changes to take place.");
			            }
			            },100l);
				}
				if(getConfig().getBoolean("LogCreation")==true){
					LogCreator h = new LogCreator();
					String date = sdf.format(cal.getTime()).toString();
					String time = asdf.format(cal.getTime()).toString();
					String directory = Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", getConfig().getString("NameOfPluginFolder")+"//PlaytimeFixer");
					String file_location = directory+"//"+date+"_Log.yml";
					
					if(h.DoesFileExist(file_location)==false){
						h.LogCreator(directory ,sdf.format(cal.getTime()).toString()+"_Log.yml");
						LogWriter write = new LogWriter(file_location, true);
						write.writeToFile(date+" Log:");
						write.writeToFile(" ");
						write.writeToFile("["+time+"] "+target.getName()+"'s (uuid = "+target.getUniqueId()+") play time was changed on "+world+" by "+difference+" seconds.");
					}
					else{
						LogWriter write = new LogWriter(file_location, true);
						write.writeToFile("["+time+"] "+target.getName()+"'s (uuid = "+target.getUniqueId()+") play time was changed on "+world+" by "+difference+" seconds.");
					}
				}
				
				read.UpdatePlaytime(target.getUniqueId().toString(),MojangOneMinute,world);
				
			}
		}
		catch (SQLException b){
			System.out.println("[Playtime Fixer Error]: "+b.getMessage());
			
		}
		catch (IOException p){
			System.out.println("[Playtime Fixer Error]: "+"The player you are searching for has not played on the server.");			
		}
	}
	
	public void UpdatePlaytime(OfflinePlayer target, int difference){
		String world = getConfig().getString("NameOfWorldFile");
		try{
		String DatabaseConnectionString = "jdbc:mysql://"+host+":"+port+"/"+database;
		DatabaseReader read = new DatabaseReader(DatabaseConnectionString, Username,Password,Prefix);
		if(difference>60){//THIS IS A VERY IMPORTANT LINE: It basically chooses whether or not the plugin adds playtime or takes it away OR does both.
			
			if(getConfig().getBoolean("ConsoleWarnings")==true){
				ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
				console.sendMessage(ChatColor.RED+"NOTE: "+ChatColor.WHITE+target.getName()+" has had their play time changed with a difference between Mojangs time and the database time of " +difference + " seconds.");
				
			}
			if(getConfig().getBoolean("LogCreation")==true){
				LogCreator h = new LogCreator();
				String date = sdf.format(cal.getTime()).toString();
				String time = asdf.format(cal.getTime()).toString();
				String directory = Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", getConfig().getString("NameOfPluginFolder")+"//PlaytimeFixer");
				String file_location = directory+"//"+date+"_Log.yml";
				
				if(h.DoesFileExist(file_location)==false){
					h.LogCreator(directory ,sdf.format(cal.getTime()).toString()+"_Log.yml");
					LogWriter write = new LogWriter(file_location, true);
					write.writeToFile(date+" Log:");
					write.writeToFile(" ");
					write.writeToFile("["+time+"] "+target.getName()+"'s (uuid = "+target.getUniqueId()+") play time was changed on "+world+" by "+difference+" seconds.");
				}
				else{
					LogWriter write = new LogWriter(file_location, true);
					write.writeToFile("["+time+"] "+target.getName()+"'s (uuid = "+target.getUniqueId()+") play time was changed on "+world+" by "+difference+" seconds.");
				}
			}
			
			read.UpdatePlaytime(target.getUniqueId().toString(),MojangOneMinute,world);
			
		}
	}
	catch (SQLException b){
		System.out.println("[Playtime Fixer Error]: "+b.getMessage());
		
	}
	catch (IOException p){
		System.out.println("[Playtime Fixer Error]: "+"The player you are searching for has not played on the server.");			
	}
}
	public int GetDifference(Player target){
		MojangOneMinute=0;
		Player g = target;
		String uuid = g.getUniqueId().toString();
		String slash = "//";
		String world = getConfig().getString("NameOfWorldFile");
		ReadFile file = new ReadFile(Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", world)+"//stats"+slash+uuid+".json");
		String[] text;
		String[] data;
		
		int difference = 0;
		
		try{
		text = file.OpenFile();
		data = text[0].split(",");
		String PlayOneMinute = "";
		int MainWorldMinute = 0;
		int OtherWorldsMinute =0;
		
		try{
			String DatabaseConnectionString = "jdbc:mysql://"+host+":"+port+"/"+database;
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
			difference = MojangOneMinute-MainWorldMinute;
			
			
		}
		catch (SQLException e){
			System.out.println("[Playtime Fixer Error]: "+e.getMessage());			
		}
		}
		catch (IOException l){
			System.out.println("[Playtime Fixer Error]: "+"The player you are searching for has not played on the server.");			
		}
		return difference;
	}
	
	
	public int GetDifference(OfflinePlayer target){
		MojangOneMinute=0;
		
		String uuid = target.getUniqueId().toString();
		String slash = "//";
		String world = getConfig().getString("NameOfWorldFile");
		ReadFile file = new ReadFile(Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", world)+"//stats"+slash+uuid+".json");
		String[] text;
		String[] data;
		
		int difference = 0;
		
		try{
		text = file.OpenFile();
		data = text[0].split(",");
		String PlayOneMinute = "";
		int MainWorldMinute = 0;
		int OtherWorldsMinute =0;
		
		try{
			String DatabaseConnectionString = "jdbc:mysql://"+host+":"+port+"/"+database;
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
			
			MainWorldMinute = read.MainWorldPlaytime(target.getUniqueId().toString(),world);
			OtherWorldsMinute = read.OtherWorldsPlaytime(target.getUniqueId().toString(), world);
			
			MojangOneMinute = MojangOneMinute-OtherWorldsMinute;
			difference = MojangOneMinute-MainWorldMinute;
			
			
		}
		catch (SQLException e){
			System.out.println("[Playtime Fixer Error]: "+e.getMessage());			
		}
		}
		catch (IOException l){
			System.out.println("[Playtime Fixer Error]: "+"The player you are searching for has not played on the server.");			
		}
		return difference;
	}
	
	
	public String[] getStatsConfig() throws IOException{
		String file_location = Bukkit.getServer().getWorldContainer().getAbsolutePath().replace(".", getConfig().getString("NameOfPluginFolder")+"//Stats//mysql.yml");
		ReadFile file = new ReadFile(file_location);
		
		String[] data = file.OpenFile();
		return data;
	}
	
}
