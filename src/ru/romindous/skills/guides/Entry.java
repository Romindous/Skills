package ru.romindous.skills.guides;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.player.Oplayer;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.TCUtil;
import ru.komiss77.utils.inventory.ClickableItem;
import ru.romindous.skills.survs.Survivor;

public class Entry extends Quest {

    private static final String DESC_CLR = "<white>";
    private static final String TASK_CLR = "<aqua>";
    private static final String GOAL_CLR = "<green>";
    private static final String CHAL_CLR = "<light_purple>";
    private static final String ENTRY_CLR = "<stale>";

    private static final Map<Section, List<Entry>> sectioned = new EnumMap<>(Section.class);
    private static final Pattern SUB_PAT = Pattern.compile("\\d\\[[^]]+(]|$)");
    private static final ItemStack OUT_BOUND = new ItemBuilder(ItemType.BEDROCK).name("<red>No item for val!").build();

    public final @Nullable Section sec;
    public final @Nullable Component page;

    public <G extends Comparable<?>> Entry(final char code, final ItemType icon, final int amount,
        final @Nullable G[] needs, final Quest parent, final String displayName, final String description,
        final String backGround, final QuestVis vision, final QuestFrame frame,
        final Section sec, final String desc, final ItemStack... shows) {
        this(code, icon.createItemStack(), amount, needs, parent, displayName,
            description, backGround, vision, frame, sec, desc, shows);
    }

    public <G extends Comparable<?>> Entry(final char code, final ItemStack icon, final int amount,
        final @Nullable G[] needs, final Quest parent, final String displayName, final String description,
        final String backGround, final QuestVis vision, final QuestFrame frame,
        final Section sec, final String desc, final ItemStack... shows) {
        super(code, icon, amount, needs, parent, (parent == null ? "" : frameClr(frame))
                + displayName, DESC_CLR + description, backGround, vision, frame, 0);
        this.sec = sec;
        Component lc = Component.empty();
        final Matcher mt = SUB_PAT.matcher(desc);
        int next = 0;
        while (mt.find()) {
            final String part = mt.group();
            final int ix = part.charAt(0) - 48;
            lc = lc.append(TCUtil.form(ENTRY_CLR + desc.substring(next, mt.start())))
                .append(TCUtil.form("<u>" + desc.substring(mt.start() + 2, mt.end() - 1) + "</u>")
                    .hoverEvent(ix < shows.length ? shows[ix] : OUT_BOUND));
            next = mt.end();
        }
        if (next < desc.length())
            lc = lc.append(TCUtil.form(ENTRY_CLR + desc.substring(next)));
        this.page = lc;
        sectioned.computeIfAbsent(sec, sc -> new ArrayList<>()).add(this);
    }

    public <G extends Comparable<?>> Entry(final char code, final ItemType icon, final int amount,
        final @Nullable G[] needs, final Quest parent, final String displayName, final String description,
        final String backGround, final QuestVis vision, final QuestFrame frame) {
        this(code, icon.createItemStack(), amount, needs, parent, displayName,
            description, backGround, vision, frame);
    }

    public <G extends Comparable<?>> Entry(final char code, final ItemStack icon, final int amount,
        final @Nullable G[] needs, final Quest parent, final String displayName, final String description,
        final String backGround, final QuestVis vision, final QuestFrame frame) {
        super(code, icon, amount, needs, parent, (parent == null ? "" : frameClr(frame))
                + displayName, DESC_CLR + description, backGround, vision, frame, 0);
        this.sec = null; this.page = null;
    }

    private static String frameClr(final QuestFrame frm) {
        return switch (frm) {
            case TASK -> TASK_CLR;
            case GOAL -> GOAL_CLR;
            case CHALLENGE -> CHAL_CLR;
        };
    }

    @Override
    public boolean complete(final Player p, final Oplayer op, final boolean silent) {
        final boolean cmp = super.complete(p, op, silent);
        if (cmp && op instanceof final Survivor sv && sec != null)
            sv.unread.add(this);
        return cmp;
    }

    @Override
    public boolean addProg(final Player p, final Oplayer op) {
        final boolean cmp = isComplete(op);
        final boolean prg = super.addProg(p, op);
        if (!cmp && isComplete(op) && sec != null
            && op instanceof final Survivor sv)
            sv.unread.add(this);
        return prg;
    }

    @Override
    public boolean addProg(final Player p, final Oplayer op, final int i) {
        final boolean cmp = isComplete(op);
        final boolean prg = super.addProg(p, op, i);
        if (!cmp && isComplete(op) && sec != null
            && op instanceof final Survivor sv)
            sv.unread.add(this);
        return prg;
    }

    @Override
    public boolean addProg(final Player p, final Oplayer op, final Comparable<?> obj) {
        final boolean cmp = isComplete(op);
        final boolean prg = super.addProg(p, op, obj);
        if (!cmp && isComplete(op) && sec != null
            && op instanceof final Survivor sv)
            sv.unread.add(this);
        return prg;
    }

    public ClickableItem item(final Player p, final Survivor sv) {
        return ClickableItem.of(new ItemBuilder(icon).flags(true, ItemFlag.HIDE_ADDITIONAL_TOOLTIP).name(displayName)
            .lore(description).lore("<dark_gray>Клик - прочесть").glint(sv.unread.contains(this)).build(), e -> {
            if (sec == null || page == null) return;
            p.closeInventory(); p.playSound(p, Sound.BLOCK_CAVE_VINES_HIT, 1f, 0.6f);
            p.openBook(Book.book(TCUtil.form(displayName), Component.text(p.getName()), page));
            sv.unread.remove(this);
        });
    }

    @Nullable
    public static Entry get(final char code) {
        return ClassUtil.cast(Quest.get(code), Entry.class);
    }

    private static final List<Entry> eel = List.of();
    public static List<Entry> getAll(final Section sc) {
        return sectioned.getOrDefault(sc, eel);
    }
}
