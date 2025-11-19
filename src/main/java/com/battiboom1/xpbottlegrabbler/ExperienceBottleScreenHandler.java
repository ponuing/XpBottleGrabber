package com.battiboom1.xpbottlegrabbler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

public class ExperienceBottleScreenHandler extends ScreenHandler {
    private final Inventory inventory;

    public ExperienceBottleScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(9));
    }

    public ExperienceBottleScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory) {
        super(ScreenHandlerType.GENERIC_9X1, syncId);
        this.inventory = inventory;

        checkSize(inventory, 9);
        inventory.onOpen(playerInventory.player);

        // Добавляем 5 слотов для отображения вариантов
        for (int i = 0; i < 5; i++) {
            this.addSlot(new Slot(inventory, i, 26 + i * 18, 20) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    return false;
                }
            });
        }

        // Инвентарь игрока
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 51 + row * 18));
            }
        }

        // Хотбар
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 109));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void onSlotClick(int slotIndex, int button, net.minecraft.screen.slot.SlotActionType actionType, PlayerEntity player) {
        if (slotIndex >= 0 && slotIndex < 5) {
            if (!player.getWorld().isClient) {
                handleBottleClick(player, slotIndex);
            }
            return;
        }
        super.onSlotClick(slotIndex, button, actionType, player);
    }

    private void handleBottleClick(PlayerEntity player, int slot) {
        switch (slot) {
            case 0: // Собрать опыт в обычную бутылочку
                collectExperience(player);
                break;
            case 1: // Бутылочка с 15 уровнем
                createBottleWithLevel(player, 15);
                break;
            case 2: // Бутылочка с 30 уровнем
                createBottleWithLevel(player, 30);
                break;
            case 3: // Бутылочка с 50 уровнем
                createBottleWithLevel(player, 50);
                break;
            case 4: // Бутылочка с 100 уровнем
                createBottleWithLevel(player, 100);
                break;
        }
    }

    private void collectExperience(PlayerEntity player) {
        int totalXp = getTotalExperience(player);

        if (totalXp >= 10 && hasEmptyBottle(player)) {
            removeEmptyBottle(player);

            ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
            giveOrDropItem(player, bottle);

            // Убираем 10 XP points
            addExperience(player, -10);

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f, 1.0f);
        }
    }

    private void createBottleWithLevel(PlayerEntity player, int level) {
        int requiredXp = getXpForLevel(level);
        int playerTotalXp = getTotalExperience(player);

        if (playerTotalXp >= requiredXp && hasEmptyBottle(player)) {
            removeEmptyBottle(player);

            ItemStack bottle = new ItemStack(XpBottleGrabbler.EXPERIENCE_BOTTLE_CUSTOM);
            bottle.getOrCreateNbt().putInt("StoredLevel", level);
            bottle.getOrCreateNbt().putInt("StoredXp", requiredXp);
            giveOrDropItem(player, bottle);

            addExperience(player, -requiredXp);

            player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BLOCK_BREWING_STAND_BREW, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }

    private boolean hasEmptyBottle(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.GLASS_BOTTLE)) {
                return true;
            }
        }
        return false;
    }

    private void removeEmptyBottle(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.isOf(Items.GLASS_BOTTLE)) {
                stack.decrement(1);
                break;
            }
        }
    }

    private void giveOrDropItem(PlayerEntity player, ItemStack stack) {
        if (!player.getInventory().insertStack(stack)) {
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
        this.inventory.onClose(player);
    }
}
