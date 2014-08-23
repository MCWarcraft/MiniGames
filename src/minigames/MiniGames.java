package minigames;

import java.util.HashMap;

import minigames.gametypes.KOTHMiniGame;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import core.Save.CoreSavable;
import core.Save.CoreSaveManager;
import core.Utilities.LocationParser;

public class MiniGames extends JavaPlugin implements CoreSavable
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
		
		CoreSaveManager.addSavable(this);
		
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
		
		getConfig().set("hilltop", LocationParser.locationToString(KOTHMiniGame.getHillTopLocation()));
		
		saveConfig();
	}
	
	public void loadData()
	{
		miniGamesOperator.setLobbyLocation(LocationParser.parseLocation(getConfig().getString("general.lobby")));
		
		ConfigurationSection spawnLocationSection = this.getConfig().getConfigurationSection("spawns");
		if (spawnLocationSection != null)
			for (String gameTypeName : spawnLocationSection.getKeys(false))
				miniGamesOperator.setSpawnLocation(gameTypeReverse.get(gameTypeName), LocationParser.parseLocation(spawnLocationSection.getString(gameTypeName)));
		
		KOTHMiniGame.setHillTopLocation(LocationParser.parseLocation(getConfig().getString("hilltop")));
		KOTHMiniGame.setHillCheckTicks(getConfig().getInt("hillticks"));
		KOTHMiniGame.setVictoryScore(getConfig().getInt("kothscorelimit"));
		KOTHMiniGame.setHillRadius(getConfig().getDouble("hillradius"));
	}
	
	public MiniGamesOperator getMiniGamesOperator()
	{
		return miniGamesOperator;
	}
	
	public HashMap<String, GameType> getGameTypeReverse()
	{
		return gameTypeReverse;
	}
	
	@Override
	public void coreSave()
	{
		saveData();
	}
}