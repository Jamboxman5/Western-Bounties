package net.jahcraft.westernbounties.main;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.jahcraft.westernbounties.commands.BountyCommand;
import net.jahcraft.westernbounties.data.BountyManager;
import net.jahcraft.westernbounties.data.RewardManager;
import net.jahcraft.westernbounties.listeners.BountyListeners;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin {
	
	public Economy eco;
	public static Main plugin;

	@Override
	public void onEnable() {
				
		plugin = this;
		RewardManager.loadRewards();
		BountyManager.loadBounties();
		
		if (!setupEconomy()) {
			
			Bukkit.getLogger().info("Economy not detected! Disabling Western Bounties!");
			getServer().getPluginManager().disablePlugin(this);
			return;
			
		}
		
		getServer().getPluginManager().registerEvents(new BountyListeners(), this);
		
		getCommand("bounty").setExecutor((CommandExecutor)new BountyCommand());
		getCommand("bounty").setTabCompleter((TabCompleter)new BountyCommand());
		
		Bukkit.getLogger().info("WesternBounties loaded and enabled!");
		
	}
	
	@Override
	public void onDisable() {
		
		BountyManager.writeBounties();
		RewardManager.writeRewards();
		
		Bukkit.getLogger().info("WesternBounties unloaded and disabled!");

		
	}
	
	private boolean setupEconomy() {
		
		RegisteredServiceProvider<Economy> economy = getServer().
				getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		
		if (economy != null)
			eco = economy.getProvider();
		return (eco != null);
		
	}

}
