package minigames.gametypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import minigames.MiniGames;
import minigames.tasks.EndGameTask;
import minigames.tasks.HillCheckTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitTask;

import core.Event.PlayerZeroHealthEvent;
import core.HonorPoints.CurrencyOperations;
import core.Scoreboard.CoreScoreboardManager;
import core.Scoreboard.DisplayBoard;
import core.Scoreboard.GameBoard;
import core.Utilities.CoreUtilities;
import core.Utilities.HungerStopper;

public class KOTHMiniGame extends MiniGame implements Listener
{
	private HashMap<UUID, Integer> points;
	private ArrayList<UUID> hillList;
	
	private GameBoard gameBoard;
	
	private static Location hillTopLocation;
	private static int hillCheckTicks = 20, victoryScore = 100;
	private static double hillRadius = 8;
	
	private BukkitTask hillCheckTask;
	
	private HashMap<UUID, Integer> kills;
	
	public KOTHMiniGame(int playersToStart, Location spawnLocation, MiniGames plugin)
	{
		super(playersToStart, spawnLocation, plugin);
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		points = new HashMap<UUID, Integer>();
		hillList = new ArrayList<UUID>();
		
		hillCheckTask = new HillCheckTask(this).runTaskTimer(plugin, 0, hillCheckTicks);
		
		kills = new HashMap<UUID, Integer>();
	}

	@Override
	public void runGame()
	{
		//Start the game
		this.hasStarted = true;
		
		//
		for (Player player : this.getPlayers())
		{
			points.put(player.getUniqueId(), 0);
			kills.put(player.getUniqueId(), 0);
			HungerStopper.setCanGetHungry(player.getUniqueId());
			CoreScoreboardManager.getDisplayBoard(player).update(true);
		}
		
		generateGameScoreboard();
	}

