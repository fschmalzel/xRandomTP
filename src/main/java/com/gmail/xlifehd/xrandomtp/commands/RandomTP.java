package com.gmail.xlifehd.xrandomtp.commands;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.gmail.xlifehd.xrandomtp.Main;

public class RandomTP implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Checking if it's a player and if the player has the right permissions
		if (sender instanceof Player && ( sender.hasPermission( "xrandomtp.rtp" ) || sender.isOp() ) ) {
			
			Player player = (Player) sender;
			
			if ( args.length >= 1 ) {
				//Print help
				if ( args[0].equalsIgnoreCase( "help" ) ) {
					player.chat( "/help rtp" );
				}
				
			} else {
				//Check cooldown
				long cooldown = isOnCooldown( player.getUniqueId() );
				
				if ( cooldown <= 0 ) {
					randomTeleport( player );
				} else {
					//Inform player about cooldown
					sender.sendMessage(Main.errorPrefix + "\"/rtp\" is still on cooldown wait " + (int) cooldown/1000 + " seconds!");
				}
				
			}
			
			return true;
			
		} else if ( sender instanceof Player ) {
			
			sender.sendMessage(Main.errorPrefix + "You are lacking the permission \"xrandomtp.rtp\"!");
			return true;
			
		} else {
			
			sender.sendMessage(Main.errorPrefix + "You have to be a player!");
			return true;
			
		}
		
	}
	
	private void randomTeleport ( Player player ) {
		//Loading the config
		FileConfiguration config = Main.getPlugin().getConfig();
		int maxRadius = config.getInt( "border.maxRadius" );
		int minRadius = config.getInt( "border.minRadius" );
		int tries = config.getInt("teleport.maxTries");
		
		//Checking config
		if ( maxRadius < 0 ) {
			maxRadius = 0;
			Main.getPlugin().getLogger().warning( Main.errorPrefix + "Max. radius too small! ERROR#100" );
		}
		
		if ( minRadius < 0 ) {
			minRadius = 0;
			Main.getPlugin().getLogger().warning( Main.errorPrefix + "Min. radius too small! ERROR#101" );
		}
		
		if ( minRadius >= maxRadius ) {
			maxRadius = minRadius + 1;
			Main.getPlugin().getLogger().warning( Main.errorPrefix + "Max. radius has to be bigger than min. radius! ERROR#102" );
		}
		
		if ( tries < 5 ) {
			tries = 5;
			Main.getPlugin().getLogger().warning( Main.errorPrefix + "Min. tries are 5! ERROR#103" );
		}
		
		if ( tries > 50 ) {
			tries = 50;
			Main.getPlugin().getLogger().warning( Main.errorPrefix + "Max. tries are 50! ERROR#104" );
		}
		
		//Initializing variables
		Location randomLoc = player.getLocation();
		boolean safe = false;
		
		//Try getting a safe location 5 times, if it fails print a message
		for ( int i = 0; i < tries && !safe; i++ ) {
			
			//Getting random polar coordinates
			double distance = minRadius + ( Math.random() * ( maxRadius - minRadius + 1 ) );
			double angle = Math.random() * 2 * Math.PI;
			
			//Calculating cartesian coordinates
			int xCoordinate = (int) ( Math.cos( angle ) * distance );
			int zCoordinate = (int) ( Math.sin( angle ) * distance );
			
			//Getting the y-Coordinate
			int yCoordinate = player.getWorld().getHighestBlockYAt( xCoordinate, zCoordinate );
			
			//Creating new Location
			randomLoc = new Location( player.getWorld(), xCoordinate + 0.5, yCoordinate, zCoordinate + 0.5 );
			
			//Checking if it's safe
			safe = isSafe( randomLoc );
		}
		
		if ( safe ) {
			//Teleporting the player
			player.teleport( randomLoc );
			
			//Informing the player
			player.sendMessage(Main.infoPrefix + "You have been teleported randomly!");
			
		} else {
			player.sendMessage(Main.infoPrefix + "Couldn't find a safe spot! Please try again!");
		}
	}
	
	private boolean isSafe( Location loc ) {
		
		int xCoordinate = loc.getBlockX();
		int zCoordinate = loc.getBlockZ();
		int yCoordinate = loc.getWorld().getHighestBlockYAt( xCoordinate, zCoordinate );
		
		for ( short i = -1; i <= 0; i++ ) {
			
			Block block = loc.getWorld().getBlockAt( xCoordinate, yCoordinate + i, zCoordinate );
			Material material = block.getType();
			
			switch( material ) {
			case ENDER_PORTAL:
			case LAVA:
			case PORTAL:
			case STATIONARY_LAVA:
			case STATIONARY_WATER:
			case FIRE:
			case MAGMA:
			case CACTUS:
				return false;
			default:
				break;
			}
			
			switch ( i ) {
			case -1:
				
				//Check if block is solid and not liquid
				if ( !( !block.isLiquid() && material.isSolid() ) ) {
					return false;
				}
				break;
				
			case 0:
				
				//Check if block is not liquid / solid
				if ( !( !block.isLiquid() && !material.isSolid() ) ) {
					return false;
				}
				break;
				
			default:
				
				//Throw Error
				Main.getPlugin().getLogger().warning( Main.errorPrefix + "Something went wrong! ERROR#000" );
				return false;
				
			}
			
		}
		
		return true;
		
	}
	
	private HashMap<UUID, Long> cooldowns = new HashMap<UUID, Long>();
	
	private long isOnCooldown( UUID uuid ) {
		
		//Initialize variables
		FileConfiguration config = Main.getPlugin().getConfig();
		long cooldown = config.getLong( "teleport.cooldown" );	
		long currentTime = System.currentTimeMillis();
		Long timestamp = cooldowns.get( uuid );

		
		if ( timestamp != null ) {
			
			//Check if cooldown is already over
			if ( (currentTime - timestamp) > cooldown ) {
				//Command ready, return 0 and reset
				cooldowns.replace(uuid, currentTime);
				return 0;
				
			} else {
				//On cooldown, return the time that is left
				long timeLeft = cooldown - (currentTime - timestamp);
				return timeLeft;
				
			}
			
			
		} else {
			//No cooldown registered, return 0 and put it on cooldown
			cooldowns.put(uuid, currentTime);
			return 0;
		}
		
		
	}
	
	
}






















