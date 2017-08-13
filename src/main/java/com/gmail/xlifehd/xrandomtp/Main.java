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
		
		//Debug
		toConsole(2, "test");
		
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
		
		config.options().header("xRandomTP Config by xLifeHD@gmail.com");
		config.addDefault("CfgVersion", 1);
		//TODO Add option to use radius around player instead of border
		
		config.addDefault("border.perWorldBorder", false);
		config.addDefault("border.maxRadius", 5000);
		config.addDefault("border.minRadius", 200);
		config.addDefault("border.offsetx", 0);
		config.addDefault("border.offsetz", 0);
		config.addDefault("teleport.maxTries", 10);
		config.addDefault("teleport.cooldown", 30);
		config.addDefault("teleport.cost", 250.0);
		config.addDefault("teleport.nether.minHeight", 20);
		config.addDefault("teleport.nether.maxHeight", 115);
		
		List<World> worlds = Bukkit.getWorlds();
		for ( World world : worlds ) {
			config.addDefault("worlds." + world.getName() + ".enabled", true);
			config.addDefault("worlds." + world.getName() + ".nether", false);
			config.addDefault("worlds." + world.getName() + ".border.maxRadius", 5000);
			config.addDefault("worlds." + world.getName() + ".border.minRadius", 200);
			config.addDefault("worlds." + world.getName() + ".border.offsetx", 0);
			config.addDefault("worlds." + world.getName() + ".border.offsetz", 0);
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
			Main.getPlugin().getLogger().info(infoPrefix + msg);
			break;
		case 2:
			Main.getPlugin().getLogger().warning(errorPrefix + msg);
			break;
		case 3:
			Main.getPlugin().getLogger().severe(errorPrefix + msg);
			break;
		default:
			Main.getPlugin().getLogger().info(infoPrefix + msg);
			break;
		}
	}
	
}
