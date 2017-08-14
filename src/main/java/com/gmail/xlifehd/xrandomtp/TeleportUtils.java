package com.gmail.xlifehd.xrandomtp;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TeleportUtils {
	
	public static boolean randomTeleport(Player player) {
		
		ConfigLoader config = new ConfigLoader( player.getWorld().getName() );
		
		if ( config.isNether() ) {
			return randomNetherTeleport(player, config);
		} else {
			return randomNormalTeleport(player, config);
		}
		
	}
	
	private static boolean randomNormalTeleport(Player player, ConfigLoader config ) {
		
		//Loading the config
		int maxRadius = config.getMaxRadius();
		int minRadius = config.getMinRadius();
		int offsetX = config.getOffsetX();
		int offsetZ = config.getOffsetZ();
		int tries = config.getTries();
		World world = player.getWorld();
		
		for ( int i = 0; i < tries; i++ ) {
			
			//Getting random polar coordinates
			double distance = minRadius + ( Math.random() * ( maxRadius - minRadius + 1 ) );
			double angle = Math.random() * 2 * Math.PI;
			
			//Converting to Cartesian coordinates
			int x = (int) ( Math.cos( angle ) * distance ) + offsetX;
			int z = (int) ( Math.sin( angle ) * distance ) + offsetZ;
			int y = world.getHighestBlockYAt(x, z);
			
			if ( isSafe( world, x, y, z, 3 ) ) {
				
				Location loc = new Location( world, x + 0.5, y, z + 0.5 );
				player.teleport( loc );
				
				return true;
				
			}
			
		}
		
		return false;
	}
	
	private static boolean randomNetherTeleport( Player player, ConfigLoader config ) {
		
		//Loading the config
		int maxHeight = config.getMaxHeight();
		int minHeight = config.getMinHeight();
		int maxRadius = config.getMaxRadius();
		int minRadius = config.getMinRadius();
		int offsetX = config.getOffsetX();
		int offsetZ = config.getOffsetZ();
		int tries = config.getTries();
		
		World world = player.getWorld();
		
		//Try X times
		for ( int i = 0; i < tries; i++ ) {
			
			//Getting random polar coordinates
			double distance = minRadius + ( Math.random() * ( maxRadius - minRadius + 1 ) );
			double angle = Math.random() * 2 * Math.PI;
			
			//Converting to Cartesian coordinates
			int x = (int) ( Math.cos( angle ) * distance ) + offsetX;
			int z = (int) ( Math.sin( angle ) * distance ) + offsetZ;
			
//			//Initializing variables for the loop
//			boolean previousSolid = world.getBlockAt(x, y, z).getType().isSolid();
//			int y = minHeight;
//			
//			//Go from bottom to top to find a safe spot in the nether
//			do {
//				
//				y++;
//				boolean currentSolid = world.getBlockAt(x, y, z).getType().isSolid();
//				
//				if ( previousSolid && !currentSolid) {
//					
//					if ( isSafe(world, x, y, z, 3) ) {
//						
//						Location loc = new Location( world, x + 0.5, y, z + 0.5 );
//						player.teleport( loc );
//						
//						return true;
//						
//					}
//					
//				}
//				
//				previousSolid = currentSolid;
//				
//			} while ( y < maxHeight);
			
			
			//Generate random height and initialize variables for the loop
			int yUp = (int) (minHeight + ( Math.random() * ( maxHeight - minHeight + 1 ) ));
			int yDown = yUp;
			boolean upFinished = false;
			boolean downFinished = false;
			boolean previousSolidUp = world.getBlockAt(x, yUp, z).getType().isSolid();
			boolean previousSolidDown = previousSolidUp;
			
			//Go up and down from that yCoordinate until a safe location was found
			do {
				
				//Go up, if max > yUp
				if ( yUp <= maxHeight ) {
					
					yUp++;
					boolean currentSolid = world.getBlockAt(x, yUp, z).getType().isSolid();
					
					if ( previousSolidUp && !currentSolid) {
						
						if ( isSafe(world, x, yUp, z, 3) ) {
							
							Location loc = new Location( world, x + 0.5, yUp, z + 0.5 );
							player.teleport( loc );
							
							return true;
							
						}
						
					}
					previousSolidUp = currentSolid;
					
				} else {
					upFinished = true;
				}
				
				//Go down, if min < yDown
				if ( yDown >= minHeight ) {
					
					yDown--;
					boolean currentSolid = world.getBlockAt(x, yDown, z).getType().isSolid();
					
					if ( !previousSolidDown && currentSolid) {
						
						if ( isSafe(world, x, yDown + 1, z, 3) ) {
							
							Location loc = new Location( world, x + 0.5, yDown + 1, z + 0.5 );
							player.teleport( loc );
							
							return true;
							
						}
						
					}
					previousSolidDown = currentSolid;
					
				} else {
					downFinished = true;
				}
				
			} while ( !downFinished || !upFinished );
			
		}
		
		return false;
		
	}
	
	private static boolean isSafe( World world, int x, int y, int z, int height ) {
		
		for ( short i = -1; i <= (height-2); i++ ) {
			
			Block block = world.getBlockAt( x, y + i, z );
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
				
				//Check if block is liquid or not solid and if that is the case return false
				if ( block.isLiquid() || !material.isSolid() ) {
					return false;
				}
				break;
				
			default:
				
				//Check if block is liquid or solid and if that is the case return false
				if ( block.isLiquid() || material.isSolid() ) {
					return false;
				}
				break;
				
			}
			
		}
		
		return true;
		
	}
	
}