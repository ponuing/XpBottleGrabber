package com.battiboom1.xpbottlegrabbler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.commands.Commands;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class XpBottleGrabber implements ModInitializer {
    @Override
    public void onInitialize() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getItemInHand(hand);

            if (stack.is(Items.EXPERIENCE_BOTTLE) && !world.isClientSide()) {
                CustomData nbtComponent = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                CompoundTag nbt = nbtComponent.copyTag();

                if (nbt.contains("StoredXp")) {
                    int storedXp = nbt.getInt("StoredXp").orElse(0);

                    if (storedXp > 0) {
                        player.giveExperiencePoints(storedXp);

                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.SPLASH_POTION_BREAK,
                                SoundSource.PLAYERS, 1.0f, 1.0f);

                        stack.shrink(1);
                        return InteractionResult.SUCCESS;
                    }
                }
            }

            return InteractionResult.PASS;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("xpbottle")
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayer();
                        if (player != null) {
                            player.openMenu(new SimpleMenuProvider(
                                    (syncId, playerInventory, playerEntity) ->
                                            new ExperienceBottleScreenHandler(syncId, playerInventory),
                                    Component.literal("Bottles of experience")
                            ));
                        }
                        return 1;
                    }));
        });
    }
}
