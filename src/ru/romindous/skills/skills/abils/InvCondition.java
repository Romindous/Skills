package ru.romindous.skills.skills.abils;

import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.objects.ItemTags;

import static org.bukkit.inventory.EquipmentSlot.*;

public abstract class InvCondition {

    private final String desc;

    protected InvCondition(final String desc) {
        this.desc = desc;
    }

    public String describe() {
        return desc;
    }

    public abstract EquipmentSlot test(final EntityEquipment inv);

    public static final InvCondition NONE = new InvCondition("") {
        public EquipmentSlot test(final EntityEquipment inv) {return BODY;}
    };

    public static final InvCondition FIST = new InvCondition(TCUtil.P + "Пустую " + TCUtil.N + "основную руку") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return it.getType().isAir() ? HAND : null;
        }
    };

    public static final InvCondition FIST_OFF = new InvCondition(TCUtil.P + "Пустую " + TCUtil.N + "вторичную руку") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInOffHand();
            return it.getType().isAir() ? OFF_HAND : null;
        }
    };

    public static final InvCondition FIST_ANY = new InvCondition(TCUtil.N + "Любую руку " + TCUtil.P + "пустой") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            if (hnd.getType().isAir()) return HAND;
            final ItemStack ofh = inv.getItemInOffHand();
            return ofh.getType().isAir() ? OFF_HAND : null;
        }
    };

    public static final InvCondition SWORD = new InvCondition(TCUtil.P + "Меч " + TCUtil.N + "в основной руке") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return ItemTags.SWORDS.contains(it.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition SWORD_BOTH = new InvCondition(TCUtil.P + "Мечи " + TCUtil.N + "в обоих руках") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand(), ofh = inv.getItemInOffHand();
            return ItemTags.SWORDS.contains(hnd.getType().asItemType())
                && ItemTags.SWORDS.contains(ofh.getType().asItemType())
                ? (Main.srnd.nextBoolean() ? HAND : OFF_HAND) : null;
        }
    };

    public static final InvCondition SWORD_FIST = new InvCondition(TCUtil.P + "Меч " + TCUtil.N + "в основной руке, " + TCUtil.P + "ничего " + TCUtil.N + "в другой") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand(), ofh = inv.getItemInOffHand();
            return ItemTags.SWORDS.contains(it.getType().asItemType()) && ofh.getType().isAir() ? HAND : null;
        }
    };

    public static final InvCondition SWORD_SHIELD = new InvCondition(TCUtil.P + "Меч " + TCUtil.N + "в основной руке, " + TCUtil.P + "щит " + TCUtil.N + "в другой") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand(), ofh = inv.getItemInOffHand();
            return ItemTags.SWORDS.contains(it.getType().asItemType())
                && ItemType.SHIELD.equals(ofh.getType().asItemType())
                ? (Main.srnd.nextBoolean() ? HAND : OFF_HAND) : null;
        }
    };

    public static final InvCondition AXE = new InvCondition(TCUtil.P + "Топор " + TCUtil.N + "в основной руке") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return ItemTags.AXES.contains(it.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition MELEE = new InvCondition(TCUtil.P + "Любой клинок " + TCUtil.N + "в основной руке") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return ItemTags.MELEE.contains(it.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition SHIELD_OFF = new InvCondition(TCUtil.P + "Щит " + TCUtil.N + "во вторичной руке") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInOffHand();
            return ItemType.SHIELD.equals(it.getType().asItemType()) ? OFF_HAND : null;
        }
    };

    public static final InvCondition STAFF_ANY = new InvCondition(TCUtil.P + "Посох " + TCUtil.N + "в любой руке") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            if (ItemTags.STAFFS.contains(hnd.getType().asItemType())) return HAND;
            final ItemStack ofh = inv.getItemInOffHand();
            return ItemTags.STAFFS.contains(ofh.getType().asItemType()) ? OFF_HAND : null;
        }
    };

    public static final InvCondition BOW = new InvCondition(TCUtil.P + "Лук / Арбалет " + TCUtil.N + "в основной руке") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            return ItemTags.RANGED.contains(hnd.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition BOW_ANY = new InvCondition(TCUtil.P + "Лук / Арбалет " + TCUtil.N + "в любой руке") {
        public EquipmentSlot test(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            if (ItemTags.RANGED.contains(hnd.getType().asItemType())) return HAND;
            final ItemStack ofh = inv.getItemInOffHand();
            return ItemTags.RANGED.contains(ofh.getType().asItemType()) ? OFF_HAND : null;
        }
    };

}
