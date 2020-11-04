package me.emnichtdayt.handcuff;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spigotmc.event.entity.EntityDismountEvent;

public class HandCuffListener implements Listener{

	HandCuffMain pl;
	
	protected HandCuffListener(HandCuffMain handCuffMain) {
		this.pl = handCuffMain;
		
		pl.getServer().getPluginManager().registerEvents(this, pl);
	}
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		if(pl.isCuffed(e.getPlayer())
		   &&e.getTo().getY()>e.getFrom().getY()+0.09) {
			e.getTo().setY(e.getFrom().getY()+0.6);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		if(pl.isCuffed(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEntityDamageByEntitiy(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player && pl.isCuffed((Player) e.getDamager())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerDropItem(PlayerDropItemEvent e) {
		if(pl.isCuffed(e.getPlayer())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryInteract(InventoryInteractEvent e) {
		if(e.getView().getTopInventory().equals(e.getInventory()) && pl.isCuffed((Player) e.getWhoClicked())) {
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if(pl.isCuffed(e.getPlayer())){
			pl.uncuff(e.getPlayer());
		}
		if(pl.isCarried(e.getPlayer())) {
			pl.stopCarry(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		if(pl.isCuffed(e.getPlayer())) {
			pl.uncuff(e.getPlayer());
		}
		if(pl.isCarried(e.getPlayer())) {
			pl.stopCarry(e.getPlayer());
		}
	}
	
	@EventHandler
	public void onEntityDismount(EntityDismountEvent e) {
		if(e.getEntity() instanceof Player) {
			if(pl.isCarried((Player) e.getEntity())) {
				e.setCancelled(true);
			}
		}
	}
	
}
