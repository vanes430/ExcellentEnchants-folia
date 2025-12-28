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
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.enchantment.type.FishingEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.excellentenchants.util.EnchantUtils;
import su.nightexpress.nightcore.config.FileConfig;

import java.io.File;
import java.util.Collection;
import java.util.Random;

public class AutoReelEnchant extends GameEnchantment implements FishingEnchant {

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
        if (event.getState() != PlayerFishEvent.State.BITE) return false;

        Player player = event.getPlayer();
        EquipmentSlot slot = EnchantUtils.getItemHand(player, Material.FISHING_ROD);
        if (slot == null) return false;

        FishHook hook = event.getHook();
        // Capture safe data from the player thread
        Location playerLoc = player.getLocation();
        
        this.plugin.runTaskForEntity(hook, () -> {
            if (hook.isValid()) {
                LootTable table = Bukkit.getLootTable(LootTables.FISHING.getKey());
                if (table != null) {
                    LootContext.Builder builder = new LootContext.Builder(hook.getLocation());
                    builder.killer(player);
                    builder.lootedEntity(hook);
                    Collection<ItemStack> drops = table.populateLoot(new Random(), builder.build());

                    for (ItemStack drop : drops) {
                        org.bukkit.entity.Item itemEntity = hook.getWorld().dropItem(hook.getLocation(), drop);
                        // Use captured playerLoc for vector calculation
                        Vector vector = playerLoc.toVector().subtract(hook.getLocation().toVector());
                        itemEntity.setVelocity(vector.normalize().multiply(0.5).add(new Vector(0, 0.2, 0)));
                    }
                }
                
                // Schedule XP reward on the player's thread
                this.plugin.runTaskForEntity(player, () -> {
                   if (player.isValid()) {
                       player.giveExp(1);
                   }
                });
                
                hook.remove();
            }
        });
        return true;
    }
}
