package minigames;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import core.Custody.CustodySwitchEvent;
import core.Event.PlayerVoidDamageEvent;

public class MiniGamesListener implements Listener
{
	private MiniGames plugin;
	
	public MiniGamesListener(MiniGames plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onCustodySwitchEvent(CustodySwitchEvent event)
	{
		plugin.getMiniGamesOperator().removePlayer(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onDamage(EntityDamageEvent event)
	{	
		if (!(event.getEntity() instanceof Player))
			return;
	
		Player player = (Player) event.getEntity();
		
		//If the player is in the game
		if (plugin.getMiniGamesOperator().getPlayerStatus(player.getName()) == MiniGamePlayerStatus.IN_LOBBY)
			event.setCancelled(true);
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onVoidDamage(PlayerVoidDamageEvent event)
	{
		if (plugin.getMiniGamesOperator().getPlayerStatus(event.getPlayer().getName()) == MiniGamePlayerStatus.IN_LOBBY && !event.isUsed())
		{
			plugin.getMiniGamesOperator().bringPlayer(event.getPlayer());
			event.use();
		}
	}
}