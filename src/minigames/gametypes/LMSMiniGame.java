package minigames.gametypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

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
import core.Utilities.HungerStopper;

public class LMSMiniGame extends MiniGame implements ScoreboardValue, Listener
{	
	private UUID winnerUUID;
	
	private HashMap<UUID, Integer> kills;
	
	public LMSMiniGame(int playersToStart, Location spawnLocation, MiniGames plugin)
	{
		super(playersToStart, spawnLocation, plugin);
		
		kills = new HashMap<UUID, Integer>();
		
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
			kills.put(player.getUniqueId(), 0);
			HungerStopper.setCanGetHungry(player.getUniqueId());
			generateGameScoreboard(player);
			CoreScoreboardManager.getDisplayBoard(player).update(true);
		}
	}

	@Override
	public void endGame()
	{		
		if (winnerUUID != null)
		{
	        //Send messages and distribute honor
			for (Player p : this.getPlayers())
			{
				p.sendMessage(ChatColor.AQUA + "-------------");
				p.sendMessage(ChatColor.AQUA + "KOTH RANKINGS");
				p.sendMessage(ChatColor.AQUA + "-------------");
				p.sendMessage(ChatColor.RED + "Winner: " + ChatColor.GREEN + Bukkit.getPlayer(winnerUUID).getName());
				p.sendMessage(ChatColor.AQUA + "-------------");
			
				p.sendMessage(ChatColor.AQUA + "You earned:");
				
				p.sendMessage(ChatColor.GREEN + "+ 10 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "participation");
				CurrencyOperations.giveCurrency(p, 10, true);
				
				//Placing messages
				if (winnerUUID.equals(p.getUniqueId()))
				{
					p.sendMessage(ChatColor.GREEN + "+ 100 Honor " + ChatColor.GOLD + "for " + ChatColor.RED + "1st place");
					CurrencyOperations.giveCurrency(p, 100, true);
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
			CurrencyOperations.giveCurrency(Bukkit.getOfflinePlayer(winnerUUID), 100, true);
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
		board.putField("", kitScoreboardConnector, player.getUniqueId().toString());
		
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
	
	public void removePlayer(UUID playerUUID)
	{
		livingPlayers.remove(playerUUID);
		deadPlayers.remove(playerUUID);
		kills.remove(playerUUID);
		
		if (this.hasStarted == true)
			//If the game is over
			if (livingPlayers.size() == 1)
			{
				//End the game
				winnerUUID = livingPlayers.get(0);
				endGame();
			}
		
		for (Player p : this.getPlayers())
			CoreScoreboardManager.getDisplayBoard(p).update(false);
		
	}
	
	private void killPlayer(UUID playerUUID)
	{
		livingPlayers.remove(playerUUID);
		
		if (this.hasStarted == true)
		{
			deadPlayers.add(playerUUID);
			
			//If the game is over
			if (livingPlayers.size() == 1)
			{
				//End the game
				winnerUUID = livingPlayers.get(0);
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
		if (this.containsPlayer(player.getUniqueId()))
		{
			//If the game hasn't started yet
			if (this.hasStarted == false)
				event.setCancelled(true);
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
	public void onPlayerDeathEvent(PlayerZeroHealthEvent event)
	{
		if (this.hasStarted == false)
			return;
		
		//If the dead player is part of this game
		if (this.containsPlayer(event.getPlayer().getUniqueId()))
		{
			//Change visibility
			for (Player player : this.getPlayers())
				player.hidePlayer(event.getPlayer());
			
			CoreUtilities.resetPlayerState(event.getPlayer(), true);
			
			CoreUtilities.deathAnimation(event.getPlayer());
			
			if (event.getDamager() != null)
				kills.put(event.getDamager().getUniqueId(), kills.get(event.getDamager().getUniqueId()) + 1);
			
			killPlayer(event.getPlayer().getUniqueId());
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
