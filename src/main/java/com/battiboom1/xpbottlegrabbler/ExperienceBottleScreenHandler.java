package com.battiboom1.xpbottlegrabbler;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class ExperienceBottleScreenHandler extends ScreenHandler {
    private final SimpleInventory buttonInventory;
    private final PlayerInventory playerInventory;

    public ExperienceBottleScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ScreenHandlerType.GENERIC_9X1, syncId);
        this.playerInventory = playerInventory;

        this.buttonInventory = new SimpleInventory(9) {
            @Override
            public void markDirty() {
            }

            @Override
            public boolean canPlayerUse(PlayerEntity player) {
                return true;
            }
        };

        initializeButtons();

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(buttonInventory, i, 8 + i * 18, 20) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    return false;
                }

                @Override
                public ItemStack takeStack(int amount) {
                    return ItemStack.EMPTY;
                }

                @Override
                public void setStack(ItemStack stack) {
                }
            });
        }

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    private void initializeButtons() {
        ItemStack filler = new ItemStack(Items.LIGHT_GRAY_STAINED_GLASS_PANE);
        filler.set(DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal(" ")); // Пустое имя

        ItemStack button0 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button0.set(DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal("§a§lБутылочка опыта (10 XP)"));
        buttonInventory.setStack(0, button0);

        buttonInventory.setStack(1, filler.copy());

        ItemStack button1 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button1.set(DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal("§6§lУровень 15"));
        buttonInventory.setStack(2, button1);

        buttonInventory.setStack(3, filler.copy());

        ItemStack button2 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button2.set(DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal("§6§lУровень 30"));
        buttonInventory.setStack(4, button2);

        buttonInventory.setStack(5, filler.copy());

        ItemStack button3 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button3.set(DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal("§6§lУровень 50"));
        buttonInventory.setStack(6, button3);

        buttonInventory.setStack(7, filler.copy());

        ItemStack button4 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button4.set(DataComponentTypes.CUSTOM_NAME,
                net.minecraft.text.Text.literal("§6§lУровень 100"));
        buttonInventory.setStack(8, button4);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < 9) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getStack();
        ItemStack originalStack = stackInSlot.copy();

        if (slotIndex >= 9 && slotIndex < 36) {
            if (!this.insertItem(stackInSlot, 36, 45, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= 36 && slotIndex < 45) {
            if (!this.insertItem(stackInSlot, 9, 36, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stackInSlot.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stackInSlot);
        return originalStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex == 0 || slotIndex == 2 || slotIndex == 4 || slotIndex == 6 || slotIndex == 8) {
            if (actionType == SlotActionType.PICKUP || actionType == SlotActionType.QUICK_MOVE) {
                if (!player.getEntityWorld().isClient()) {
                    boolean isShiftDown = (actionType == SlotActionType.QUICK_MOVE);
                    handleBottleClick(player, slotIndex, isShiftDown);
                }
            }
            return;
        }

        if (slotIndex >= 0 && slotIndex < 9) {
            return;
        }

        super.onSlotClick(slotIndex, button, actionType, player);
    }

    private void handleBottleClick(PlayerEntity player, int slot, boolean craftMax) {
        switch (slot) {
            case 0:
                if (craftMax) {
                    collectExperienceMax(player);
                } else {
                    collectExperience(player);
                }
                break;
            case 2:
                if (craftMax) {
                    createBottleWithLevelMax(player, 15);
                } else {
                    createBottleWithLevel(player, 15);
                }
                break;
            case 4:
                if (craftMax) {
                    createBottleWithLevelMax(player, 30);
                } else {
                    createBottleWithLevel(player, 30);
                }
                break;
            case 6:
                if (craftMax) {
                    createBottleWithLevelMax(player, 50);
                } else {
                    createBottleWithLevel(player, 50);
                }
                break;
            case 8:
                if (craftMax) {
                    createBottleWithLevelMax(player, 100);
                } else {
                    createBottleWithLevel(player, 100);
                }
                break;
        }
    }

    private void collectExperience(PlayerEntity player) {
        int totalXp = getTotalExperience(player);

        if (totalXp < 10) {
            // Сообщение о недостатке опыта
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНедостаточно опыта."),
                    false
            );
            return;
        }

        if (!hasEmptyBottle(player)) {
            // Сообщение о недостатке пустых бутылочек
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНет пустых бутылочек в инвентаре."),
                    false
            );
            return;
        }

        removeEmptyBottle(player);

        ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
        giveOrDropItem(player, bottle);

        addExperience(player, -10);

        // Сообщение об успешном создании
        player.sendMessage(
                net.minecraft.text.Text.literal("§aСоздана бутылочка опыта! Потрачено: 10 XP"),
                false
        );

        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.0f);
    }

    private void collectExperienceMax(PlayerEntity player) {
        int totalXp = getTotalExperience(player);
        int bottleCount = countEmptyBottles(player);

        if (totalXp < 10) {
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНедостаточно опыта."),
                    false
            );
            return;
        }

        if (bottleCount == 0) {
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНет пустых бутылочек в инвентаре."),
                    false
            );
            return;
        }

        int maxBottles = Math.min(totalXp / 10, bottleCount);

        if (maxBottles > 0) {
            for (int i = 0; i < maxBottles; i++) {
                removeEmptyBottle(player);
                ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
                giveOrDropItem(player, bottle);
            }

            addExperience(player, -maxBottles * 10);

            player.sendMessage(
                    net.minecraft.text.Text.literal("§aСоздано бутылочек: " + maxBottles + "! Потрачено: " + (maxBottles * 10) + " XP"),
                    false
            );

            player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.0f);
        }
    }

    private void createBottleWithLevel(PlayerEntity player, int level) {
        int requiredXp = getXpForLevel(level);
        int playerTotalXp = getTotalExperience(player);

        if (playerTotalXp < requiredXp) {
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНедостаточно опыта."),
                    false
            );
            return;
        }

        if (!hasEmptyBottle(player)) {
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНет пустых бутылочек в инвентаре."),
                    false
            );
            return;
        }

        removeEmptyBottle(player);

        ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);

        NbtCompound nbt = new NbtCompound();
        nbt.putInt("StoredLevel", level);
        nbt.putInt("StoredXp", requiredXp);
        bottle.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

        bottle.set(DataComponentTypes.ITEM_NAME,
                net.minecraft.text.Text.literal("§6§lБутылочка опыта (Ур. " + level + ")"));

        giveOrDropItem(player, bottle);
        addExperience(player, -requiredXp);

        player.sendMessage(
                net.minecraft.text.Text.literal("§aСоздана бутылочка уровня " + level + "."),
                false
        );

        player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }

    private void createBottleWithLevelMax(PlayerEntity player, int level) {
        int requiredXp = getXpForLevel(level);
        int playerTotalXp = getTotalExperience(player);
        int bottleCount = countEmptyBottles(player);

        if (playerTotalXp < requiredXp) {
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНедостаточно опыта."),
                    false
            );
            return;
        }

        if (bottleCount == 0) {
            player.sendMessage(
                    net.minecraft.text.Text.literal("§cНет пустых бутылочек в инвентаре."),
                    false
            );
            return;
        }

        int maxBottles = Math.min(playerTotalXp / requiredXp, bottleCount);

        if (maxBottles > 0) {
            for (int i = 0; i < maxBottles; i++) {
                removeEmptyBottle(player);

                ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);

                NbtCompound nbt = new NbtCompound();
                nbt.putInt("StoredLevel", level);
                nbt.putInt("StoredXp", requiredXp);
                bottle.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));

                bottle.set(DataComponentTypes.ITEM_NAME,
                        net.minecraft.text.Text.literal("§6§lБутылочка опыта (Ур. " + level + ")"));

                giveOrDropItem(player, bottle);
            }

            addExperience(player, -maxBottles * requiredXp);

            player.sendMessage(
                    net.minecraft.text.Text.literal("§aСоздано бутылочек уровня " + level + ": §l" + maxBottles + "."),
                    false
            );

            player.getEntityWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private boolean hasEmptyBottle(PlayerEntity player) {
        return countEmptyBottles(player) > 0;
    }

    private int countEmptyBottles(PlayerEntity player) {
        int count = 0;
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack stack = playerInventory.getStack(i);
            if (stack.isOf(Items.GLASS_BOTTLE)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeEmptyBottle(PlayerEntity player) {
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack stack = playerInventory.getStack(i);
            if (stack.isOf(Items.GLASS_BOTTLE)) {
                stack.decrement(1);
                break;
            }
        }
    }

    private void giveOrDropItem(PlayerEntity player, ItemStack stack) {
        if (!playerInventory.insertStack(stack)) {
            player.dropItem(stack, false);
        }
    }

    private int getTotalExperience(PlayerEntity player) {
        int total = 0;
        int level = player.experienceLevel;

        if (level >= 0 && level <= 16) {
            total = (int) (Math.pow(level, 2) + 6 * level);
        } else if (level > 16 && level <= 31) {
            total = (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else {
            total = (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }

        total += Math.round(player.experienceProgress * player.getNextLevelExperience());
        return total;
    }

    private void addExperience(PlayerEntity player, int xp) {
        int currentTotal = getTotalExperience(player);
        int newTotal = Math.max(0, currentTotal + xp);

        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.totalExperience = 0;

        player.addExperience(newTotal);
    }

    private int getXpForLevel(int level) {
        if (level <= 16) {
            return (int) (Math.pow(level, 2) + 6 * level);
        } else if (level <= 31) {
            return (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else {
            return (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
    }
}
