package com.gmail.xlifehd.xrandomtp;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.xlifehd.xrandomtp.commands.RandomTP;

public class Main extends JavaPlugin {
	
	private static Main instance;
	
	public static Main getPlugin() {
		return instance;
	}
	
	public static String pluginPrefix = ChatColor.DARK_GRAY + "[" + ChatColor.AQUA + "xRandom" + ChatColor.GREEN + ChatColor.BOLD + "TP" + ChatColor.RESET + ChatColor.DARK_GRAY + "] ";
	public static String errorPrefix = pluginPrefix + ChatColor.RED;
	public static String infoPrefix = pluginPrefix + ChatColor.WHITE;
	
	@Override
	public void onEnable() {
		
		instance = this;
		
		//Configuration
		FileConfiguration config = this.getConfig();
		
		config.options().header("xRandomTP Config by xLifeHD@gmail.com");
		config.addDefault("CfgVersion", 1);
		config.addDefault("border.maxRadius", 5000);
		config.addDefault("border.minRadius", 200);
		config.addDefault("teleport.maxTries", 10);
		config.addDefault("teleport.cooldown", 60000);
		config.options().copyHeader(true);
		config.options().copyDefaults(true);
		saveConfig();
		
		//Command register
		this.getCommand("rtp").setExecutor(new RandomTP());
		
	}
	
	@Override
	public void onDisable() {
		
	}
}
