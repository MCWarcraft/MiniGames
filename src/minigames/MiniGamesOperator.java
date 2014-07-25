package minigames;

import java.util.HashMap;

import minigames.gametypes.KOTHMiniGame;
import minigames.gametypes.LMSMiniGame;
import minigames.gametypes.MiniGame;
import minigames.tasks.MiniGameLobbyTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import core.Custody.Custody;
import core.HonorPoints.HonorConnector;
import core.Kits.KitLockManager;
import core.Kits.KitScoreboardConnector;
import core.Scoreboard.CoreScoreboardManager;
import core.Scoreboard.DisplayBoard;
import core.Utilities.CoreItems;
import core.Utilities.CoreUtilities;

public class MiniGamesOperator
{
	private MiniGames plugin;
	private Location lobbyLocation, hillTopLocation;
	
	private MiniGameLobbyTask sbTimeTask;
	private HonorConnector honorConnector;
	
	private HashMap<String, MiniGamePlayerStatus> minigamePlayers;
	private MiniGame activeGame;
	
	private GameType nextGameType;
	
	private HashMap<GameType, Location> spawnLocations;
	
	private KitScoreboardConnector kitScoreboardConnector;
	
	public MiniGamesOperator(MiniGames miniGamesPlugin)
	{
		this.plugin = miniGamesPlugin;
		
		honorConnector = new HonorConnector();
		
		minigamePlayers = new HashMap<String, MiniGamePlayerStatus>();
		
		spawnLocations = new HashMap<GameType, Location>();
		
		nextGameType = GameType.KOTH;
		
		kitScoreboardConnector = new KitScoreboardConnector();
		
		sbTimeTask = new MiniGameLobbyTask(plugin, plugin.getConfig().getInt("general.timer"));
		sbTimeTask.runTaskTimer(plugin, 20, 20);
	}
	
	public boolean isInMiniGames(String playerName)
	{
		return minigamePlayers.containsKey(playerName);
	}
	
	@SuppressWarnings("deprecation")
	public boolean bringPlayer(Player player)
	{
		if (lobbyLocation == null) return false;
		
		//If the player is not tracked by the system
		//if (minigamePlayers.get(player.getName()) == null)
		Custody.switchCustody(player, "mini");
		
		setInLobby(player.getName(), MiniGamePlayerStatus.IN_LOBBY);
		KitLockManager.setCanNotEquip(player.getName());
		player.teleport(lobbyLocation);
		
		CoreUtilities.resetPlayerState(player, true);
		player.getInventory().addItem(CoreItems.COMPASS);
		player.getInventory().addItem(CoreItems.WATCH);
		player.getInventory().addItem(CoreItems.NETHER_STAR);
		player.updateInventory();
		
		generateLobbyScoreboard(player);
		CoreScoreboardManager.getDisplayBoard(player).update(true);
		
		return true;
	}
	
	public void removePlayer(String playerName)
	{
		minigamePlayers.remove(playerName);
		if (activeGame != null)
		{
			activeGame.removePlayer(playerName);
		}
	}
	
	public void generateLobbyScoreboard(Player player)
	{
		DisplayBoard board = CoreScoreboardManager.getDisplayBoard(player);
		
		board.resetFormat();
		
		board.setScoreColor(ChatColor.AQUA);
		
		board.setTitle(sbTimeTask.getTimeString(), "");
		board.putDivider();
		board.putHeader(ChatColor.GREEN + "Next Gametype:");
		board.putHeader(ChatColor.AQUA + nextGameType.toString().toUpperCase());
		board.putHeader(ChatColor.GREEN + "Selected Kit:");
		board.putField("", kitScoreboardConnector, player.getName());
		board.putHeader(ChatColor.GREEN + "Honor Points:");
		board.putField("", honorConnector, player.getName());
		board.putDivider();
		board.update(false);
	}
	
	public void broadcastToLobby(String message)
	{
		for (String player : minigamePlayers.keySet())
			if (plugin.getServer().getPlayer(player) != null && minigamePlayers.get(player) == MiniGamePlayerStatus.IN_LOBBY)
				plugin.getServer().getPlayer(player).sendMessage(message);
	}
	
	public void openLobby()
	{
		//If there's a running game
		if (activeGame != null)
		{	
			for (Player p : plugin.getServer().getOnlinePlayers())
				p.sendMessage(ChatColor.RED + "The " + nextGameType.toString().toUpperCase() + " game failed to start. There is already a game.");
			return;
		}

		//If the next gametype is KOTH
		if (nextGameType == GameType.KOTH)
		{
			//If the spawn is created for KOTH
			if (spawnLocations.get(GameType.KOTH) == null)
			{
				for (Player p : plugin.getServer().getOnlinePlayers())
					p.sendMessage(ChatColor.RED + "The " + nextGameType.toString().toUpperCase() + " game failed to start. There is no spawn location.");
				return;
			}
			//If the hilltop isn't set
			if (hillTopLocation == null)
			{
				for (Player p : plugin.getServer().getOnlinePlayers())
					p.sendMessage(ChatColor.RED + "The KOTH game failed to start. There is no hilltop location.");
				return;
			}
			
			activeGame = new KOTHMiniGame(3, spawnLocations.get(GameType.KOTH), hillTopLocation, plugin);		
			
			nextGameType = GameType.LMS;
		}
		
		//If the next gametype is LMS
		else if (nextGameType == GameType.LMS)
		{
			//If the spawn is created for LMS
			if (spawnLocations.get(GameType.LMS) == null)
			{
				for (Player p : plugin.getServer().getOnlinePlayers())
					p.sendMessage(ChatColor.RED + "The " + nextGameType.toString().toUpperCase() + " game failed to start. There is no spawn location.");
				return;
			}
			
			activeGame = new LMSMiniGame(3, spawnLocations.get(GameType.LMS), plugin);
			
			nextGameType = GameType.KOTH;
		}
		
		activeGame.startCountdown();
		
		//Move players
		for (String playerName : minigamePlayers.keySet())
			if (minigamePlayers.get(playerName) == MiniGamePlayerStatus.IN_LOBBY)
				activeGame.addPlayer(Bukkit.getServer().getPlayer(playerName), false);
		
		sbTimeTask.restartTimer();

	}
	
	public void setSpawnLocation(GameType type, Location loc)
	{
		spawnLocations.put(type, loc);
	}
	
	public void setHillTopLocation(Location loc)
	{
		this.hillTopLocation = loc;
	}
	
	public Location getHillTopLocation()
	{
		return hillTopLocation;
	}
	
	public MiniGame getActiveGame()
	{
		return activeGame;
	}
	
	public void clearActiveGame()
	{
		activeGame = null;
	}
	
	public Location getLobbyLocation()
	{
		return lobbyLocation;
	}
	
	public void setLobbyLocation(Location loc)
	{
		lobbyLocation = loc;
	}
	
	public HashMap<String, MiniGamePlayerStatus> getMinigamePlayers()
	{
		return minigamePlayers;
	}
	
	public void setInLobby(String playerName, MiniGamePlayerStatus playerStatus)
	{
		minigamePlayers.put(playerName, playerStatus);
	}
	
	public Location getSpawnLocation(GameType type)
	{
		return spawnLocations.get(type);
	}
	
	public MiniGamePlayerStatus getPlayerStatus(String playerName)
	{
		return minigamePlayers.get(playerName);
	}
}