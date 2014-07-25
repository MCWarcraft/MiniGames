package minigames.gametypes;

import java.util.ArrayList;

import minigames.MiniGamePlayerStatus;
import minigames.MiniGames;
import minigames.tasks.StartGameTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import core.Kits.KitLockManager;
import core.Scoreboard.CoreScoreboardManager;
import core.Utilities.CoreItems;
import core.Utilities.CoreUtilities;

public abstract class MiniGame
{	
	private int playersToStart;
	protected boolean hasStarted;
	private Location spawnLocation;

	protected StartGameTask startGameTask;
	protected MiniGames plugin;
	
	protected ArrayList<String> livingPlayers;
	protected ArrayList<String> deadPlayers;
	
	public MiniGame(int playersToStart, Location spawnLocation, MiniGames plugin)
	{
		this.playersToStart = playersToStart;
		this.spawnLocation = spawnLocation;
		hasStarted = false;
		this.plugin = plugin;
		
		livingPlayers = new ArrayList<String>();
		deadPlayers = new ArrayList<String>();
	}
	
	public void startCountdown()
	{
		startGameTask = new StartGameTask(plugin);
		startGameTask.runTaskTimer(plugin, 0, 20);
	}
	
	public abstract void runGame();
	
	public abstract void endGame();
	
	public abstract void endGamePost();
	
	public abstract String getGameName();
	
	public abstract void generateLobbyScoreboard(Player player);
	
	public abstract void removePlayer(String playerName);
	
	public abstract int getPlayersNeeded();
	
	public int getTotalPlayersNeeded()
	{
		return this.playersToStart;
	}
	
	public boolean canStart()
	{
		return getPlayersNeeded() == 0;
	}
	
	@SuppressWarnings("deprecation")
	public void addPlayer(Player player, boolean message)
	{
		if (hasStarted)
			return;
		
		livingPlayers.add(player.getName());
		
		if (message)
		{
			for (Player p : Bukkit.getServer().getOnlinePlayers())
				p.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GOLD + " has joined the minigame.");
			if (!canStart())
				for (Player p : Bukkit.getServer().getOnlinePlayers())
					p.sendMessage(ChatColor.GOLD + "The game can start with " + ChatColor.RED + getPlayersNeeded() + ChatColor.GOLD + " more players.");
		}
		
		plugin.getMiniGamesOperator().setInLobby(player.getName(), MiniGamePlayerStatus.IN_GAME);
		
		KitLockManager.setCanEquip(false, player.getName());
		
		CoreUtilities.resetPlayerState(player, true);
		player.getInventory().addItem(CoreItems.COMPASS);
		player.getInventory().addItem(CoreItems.WATCH);
		player.getInventory().addItem(CoreItems.NETHER_STAR);
		player.updateInventory();
		
		player.teleport(spawnLocation);
		generateLobbyScoreboard(player);
		CoreScoreboardManager.getDisplayBoard(player).update(true);
	}
	
	public boolean hasStarted()
	{
		return hasStarted;
	}
	
	public ArrayList<Player> getPlayers()
	{
		ArrayList<Player> players = new ArrayList<Player>();
		
		for (String playerName : livingPlayers)
			players.add(Bukkit.getServer().getPlayer(playerName));
		for (String playerName : deadPlayers)
			players.add(Bukkit.getServer().getPlayer(playerName));
		
		return players;
	}
	
	public ArrayList<String> getPlayerNames()
	{
		ArrayList<String> players = new ArrayList<String>();
		
		for (String playerName : livingPlayers)
			players.add(playerName);
		for (String playerName : deadPlayers)
			players.add(playerName);
		
		return players;
	}
	
	public boolean containsPlayer(String playerName)
	{
		if (livingPlayers.contains(playerName) || deadPlayers.contains(playerName))
			return true;
		return false;
	}
	
	public Location getSpawnLocation()
	{
		return spawnLocation;
	}
}
