package net.jahcraft.westernbounties.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.jahcraft.westernbounties.data.Bounty;
import net.jahcraft.westernbounties.data.BountyManager;
import net.jahcraft.westernbounties.data.BountyReward;
import net.jahcraft.westernbounties.data.RewardManager;
import net.jahcraft.westernbounties.main.Main;
import net.jahcraft.westernbounties.util.Colors;
import net.md_5.bungee.api.ChatColor;

public class BountyCommand implements CommandExecutor, TabCompleter {

	List<String> arguments1 = new ArrayList<>();
	
	public static List<Player> claimingRewards = new ArrayList<>();
	
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		
		arguments1.clear();
		arguments1.add("set");
		arguments1.add("list");
		arguments1.add("pay");
		arguments1.add("claim");
		
		if (sender.hasPermission("westernbounties.admin")) {
			arguments1.add("clear");
		}

		List<String> result = new ArrayList<>();
		if (args.length == 1) {
			for (String s : arguments1) {
				if (s.toLowerCase().startsWith(args[0])) {
					result.add(s);
				}
			}
			return result;
		} 
		return null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!label.equalsIgnoreCase("bounty")) return false;
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: /bounty <set/list/pay/claim>");
			return true;
		}
		else if (args[0].equalsIgnoreCase("set")) set(sender,args);
		else if (args[0].equalsIgnoreCase("list")) list(sender,args);
		else if (args[0].equalsIgnoreCase("pay")) pay(sender,args);
		else if (args[0].equalsIgnoreCase("claim")) claim(sender,args);
		else if (args[0].equalsIgnoreCase("clear")) clear(sender,args);
		else if (args[0].equalsIgnoreCase("rewards")) rewards(sender,args);
		else sender.sendMessage(ChatColor.RED + "Usage: /bounty <set/list/pay/claim>");

		return true;
	}
	
	private void rewards(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) return;
		if (!sender.hasPermission("westernbounties.rewards")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
//		if (!RewardManager.hasReward((Player)sender)) {
//			sender.sendMessage(ChatColor.RED + "You don't have any rewards to claim!");
//			return;
//		}
		Player player = (Player) sender;
//		BountyReward r = RewardManager.getReward(player);
//		ItemStack[] contents = RewardManager.getRewardContents(r);
		
		for (BountyReward r : RewardManager.getAllRewards()) {
			player.sendMessage(Colors.BRIGHTBLUE + r.getVictim());
		}
		
	}
	
	private void claim(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) return;
		if (!sender.hasPermission("westernbounties.claim")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
		if (!RewardManager.hasReward((Player)sender)) {
			sender.sendMessage(ChatColor.RED + "You don't have any rewards to claim!");
			return;
		}
		Player player = (Player) sender;
		BountyReward r = RewardManager.getReward(player);
		ItemStack[] contents = RewardManager.getRewardContents(r);
		
		Inventory inv = Bukkit.createInventory(player, 54, "Items Dropped On Exit");
		inv.setContents(contents);
		player.openInventory(inv);
		player.giveExp(r.getXP());
		player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
		claimingRewards.add(player);
		
		RewardManager.removeReward(r);
		
		if (RewardManager.hasReward(player)) {
			player.sendMessage(Colors.BLUE + "You claimed " + Colors.GOLD + r.getVictim() + Colors.BLUE + "'s loot!");
			player.sendMessage(Colors.BLUE + "You still have more rewards to claim! " + Colors.BRIGHTBLUE + "/bounty claim");
		}
		
	}
	
	private void pay(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) return;
		if (!sender.hasPermission("westernbounties.pay")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
		if (!BountyManager.hasBounty((Player)sender)) {
			sender.sendMessage(ChatColor.RED + "You don't have a bounty to pay off!");
			return;
		}
		Player player = (Player) sender;
		Bounty b = BountyManager.getBounty(player);
		if (Main.plugin.eco.getBalance(player) < b.getRawReward()) {
			sender.sendMessage(ChatColor.RED + "You can't afford to pay off your bounty!");
			return;
		}
		
		BountyManager.removeBounty(player);
		Main.plugin.eco.withdrawPlayer(player, b.getRawReward());
		
		player.sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "‣ " + Colors.BRIGHTBLUE + "You paid off your bounty of " + ChatColor.GREEN + b.getReward() + Colors.BRIGHTBLUE + "!");
		Bukkit.broadcastMessage(Colors.BLUE + "" + ChatColor.BOLD + "‣ " + Colors.GOLD + player.getDisplayName() + Colors.BRIGHTBLUE + " paid off their bounty of " + ChatColor.GREEN + b.getReward() + Colors.BRIGHTBLUE + "!");
		
	}
	
	private void clear(CommandSender sender, String[] args) {
		if (!sender.hasPermission("westernbounties.clear")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
		if (args.length != 2) {
			sender.sendMessage(ChatColor.RED + "Usage: /bounty clear <name/all>");
			return;
		}
		
		if (args[1].equalsIgnoreCase("all")) {
			if (BountyManager.getBounties().size() == 0) {
				sender.sendMessage(ChatColor.RED + "There are no bounties left to clear!");
				return;
			} else {
				BountyManager.clearBounties();
				sender.sendMessage(Colors.BLUE + "All bounties cleared!");
				return;
			}
		}
		
		if (Bukkit.getPlayer(args[1]) != null) {
			Player target = Bukkit.getPlayer(args[1]);
			if (BountyManager.hasBounty(target)) {
				BountyManager.removeBounty(target);
				sender.sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "‣ " + Colors.GOLD + target.getDisplayName() + Colors.BLUE + "'s bounty has been cleared!");
				target.sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "‣ " + Colors.BRIGHTBLUE + "Your bounty has been cleared!");
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "That player doesn't have an active bounty!");
				return;
			}
		} else {
			if (BountyManager.hasBounty(args[1])) {
				Bounty b = BountyManager.getBounty(args[1]);
				BountyManager.removeBounty(args[1]);
				sender.sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "‣ " + Colors.GOLD + b.getName() + Colors.BLUE + "'s bounty has been cleared!");
				return;
			} else {
				sender.sendMessage(ChatColor.RED + "That player doesn't have an active bounty!");
				return;
			}
		}
		
			
	}
	
	private void list(CommandSender sender, String[] args) {
		if (!sender.hasPermission("westernbounties.list")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
		
		List<Bounty> bounties = BountyManager.getBounties();
		
		if (bounties.size() == 0) {
			sender.sendMessage(Colors.BLUE + "There are currently no active bounties!");
			return;
		} else {
			if (bounties.size() == 1) sender.sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "» " + Colors.BRIGHTRED + bounties.size() + " Active Bounty" + Colors.BLUE + ":");
			else sender.sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "» " + Colors.BRIGHTRED + bounties.size() + " Active Bounties" + Colors.BLUE + ":");
			for (Bounty b : bounties) {
				sender.sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "‣ " + Colors.BRIGHTBLUE + "Player: " + Colors.GOLD + b.getName() + Colors.BLUE + " | " + Colors.BRIGHTBLUE + "Reward: " + ChatColor.GREEN + "$" + b.getReward());
			}
		}		
	}

	private void set(CommandSender sender, String[] args) {
		if (!sender.hasPermission("westernbounties.set")) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
			return;
		}
		if (args.length != 3) {
			sender.sendMessage(ChatColor.RED + "Usage: /bounty set <player> <amount>"); 
			return;
		}
		if (Bukkit.getPlayer(args[1]) == null) {
			sender.sendMessage(ChatColor.RED + "Player not found!"); 
			return;
		}
		if (Bukkit.getPlayer(args[1]).equals(sender)) {
			sender.sendMessage(ChatColor.RED + "You can't set a bounty on yourself!"); 
			return;
		}
		try {
			Double.parseDouble(args[2]);
		} catch (NumberFormatException e) {
			sender.sendMessage(ChatColor.RED + "Invalid amount!"); 
			return;
		}
		
		Player target = Bukkit.getPlayer(args[1]);
		
		if (BountyManager.hasBounty(target)) {
			sender.sendMessage(ChatColor.RED + "That player already has a bounty!"); 
			return;
		}
		
		Bounty b = null;
		
		if (sender instanceof Player) {
			BountyManager.addBounty(target, (Player) sender, Double.parseDouble(args[2]));
			b = BountyManager.getBounty(target);
			Bukkit.broadcastMessage(Colors.GOLD + ((Player) sender).getDisplayName() + Colors.BRIGHTRED + " has placed a bounty on " + Colors.GOLD + target.getDisplayName() + Colors.BRIGHTRED + " worth " + ChatColor.GREEN + "$" + b.getReward() + Colors.BRIGHTRED + "!");
		} else {
			BountyManager.addBounty(target, null, Double.parseDouble(args[2]));
			b = BountyManager.getBounty(target);
			Bukkit.broadcastMessage(Colors.BRIGHTRED + "A bounty has been placed on " + Colors.GOLD + target.getDisplayName() + Colors.BRIGHTRED + " worth " + ChatColor.GREEN + "$" + b.getReward() + Colors.BRIGHTRED + "!");
		}
		
		sender.sendMessage(Colors.BLUE + "Bounty has been set on player " + Colors.GOLD + target.getDisplayName() + Colors.BLUE + " for " + Colors.GOLD + "$" + b.getReward() + Colors.BLUE + "!");
		
	}

}