	@Override
	public void endGame()
	{
		hillCheckTask.cancel();
		
		if (hasStarted)
		{
			UUID first = null, second = null, third = null;
			
			UUID tempPlayerUUID;
			UUID[] scorePlayerUUIDs = points.keySet().toArray(new UUID[points.size()]);
			
			int n = scorePlayerUUIDs.length;
			
	        //Sort entities
	        for(int i  = 0; i < n; i++)
	            for(int j = 1; j < (n - i); j++)
	                if(points.get(scorePlayerUUIDs[j]) > points.get(scorePlayerUUIDs[j - 1]))
	                {
	                    //swap the elements!
	                    tempPlayerUUID = scorePlayerUUIDs[j];
	                    scorePlayerUUIDs[j] = scorePlayerUUIDs[j - 1];
	                    scorePlayerUUIDs[j - 1] = tempPlayerUUID;
	                }
			
	        first = scorePlayerUUIDs[0];
	        if (scorePlayerUUIDs.length >= 2)
	        	second = scorePlayerUUIDs[1];
	        if (scorePlayerUUIDs.length >= 3)
	        	third = scorePlayerUUIDs[2];
	        
	        
	        String firstName = (Bukkit.getPlayer(first) == null) ? "Nobody" : Bukkit.getPlayer(first).getName(),
	        		secondName = (Bukkit.getPlayer(second) == null) ? "Nobody" : Bukkit.getPlayer(second).getName(),
	        		thirdName = (Bukkit.getPlayer(third) == null) ? "Nobody" : Bukkit.getPlayer(third).getName();
	        
	        //Send messages and distribute honor
			for (Player p : this.getPlayers())
			{
				p.sendMessage(ChatColor.AQUA + "-------------");
				p.sendMessage(ChatColor.AQUA + "KOTH RANKINGS");
				p.sendMessage(ChatColor.AQUA + "-------------");
				p.sendMessage(ChatColor.RED + "1st: " + ChatColor.GREEN + (first == null ? "Nobody" : firstName));
				p.sendMessage(ChatColor.GOLD + "2nd: " + ChatColor.GREEN + (second == null ? "Nobody" : secondName));
				p.sendMessage(ChatColor.YELLOW + "3rd: " + ChatColor.GREEN + (third == null ? "Nobody" : thirdName));
				p.sendMessage(ChatColor.AQUA + "-------------");
			
				p.sendMessage(ChatColor.AQUA + "You earned:");
				
				p.sendMessage(ChatColor.GREEN + "+ 10 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "participation");
				CurrencyOperations.giveCurrency(p, 10, true);
				
				//Placing messages
				if (first.equals(p.getUniqueId()))
				{
					p.sendMessage(ChatColor.GREEN + "+ 75 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "1st place");
					CurrencyOperations.giveCurrency(p, 75, true);
				}
				else if (second.equals(p.getUniqueId()))
				{
					p.sendMessage(ChatColor.GREEN + "+ 50 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "2nd place");
					CurrencyOperations.giveCurrency(p, 50, true);
				}
				else if (third.equals(p.getUniqueId()))
				{
					p.sendMessage(ChatColor.GREEN + "+ 25 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "3rd place");
					CurrencyOperations.giveCurrency(p, 25, true);
				}
				
				//Kill messages
				if (kills.get(p.getUniqueId()) != 0)
				{
					p.sendMessage(ChatColor.GREEN + "+ " + kills.get(p.getUniqueId()) +" Honor " + ChatColor.GOLD + "for " + ChatColor.RED + kills.get(p.getUniqueId()) + " kills");
					CurrencyOperations.giveCurrency(p, kills.get(p.getUniqueId()), true);
				}
				
				p.sendMessage(ChatColor.AQUA + "-------------");
			}
			
			new EndGameTask(this).runTaskLater(plugin, 60);
		}
		else
			endGamePost();
	}

	@Override
	public void endGamePost()
	{
		ArrayList<Player> players = this.getPlayers();
		
		
		//TODO This may need to be moved back to the end
		plugin.getMiniGamesOperator().clearActiveGame();
		
		//Return players to lobby
		for (Player player : players)
			plugin.getMiniGamesOperator().bringPlayer(player);
		
		//Unregister local listeners
		EntityDamageEvent.getHandlerList().unregister(this);
		PlayerZeroHealthEvent.getHandlerList().unregister(this);
		
		//Fix invis
		for (int i = 0; i < players.size() - 1; i++)
			for (int n = i + 1; n < players.size(); n++)
			{
				players.get(i).showPlayer(players.get(n));
				players.get(n).showPlayer(players.get(i));
			}
		
		//run this last
	}

	@Override
	public String getGameName()
	{
		return "KOTH";
	}

	@Override
	public void generateLobbyScoreboard(Player player)
	{
		DisplayBoard board = CoreScoreboardManager.getDisplayBoard(player);
		board.resetFormat();
		
		board.setScoreColor(ChatColor.GOLD);
		
		board.setTitle(startGameTask.getTimeString(), "");
		
		board.putDivider();
		
		board.putHeader(ChatColor.GREEN + "Selected Kit:");
		board.putField("", kitScoreboardConnector, player.getUniqueId().toString());
		
		board.putDivider();
	}

	public void generateGameScoreboard()
	{
		this.gameBoard = CoreScoreboardManager.getNewGameBoard();
		for (Player p : this.getPlayers())
			gameBoard.setScore(p, points.get(p.getUniqueId()));
		for (Player p : this.getPlayers())
			gameBoard.assign(p);
	}

	@Override
	public void removePlayer(UUID playerUUID)
	{
		livingPlayers.remove(playerUUID);
		deadPlayers.remove(playerUUID);
		points.remove(playerUUID);
		kills.remove(playerUUID);
		hillList.remove(playerUUID);
		
		if (this.hasStarted == true)
			//If the game is over
			if (livingPlayers.size() == 1)
			{
				//End the game
				endGame();
			}
		
		for (Player p : this.getPlayers())
			CoreScoreboardManager.getDisplayBoard(p).update(false);
	}

	@Override
	public int getPlayersNeeded()
	{
		if (this.getTotalPlayersNeeded() - livingPlayers.size() > 0)
			return this.getTotalPlayersNeeded() - livingPlayers.size();
		return 0;
	}

	public void checkHill()
	{
		for (Player p : this.getPlayers())
		{
			if (hillTopLocation.distance(p.getLocation()) < hillRadius)
			{
				if (!hillList.contains(p.getUniqueId()))
					hillList.add(p.getUniqueId());
			}
			else
				hillList.remove(p.getUniqueId());
		}
		
		if (hillList.size() != 0 && this.hasStarted)
		{
			gameBoard.setTitle(Bukkit.getPlayer(hillList.get(0)).getName());
			points.put(hillList.get(0), points.get(hillList.get(0)) + 1);
			gameBoard.setScore(Bukkit.getOfflinePlayer(hillList.get(0)), points.get(hillList.get(0)));
			if (points.get(hillList.get(0)) == victoryScore)
				endGame();
		}
		else if (hillList.size() == 0 && this.hasStarted)
			gameBoard.setTitle("No King!");
	}
	
	@EventHandler
	public void onPlayerDamageEvent(EntityDamageEvent event)
	{		
		if (!(event.getEntity() instanceof Player))
			return;
	
		Player player = (Player) event.getEntity();
		
		//If the player is in the game
		if (this.containsPlayer(player.getUniqueId()))
		{
			//If the game hasn't started yet
			if (this.hasStarted == false)
			{
				event.setCancelled(true);
				return;
			}
			//If the game has started
			else
			{
				//If the player is dead
				if (deadPlayers.contains(player.getUniqueId()))
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerZeroHealth(PlayerZeroHealthEvent event)
	{
		if (this.hasStarted == false)
			return;
		
		if (this.getPlayerUUIDs().contains(event.getPlayer().getUniqueId()))
		{
			hillList.remove(event.getPlayer().getUniqueId());
			event.getPlayer().setHealth(20);
			CoreUtilities.deathAnimation(event.getPlayer());
			event.getPlayer().teleport(this.getSpawnLocation());
			
			if (event.getDamager() != null)
				kills.put(event.getDamager().getUniqueId(), kills.get(event.getDamager().getUniqueId()) + 1);
		}
	}
	
	public static void setHillTopLocation(Location loc)
	{
		hillTopLocation = loc;
	}
	
	public static Location getHillTopLocation()
	{
		return hillTopLocation;
	}
	
	public static void setHillCheckTicks(int ticks)
	{
		hillCheckTicks = ticks;
	}
	
	public static void setVictoryScore(int score)
	{
		victoryScore = score;
	}
	
	public static void setHillRadius(double radius)
	{
		hillRadius = radius;
	}
}
