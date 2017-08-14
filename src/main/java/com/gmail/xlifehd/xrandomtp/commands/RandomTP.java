package com.gmail.xlifehd.xrandomtp.commands;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.gmail.xlifehd.xrandomtp.Main;
import com.gmail.xlifehd.xrandomtp.TeleportUtils;

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
						
						//Get Economy and configuration
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
							if ( TeleportUtils.randomTeleport( player ) ) {
								
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