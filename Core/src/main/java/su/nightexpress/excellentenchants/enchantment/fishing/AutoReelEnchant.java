package su.nightexpress.excellentenchants.enchantment.fishing;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.util.Vector;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;
import java.util.Random;

public class AutoReelEnchant extends GameEnchantment implements FishingEnchant {

    private static final String META_AUTO_REEL = "excellentenchants_autoreel_active";

    public AutoReelEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {

    }

    @Override
    @NotNull
    public EnchantPriority getFishingPriority() {
        return EnchantPriority.MONITOR;
    }

    @Override
    public boolean onFishing(@NotNull PlayerFishEvent event, @NotNull ItemStack itemStack, int level) {
        Player player = event.getPlayer();
        FishHook hook = event.getHook();
        PlayerFishEvent.State state = event.getState();

        if (state == PlayerFishEvent.State.FISHING) {
            player.setMetadata(META_AUTO_REEL, new FixedMetadataValue(this.plugin, true));
            return false;
        }

        if (state == PlayerFishEvent.State.BITE) {
            EquipmentSlot slot = EnchantUtils.getItemHand(player, Material.FISHING_ROD);
            if (slot != null) {
                this.plugin.runTaskForEntity(hook, () -> {
                    if (hook.isValid() && player.isOnline()) {
                        hook.retrieve(slot);
                    }
                });
                return true;
            }
        }

        if (state == PlayerFishEvent.State.REEL_IN || state == PlayerFishEvent.State.IN_GROUND) {
            player.removeMetadata(META_AUTO_REEL, this.plugin);
            return false;
        }

        if (state == PlayerFishEvent.State.CAUGHT_FISH || state == PlayerFishEvent.State.CAUGHT_ENTITY
            || state == PlayerFishEvent.State.REEL_IN || state == PlayerFishEvent.State.FAILED_ATTEMPT) {

            if (!player.hasMetadata(META_AUTO_REEL)) return false;

            this.plugin.runFoliaTaskLater(() -> {
                this.plugin.runTaskForEntity(player, () -> {
                    if (!player.isOnline()) return;

                    EquipmentSlot slot = EnchantUtils.getItemHand(player, Material.FISHING_ROD);
                    if (slot == null) return;

                    ItemStack rod = player.getInventory().getItem(slot);
                    if (rod == null || !EnchantUtils.contains(rod, this.getBukkitEnchantment())) return;

                    if (player.getFishHook() != null) return;

                    FishHook newHook = player.launchProjectile(FishHook.class);
                    PlayerFishEvent castEvent = new PlayerFishEvent(player, null, newHook, slot, PlayerFishEvent.State.FISHING);
                    this.plugin.getServer().getPluginManager().callEvent(castEvent);
                    if (castEvent.isCancelled()) {
                        newHook.remove();
                    }
                });
            }, 20 + new Random().nextInt(20));
        }

        return false;
    }
}
