package me.emnichtdayt.handcuff;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.command.Command;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;

public class HandCuffMain extends JavaPlugin {
	private HandCuffMain instance;
	private HandCuffListener listener;

	private ArrayList<Player> cuffed = new ArrayList<>();
	private HashMap<Player, Player> carried = new HashMap<>();

	private String messageNoPermissions = "%NoPermissions%";
	private String messageSenderCuffed = "%YouAreCuffed%";
	private String messageNoPlayer = "%NoPlayer%";
	private String messageNoNearby = "%NoOneNearby";
	private String messageCuff = "%Cuffed%";
	private String messageUncuff = "%Uncuffed%";
	private String messagePlayerOffline = "%PlayerNotOnline%";
	private String messageCuffUsage = "%/handcuff%";
	private String messagePlayerNotCuffed = "%notCuffed%";
	private String messageStopCarried = "%stopCarry%";
	private String messageNotCarriedByYou = "%notCarriedByYou%";
	private String messsageCarried = "%messageCarried%";
	private String messageCarryUsage = "%/carry%";

	@Override
	public void onEnable() {
		instance = this;

		this.reloadConfig();
		this.getConfig().options().header("Hi.");
		this.getConfig().options().copyHeader(true);

		this.getConfig().addDefault("message.error.permissions", ChatColor.GREEN + "[HandCuff] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "You don't have permission!");
		this.getConfig().addDefault("message.error.senderCuffed", ChatColor.GREEN + "[HandCuff] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED
				+ "You try to reach to your handcuffs but you realise that you are cuffed. Bad day, really bad day...");
		this.getConfig().addDefault("message.error.senderNoPlayer", ChatColor.GREEN + "[HandCuff] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "The matrix can't do this!");
		this.getConfig().addDefault("message.error.noPlayerNearby", ChatColor.GREEN + "[HandCuff] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "There is no player nearby!");
		this.getConfig().addDefault("message.error.playerOffline", ChatColor.GREEN + "[HandCuff] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "That player is not online!");
		this.getConfig().addDefault("message.error.targetNotCuffed", ChatColor.GREEN + "[HandCuff] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "That player is not cuffed");
		this.getConfig().addDefault("message.error.targetNotCarriedByYou", ChatColor.GREEN + "[HandCuff] " + ChatColor.RED
				+ "Error: " + ChatColor.DARK_RED + "That player is not carried by you");
		this.getConfig().addDefault("message.success.uncuffed",
				ChatColor.GREEN + "[HandCuff] " + ChatColor.GOLD + "Uncuffed: ");
		this.getConfig().addDefault("message.success.cuffed",
				ChatColor.GREEN + "[HandCuff] " + ChatColor.GOLD + "Cuffed: ");
		this.getConfig().addDefault("message.success.carry",
				ChatColor.GREEN + "[HandCuff] " + ChatColor.GOLD + "You are now carrying: ");
		this.getConfig().addDefault("message.success.stopCarry",
				ChatColor.GREEN + "[HandCuff] " + ChatColor.GOLD + "You are no longer carrying: ");
		this.getConfig().addDefault("message.success.cuffed",
				ChatColor.GREEN + "[HandCuff] " + ChatColor.GOLD + "Cuffed: ");
		this.getConfig().addDefault("message.usage.cuff",
				ChatColor.GREEN + "[HandCuff] " + ChatColor.GOLD + "Write /handcuff to cuff/uncuff someone nearby (handcuff.cuff) or /handcuff [Name] to cuff/uncuff someone further away (handcuff.cuff.admin)");
		this.getConfig().addDefault("message.usage.carry",
				ChatColor.GREEN + "[HandCuff] " + ChatColor.GOLD + "Write /carry to carry someone nearby (handcuff.carry) or stop carrying the player you are carrying. Write /carry [Name] to carry/force stop carrying someone further away (handcuff.carry.admin)");

		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		this.saveDefaultConfig();

		this.rlConfig();

		listener = new HandCuffListener(this);
	}

	protected void rlConfig() {
		this.reloadConfig();

		setMessageNotCarriedByYou(this.getConfig().getString("message.error.targetNotCarriedByYou"));
		setMessagePlayerNotCuffed(this.getConfig().getString("message.error.targetNotCuffed"));
		setMessagePlayerOffline(this.getConfig().getString("message.error.playerOffline"));
		setMessageNoPermissions(this.getConfig().getString("message.error.permissions"));
		setMessageSenderCuffed(this.getConfig().getString("message.error.senderCuffed"));
		setMessageStopCarried(this.getConfig().getString("message.success.stopCarry"));
		setMessageNoPlayer(this.getConfig().getString("message.error.senderNoPlayer"));
		setMessageNoNearby(this.getConfig().getString("message.error.noPlayerNearby"));
		setMessageUncuff(this.getConfig().getString("message.success.uncuffed"));
		setMesssageCarried(this.getConfig().getString("message.success.carry"));
		setMessageCarryUsage(this.getConfig().getString("message.usage.carry"));
		setMessageCuffUsage(this.getConfig().getString("message.usage.cuff"));		
		setMessageCuff(this.getConfig().getString("message.success.cuffed"));	
		
	}

	public boolean onCommand(org.bukkit.command.CommandSender sender, Command cmd, String cmdlabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("handcuff") || cmd.getName().equalsIgnoreCase("festnehmen")) {
			if (args.length == 0) {
				if (!sender.hasPermission("HandCuff.cuff")) {
					sender.sendMessage(getMessageNoPermissions());
					return true;
				}

				if (!(sender instanceof Player)) {
					sender.sendMessage(getMessageNoPlayer());
					return true;
				}

				Player senderPlayer = (Player) sender;
				Player target = null;

				for (Entity nearby : senderPlayer.getNearbyEntities(2, 2, 2)) {
					if (nearby instanceof Player) {
						target = (Player) nearby;
						break;
					}
				}

				if (target != null) {
					handleCuffCommand(senderPlayer, target);
				} else {
					sender.sendMessage(getMessageNoNearby());
				}

				return true;
			} else if (args.length == 1) {
				if (!sender.hasPermission("HandCuff.cuff.admin")) {
					sender.sendMessage(getMessageNoPermissions());
					return true;
				}

				Player target = this.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage(getMessagePlayerOffline());
					return true;
				}
				
				if (isCuffed(target)) {
					uncuff(target);
					sender.sendMessage(getMessageUncuff() + target.getName());
				} else {
					cuff(target);
					sender.sendMessage(getMessageCuff() + target.getName());
				}
				
			} else {
				sender.sendMessage(getMessageCuffUsage());
			}
		}else if(cmd.getName().equalsIgnoreCase("carry")||cmd.getName().equalsIgnoreCase("tragen")) {
			if(args.length == 0) {
			if(!sender.hasPermission("handcuff.carry")) {
				sender.sendMessage(getMessageNoPermissions());
				return true;
			}
			
			if(!(sender instanceof Player)) {
				return true;
			}
			
			Player senderPlayer = (Player) sender;
			Player target = null;

			for (Entity nearby : senderPlayer.getNearbyEntities(2, 2, 2)) {
				if (nearby instanceof Player) {
					target = (Player) nearby;
					break;
				}
			}
			
			if(target == null) {
				return true;
			}
			
			handleTragenCommand(senderPlayer, target);
			}else if(args.length == 1) {
				if(!sender.hasPermission("handcuff.carry.admin")) {
					sender.sendMessage(getMessageNoPermissions());
					return true;
				}
				
				Player target = this.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage(getMessagePlayerOffline());
					return true;
				}
				
				if(isCarried(target)) {
					sender.sendMessage(getMessageUncuff() + " " + target.getName());
					stopCarry(target);
				}else if(sender instanceof Player){
					sender.sendMessage(getMessageCuff() + " " + target.getName());
					carry(target, (Player) sender);
				}else {
					sender.sendMessage("That player is not beeing carryied and you are no player.");
				}
				
			}else {
				sender.sendMessage(getMessageCarryUsage());
			}
			
		}
		return true;
	}

	private void handleTragenCommand(Player senderPlayer, Player target) {
		if(isCuffed(senderPlayer)) {
			senderPlayer.sendMessage(getMessageCuff());
			return;
		}
		
		if(!isCuffed(target)) {
			senderPlayer.sendMessage(getMessagePlayerNotCuffed() + " (" + target.getName() + ")");
			return;
		}
		
		for(Entity ent : senderPlayer.getPassengers()) {
			if(ent instanceof Player) {
				Player entPlayer = (Player) ent;
				if(getCarried().containsKey(entPlayer)) {
					stopCarry(entPlayer);
					senderPlayer.sendMessage(getMessageStopCarried() + " " + ent.getName());
					return;
				}
			}
		}
		
		if(isCarried(target)) {
			if(isCarried(target, senderPlayer)) {
				stopCarry(target);
				senderPlayer.sendMessage(getMessageStopCarried() + " " +  target.getName());
			}else {
				senderPlayer.sendMessage(getMessageNotCarriedByYou() + " (" + target.getName() + ")");
				return;
			}
		}else {
			carry(target, senderPlayer);
			senderPlayer.sendMessage(getMesssageCarried() + " " + target.getName());
		}
	}

	private void handleCuffCommand(Player sender, Player toHandle) {
		if (isCuffed(sender)) {
			sender.sendMessage(getMessageSenderCuffed());
			return;
		}

		if (isCuffed(toHandle)) {
			uncuff(toHandle);
			sender.sendMessage(getMessageUncuff() + toHandle.getName());
		} else {
			cuff(toHandle);
			sender.sendMessage(getMessageCuff() + toHandle.getName());
		}
	}
	
	public boolean isCarried(Player target) {
		return getCarried().containsKey(target);
	}
	
	public boolean isCarried(Player target, Player carrier) {
		if(isCarried(target)) {
			if(getCarried().get(target).equals(carrier)) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	public void carry(Player carried, Player carrier) {
		getCarried().put(carried, carrier);
		carrier.addPassenger(carried);
	}
	
	public void stopCarry(Player carried) {
		if(getCarried().containsKey(carried)) {
			Player carrier = getCarried().get(carried);
			getCarried().remove(carried);
			carrier.removePassenger(carried);
		}		
	}

	public boolean isCuffed(Player target) {
		return getCuffed().contains(target);
	}

	public void uncuff(Player target) {
		target.setWalkSpeed(0.2f);
		getCuffed().remove(target);
	}

	public void cuff(Player target) {
		target.setWalkSpeed(0.06f);
		getCuffed().add(target);
	}

	public ArrayList<Player> getCuffed() {
		return cuffed;
	}

	public HashMap<Player, Player> getCarried(){
		return carried;
	}
	
	public String getMessageNoPermissions() {
		return messageNoPermissions;
	}

	private void setMessageNoPermissions(String messageNoPermissions) {
		this.messageNoPermissions = messageNoPermissions;
	}

	public String getMessageSenderCuffed() {
		return messageSenderCuffed;
	}

	private void setMessageSenderCuffed(String messageSenderCuffed) {
		this.messageSenderCuffed = messageSenderCuffed;
	}

	public String getMessageNoPlayer() {
		return messageNoPlayer;
	}

	private void setMessageNoPlayer(String messageNoPlayer) {
		this.messageNoPlayer = messageNoPlayer;
	}

	public String getMessageNoNearby() {
		return messageNoNearby;
	}

	private void setMessageNoNearby(String messageNoNearby) {
		this.messageNoNearby = messageNoNearby;
	}

	public String getMessageCuff() {
		return messageCuff;
	}

	private void setMessageCuff(String messageCuff) {
		this.messageCuff = messageCuff;
	}

	public String getMessageUncuff() {
		return messageUncuff;
	}

	private void setMessageUncuff(String messageUncuff) {
		this.messageUncuff = messageUncuff;
	}

	public HandCuffListener getListener() {
		return listener;
	}

	public HandCuffMain getInstance() {
		return instance;
	}

	public String getMessagePlayerOffline() {
		return messagePlayerOffline;
	}

	private void setMessagePlayerOffline(String messagePlayerOffline) {
		this.messagePlayerOffline = messagePlayerOffline;
	}

	public String getMessageCuffUsage() {
		return messageCuffUsage;
	}

	private void setMessageCuffUsage(String messageCuffUsage) {
		this.messageCuffUsage = messageCuffUsage;
	}

	public String getMessagePlayerNotCuffed() {
		return messagePlayerNotCuffed;
	}

	private void setMessagePlayerNotCuffed(String messagePlayerNotCuffed) {
		this.messagePlayerNotCuffed = messagePlayerNotCuffed;
	}

	public String getMessageStopCarried() {
		return messageStopCarried;
	}

	private void setMessageStopCarried(String messageStopCarried) {
		this.messageStopCarried = messageStopCarried;
	}

	public String getMesssageCarried() {
		return messsageCarried;
	}

	private void setMesssageCarried(String messsageCarried) {
		this.messsageCarried = messsageCarried;
	}

	public String getMessageNotCarriedByYou() {
		return messageNotCarriedByYou;
	}

	private void setMessageNotCarriedByYou(String messageNotCarriedByYou) {
		this.messageNotCarriedByYou = messageNotCarriedByYou;
	}

	public String getMessageCarryUsage() {
		return messageCarryUsage;
	}

	private void setMessageCarryUsage(String messageCarryUsage) {
		this.messageCarryUsage = messageCarryUsage;
	}
}
