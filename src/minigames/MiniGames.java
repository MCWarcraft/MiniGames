package minigames;

import java.util.HashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import core.Utilities.LocationParser;

public class MiniGames extends JavaPlugin
{	
	private MiniGamesOperator miniGamesOperator;
	
	private MiniGamesExecutor miniGamesExecutor;
	
	private HashMap<String, GameType> gameTypeReverse;
	
	public void onEnable()
	{
		miniGamesOperator = new MiniGamesOperator(this);
		
		miniGamesExecutor = new MiniGamesExecutor(this);
		
		gameTypeReverse = new HashMap<String, GameType>();
		for (GameType type : GameType.values())
			gameTypeReverse.put(type.toString(), type);
		
		saveDefaultConfig();
		loadData();
		
		getCommand("minigames").setExecutor(miniGamesExecutor);
		getCommand("join").setExecutor(miniGamesExecutor);
		
		this.getServer().getPluginManager().registerEvents(new MiniGamesListener(this), this);
		
		getServer().getLogger().info("Minigames enabled");
	}
	
	public void onDisable()
	{
		saveData();
		getServer().getLogger().info("Minigames disabled");
	}
	
	public void saveData()
	{
		getConfig().set("general.lobby", LocationParser.locationToString(miniGamesOperator.getLobbyLocation()));
		
		for (GameType type : GameType.values())
			getConfig().set("spawns." + type.toString(), LocationParser.locationToString(miniGamesOperator.getSpawnLocation(type)));
		
		getConfig().set("hilltop", LocationParser.locationToString(miniGamesOperator.getHillTopLocation()));
		
		saveConfig();
	}
	
	public void loadData()
	{
		miniGamesOperator.setLobbyLocation(LocationParser.parseLocation(getConfig().getString("general.lobby")));
		
		ConfigurationSection spawnLocationSection = this.getConfig().getConfigurationSection("spawns");
		if (spawnLocationSection != null)
			for (String gameTypeName : spawnLocationSection.getKeys(false))
				miniGamesOperator.setSpawnLocation(gameTypeReverse.get(gameTypeName), LocationParser.parseLocation(spawnLocationSection.getString(gameTypeName)));
		
		miniGamesOperator.setHillTopLocation(LocationParser.parseLocation(getConfig().getString("hilltop")));
	}
	
	public MiniGamesOperator getMiniGamesOperator()
	{
		return miniGamesOperator;
	}
	
	public HashMap<String, GameType> getGameTypeReverse()
	{
		return gameTypeReverse;
	}
}