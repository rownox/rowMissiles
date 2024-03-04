package me.rownox.rowmissiles.listeners;

import me.rownox.rowmissiles.RowMissiles;
import me.rownox.rowmissiles.objects.MissileObject;
import me.rownox.rowmissiles.objects.PlayerValuesObject;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {
    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();
        PlayerValuesObject pValues = RowMissiles.playerValues.get(p.getUniqueId());
        Block b = e.getClickedBlock();
        if (b == null) return;

        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        for (MissileObject MM : RowMissiles.missileList.keySet()) {
            if (MM.getItem().equals(item)) {
                if (b.getType().equals(Material.DISPENSER)) {
                    if (b.getBlockData() instanceof Directional directional) {
                        e.setCancelled(true);

                        if (directional.getFacing() != BlockFace.UP) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', RowMissiles.prefix + "&cPlease face the launcher upward."));
                            p.playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
                            return;
                        }

                        Block blockUnder = b.getLocation().subtract(0, 1, 0).getBlock();
                        if (blockUnder.getType().equals(Material.LAVA_CAULDRON)) {
                            new MissileObject(MM.getItem(), MM.getColor(), MM.getRange(), MM.getMagnitude(), MM.getSpeed(), MM.isNuclear()).launch(p, pValues.getTargetLoc(), blockUnder);
                        } else {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', RowMissiles.prefix + "&eFuel the launcher by placing a lava cauldron underneath the dispenser."));
                        }
                    }
                    return;
                }
                spawnMissile(p, b.getLocation());
                if (p.getGameMode() == GameMode.SURVIVAL)
                    p.getInventory().getItemInMainHand().setAmount(item.getAmount() - 1);
            }
        }

    }

    public void spawnMissile(Player player, Location location) {
        ArmorStand missileEntity;
        missileEntity = (ArmorStand) location.getWorld().spawnEntity(location.add(0.5, 2, 0.5), EntityType.ARMOR_STAND);

        missileEntity.setGravity(false);

        missileEntity.setHelmet(new ItemStack(Material.TNT));
        missileEntity.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
        missileEntity.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
        missileEntity.setBoots(new ItemStack(Material.NETHERITE_BOOTS));

        missileEntity.setInvulnerable(true);
        missileEntity.setCollidable(false);

        missileEntity.setBasePlate(false);
    }

    private boolean isMissileArmorStand(ArmorStand armorStand) {
        ItemStack item = new ItemStack(Material.TNT);
        return armorStand.getHelmet().equals(item);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof ArmorStand) {
            ArmorStand armorStand = (ArmorStand) event.getRightClicked();

            // Check if the armor stand is a missile (customize this condition based on your setup)
            if (isMissileArmorStand(armorStand)) {
                if (event.getPlayer().isSneaking()) {
                    armorStand.getWorld().spawnParticle(Particle.SMOKE_LARGE, armorStand.getLocation(), 5, 0.2, 0.2, 0.2, 0);
                    armorStand.remove();
                }
                event.setCancelled(true);
            }
        }
    }
}
