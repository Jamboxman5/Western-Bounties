package net.jahcraft.westernbounties.listeners;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import net.jahcraft.westernbounties.commands.BountyCommand;
import net.jahcraft.westernbounties.data.Bounty;
import net.jahcraft.westernbounties.data.BountyManager;
import net.jahcraft.westernbounties.data.RewardManager;
import net.jahcraft.westernbounties.main.Main;
import net.jahcraft.westernbounties.util.Colors;
import net.md_5.bungee.api.ChatColor;

public class BountyListeners implements Listener {
	
	public Player bountyAttacker;
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		if (e.getPlayer() == null) return;
		if (!BountyCommand.claimingRewards.contains(e.getPlayer())) return;
		BountyCommand.claimingRewards.remove(e.getPlayer());
		if (e.getInventory().getContents().length == 0) return;
		Location loc = e.getPlayer().getLocation();
		for (ItemStack item : e.getInventory().getContents()) {
			loc.getWorld().dropItemNaturally(loc, item);
		}
	}
	
	@EventHandler
	public void bountyAttack(EntityDamageByEntityEvent e) {
//		e.getDamager().sendMessage((!(e.getDamager() instanceof Player)) + "");
		if (!(e.getDamager() instanceof Player)) return;
//		e.getDamager().sendMessage((!(e.getEntity() instanceof Player)) + "");
		if (!(e.getEntity() instanceof Player)) return;
//		e.getDamager().sendMessage((!SkillManager.activePerk((Player)e.getDamager(), Perk.HITMAN)) + "");
		if (!BountyManager.hasBounty((Player) e.getEntity())) {
			bountyAttacker = null; 
			return;
		} else {
			bountyAttacker = (Player) e.getDamager();
		}
	}
	@EventHandler(priority = EventPriority.HIGH)
	public void bountyDeath(EntityDeathEvent e) {
//		e.getEntity().sendMessage((hitAttacker == null) + "");
		if (bountyAttacker == null) return;
//		e.getEntity().sendMessage((!(e.getEntity() instanceof Player)) + "");
		if (!(e.getEntity() instanceof Player)) return;
//		e.getEntity().sendMessage((!SkillManager.activePerk(hitAttacker, Perk.HITMAN)) + "");
		if (!BountyManager.hasBounty((Player) e.getEntity())) return;
		
		Player p = (Player) e.getEntity();
		int XP = e.getDroppedExp();
		List<ItemStack> contents = e.getDrops();
		
		Bounty b = BountyManager.getBounty(p);
		if (b.getSetterUUID().contains("world")) {
			BountyManager.removeBounty(p);
			
			Bukkit.broadcastMessage(Colors.GOLD + bountyAttacker.getDisplayName() + Colors.BRIGHTRED + " has collected " + Colors.GOLD + p.getDisplayName() + Colors.BRIGHTRED + "'s bounty!");			
			bountyAttacker.playSound(bountyAttacker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
			bountyAttacker.sendTitle(Colors.GOLD + "" + ChatColor.BOLD + "Bounty Collected!", ChatColor.GREEN + "+ $" + b.getReward(), 6, 90, 6);
			Main.plugin.eco.depositPlayer(bountyAttacker, b.getRawReward());
			e.getDrops().clear();
			return;
		}
		if (bountyAttacker.getUniqueId().equals(UUID.fromString(b.getSetterUUID()))) {
			bountyAttacker.sendMessage(ChatColor.RED + "You can't collect on a bounty you set!");
			return;
		}
		
		bountyAttacker.playSound(bountyAttacker.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
		bountyAttacker.sendTitle(Colors.GOLD + "" + ChatColor.BOLD + "Bounty Collected!", ChatColor.GREEN + "+ $" + b.getReward(), 6, 90, 6);
		Main.plugin.eco.depositPlayer(bountyAttacker, b.getRawReward());
		
		RewardManager.generateReward(contents.toArray(new ItemStack[contents.size()]), b, XP);
		BountyManager.removeBounty(p);
		
		Bukkit.broadcastMessage(Colors.GOLD + bountyAttacker.getDisplayName() + Colors.BRIGHTRED + " has collected " + Colors.GOLD + p.getDisplayName() + Colors.BRIGHTRED + "'s bounty!");
		e.getDrops().clear();
		e.setDroppedExp(0);
	}

}
