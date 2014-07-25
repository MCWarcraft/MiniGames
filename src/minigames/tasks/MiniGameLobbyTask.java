package minigames.tasks;

import java.util.HashMap;

import minigames.MiniGamePlayerStatus;
import minigames.MiniGames;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

import core.Scoreboard.CoreScoreboardManager;

public class MiniGameLobbyTask extends BukkitRunnable
{
	private MiniGames plugin;
	
	private int timeToGame;
	private final int timerLength;
	
	public MiniGameLobbyTask(MiniGames plugin, int timerLength)
	{
		this.plugin = plugin;
		
		this.timerLength = timerLength;
		timeToGame = timerLength;
	}
	
	@Override
	public void run()
	{
		if (timeToGame > 1)
			timeToGame--;
		else
		{
			plugin.getMiniGamesOperator().openLobby();
			timeToGame = timerLength;
		}
		
		HashMap<String, MiniGamePlayerStatus> minigamePlayers = plugin.getMiniGamesOperator().getMinigamePlayers();
		
		for (String player : minigamePlayers.keySet())
			if (CoreScoreboardManager.getDisplayBoard(player) != null && minigamePlayers.get(player) == MiniGamePlayerStatus.IN_LOBBY)
				CoreScoreboardManager.getDisplayBoard(player).setTitle(getTimeString(), "");
	}

	public void restartTimer()
	{
		timeToGame = timerLength;
	}
	
	public String getTimeString()
	{
		return ChatColor.GOLD + "Next: " + ChatColor.GREEN + (timeToGame / 60) + ":" + (("" + (timeToGame % 60)).length() == 2 ? timeToGame % 60 : "0" + timeToGame % 60);
	}
}