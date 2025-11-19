package com.battiboom1.xpbottlegrabbler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class CustomExperienceBottleItem extends Item {

    public CustomExperienceBottleItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (!world.isClient && stack.hasNbt()) {
            int storedXp = stack.getOrCreateNbt().getInt("StoredXp");

            if (storedXp > 0) {
                player.addExperience(storedXp);

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                        SoundCategory.PLAYERS, 1.0f, 1.0f);

                stack.decrement(1);
                return TypedActionResult.success(stack);
            }
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (stack.hasNbt()) {
            int level = stack.getOrCreateNbt().getInt("StoredLevel");
            int xp = stack.getOrCreateNbt().getInt("StoredXp");
            tooltip.add(Text.literal("Уровень: " + level).formatted(Formatting.GREEN));
            tooltip.add(Text.literal("Опыт: " + xp + " XP").formatted(Formatting.AQUA));
        }
    }
}
