package com.gmail.xlifehd.xrandomtp;

import org.bukkit.ChatColor;
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
		
		//Configuration
		FileConfiguration config = this.getConfig();
		
		config.options().header("xRandomTP Config by xLifeHD@gmail.com");
		config.addDefault("CfgVersion", 1);
		//TODO Add option to use radius around player instead of border
		//TODO Per world configuration
		config.addDefault("border.maxRadius", 5000);
		config.addDefault("border.minRadius", 200);
		config.addDefault("border.offsetx", 0);
		config.addDefault("border.offsetz", 0);
		config.addDefault("teleport.maxTries", 10);
		config.addDefault("teleport.cooldown", 30);
		config.addDefault("teleport.cost", 250.0);
		config.options().copyHeader(true);
		config.options().copyDefaults(true);
		saveConfig();
		
		//Command register
		this.getCommand("rtp").setExecutor(new RandomTP());
        if (!setupEconomy() ) {
            getLogger().severe(errorPrefix + "Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
	}
	
	@Override
	public void onDisable() {
		
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
}
