package com.battiboom1.xpbottlegrabbler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;

public class XpBottleGrabbler implements ModInitializer {
    public static final String MOD_ID = "xpbottlegrabbler";

    public static final Item EXPERIENCE_BOTTLE_CUSTOM = Registry.register(
            Registries.ITEM,
            Identifier.of(MOD_ID, "experience_bottle_custom"),
            new CustomExperienceBottleItem(new Item.Settings().maxCount(16))
    );

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("xpbottle")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player != null) {
                            SimpleInventory inventory = new SimpleInventory(9);

                            // Заполняем слоты визуальными предметами
                            ItemStack collectBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
                            collectBottle.setCustomName(Text.literal("§aСобрать опыт (10 XP)"));
                            inventory.setStack(0, collectBottle);

                            ItemStack bottle15 = new ItemStack(Items.EXPERIENCE_BOTTLE);
                            bottle15.setCustomName(Text.literal("§6Уровень 15"));
                            inventory.setStack(1, bottle15);

                            ItemStack bottle30 = new ItemStack(Items.EXPERIENCE_BOTTLE);
                            bottle30.setCustomName(Text.literal("§6Уровень 30"));
                            inventory.setStack(2, bottle30);

                            ItemStack bottle50 = new ItemStack(Items.EXPERIENCE_BOTTLE);
                            bottle50.setCustomName(Text.literal("§6Уровень 50"));
                            inventory.setStack(3, bottle50);

                            ItemStack bottle100 = new ItemStack(Items.EXPERIENCE_BOTTLE);
                            bottle100.setCustomName(Text.literal("§6Уровень 100"));
                            inventory.setStack(4, bottle100);

                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                                    (syncId, playerInventory, playerEntity) ->
                                            new ExperienceBottleScreenHandler(syncId, playerInventory, inventory),
                                    Text.literal("Бутылочки опыта")
                            ));
                        }
                        return 1;
                    }));
        });
    }
}
