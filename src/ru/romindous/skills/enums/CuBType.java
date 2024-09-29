package ru.romindous.skills.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import ru.komiss77.utils.ItemUtil;

public enum CuBType {

    REPAIR(Material.END_PORTAL_FRAME, "END_PORTAL_FRAME&1&§bСтол Подношений&;§7Чинит ваши §bпредметы;§7за §bоплату §7душами&18", TransferType.TAKE, 24, 0),
    RELAY(Material.LIGHTNING_ROD, "LIGHTNING_ROD&1&§dРеле Трансмиссий&;§7Передает §dдуши §7из;§7одних §dмест §7в другие&24", TransferType.BOTH, 8, 1),
    FEED(Material.SOUL_CAMPFIRE, "SOUL_CAMPFIRE&1&§dГолодный Костер&;§7Выжигает §dдуши §7из;§dмясных §7остатков мобов&25&G", TransferType.GIVE, 128, 1),
    STORE(Material.SCULK_SHRIEKER, "SCULK_SHRIEKER&1&§5Стабильный Омут&;§5Хранит §7в себе;§7стабильные §5души&31&G", TransferType.BOTH, 1024, 2),
    SPREAD(Material.SCULK_CATALYST, "SCULK_CATALYST&1&§cЗараженный Катализатор&;§7Использует §cдуши §7для;§7создания §cскалка&34&G", TransferType.TAKE, 128, 0),
    MINE(Material.LODESTONE, "LODESTONE&1&§bУпитанная Скала&;§7Вбирает разные §bкамни,;§7создав §bдуши §7взамен&27", TransferType.GIVE, 64, 1),
    FUSE(Material.RESPAWN_ANCHOR, "RESPAWN_ANCHOR&1&§5Пробужденный Котел&;§7Привязывает §5души §7к;§5допустимым §7вещам&30", TransferType.TAKE, 64, 0),
    ;
	
    public final Material mat;
    public final ItemStack blockItem;
    public final TransferType trans;
    public final int maxSouls;
    public final int maxTeth;
    
    CuBType(final Material mat, final String blockItem, final TransferType trans, final int maxSouls, final int maxTeth) {
        this.blockItem = ItemUtil.parseItem(blockItem, "&");
        this.mat = mat;
        this.trans = trans;
        this.maxSouls = maxSouls;
        this.maxTeth = maxTeth;
    }
}
