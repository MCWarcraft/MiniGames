package minigames;

import minigames.gametypes.KOTHMiniGame;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import core.Utilities.LocationSelector;

public class MiniGamesExecutor implements CommandExecutor
{
	private MiniGames plugin;
	private MiniGamesOperator operator;

	public MiniGamesExecutor(MiniGames miniGamesPlugin)
	{
		plugin = miniGamesPlugin;
		operator = plugin.getMiniGamesOperator();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		if (!(sender instanceof Player))
		{
			sender.sendMessage(ChatColor.RED + "Only players can use MiniGames commands.");
			return true;
		}

		Player player = (Player) sender;

		//If the root command is minigames
		if (cmd.getLabel().equalsIgnoreCase("minigames"))
		{
			//If there are no extra arguments
			if (args.length == 0)
			{
				//If the player can't be brought successfully
				if (!operator.bringPlayer(player))
					player.sendMessage(ChatColor.RED + "The nexus has not been set.");
			}
			//If there are some args supplied
			else if (args.length >= 1)
			{
				//If the player is dealing with the lobby
				if (args[0].equalsIgnoreCase("lobby"))
				{
					//If there are enough args
					if (args.length == 2)
					{
						//If the player is trying to set the lobby and has permission
						if (args[1].equalsIgnoreCase("set") && player.hasPermission("minigames.lobby.set"))
						{
							if (LocationSelector.getSelectedLocation(player.getUniqueId()) != null)
							{
								operator.setLobbyLocation(LocationSelector.getSelectedLocation(player.getUniqueId()));
								player.sendMessage(ChatColor.GREEN + "Minigames lobby location set.");
							}
							else
								player.sendMessage(ChatColor.RED + "You need to select a location with the stick");
						}
					}
					//If there isn't the right number of arguments
					else
						player.sendMessage(ChatColor.RED + "All lobby commands take 2 arguments");
				}
				//
				else if (args[0].equalsIgnoreCase("setspawn") && player.hasPermission("minigames.spawn.set"))
				{
					//If there are enough args
					if (args.length == 2)
					{
						//If there is a gamemode of the supplied name
						if (plugin.getGameTypeReverse().keySet().contains(args[1].toLowerCase()))
						{
							if (LocationSelector.getSelectedLocation(player.getUniqueId()) != null)
							{
								operator.setSpawnLocation(plugin.getGameTypeReverse().get(args[1].toLowerCase()), LocationSelector.getSelectedLocation(player.getUniqueId()));
								player.sendMessage(ChatColor.GREEN + args[1] + " spawn location set.");
							}
							else
								player.sendMessage(ChatColor.RED + "You need to select a location with the stick");
						}
						//If there is no gamemode of the supplied name
						else
							player.sendMessage(ChatColor.RED + "There is no gamemode called " + args[1]);
					}
					//If there isn't the right number of arguments
					else
						player.sendMessage(ChatColor.RED + "/minigames setspawn <gametype>");
				}
				//Else if the player is trying to set the hilltop
				else if (args[0].equalsIgnoreCase("sethilltop") && player.hasPermission("minigames.spawn.set"))
				{
					if (LocationSelector.getSelectedLocation(player.getUniqueId()) != null)
					{
						KOTHMiniGame.setHillTopLocation(LocationSelector.getSelectedLocation(player.getUniqueId()));
						player.sendMessage(ChatColor.GREEN + "Hilltop location set.");
					}
					else
						player.sendMessage(ChatColor.RED + "You need to select a location with the stick");
				}
				//If the command is for a force start
				else if (args[0].equalsIgnoreCase("start") && player.hasPermission("minigames.start"))
				{
					//If the right number of args is supplied
					if (args.length == 1)
						operator.openLobby();
					else
						player.sendMessage(ChatColor.RED + "/minigames start");
				}
			}
			return true;
		}
		//If the root command is join
		if (cmd.getLabel().equalsIgnoreCase("join"))
		{
			//If there's an active game
			if (operator.getActiveGame() != null)
			{
				//If the player isn't in it
				if (!operator.getActiveGame().getPlayerUUIDs().contains(player.getUniqueId()))
					operator.getActiveGame().addPlayer(player, true);
			}
			//If there isn't an active game in progress or there is a game that is already going
			else
			{
				if (!operator.bringPlayer(player))
					player.sendMessage(ChatColor.RED + "The nexus has not been set.");
			}

			return true;
		}

		return false;
	}
}