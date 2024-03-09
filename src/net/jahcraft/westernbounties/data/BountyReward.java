package net.jahcraft.westernbounties.data;

public class BountyReward {
	
	String ownerUUID;
	String owner;
	String victim;
	String contents64;
	int xp;
	
	public BountyReward(String name, String uuid, String victim, String contents, int exp) {
		owner = name;
		ownerUUID = uuid;
		contents64 = contents;
		this.victim = victim;
		xp = exp;
	}
	
	public String getUUID() { return ownerUUID; }

	public String getVictim() { return victim; }

	public int getXP() {return xp;}
	
}
