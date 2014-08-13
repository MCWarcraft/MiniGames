package minigames.gametypes;

import java.util.ArrayList;
import java.util.HashMap;

import minigames.MiniGames;
import minigames.tasks.EndGameTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import core.Event.PlayerZeroHealthEvent;
import core.HonorPoints.CurrencyOperations;
import core.Scoreboard.CoreScoreboardManager;
import core.Scoreboard.DisplayBoard;
import core.Scoreboard.ScoreboardValue;
import core.Utilities.CoreUtilities;

public class LMSMiniGame extends MiniGame implements ScoreboardValue, Listener
{	
	private String winnerName;
	
	private HashMap<String, Integer> kills;
	
	public LMSMiniGame(int playersToStart, Location spawnLocation, MiniGames plugin)
	{
		super(playersToStart, spawnLocation, plugin);
		
		kills = new HashMap<String, Integer>();
		
		//Register events to this game
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public void runGame()
	{
		//Start the game
		this.hasStarted = true;
		
		//
		for (Player player : this.getPlayers())
		{
			kills.put(player.getName(), 0);
			
			generateGameScoreboard(player);
			CoreScoreboardManager.getDisplayBoard(player).update(true);
		}
	}

	@Override
	public void endGame()
	{		
		if (winnerName != null)
		{
	        //Send messages and distribute honor
			for (Player p : this.getPlayers())
			{
				p.sendMessage(ChatColor.AQUA + "-------------");
				p.sendMessage(ChatColor.AQUA + "KOTH RANKINGS");
				p.sendMessage(ChatColor.AQUA + "-------------");
				p.sendMessage(ChatColor.RED + "Winner: " + ChatColor.GREEN + winnerName);
				p.sendMessage(ChatColor.AQUA + "-------------");
			
				p.sendMessage(ChatColor.AQUA + "You earned:");
				
				p.sendMessage(ChatColor.GREEN + "+ 10 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "participation");
				CurrencyOperations.giveCurrency(p, 10, true);
				
				//Placing messages
				if (winnerName.equals(p.getName()))
				{
					p.sendMessage(ChatColor.GREEN + "+ 100 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "1st place");
					CurrencyOperations.giveCurrency(p, 100, true);
				}
				
				//Kill messages
				if (kills.get(p.getName()) != 0)
				{
					p.sendMessage(ChatColor.GREEN + "+ " + kills.get(p.getName()) +" Honor " + ChatColor.GOLD + "for " + ChatColor.RED + kills.get(p.getName()) + " kills");
					CurrencyOperations.giveCurrency(p, kills.get(p.getName()), true);
				}
				
				p.sendMessage(ChatColor.AQUA + "-------------");
			}
			
			new EndGameTask(this).runTaskLater(plugin, 60);
			CurrencyOperations.giveCurrency(Bukkit.getOfflinePlayer(winnerName), 100, true);
		}
		else
			endGamePost();
	}
	
	@Override
	public void endGamePost()
	{
		ArrayList<Player> players = this.getPlayers();
		
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
		plugin.getMiniGamesOperator().clearActiveGame();
	}
	
	public String getGameName()
	{
		return "LMS";
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
		board.putField("", kitScoreboardConnector, player.getName());
		
		board.putDivider();
	}
	
	public void generateGameScoreboard(Player player)
	{
		DisplayBoard board = CoreScoreboardManager.getDisplayBoard(player);
		board.resetFormat();
		
		board.setTitle("LMS Game", "");
		board.putDivider();
		board.putField("Alive: ", this, "");	
		board.putDivider();
	}
	
	public void removePlayer(String playerName)
	{
		livingPlayers.remove(playerName);
		deadPlayers.remove(playerName);
		kills.remove(playerName);
		
		if (this.hasStarted == true)
			//If the game is over
			if (livingPlayers.size() == 1)
			{
				//End the game
				winnerName = livingPlayers.get(0);
				endGame();
			}
		
		for (Player p : this.getPlayers())
			CoreScoreboardManager.getDisplayBoard(p).update(false);
		
	}
	
	private void killPlayer(String playerName)
	{
		livingPlayers.remove(playerName);
		
		if (this.hasStarted == true)
		{
			deadPlayers.add(playerName);
			
			//If the game is over
			if (livingPlayers.size() == 1)
			{
				//End the game
				winnerName = livingPlayers.get(0);
				endGame();
			}
		}
		
		for (Player p : this.getPlayers())
			CoreScoreboardManager.getDisplayBoard(p).update(false);
	}

	@Override
	public String getScoreboardValue(String key)
	{
		return "" + livingPlayers.size();
	}
	
	@EventHandler
	public void onPlayerDamageEvent(EntityDamageEvent event)
	{		
		if (!(event.getEntity() instanceof Player))
			return;
	
		Player player = (Player) event.getEntity();
		
		//If the player is in the game
		if (this.containsPlayer(player.getName()))
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
				if (deadPlayers.contains(player.getName()))
				{
					event.setCancelled(true);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeathEvent(PlayerZeroHealthEvent event)
	{
		//If the dead player is part of this game
		if (this.containsPlayer(event.getPlayer().getName()))
		{
			//Change visibility
			for (Player player : this.getPlayers())
				player.hidePlayer(event.getPlayer());
			
			CoreUtilities.resetPlayerState(event.getPlayer(), true);
			
			CoreUtilities.deathAnimation(event.getPlayer());
			
			if (event.getDamager() != null)
				kills.put(event.getDamager().getName(), kills.get(event.getDamager().getName()) + 1);
			
			killPlayer(event.getPlayer().getName());
		}
	}

	@Override
	public int getPlayersNeeded()
	{
		if (this.getTotalPlayersNeeded() - livingPlayers.size() > 0)
			return this.getTotalPlayersNeeded() - livingPlayers.size();
		return 0;
	}
}
