package net.jahcraft.westernbounties.data;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonStreamParser;

import net.jahcraft.westernbounties.main.Main;
import net.jahcraft.westernbounties.util.Colors;
import net.md_5.bungee.api.ChatColor;


public class RewardManager {
	
	private static Map<BountyReward, ItemStack[]> rewardContents;
	private static List<BountyReward> bountyRewards;
	
	
	public static File getRewardsFile() {
		return new File(Main.plugin.getDataFolder(), "bountyrewards.json");
	}
	
	
	
	
	public static void loadRewards() {
		if (!getRewardsFile().exists()) {
			Bukkit.getLogger().info("No Reward DB detected, generating blank list...");
			bountyRewards = new ArrayList<>();
			return;
		} else {
			Bukkit.getLogger().info("Loading Rewards...");
			List<BountyReward> rewards = new ArrayList<>();
			bountyRewards = new ArrayList<>();
			rewardContents = new HashMap<>();
			Gson gson = new Gson();
			
			try {
				JsonStreamParser parser = new JsonStreamParser(new FileReader(getRewardsFile()));
				
				
				while (parser.hasNext()) {
					rewards.add(gson.fromJson(parser.next(), BountyReward.class));
				}
				
				bountyRewards = rewards;
				
				convertRewards();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				Bukkit.getLogger().severe("Rewards file not found! Disabling!");
				Bukkit.getServer().getPluginManager().disablePlugin(Main.plugin);
				return;
			} catch (JsonIOException e) {
				bountyRewards = new ArrayList<>();
				return;
			}
			
		}
	}
	
	private static void convertRewards() {
		rewardContents = new HashMap<>();
		for (BountyReward b : bountyRewards) {
			rewardContents.put(b, stacksFromBase64(b.contents64));
		}
	}
	
	public static void writeRewards() {
		
		try {
			Gson gson = new Gson();
			
			File db = getRewardsFile();
			if (!db.exists()) Main.plugin.saveResource("bountyrewards.json", false);
			
			FileWriter fileWriter = new FileWriter(getRewardsFile());
			BufferedWriter writer = new BufferedWriter(fileWriter);
			
			for (BountyReward b : bountyRewards) {
				String jsonString = gson.toJson(b, BountyReward.class);
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
	
	public static List<BountyReward> getAllRewards() {
		return new ArrayList<>(bountyRewards);
	}
	
	public static void generateReward(ItemStack[] inventory, Bounty b, int xp) {
		new BukkitRunnable() {

			@Override
			public void run() {
				String name = b.getSetterRawName();
				String uuid = b.getSetterUUID();
				String contents = toBase64(inventory);
				
				BountyReward reward = new BountyReward(name, uuid, b.getName(), contents, xp);
				bountyRewards.add(reward);
				rewardContents.put(reward, inventory);
				
				if (Bukkit.getPlayer(UUID.fromString(uuid)) != null) {
					Player rewarded = Bukkit.getPlayer(UUID.fromString(uuid));
					rewarded.sendTitle(Colors.GOLD + "" + ChatColor.BOLD + "Bounty Fulfilled!", Colors.BLUE + "Use " + Colors.BRIGHTBLUE + "/bounty claim" + Colors.BLUE + " to collect!", 1, 90, 1);
					rewarded.playSound(rewarded.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
				}
				
			}
			
		}.runTaskAsynchronously(Main.plugin);
	}
	
	public static boolean hasReward(Player player) {
		for (BountyReward r : bountyRewards) {
			if (player.getUniqueId().equals(UUID.fromString(r.getUUID()))) return true;
		}
		return false;
	}
	
	public static BountyReward getReward(Player player) {
		for (BountyReward r : bountyRewards) {
			if (player.getUniqueId().equals(UUID.fromString(r.getUUID()))) return r;
		}
		return null;
	}
	
	public static ItemStack[] getRewardContents(BountyReward reward) {
		if (!rewardContents.containsKey(reward)) return null;
		return rewardContents.get(reward);
	}
	
	public static void removeReward(BountyReward reward) {
		bountyRewards.remove(reward);
		rewardContents.remove(reward);
	}
	
	//Conversion to Base64 code courtesy of github.com/JustRayz
    public static String toBase64(ItemStack[] contents) {
        boolean convert = false;

        for (ItemStack item : contents) {
            if (item != null) {
                convert = true;
                break;
            }
        }

        if (convert) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

                dataOutput.writeInt(contents.length);

                for (ItemStack stack : contents) {
                    dataOutput.writeObject(stack);
                }
                dataOutput.close();
                byte[] byteArr = outputStream.toByteArray();
                return Base64Coder.encodeLines(byteArr);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to save item stacks.", e);
            }
        }

        return null;
    }
    
    public static ItemStack[] stacksFromBase64(String data) {
        if (data == null)
            return new ItemStack[]{};

        ByteArrayInputStream inputStream = null;

        try {
            inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        } catch (IllegalArgumentException e) {
            return new ItemStack[]{};
        }

        BukkitObjectInputStream dataInput = null;
        ItemStack[] stacks = null;

        try {
            dataInput = new BukkitObjectInputStream(inputStream);
            stacks = new ItemStack[dataInput.readInt()];
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        if (stacks == null)
            return new ItemStack[]{};

        for (int i = 0; i < stacks.length; i++) {
            try {
                stacks[i] = (ItemStack) dataInput.readObject();
            } catch (IOException | ClassNotFoundException | NullPointerException e) {
                //Backup generated before InventoryRollback v1.3
//                if (packageVersion == null) {
//                    Bukkit.getLogger().severe("There was an error deserializing the material data. This is likely caused by a now incompatible material ID if the backup was originally generated on a different Minecraft server version.");
//                }
//                //Backup was not generated on the same server version
//                else if (!packageVersion.equalsIgnoreCase(InventoryRollbackPlus.getPackageVersion())) {
//                	Bukkit.getLogger().severe(ChatColor.stripColor(MessageData.getPluginPrefix()) + "There was an error deserializing the material data. The backup was generated on a " + packageVersion + " version server whereas you are now running a " + InventoryRollback.getPackageVersion() + " version server. It is likely a material ID inside the backup is no longer valid on this Minecraft server version and cannot be convereted.");
//                }
//                //Unknown error
//                else if (packageVersion.equalsIgnoreCase(InventoryRollbackPlus.getPackageVersion())) {
                	Bukkit.getLogger().severe("There was an error deserializing the material data.");
//                }

                try {
                    dataInput.close();
                } catch (IOException e1) {
                	Bukkit.getLogger().severe("There was an error while terminating read of backup data after an error already occurred.");
                }
                return null;
            }
        }

        try {
            dataInput.close();
        } catch (IOException e1) {
        	Bukkit.getLogger().severe("There was an error while terminating read of backup data after normal read.");
        }

        return stacks;
    }
	
}
