package ru.romindous.skills.skills.abils;

import javax.annotation.Nullable;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.Main;
import ru.romindous.skills.items.ItemTags;

import static org.bukkit.inventory.EquipmentSlot.*;

public abstract class InvCondition {

    private final String desc;

    protected InvCondition(final String desc) {
        this.desc = desc;
    }

    public String describe() {
        return desc;
    }

    public abstract @Nullable EquipmentSlot result(final EntityEquipment inv);

    public boolean test(final EntityEquipment inv) {
        return result(inv) != null;
    }

    private static String clr(final String eq) {
        return TCUtil.P + "<u>" + eq + "</u>";
    }

    public static final InvCondition FIST = new InvCondition(clr("Пустую") + TCUtil.N + " основную руку") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return it.getType().isAir() ? HAND : null;
        }
    };

    public static final InvCondition FIST_OFF = new InvCondition(clr("Пустую") + TCUtil.N + " вторичную руку") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInOffHand();
            return it.getType().isAir() ? OFF_HAND : null;
        }
    };

    public static final InvCondition FIST_ANY = new InvCondition(TCUtil.N + "Любую руку " + clr("пустой")) {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            if (hnd.getType().isAir()) return HAND;
            final ItemStack ofh = inv.getItemInOffHand();
            return ofh.getType().isAir() ? OFF_HAND : null;
        }
    };

    public static final InvCondition SWORD = new InvCondition(clr("Меч") + TCUtil.N + " в основной руке") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return ItemTags.SWORDS.contains(it.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition SWORD_BOTH = new InvCondition(clr("Мечи") + TCUtil.N + " в обоих руках") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand(), ofh = inv.getItemInOffHand();
            return ItemTags.SWORDS.contains(hnd.getType().asItemType())
                && ItemTags.SWORDS.contains(ofh.getType().asItemType())
                ? (Main.srnd.nextBoolean() ? HAND : OFF_HAND) : null;
        }
    };

    public static final InvCondition SWORD_FIST = new InvCondition(clr("Меч") + TCUtil.N + " в основной руке, " + clr("ничего") + TCUtil.N + " в другой") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand(), ofh = inv.getItemInOffHand();
            return ItemTags.SWORDS.contains(it.getType().asItemType()) && ofh.getType().isAir() ? HAND : null;
        }
    };

    public static final InvCondition SWORD_SHIELD = new InvCondition(clr("Меч") + TCUtil.N + " в основной руке, " + clr("щит") + TCUtil.N + " в другой") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand(), ofh = inv.getItemInOffHand();
            return ItemTags.SWORDS.contains(it.getType().asItemType())
                && ItemType.SHIELD.equals(ofh.getType().asItemType())
                ? (Main.srnd.nextBoolean() ? HAND : OFF_HAND) : null;
        }
    };

    public static final InvCondition AXE = new InvCondition(clr("Топор") + TCUtil.N + " в основной руке") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return ItemTags.AXES.contains(it.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition MELEE = new InvCondition(clr("Любой клинок") + TCUtil.N + " в основной руке") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInMainHand();
            return ItemTags.MELEE.contains(it.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition SHIELD_OFF = new InvCondition(clr("Щит") + TCUtil.N + " во вторичной руке") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack it = inv.getItemInOffHand();
            return ItemType.SHIELD.equals(it.getType().asItemType()) ? OFF_HAND : null;
        }
    };

    public static final InvCondition STAFF_ANY = new InvCondition(clr("Посох") + TCUtil.N + " в любой руке") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            if (ItemTags.STAFFS.contains(hnd.getType().asItemType())) return HAND;
            final ItemStack ofh = inv.getItemInOffHand();
            return ItemTags.STAFFS.contains(ofh.getType().asItemType()) ? OFF_HAND : null;
        }
    };

    public static final InvCondition BOW = new InvCondition(clr("Лук / Арбалет") + TCUtil.N + " в основной руке") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            return ItemTags.RANGED.contains(hnd.getType().asItemType()) ? HAND : null;
        }
    };

    public static final InvCondition BOW_ANY = new InvCondition(clr("Лук / Арбалет") + TCUtil.N + " в любой руке") {
        public EquipmentSlot result(final EntityEquipment inv) {
            final ItemStack hnd = inv.getItemInMainHand();
            if (ItemTags.RANGED.contains(hnd.getType().asItemType())) return HAND;
            final ItemStack ofh = inv.getItemInOffHand();
            return ItemTags.RANGED.contains(ofh.getType().asItemType()) ? OFF_HAND : null;
        }
    };

    public static final InvCondition ARMOR_ANY = new InvCondition("Любую часть " + clr("брони") + TCUtil.N + " на теле") {
        public EquipmentSlot result(final EntityEquipment inv) {
            for (final ItemStack it : inv.getArmorContents())
                if (ItemUtil.isBlank(it, false)) return null;
            return BODY;
        }
    };

    public static final InvCondition ARMOR_FULL = new InvCondition("Полный " + clr("сет брони") + TCUtil.N + " (4 части)") {
        public EquipmentSlot result(final EntityEquipment inv) {
            for (final ItemStack it : inv.getArmorContents())
                if (ItemUtil.isBlank(it, false)) return null;
            return BODY;
        }
    };

}
