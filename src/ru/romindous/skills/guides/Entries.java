package ru.romindous.skills.guides;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.quests.Quest;
import ru.komiss77.modules.quests.QuestManager;
import ru.komiss77.utils.TCUtil;
import ru.romindous.skills.SubServer;
import ru.romindous.skills.objects.Groups;
import ru.romindous.skills.skills.Rarity;
import ru.romindous.skills.skills.abils.Ability;
import ru.romindous.skills.skills.mods.Modifier;
import ru.romindous.skills.skills.sels.Selector;
import ru.romindous.skills.skills.trigs.Trigger;

public class Entries {

    public static final Entry WASTES = new Entry('a', ItemType.PODZOL, 0, null, null, SubServer.WASTES.disName,
        TCUtil.N + "Последствия мировой войны...", SubServer.WASTES.bGrndTxtr, Quest.QuestVis.ALWAYS, Quest.QuestFrame.TASK);

        public static final Entry table = new Entry('b', ItemType.CRAFTING_TABLE, 0, null, WASTES, "Ремесло",
            "Создай верстак для прогрессии", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK);

            public static final Entry iron = new Entry('c', ItemType.IRON_INGOT, 2, null, table, "Сидим, куём",
                "Найди железо и выплавь его в слитки", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK, Section.MATS,
                "Используя добытые слитки, я могу соорудить 0[<dark_gray>кузнечный стол], который пригодится мне в изготовлении новых материалов! Надо бы подкопить различных руд...",
                ItemType.SMITHING_TABLE.createItemStack());

                public static final Entry smith = new Entry('d', ItemType.SMITHING_TABLE, 0, null, iron, "Твердая основа",
                    "Скрафти кузнечный стол из выплавленых слитков", "", Quest.QuestVis.PARENT, Quest.QuestFrame.GOAL, Section.CRAFTS,
                    "Теперь я могу применить 0[<dark_gray>стол кузнеца] для создания новых материалов! Начну пожалуй с комбинации обыденных металлов этого мира - сырых 1[<gray>железа] и 2[<amber>меди], в одно целое...",
                    ItemType.SMITHING_TABLE.createItemStack(), ItemType.RAW_IRON.createItemStack(), ItemType.RAW_COPPER.createItemStack());

                    public static final Entry silver = new Entry('e', new ItemBuilder(ItemType.RAW_IRON).glint(true).build(), 0, null, smith, "Сияющие камни",
                        "Скомбинируй сырое железо с медью", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK, Section.MATS,
                        "Меня удивила стабильность этого 0[<gray>'сплава']. Из него можно создать 2[<gray>оружие] и 3[<gray>инструменты], или совместить с 1[<dark_gray>чешуёй] местной твари, для создания 4[<gray>особой брони]. Их рецепты теперь записаны в крафт книжке!",
                        Groups.SILVER.item(ItemType.IRON_INGOT), Groups.SILVER.item(ItemType.PHANTOM_MEMBRANE), Groups.SILVER.item(ItemType.IRON_SWORD), Groups.SILVER.item(ItemType.IRON_PICKAXE), Groups.SILVER.item(ItemType.CHAINMAIL_CHESTPLATE));

            public static final Entry copper = new Entry('f', ItemType.COPPER_INGOT, 12, null, table, "Медный филиал",
                "Выплавь слитки меди для снаряжения", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK, Section.MATS,
                "По моим наблюдениям, 0[<amber>медь] была хрупче железа. Однако, 1[<gold>оружие] и 2[<gold>броня] скованые из нее выдают электрические заряды в сторону врагов, что может быть полезно в бою. Их рецепты теперь записаны в крафт книжке!",
                ItemType.COPPER_INGOT.createItemStack(), Groups.MEDAL.item(ItemType.GOLDEN_SWORD), Groups.MEDAL.item(ItemType.GOLDEN_CHESTPLATE));

        public static final Entry mob = new Entry('g', ItemType.ROTTEN_FLESH, 0, null, WASTES, "Чужая кровь",
            "Убей первого монстра любого вида", "", Quest.QuestVis.PARENT, Quest.QuestFrame.GOAL);

