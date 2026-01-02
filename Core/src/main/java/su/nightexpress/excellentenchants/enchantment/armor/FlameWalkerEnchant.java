package su.nightexpress.excellentenchants.enchantment.armor;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.excellentenchants.EnchantsPlugin;
import su.nightexpress.excellentenchants.api.EnchantData;
import su.nightexpress.excellentenchants.api.EnchantPriority;
import su.nightexpress.excellentenchants.api.damage.DamageBonus;
import su.nightexpress.excellentenchants.api.damage.DamageBonusType;
import su.nightexpress.excellentenchants.api.enchantment.component.EnchantComponent;
import su.nightexpress.excellentenchants.api.enchantment.meta.Probability;
import su.nightexpress.excellentenchants.api.enchantment.type.ProtectionEnchant;
import su.nightexpress.excellentenchants.enchantment.GameEnchantment;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.wrapper.UniParticle;

import java.io.File;

public class FlameWalkerEnchant extends GameEnchantment implements ProtectionEnchant {

    public FlameWalkerEnchant(@NotNull EnchantsPlugin plugin, @NotNull File file, @NotNull EnchantData data) {
        super(plugin, file, data);
        this.addComponent(EnchantComponent.PROBABILITY, Probability.addictive(25, 25));
    }

    @Override
    protected void loadAdditional(@NotNull FileConfig config) {
        // No additional configuration needed
    }

    @Override
    @NotNull
    public EnchantPriority getProtectionPriority() {
        return EnchantPriority.HIGHEST;
    }

    @Override
    @NotNull
    public DamageBonus getDamageBonus() {
        return new DamageBonus(DamageBonusType.NORMAL);
    }

    @Override
    public boolean onProtection(@NotNull EntityDamageEvent event, @NotNull DamageBonus damageBonus, @NotNull LivingEntity entity, @NotNull ItemStack itemStack, int level) {
        DamageCause cause = event.getCause();
        if (cause != DamageCause.LAVA && cause != DamageCause.FIRE && cause != DamageCause.FIRE_TICK && cause != DamageCause.HOT_FLOOR) {
            return false;
        }

        event.setCancelled(true);
        if (this.hasVisualEffects()) {
            UniParticle.of(Particle.FLAME).play(entity.getEyeLocation(), 0.25, 0.1, 5);
        }
        return true;
    }
}