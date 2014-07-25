package minigames.tasks;

import java.util.Arrays;
import java.util.List;

import minigames.MiniGames;
import minigames.gametypes.MiniGame;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import core.Scoreboard.CoreScoreboardManager;

public class StartGameTask extends BukkitRunnable
{
	private MiniGames plugin;
	
	private int timeToGame;
	
	private static List<Integer> broadcastTimes = Arrays.asList(50, 40, 30, 20, 10);
	
	public StartGameTask(MiniGames plugin)
	{
		this.plugin = plugin;
		timeToGame = 60;
		broadcastStart();
		updateScoreboards();
	}
	
	@Override
	public void run()
	{
		if (timeToGame >= 1)
		{
			if (broadcastTimes.contains(timeToGame))
			{
				broadcastStart();
			}
			updateScoreboards();
			timeToGame--;
		}
		else
		{
			MiniGame game = plugin.getMiniGamesOperator().getActiveGame();
			
			//If the game can be started
			if (!game.canStart())
			{
				for (Player p : plugin.getServer().getOnlinePlayers())
					p.sendMessage(ChatColor.GOLD + "The " + ChatColor.RED + game.getGameName() + ChatColor.GOLD + " game didn't have enough entrants to start.");
				game.endGame();
			}
			//If the game can't be started
			else
			{
				for (Player p : plugin.getServer().getOnlinePlayers())
					p.sendMessage(ChatColor.GOLD + "A " + ChatColor.RED + game.getGameName() + ChatColor.GOLD + " game has started!");
				game.runGame();
			}
			
			this.cancel();
		}
	}
	
	private void broadcastStart()
	{
		for (Player p : plugin.getServer().getOnlinePlayers())
			//If the player isn't in the game already
			if (plugin.getMiniGamesOperator().getMinigamePlayers().get(p.getName()) == null)
				p.sendMessage(ChatColor.GOLD + "A " + ChatColor.RED + plugin.getMiniGamesOperator().getActiveGame().getGameName() + ChatColor.GOLD + " game is starting in " + timeToGame + " seconds. Type /join to enter!");
	}

	private void updateScoreboards()
	{
		//Update scoreboards
		for (Player player : plugin.getMiniGamesOperator().getActiveGame().getPlayers())
			CoreScoreboardManager.getDisplayBoard(player).setTitle(getTimeString(), "");
	}
	
	public String getTimeString()
	{
		return ChatColor.GOLD + "Starts: " + ChatColor.GREEN + (timeToGame / 60) + ":" + (("" + (timeToGame % 60)).length() == 2 ? timeToGame % 60 : "0" + timeToGame % 60);
	}
}
