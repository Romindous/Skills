package ru.romindous.skills;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.komiss77.OConfigManager;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.menuItem.MenuItem;
import ru.komiss77.modules.menuItem.MenuItemBuilder;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.config.ConfigVars;
import ru.romindous.skills.skills.chas.Chastic;
import ru.romindous.skills.survs.Role;
import ru.romindous.skills.guides.Entries;
import ru.romindous.skills.objects.Groups;
import ru.romindous.skills.items.SkillGroup;
import ru.romindous.skills.mobs.Minion;
import ru.romindous.skills.mobs.Mobs;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Survivor;
import ru.romindous.skills.utils.pets.IPetManager;


public class Main extends JavaPlugin {

    public static Main main;
    public static final String manaClr = Chastic.MANA.color() + "🔥 ";
    public static final String cdClr = Chastic.COOLDOWN.color() + "⌚ ";
    public static final String prefix = "<sky>[<cardinal>SN<sky>] ";
    public static final SecureRandom srnd = new SecureRandom();
    public static final SubServer subServer = SubServer.get();
    public static MenuItem diary;
    public static Mobs mobs;
    public static SkillGroup mats;

    public static OConfigManager configManager;
    public static IPetManager petMgr;
    public static final Path configDir = Path.of(Path.of(Bukkit.getPluginsFolder().toURI())
        .toAbsolutePath().getParent().getParent().toString(), "skills");

    private static final boolean turnOffAddons = false;

    @Override
    public void onEnable() {
        //OSTROV stuff
        TCUtil.N = "<sky>";
        TCUtil.P = "<cardinal>";
        TCUtil.A = "<indigo>";
        PM.setOplayerFun(Survivor::new, true);

        main = this;
        configManager = new OConfigManager(main);
        Ostrov.log_ok("Skills loading...");
        Ostrov.log_ok("§bПодсервер седны: " + subServer.disName);
        ConfigVars.load();

        /*if (Bukkit.getPluginManager().getPlugin("MyPet") == null || turnOffAddons) {
            petMgr = new PetVanilla();
        } else {
            petMgr = new PetVanilla();
        }*/

        diary = new MenuItemBuilder("diary", new ItemBuilder(ItemType.WRITTEN_BOOK)
            .name("§6Дневник").lore(Arrays.asList(" ", "§7С двойкой домой", "§7не приходить!")).glint(true).build())
            .slot(8).giveOnJoin(false).giveOnRespavn(true).giveOnWorld_change(false)
            .forced(false).canDrop(false).canMove(true).canPickup(false)
            .duplicate(false).rightClickCmd("skill menu").leftClickCmd("menu").create();

        mobs = new Mobs();

        SubServer.init();
        Role.init();
        Groups.init();
        Entries.init();
        SM.init();

        for (final Class<?> clazz : ClassUtil.getClasses(main.getFile(), "ru.romindous.skills.listeners")) {
            try {
                getServer().getPluginManager().registerEvents((Listener) clazz.getDeclaredConstructor().newInstance(), this);
            } catch (IllegalAccessException | IllegalArgumentException | InstantiationException
                     | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                Ostrov.log_err("Инициализация listener " + clazz.getName() + " -> " + ex.getMessage());
            }
        }

        getCommand("skill").setExecutor(new SkillCmd());

        Ostrov.log_ok("Skills done!");
    }

    @Override
    public void onDisable() {
//        bossMgr.disable();
        SM.disable();
        HandlerList.unregisterAll(this);
        Ostrov.log_ok("§4Sedna is off!");
    }

    public static boolean addHp(final LivingEntity le, final double hlth, final boolean goOver) {
        final AttributeInstance hpa = le.getAttribute(Attribute.MAX_HEALTH);
        if (hpa.getBaseValue() < le.getHealth() + hlth) {
            if (goOver) {
                hpa.setBaseValue((int) le.getHealth() + hlth + 1);
            } else {
                le.setHealth(hpa.getBaseValue());
                return false;
            }
        }
        le.setHealth(le.getHealth() + hlth);
        return true;
        //Bukkit.broadcast(Component.text("gained " + hlth + ", now " + le.getHealth() + hlth));
    }

    public static boolean canAttack(final LivingEntity dmgr, final Entity tgt, final boolean tell) {
        if (!(dmgr instanceof final Player dpl) || !(tgt instanceof final Player tpl)) {
            return dmgr.getEntityId() != tgt.getEntityId() && !Minion.isOwner(tgt, dmgr);
        }
        final Survivor dsv = PM.getOplayer(dpl, Survivor.class);
        if (dsv == null) return false;
        final Survivor tsv = PM.getOplayer(tpl, Survivor.class);
        if (tsv == null) return false;

        if (dpl.getEntityId() == tpl.getEntityId()) {
            if (tell) dmgr.sendMessage(TCUtil.form(Main.prefix + "§cНельзя атаковать себя!"));
            return false;
        } else if (NumUtil.mulDiff(tsv.exp, dsv.exp) != 1) {
            if (tell) dmgr.sendMessage(TCUtil.form(Main.prefix + "§cРазница в вашем уровне слишком велика!"));
            return false;
        }
        return true;
    }

	/*public static String nrmlzStr(final String s) {
		final char[] ss = s.toLowerCase().toCharArray();
		ss[0] = Character.toUpperCase(ss[0]);
		for (byte i = (byte) (ss.length - 1); i > 0; i--) {
			switch (ss[i]) {
			case '_':
				ss[i] = ' ';
			case ' ':
				ss[i + 1] = Character.toUpperCase(ss[i + 1]);
				break;
			default:
				break;
			}
		}
		return String.valueOf(ss);
	}

	public static String toSigFigs(final float n, final byte sf) {
		final String nm = String.valueOf(n);
		return nm.indexOf('.') + sf + 1 < nm.length() ? nm.substring(0, nm.indexOf('.') + sf + 1) : nm;
	}*/
}
