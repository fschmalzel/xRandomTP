package com.gmail.xlifehd.xrandomtp;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigLoader {
	private int maxRadius;
	private int minRadius;
	private int offsetX;
	private int offsetZ;
	private int tries;
	private int minHeight;
	private int maxHeight;
	private boolean nether;
	
	public ConfigLoader(String worldName) {
		
		//Loading the config
		FileConfiguration config = Main.getPlugin().getConfig();
		tries = config.getInt( "teleport.maxTries" );
		minHeight = config.getInt( "teleport.nether.minHeight" );
		maxHeight = config.getInt( "teleport.nether.maxHeight" );
		nether = config.getBoolean( "worlds." + worldName + ".nether" );
		
		if ( config.getBoolean("border.perWorldBorder") ) {

			maxRadius = config.getInt( "worlds." + worldName + "border.maxRadius" );
			minRadius = config.getInt( "worlds." + worldName + "border.minRadius" );
			offsetX = config.getInt( "worlds." + worldName + "border.offsetx" );
			offsetZ = config.getInt( "worlds." + worldName + "border.offsetz" );
			
		} else {
			
			maxRadius = config.getInt( "border.maxRadius" );
			minRadius = config.getInt( "border.minRadius" );
			offsetX = config.getInt( "border.offsetx" );
			offsetZ = config.getInt( "border.offsetz" );
			
		}
		
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

	public int getMinRadius() {
		return minRadius;
	}

	public int getMaxRadius() {
		return maxRadius;
	}

	public int getOffsetX() {
		return offsetX;
	}

	public int getOffsetZ() {
		return offsetZ;
	}

	public int getTries() {
		return tries;
	}

	public int getMinHeight() {
		return minHeight;
	}

	public int getMaxHeight() {
		return maxHeight;
	}

	public boolean isNether() {
		return nether;
	}
	
}
