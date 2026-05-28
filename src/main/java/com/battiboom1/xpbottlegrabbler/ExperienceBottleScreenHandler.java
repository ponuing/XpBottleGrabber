package com.battiboom1.xpbottlegrabbler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

public class ExperienceBottleScreenHandler extends AbstractContainerMenu {
    private final SimpleContainer buttonInventory;
    private final Inventory playerInventory;

    public ExperienceBottleScreenHandler(int syncId, Inventory playerInventory) {
        super(MenuType.GENERIC_9x1, syncId);
        this.playerInventory = playerInventory;

        this.buttonInventory = new SimpleContainer(9) {
            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        };

        initializeButtons();

        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(buttonInventory, i, 8 + i * 18, 20) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean mayPickup(Player playerEntity) {
                    return false;
                }

                @Override
                public ItemStack remove(int amount) {
                    return ItemStack.EMPTY;
                }

                @Override
                public void setByPlayer(ItemStack stack) {
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
        filler.set(DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal(" ")); // Empty name

        ItemStack button0 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button0.set(DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("§a§lBottle o' Enchanting (10 XP)"));
        buttonInventory.setItem(0, button0);

        buttonInventory.setItem(1, filler.copy());

        ItemStack button1 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button1.set(DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("§6§lLVL 15"));
        buttonInventory.setItem(2, button1);

        buttonInventory.setItem(3, filler.copy());

        ItemStack button2 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button2.set(DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("§6§lLVL 30"));
        buttonInventory.setItem(4, button2);

        buttonInventory.setItem(5, filler.copy());

        ItemStack button3 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button3.set(DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("§6§lLVL 50"));
        buttonInventory.setItem(6, button3);

        buttonInventory.setItem(7, filler.copy());

        ItemStack button4 = new ItemStack(Items.EXPERIENCE_BOTTLE);
        button4.set(DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("§6§lLVL 100"));
        buttonInventory.setItem(8, button4);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        if (slotIndex < 9) {
            return ItemStack.EMPTY;
        }

        Slot slot = this.slots.get(slotIndex);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = slot.getItem();
        ItemStack originalStack = stackInSlot.copy();

        if (slotIndex >= 9 && slotIndex < 36) {
            if (!this.moveItemStackTo(stackInSlot, 36, 45, false)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= 36 && slotIndex < 45) {
            if (!this.moveItemStackTo(stackInSlot, 9, 36, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stackInSlot.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stackInSlot.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stackInSlot);
        return originalStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clicked(int slotIndex, int button, ContainerInput actionType, Player player) {
        if (slotIndex == 0 || slotIndex == 2 || slotIndex == 4 || slotIndex == 6 || slotIndex == 8) {
            if (actionType == ContainerInput.PICKUP || actionType == ContainerInput.QUICK_MOVE) {
                if (!player.level().isClientSide()) {
                    boolean isShiftDown = (actionType == ContainerInput.QUICK_MOVE);
                    handleBottleClick(player, slotIndex, isShiftDown);
                }
            }
            return;
        }

        if (slotIndex >= 0 && slotIndex < 9) {
            return;
        }

        super.clicked(slotIndex, button, actionType, player);
    }

    private void handleBottleClick(Player player, int slot, boolean craftMax) {
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

    private void collectExperience(Player player) {
        int totalXp = getTotalExperience(player);

        // Message of not enough experience or bottles
        if (totalXp < 10) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNot enough experience.")
            );
            return;
        }
        if (!hasEmptyBottle(player)) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNo empty bottles in inventory.")
            );
            return;
        }

        removeEmptyBottle(player);

        ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
        giveOrDropItem(player, bottle);

        addExperience(player, -10);

        // Successful creation bottles message
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("§aBottle o' Enchanting created! 10 XP spent.")
        );

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
    }

    private void collectExperienceMax(Player player) {
        int totalXp = getTotalExperience(player);
        int bottleCount = countEmptyBottles(player);

        if (totalXp < 10) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNot enough experience.")
            );
            return;
        }

