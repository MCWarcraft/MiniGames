package minigames.tasks;

import java.util.HashMap;
import java.util.UUID;

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
		
		HashMap<UUID, MiniGamePlayerStatus> minigamePlayers = plugin.getMiniGamesOperator().getMinigamePlayerUUIDs();
		
		for (UUID playerUUID : minigamePlayers.keySet())
			if (CoreScoreboardManager.getDisplayBoard(playerUUID) != null && minigamePlayers.get(playerUUID) == MiniGamePlayerStatus.IN_LOBBY)
				CoreScoreboardManager.getDisplayBoard(playerUUID).setTitle(getTimeString(), "");
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