package net.jahcraft.westernbounties.data;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Bounty {
	
	String target;
	String targetUUID;
	String setter;
	String setterUUID;
	double payment;
	
	public Bounty(Player target, Player setter, double payment) {
		this.target = target.getName();
		this.targetUUID = target.getUniqueId().toString();
		if (setter != null) {
			this.setter = setter.getName();
			this.setterUUID = setter.getUniqueId().toString();
		} else {
			this.setter = "world";
			this.setterUUID = "world";
		}
		
		this.payment = payment;
	}
	
	public String getName() {
		if (Bukkit.getPlayer(UUID.fromString(targetUUID)) != null) return Bukkit.getPlayer(UUID.fromString(targetUUID)).getDisplayName();
		else return target;
	}
	
	public String getSetterRawName() { return setter; }
	public String getSetterUUID() { return setterUUID; }

	public String getReward() {
		return String.format("%,.2f", payment);
	}

	public double getRawReward() { return payment; }

}
