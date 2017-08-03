package com.gmail.xlifehd.xrandomtp.commands;

import java.text.DecimalFormat;
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

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

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
					
				} else if ( args[0].equalsIgnoreCase( "reset" ) ) {
					
					if ( player.hasPermission( "xrandomtp.cooldown.reset" ) ) {
						
						UUID uuid = player.getUniqueId();
						resetCooldown(uuid);
						
					} else {
						
						sender.sendMessage(Main.errorPrefix + "You are lacking the permission \"xrandomtp.cooldown.reset\"!");
						
					}
					
				}
				
			} else {
				
				//Get UUID
				UUID uuid = player.getUniqueId();
				
				//Check cooldown
				if ( isOnCooldown( uuid ) ) {
					
					DecimalFormat df = new DecimalFormat("#.##");
					sender.sendMessage(Main.errorPrefix + "The command \"/" + label + "\" is still on cooldown! Wait " + df.format( cooldownTimeLeft(uuid) / 1000D ) + " seconds.");
					
				} else {
					
					//Get Economy and config
					Economy econ = Main.getEconomy();
					double cost = Main.getPlugin().getConfig().getDouble("teleport.cost");
					//Withdraw money
					EconomyResponse r = econ.withdrawPlayer(player, cost);
					
					//Check if transaction was successful
					if ( r.transactionSuccess() ) {
						
						//Check if teleport was successful
						if ( randomTeleport( player ) ) {
							
							//Set cooldown
							setCooldown(uuid);
							player.sendMessage(Main.infoPrefix + "You have been teleported randomly! This cost you: " + econ.format(cost) + "!");
							
						} else {
							
							//If not return the money
							player.sendMessage(Main.errorPrefix + "Couldn't find a safe spot! Please try again!");
							econ.depositPlayer(player, cost);
							
						}
						
					} else {
						
						double missingMoney = cost - econ.getBalance( player );
						sender.sendMessage(Main.errorPrefix + "You don't have enough money! You are missing: " + econ.format(missingMoney) + "!");
						
					}
					
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
	
	private boolean randomTeleport ( Player player ) {
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
			
			return true;
			
		} else {
			
			return false;
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
	
	private boolean isOnCooldown( UUID uuid ) {
		
		//Initialize variables
		FileConfiguration config = Main.getPlugin().getConfig();
		long cooldown = config.getInt( "teleport.cooldown" ) * 1000;	
		long currentTime = System.currentTimeMillis();
		Long timestamp = cooldowns.get( uuid );
		
		if ( timestamp != null ) {
			
			//Check if cooldown is already over
			if ( (currentTime - timestamp) > cooldown ) {
				return false;
			}
			
		} else {
			return false;
		}
		
		return true;
	}
	
	private long cooldownTimeLeft( UUID uuid ) {
		FileConfiguration config = Main.getPlugin().getConfig();
		long cooldown = config.getInt( "teleport.cooldown" ) * 1000;	
		long currentTime = System.currentTimeMillis();
		Long timestamp = cooldowns.get( uuid );
		
		if ( timestamp != null ) {
			long timeLeft = cooldown - (currentTime - timestamp);
			if ( timeLeft > 0 ) {
				return timeLeft;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}
	
	private void setCooldown( UUID uuid ) {
		long currentTime = System.currentTimeMillis();
		if ( cooldowns.containsKey(uuid) ) {
			cooldowns.replace(uuid, currentTime);
		} else {
			cooldowns.put(uuid, currentTime);
		}
	}
	
	private void resetCooldown( UUID uuid ) {
		if ( cooldowns.containsKey(uuid) ) {
			cooldowns.remove( uuid );
		}
	}
	
}






















