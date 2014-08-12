package minigames.tasks;

import minigames.gametypes.MiniGame;

import org.bukkit.scheduler.BukkitRunnable;

public class EndGameTask extends BukkitRunnable
{
	private MiniGame miniGame;
	
	public EndGameTask(MiniGame miniGame)
	{
		this.miniGame = miniGame;
	}
	
	@Override
	public void run()
	{
		miniGame.endGamePost();
	}
}