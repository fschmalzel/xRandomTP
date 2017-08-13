package com.gmail.xlifehd.xrandomtp.commands;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
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
			
			
			if (Main.getPlugin().getConfig().getBoolean("worlds." + player.getWorld().getName() + ".enabled")) {
				if ( args.length >= 1 ) {
					//Print help
					if ( args[0].equalsIgnoreCase( "help" ) ) {
						
						Bukkit.dispatchCommand(player, "help " + label);
						
					} else if ( args[0].equalsIgnoreCase( "reset" ) ) {
						
						if ( player.hasPermission( "xrandomtp.cooldown.reset" ) ) {
							
							UUID uuid = player.getUniqueId();
							resetCooldown(uuid);
							player.sendMessage(Main.infoPrefix + "Your cooldown has been reset!");
							
						} else {
							
							sender.sendMessage(Main.errorPrefix + "You are lacking the permission \"xrandomtp.cooldown.reset\"!");
							
						}
						
					}
					
				} else {
					
					//Get UUID
					UUID uuid = player.getUniqueId();
					boolean ignoreCooldown = player.hasPermission("xrandomtp.cooldown.ignore");
					//Check cooldown
					if ( !isOnCooldown( uuid ) || ignoreCooldown ) {
						
						//Get Economy and config
						Economy econ = Main.getEconomy();
						double cost = Main.getPlugin().getConfig().getDouble("teleport.cost");
						
						if ( player.hasPermission("xrandomtp.cost.ignore") ) {
							cost = 0;
						}
						
						//Withdraw money
						EconomyResponse r = econ.withdrawPlayer(player, cost);
						
						//Check if transaction was successful
						if ( r.transactionSuccess() ) {
							
							//Check if teleport was successful
							if ( randomTeleport( player ) ) {
								
								//Set cooldown
								if ( !ignoreCooldown ) {
									setCooldown(uuid);
								}
								player.sendMessage(Main.infoPrefix + "You have been teleported randomly! " + econ.format(cost) + " have been deducted from your account!");
								
							} else {
								
								//If not return the money
								player.sendMessage(Main.errorPrefix + "Couldn't find a safe spot! Please try again!");
								econ.depositPlayer(player, cost);
								
							}
							
						} else {
							
							double missingMoney = cost - econ.getBalance( player );
							sender.sendMessage(Main.errorPrefix + "You don't have enough money! You are missing: " + econ.format(missingMoney) + "!");
							
						}
						
					} else {
						
						DecimalFormat df = new DecimalFormat("#.##");
						sender.sendMessage(Main.errorPrefix + "The command \"/" + label + "\" is still on cooldown! Wait " + df.format( cooldownTimeLeft(uuid) / 1000D ) + " seconds.");
						
					}
					
					
				}
				
			} else {
				player.sendMessage(Main.infoPrefix + "\"/rtp\" is disabled in this world.");
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
		
		//Initializing variables
		Location randomLoc = player.getLocation();
		boolean safe = false;
		
		//Loading the configuration
		FileConfiguration config = Main.getPlugin().getConfig();
		int maxRadius = config.getInt( "border.maxRadius" );
		int minRadius = config.getInt( "border.minRadius" );
		int offsetx = config.getInt("border.offsetx");
		int offsetz = config.getInt("border.offsetz");
		int tries = config.getInt("teleport.maxTries");
		boolean nether = config.getBoolean("worlds." + randomLoc.getWorld().getName() + ".nether");
		
		//Checking configuration
		if ( maxRadius < 0 ) {
			maxRadius = 0;
			Main.toConsole(2, "Max. radius too small! ERROR#100" );
			
		}
		
		if ( minRadius < 0 ) {
			minRadius = 0;
			Main.toConsole(2, "Min. radius too small! ERROR#101" );
		}
		
		if ( minRadius >= maxRadius ) {
			maxRadius = minRadius + 1;
			Main.toConsole(2, "Max. radius has to be bigger than min. radius! ERROR#102" );
		}
		
		if ( tries < 5 ) {
			tries = 5;
			Main.toConsole(2, "Min. tries are 5! ERROR#103" );
		}
		
		if ( tries > 50 ) {
			tries = 50;
			Main.toConsole(2, "Max. tries are 50! ERROR#104" );
		}
		
		int minHeight = 0;
		int maxHeight = 0;
		
		if (nether) {
			minHeight = config.getInt("teleport.nether.minHeight");
			maxHeight = config.getInt("teleport.nether.maxHeight");
			if (maxHeight < 0) {
				maxHeight = 0;
				Main.toConsole(2, "Max. height too small! ERROR#105");
			}
			if (minHeight < 0) {
				minHeight = 0;
				Main.toConsole(2, "Min. height too small! ERROR#106");
			}
			if (maxHeight > 256) {
				maxHeight = 256;
				Main.toConsole(2, "Max. height too big! ERROR#107");
			}
			if (minHeight > 255) {
				minHeight = 255;
				Main.toConsole(2, "Min. height too big! ERROR#108");
			}
			if (minHeight >= maxHeight) {
				maxHeight = minHeight + 1;
				Main.toConsole(2, "Max. height has to be bigger than min. height! ERROR#109");
			} 
		}
		
		//Try getting a safe location X times, if it fails print a message
		for ( int i = 0; i < tries && !safe; i++ ) {
			
			//Getting random polar coordinates
			double distance = minRadius + ( Math.random() * ( maxRadius - minRadius + 1 ) );
			double angle = Math.random() * 2 * Math.PI;
			
			//Calculating cartesian coordinates
			int xCoordinate = (int) ( Math.cos( angle ) * distance ) + offsetx;
			int zCoordinate = (int) ( Math.sin( angle ) * distance ) + offsetz;
			int yCoordinate;
			
			randomLoc.setX(xCoordinate + 0.5);
			randomLoc.setZ(zCoordinate + 0.5);
			
			//Getting the y-Coordinate
			if ( nether ) {
				
				yCoordinate = (int) (minHeight + ( Math.random() * (maxHeight - minHeight + 1) ));
				int startYCoordinate = yCoordinate;
				boolean finished = false;
				
				do {
					
					randomLoc.setY(yCoordinate);
					
					if ( randomLoc.getBlock().getType() == Material.AIR) {
						
						safe = isSafe( randomLoc, (short) 3 );
						
						if ( safe ) {
							
							finished = true;
							
						} else {
							
							yCoordinate += 1;
							
						}
						
					} else {
						
						yCoordinate += 1;
						
					}
					
				} while (yCoordinate < maxHeight && !finished);
				
				if ( !finished ) {
					yCoordinate = startYCoordinate;
					do {
						
						randomLoc.setY(yCoordinate);
						
						if ( randomLoc.getBlock().getType() == Material.AIR) {
							
							randomLoc.setY(yCoordinate-1);
							
							if (randomLoc.getBlock().getType().isSolid()) {
								
								randomLoc.setY(yCoordinate);
								
								safe = isSafe(randomLoc, (short) 3);
								if (safe) {
									
									yCoordinate -= 1;
									finished = true;

								} else {

									yCoordinate -= 1;

								} 
							} else {
								yCoordinate -= 1;
							}
							
						} else {
							
							yCoordinate -= 1;
							
						}
						
					} while (yCoordinate > minHeight && !finished);
				}
				
				
			} else {
				
				yCoordinate = player.getWorld().getHighestBlockYAt( xCoordinate, zCoordinate );
				randomLoc.setY(yCoordinate);
				
				safe = isSafe( randomLoc, (short) 2 );
				
			}
		}
		
		if ( safe ) {
			//Teleporting the player
			player.teleport( randomLoc );
			
			return true;
			
		} else {
			
			return false;
			
		}
	}
	
	private boolean isSafe( Location loc, short height ) {
		
		int xCoordinate = loc.getBlockX();
		int zCoordinate = loc.getBlockZ();
		int yCoordinate = loc.getWorld().getHighestBlockYAt( xCoordinate, zCoordinate );
		
		for ( short i = -1; i <= (height-2); i++ ) {
			
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
				if ( block.isLiquid() || material.isSolid() ) {
					return false;
				}
				break;
				
			case 1:
				
				//Check if block is not liquid / solid
				if ( block.isLiquid() || material.isSolid() ) {
					return false;
				}
				break;
				
			default:
				
				//Throw Error
				Main.toConsole(2, "Something went wrong! ERROR#000" );
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