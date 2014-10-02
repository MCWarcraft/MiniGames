package minigames.gametypes;

import java.util.ArrayList;
import java.util.UUID;

import minigames.MiniGamePlayerStatus;
import minigames.MiniGames;
import minigames.tasks.StartGameTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import core.Kits.KitLockManager;
import core.Kits.KitScoreboardConnector;
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
	protected KitScoreboardConnector kitScoreboardConnector;
	
	protected ArrayList<UUID> livingPlayers;
	protected ArrayList<UUID> deadPlayers;
	
	public MiniGame(int playersToStart, Location spawnLocation, MiniGames plugin)
	{
		this.playersToStart = playersToStart;
		this.spawnLocation = spawnLocation;
		hasStarted = false;
		this.plugin = plugin;
		
		this.kitScoreboardConnector = new KitScoreboardConnector();
		
		livingPlayers = new ArrayList<UUID>();
		deadPlayers = new ArrayList<UUID>();
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
	
	public abstract void removePlayer(UUID playerUUID);
	
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
		
		livingPlayers.add(player.getUniqueId());
		
		if (message)
		{
			for (Player p : Bukkit.getServer().getOnlinePlayers())
				p.sendMessage(ChatColor.GREEN + player.getName() + ChatColor.GOLD + " has joined the minigame.");
			if (!canStart())
				for (Player p : Bukkit.getServer().getOnlinePlayers())
					p.sendMessage(ChatColor.GOLD + "The game can start with " + ChatColor.RED + getPlayersNeeded() + ChatColor.GOLD + " more players.");
		}
		
		plugin.getMiniGamesOperator().setInLobby(player.getUniqueId(), MiniGamePlayerStatus.IN_GAME);
		
		KitLockManager.setCanEquip(false, player.getUniqueId());
		
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
		
		for (UUID playerUUID : livingPlayers)
			players.add(Bukkit.getServer().getPlayer(playerUUID));
		for (UUID playerUUID : deadPlayers)
			players.add(Bukkit.getServer().getPlayer(playerUUID));
		
		return players;
	}
	
	public ArrayList<UUID> getPlayerUUIDs()
	{
		ArrayList<UUID> playerUUIDs = new ArrayList<UUID>();
		
		for (UUID playerUUID : livingPlayers)
			playerUUIDs.add(playerUUID);
		for (UUID playerUUID : deadPlayers)
			playerUUIDs.add(playerUUID);
		
		return playerUUIDs;
	}
	
	public boolean containsPlayer(UUID playerUUID)
	{
		if (livingPlayers.contains(playerUUID) || deadPlayers.contains(playerUUID))
			return true;
		return false;
	}
	
	public Location getSpawnLocation()
	{
		return spawnLocation;
	}
}
