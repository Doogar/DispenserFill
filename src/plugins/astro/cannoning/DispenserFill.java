package plugins.astro.cannoning;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class DispenserFill extends JavaPlugin implements Listener
{
	public static Logger logger = Logger.getLogger("Minecraft");

	@Override
	public void onEnable()
	{
		super.onEnable();
		plugin = this;

		FileConfiguration config = getConfig();
		config.options().copyDefaults(true);
		saveDefaultConfig();

		logger.info("DispenserFill has been enabled!");
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(this, this);
	}

	@Override
	public void onDisable()
	{
		super.onDisable();
		logger.info("DispenserFill has been disabled!");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) 
	{
		Player p = (Player)sender;
		if(cmd.getName().equalsIgnoreCase("cf"))
		{
			if(p.hasPermission("dispenserfill.use"))
			{
				if(args.length > 0)
				{
					if(args[0].equalsIgnoreCase("reload"))
					{
						if(p.hasPermission("dispenserfill.reload"))
						{
							reloadConfig();
							saveConfig();
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &6Successfully reloaded the config!"));
						}
						else
						{
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cError: insufficient permissions."));
						}
					}
					else if(args[0].equalsIgnoreCase("view"))
					{
						if(p.hasPermission("dispenserfill.view"))
						{
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &6Variables currently set in the config:"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Fill Radius&8: &e" + getFillRadius()));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Chat Prefix&8: &e" + getPluginPrefix()));
						}
						else
						{
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cError: insufficient permissions."));
						}
					}
					else if(args[0].equalsIgnoreCase("help"))
					{
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &6Help Menu"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/cf&8: &eFills nearby dispensers!"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/cf reload&8: &eReloads the config."));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/cf set chatprefix <prefix>&8: &eSets a new chat prefix!"));
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6/cf set fillradius <prefix>&8: &eSets a new fill radius!"));
					}
					else if(args[1] != null)
					{
						if(args[0].equalsIgnoreCase("set"))
						{
							if(args[1].equalsIgnoreCase("fillradius"))
							{
								if(p.hasPermission("dispenserfill.fillradius.set"))
								{
									if(args.length == 3)
									{
										int newRadius = 0;
										try
										{
											newRadius = Integer.parseInt(args[2]);
											if(newRadius >= 1)
											{
												getConfig().set("fillradius", newRadius);
												saveConfig();
												reloadConfig();
												p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &6Successfully updated variables!"));
											}
											else
											{
												p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cError, please pick a radius greater than or equal to 1."));
											}
										}
										catch(Exception e)
										{
											p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cError setting new radius."));
										}
									}
									else
									{
										p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cYou provided invalid arguments! /cf help for help."));
									}
								}
								else
								{
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cError: insufficient permissions."));
								}
							}
							else if(args[1].equalsIgnoreCase("chatprefix") && args.length >= 3)
							{
								if(p.hasPermission("dispenserfill.chatprefix.set"))
								{
									String newPrefix = "";
									for(int i = 2; i < args.length; i++)
									{
										newPrefix += args[i];
									}
									getConfig().set("chatprefix", newPrefix);
									saveConfig();
									reloadConfig();p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &6Successfully updated variables!"));
								}
								else
								{
									p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cError: insufficient permissions."));
								}
							}
							else
							{
								p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cYou provided invalid arguments! /cf help for help."));
							}
						}
					}
					else
					{
						p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cYou provided invalid arguments! /cf help for help."));
					}
				}
				else
				{
					int count = 0;
					int radius = getFillRadius();
					for (int x = -(radius); x <= radius; x ++)
					{
						for (int y = -(radius); y <= radius; y ++) 
						{
							for (int z = -(radius); z <= radius; z ++) 
							{
								if (p.getLocation().getBlock().getRelative(x, y, z).getType() == Material.DISPENSER)
								{
									count++;
									Block dispenser = p.getLocation().getBlock().getRelative(x, y, z);
									Inventory dispenserInv = ((Dispenser) dispenser.getState()).getInventory();
									fill(dispenserInv);
								}
							}
						}
					}
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r " + (count > 0 ? ("&6Filled &e" + count + " &6dispensers.") : ("&cNo dispensers were in range."))));
				}
			}
			else
			{
				p.sendMessage(ChatColor.translateAlternateColorCodes('&', getPluginPrefix() + "&r &cError: insufficient permissions."));
			}
		}
		return true;
	}

	public int getFillRadius()
	{
		return getConfig().getInt("fillradius");
	}

	public String getPluginPrefix()
	{
		return getConfig().getString("chatprefix");
	}

	public void fill(Inventory dispenserInv)
	{
		for(int i = 0; i < 9; i++)
		{
			if(dispenserInv.getItem(i) == null)
			{
				dispenserInv.setItem(i, new ItemStack(Material.TNT, 64));
			}
			else if((dispenserInv.getItem(i).getType() != null) && (dispenserInv.getItem(i).getType() == Material.TNT))
			{
				if(dispenserInv.getItem(i).getAmount() < 64)
				{
					dispenserInv.setItem(i, new ItemStack(Material.TNT, 64));
				}
			}
			else
			{
				dispenserInv.setItem(i, new ItemStack(Material.TNT, 64));
			}
		}
	}
}