        private static final ItemStack TRIG_IT = new ItemBuilder(ItemType.HOPPER).name(TCUtil.sided(Rarity.COMMON.color() + "<i>Тригер<!i>", Trigger.SIDE))
            .lore("<mithril>[Описание цели / действия]").build();
        private static final ItemStack ABIL_IT = new ItemBuilder(ItemType.COAST_ARMOR_TRIM_SMITHING_TEMPLATE).name(TCUtil.sided(Rarity.COMMON.color() + "<i>Способность<!i> I", Ability.SIDE))
            .lore("<mithril>[Требуемая роль]", "", "<mithril>[Описание способности]", "<mithril>(Нужная экипировка)", "", "<mithril>[Влияющие модификаторы]").hide(DataComponentTypes.PROVIDES_TRIM_MATERIAL, DataComponentTypes.TRIM).build();
        public static final Entry trig = new Entry('h', ItemType.HOPPER, 0, null, WASTES, "Начало цикла",
            "Выбери тригер своему навыку", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK, Section.SKILLS,
            "Суровые условия этой местности заставляют меня експерементировать с навыками. Выбранный мной 0[<pink>тригер] - начало навыка, исполняющий конкретную 1[<indigo>способность]. Т.е. эффект навыка напрямую зависит от выбранного тригера...",
            TRIG_IT, ABIL_IT);

            private static final ItemStack SEL_IT = new ItemBuilder(ItemType.LIGHT_GRAY_DYE).name(TCUtil.sided(Rarity.COMMON.color() + "<i>Подборник<!i> I", Selector.SIDE))
                .lore("<mithril>[Требуемая роль]", "", "<mithril>[Способ выбора сущностей]", "", "<mithril>[Влияющие модификаторы]", "<mithril>(Эффект на навык)").build();
            private static final ItemStack MOD_IT = new ItemBuilder(ItemType.ARMS_UP_POTTERY_SHERD).name(TCUtil.sided(Rarity.COMMON.color() + "<i>Модификатор<!i> I", Modifier.SIDE))
                .lore("<mithril>[Требуемая роль]", "", "<mithril>[Пересчет характеристик]", "<mithril>(Другие требования)").build();
            public static final Entry abil = new Entry('i', ItemType.DRIED_KELP, 0, null, trig, "Интересный лут",
                "Получи скрижаль способности", "", Quest.QuestVis.PARENT, Quest.QuestFrame.GOAL, Section.SKILLS,
                "Используя эту скрижаль, я могу выучить данную 0[<indigo>способность]. Ее комбинация с 1[<sky>подборником] и подходящими 2[<dark_aqua>модификаторами] позволит мне создать навык, который поможет мне выжить в этой дыре...",
                ABIL_IT, SEL_IT, MOD_IT);

                public static final Entry skill = new Entry('k', ItemType.CAMPFIRE, 0, null, abil, "А на деле...",
                    "Используй навык в первый раз", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK, Section.SKILLS,
                    "Использование навыка истощает собранные души, но может мне немало помочь. Открытие различных 0[<indigo>способностей] и 1[<dark_aqua>модификаторов] позволит мне оптимизировать данный навык и создать из него свой боевой стиль...",
                    ABIL_IT, MOD_IT);

                    public static final Entry new_abil = new Entry('m', ItemType.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 0, null, skill, "Друг за дружкой",
                        "Создай цепочку из двух способностей", "", Quest.QuestVis.HIDDEN, Quest.QuestFrame.TASK, Section.SKILLS,
                        "Возможность иметь более одного 0[<dark_aqua>модификатора] заставила меня задуматся о похожем потенциале со 1[<indigo>способностями]. В теории, их можно связать в цепочку, и изполнять одну за другой...",
                        MOD_IT, ABIL_IT);

                    public static final Entry new_skill = new Entry('n', ItemType.SOUL_CAMPFIRE, 0, null, skill, "Второе чувство",
                        "Открой второй навык у себя в меню", "", Quest.QuestVis.HIDDEN, Quest.QuestFrame.TASK, Section.SKILLS,
                        "У меня уже достаточно опыта, чтобы реагировать на несколько 0[<pink>тригеров] за раз. Почему бы мне не создать еще один навык, со своими отдельными 1[<indigo>способностями]? Можно даже скомбинировать его выполнение с предыдущим...",
                        TRIG_IT, ABIL_IT);

                public static final Entry combine = new Entry('l', ItemType.ANVIL, 0, null, abil, "Ступень развития",
                    "Соедени два одинаковых компонента навыка", "", Quest.QuestVis.PARENT, Quest.QuestFrame.TASK);

    public static void init() {
        QuestManager.loadQuests();
    }
}
