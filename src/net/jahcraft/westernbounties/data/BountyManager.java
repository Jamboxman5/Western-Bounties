package net.jahcraft.westernbounties.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonStreamParser;

import net.jahcraft.westernbounties.main.Main;
import net.jahcraft.westernbounties.util.Colors;
import net.md_5.bungee.api.ChatColor;

public class BountyManager {
	
	private static List<Bounty> activeBounties;
	
	public static File getDatabaseFile() {
		return new File(Main.plugin.getDataFolder(), "database.json");
	}

	public static void writeBounties() {
		
		try {
			Gson gson = new Gson();
			
			File db = getDatabaseFile();
			if (!db.exists()) Main.plugin.saveResource("database.json", false);
			
			FileWriter fileWriter = new FileWriter(getDatabaseFile());
			BufferedWriter writer = new BufferedWriter(fileWriter);
			
			for (Bounty b : activeBounties) {
				String jsonString = gson.toJson(b, Bounty.class);
				writer.write(jsonString);
				writer.newLine();
			}
			
			writer.flush();
			writer.close();
			
			fileWriter.flush();
			fileWriter.close();
			
		} catch (IOException e) {
//			e.printStackTrace();
//			Bukkit.getLogger().severe("UNABLE TO WRITE BOUNTY DATABASE! CRITICAL FAILURE!");
		}
		
	}
	
	public static void loadBounties() {
		if (!getDatabaseFile().exists()) {
			Bukkit.getLogger().info("No Bounty DB detected, generating blank list...");
			activeBounties = new ArrayList<>();
			return;
		} else {
			Bukkit.getLogger().info("Loading Bounties...");
			List<Bounty> bounties = new ArrayList<>();
			Gson gson = new Gson();
			
			try {
				JsonStreamParser parser = new JsonStreamParser(new FileReader(getDatabaseFile()));
				
				
				while (parser.hasNext()) {
					bounties.add(gson.fromJson(parser.next(), Bounty.class));
				}
				
				activeBounties = bounties;
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Bukkit.getLogger().severe("Database file not found! Disabling!");
				Bukkit.getServer().getPluginManager().disablePlugin(Main.plugin);
				return;
			} catch (JsonIOException e) {
				activeBounties = new ArrayList<>();
				return;
			}
			
		}
	}
	
	public static void reloadBounties(Player reloader) {
		
		new BukkitRunnable() {

			@Override
			public void run() {
				writeBounties();
				loadBounties();
				
				Bukkit.getLogger().info("Bounty Database reloaded");	
				if (reloader != null) reloader.sendMessage(Colors.BLUE + "Bounty Database Reloaded!");
			}
			
		}.runTaskAsynchronously(Main.plugin);
		
	}
	
	public static boolean hasBounty(Player target) {
		for (Bounty b : activeBounties) {
			if (b.targetUUID.equals(target.getUniqueId().toString())) return true;
		}
		return false;
	}
	
	public static boolean hasBounty(String name) {
		for (Bounty b : activeBounties) {
			if (b.target.equals(name)) return true;
		}
		return false;
	}
	
	public static void removeBounty(Player target) {
		List<Bounty> bounties = new ArrayList<>();
		for (Bounty b : activeBounties) {
			if (b.targetUUID.equals(target.getUniqueId().toString())) bounties.add(b);
		}
		activeBounties.removeAll(bounties);
	}
	
	public static void removeBounty(String name) {
		List<Bounty> bounties = new ArrayList<>();
		for (Bounty b : activeBounties) {
			if (b.target.equals(name)) bounties.add(b);
		}
		activeBounties.removeAll(bounties);
	}
	
	public static void addBounty(Player target, Player setter, double reward) {
		if (hasBounty(target)) return;
		Bounty bounty = new Bounty(target, setter, reward);
		activeBounties.add(bounty);
	}
	
	public static Bounty getBounty(Player target) {
		for (Bounty b : activeBounties) {
			if (b.targetUUID.equals(target.getUniqueId().toString())) return b;
		}
		return null;
	}
	
	public static List<Bounty> getBounties() {
		return new ArrayList<Bounty>(activeBounties);
	}
	
	public static void clearBounties() { 
		
		for (Bounty b : activeBounties) {
			if (Bukkit.getPlayer(UUID.fromString(b.targetUUID)) != null) {
				Bukkit.getPlayer(UUID.fromString(b.targetUUID)).sendMessage(Colors.BLUE + "" + ChatColor.BOLD + "‣ " + Colors.BRIGHTBLUE + "Your bounty has been cleared!");
			}
		}
		
		activeBounties.clear();
	}

	public static Bounty getBounty(String name) {
		for (Bounty b : activeBounties) {
			if (b.target.equals(name)) return b;
		}
		return null;
	}
	
}
