package minigames.tasks;

import minigames.gametypes.KOTHMiniGame;

import org.bukkit.scheduler.BukkitRunnable;

public class HillCheckTask extends BukkitRunnable
{
	KOTHMiniGame kothGame;
	
	public HillCheckTask(KOTHMiniGame kothGame)
	{
		this.kothGame = kothGame;
	}
	
	@Override
	public void run()
	{
		kothGame.checkHill();
	}	
}
