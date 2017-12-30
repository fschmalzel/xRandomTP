package com.gmail.xlifehd.xrandomtp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.gmail.xlifehd.xrandomtp.Main;

public class RandomTPv2 implements CommandExecutor {

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        
        // TODO: Check isEnabled in that World, are there args?, money, 
        if (!(sender instanceof Player)) {
            sender.sendMessage("You have to be a Player to use this command!");
            return false;
        }
        
        if (!(sender.hasPermission("xrandomtp.rtp") || sender.isOp())) {
            sender.sendMessage("Insufficient Permission!");
            return false;
        }

        Player player = (Player) sender;
        
        if ( !Main.getPlugin().getConfig().getBoolean("worlds." + player.getWorld().getName() + ".enabled") ) {
            player.sendMessage(Main.infoPrefix + "\"/rtp\" is disabled in this world.");
            return false;
        }
        
            
        
        
        
        
        
        return false;
    }
    
}
