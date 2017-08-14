package com.gmail.xlifehd.xrandomtp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.xlifehd.xrandomtp.commands.RandomTP;

import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	
	private static Main instance;
	
	private static Economy econ = null;
	
	public static String pluginPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "xRandom" + ChatColor.GREEN + ChatColor.BOLD + "TP" + ChatColor.RESET + ChatColor.DARK_GRAY + "] ";
	public static String errorPrefix = pluginPrefix + ChatColor.RED;
	public static String infoPrefix = pluginPrefix + ChatColor.WHITE;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		setupConfig();
		
		//Command register
		this.getCommand("rtp").setExecutor(new RandomTP());
		
		if (!setupEconomy() ) {
			toConsole(3, "Disabled due to no Vault dependency found!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
	}
	
	@Override
	public void onDisable() {
		
	}
	
	private void setupConfig() {
		
		FileConfiguration config = this.getConfig();
		
		config.options().header("xRandomTP Config. Please post feedback and bug reports on the spigot resource page!\n" + 
		"\"useBorderasRadiusAroundPlayer\" disables the border and lets players teleport around them using the two radii of the border!");
		config.addDefault("CfgVersion", 1);
		
		config.addDefault("border.perWorldSetting", false);
		config.addDefault("border.maxRadius", 5000);
		config.addDefault("border.minRadius", 200);
		config.addDefault("border.offsetx", 0);
		config.addDefault("border.offsetz", 0);
		config.addDefault("border.useBorderAsRadiusAroundPlayer", false);
		config.addDefault("teleport.maxTries", 10);
		config.addDefault("teleport.cooldown", 30);
		config.addDefault("teleport.cost", 250.0);
		config.addDefault("teleport.nether.minHeight", 20);
		config.addDefault("teleport.nether.maxHeight", 115);
		
		List<World> worlds = Bukkit.getWorlds();
		for ( World world : worlds ) {
			String worldName = world.getName();
			
			if ( worldName.equalsIgnoreCase( "world_the_end" ) ) {
				config.addDefault("worlds." + worldName + ".enabled", false);
			} else {
				config.addDefault("worlds." + worldName + ".enabled", true);
			}
			
			if ( worldName.equalsIgnoreCase( "world_nether" ) ) {
				config.addDefault("worlds.world_nether.nether", true);
			} else {
				config.addDefault("worlds." + worldName + ".nether", false);
			}
			
			config.addDefault("worlds." + worldName + ".border.maxRadius", 5000);
			config.addDefault("worlds." + worldName + ".border.minRadius", 200);
			config.addDefault("worlds." + worldName + ".border.offsetx", 0);
			config.addDefault("worlds." + worldName + ".border.offsetz", 0);
			config.addDefault("worlds." + worldName + ".border.useBorderAsRadiusAroundPlayer", false);
		}
		
		config.options().copyHeader(true);
		config.options().copyDefaults(true);
		saveConfig();
		
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if ( rsp == null ) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
		
	}
	
	public static Main getPlugin() {
		return instance;
	}
	
	public static Economy getEconomy() {
		return econ;
	}
	
	public static void toConsole(int type, String msg) {
		switch ( type ) {
		case 1:
			Bukkit.getLogger().info(infoPrefix + msg);
			break;
		case 2:
			Bukkit.getLogger().warning(errorPrefix + msg);
			break;
		case 3:
			Bukkit.getLogger().severe(errorPrefix + msg);
			break;
		default:
			Bukkit.getLogger().info(infoPrefix + msg);
			break;
		}
	}
	
}