        if (bottleCount == 0) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNo empty bottles in inventory.")
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

            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§aBottles created: " + maxBottles + "! Spent: " + (maxBottles * 10) + " XP")
            );

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.5f, 1.0f);
        }
    }

    private void createBottleWithLevel(Player player, int level) {
        int requiredXp = getXpForLevel(level);
        int playerTotalXp = getTotalExperience(player);

        if (playerTotalXp < requiredXp) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNot enough experience.")
            );
            return;
        }

        if (!hasEmptyBottle(player)) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNo empty bottles in inventory.")
            );
            return;
        }

        removeEmptyBottle(player);

        ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);

        CompoundTag nbt = new CompoundTag();
        nbt.putInt("StoredLevel", level);
        nbt.putInt("StoredXp", requiredXp);
        bottle.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

        bottle.set(DataComponents.ITEM_NAME,
                net.minecraft.network.chat.Component.literal("§6§lBottle o' Enchanting (LVL. " + level + ")"));

        giveOrDropItem(player, bottle);
        addExperience(player, -requiredXp);

        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal("§aLevel bottle created. Level " + level + ".")
        );

        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private void createBottleWithLevelMax(Player player, int level) {
        int requiredXp = getXpForLevel(level);
        int playerTotalXp = getTotalExperience(player);
        int bottleCount = countEmptyBottles(player);

        if (playerTotalXp < requiredXp) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNot enough experience.")
            );
            return;
        }

        if (bottleCount == 0) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§cNo empty bottles in inventory.")
            );
            return;
        }

        int maxBottles = Math.min(playerTotalXp / requiredXp, bottleCount);

        if (maxBottles > 0) {
            for (int i = 0; i < maxBottles; i++) {
                removeEmptyBottle(player);

                ItemStack bottle = new ItemStack(Items.EXPERIENCE_BOTTLE);

                CompoundTag nbt = new CompoundTag();
                nbt.putInt("StoredLevel", level);
                nbt.putInt("StoredXp", requiredXp);
                bottle.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt));

                bottle.set(DataComponents.ITEM_NAME,
                        net.minecraft.network.chat.Component.literal("§6§lBottle o' Enchanting (LVL. " + level + ")"));

                giveOrDropItem(player, bottle);
            }

            addExperience(player, -maxBottles * requiredXp);

            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal("§aLevel bottle created. Level " + level + ": §lCount " + maxBottles + ".")
            );

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
        }
    }

    private boolean hasEmptyBottle(Player player) {
        return countEmptyBottles(player) > 0;
    }

    private int countEmptyBottles(Player player) {
        int count = 0;
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack.is(Items.GLASS_BOTTLE)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private void removeEmptyBottle(Player player) {
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            if (stack.is(Items.GLASS_BOTTLE)) {
                stack.shrink(1);
                break;
            }
        }
    }

    private void giveOrDropItem(Player player, ItemStack stack) {
        if (!playerInventory.add(stack)) {
            player.drop(stack, false);
        }
    }

    private int getTotalExperience(Player player) {
        int total = 0;
        int level = player.experienceLevel;

        if (level >= 0 && level <= 16) {
            total = (int) (Math.pow(level, 2) + 6 * level);
        } else if (level > 16 && level <= 31) {
            total = (int) (2.5 * Math.pow(level, 2) - 40.5 * level + 360);
        } else {
            total = (int) (4.5 * Math.pow(level, 2) - 162.5 * level + 2220);
        }

        total += Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return total;
    }

    private void addExperience(Player player, int xp) {
        int currentTotal = getTotalExperience(player);
        int newTotal = Math.max(0, currentTotal + xp);

        player.experienceLevel = 0;
        player.experienceProgress = 0;
        player.totalExperience = 0;

        player.giveExperiencePoints(newTotal);
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
    public void removed(Player player) {
        super.removed(player);
    }
}
