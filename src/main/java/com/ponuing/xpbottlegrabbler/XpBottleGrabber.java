package com.ponuing.xpbottlegrabbler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

public class XpBottleGrabber implements ModInitializer {
    @Override
    public void onInitialize() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            if (stack.isOf(Items.EXPERIENCE_BOTTLE) && !world.isClient()) {
                NbtComponent nbtComponent = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
                NbtCompound nbt = nbtComponent.copyNbt();

                if (nbt.contains("StoredXp")) {
                    int storedXp = nbt.getInt("StoredXp").orElse(0);

                    if (storedXp > 0) {
                        player.addExperience(storedXp);

                        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                SoundEvents.ENTITY_SPLASH_POTION_BREAK,
                                SoundCategory.PLAYERS, 1.0f, 1.0f);

                        stack.decrement(1);
                        return ActionResult.SUCCESS;
                    }
                }
            }

            return ActionResult.PASS;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("xpbottle")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                    (syncId, playerInventory, playerEntity) ->
                                            new ExperienceBottleScreenHandler(syncId, playerInventory),
                                    Text.literal("Bottles of experience")
                            ));
                        }
                        return 1;
                    }));
        });
    }
}
