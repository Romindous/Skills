package ru.romindous.skills.utils.pets;

public class PetManager /*implements IPetManager*/ {/*

    @Override
    public List<String> getDebugInfo(List<String> lore) {
        lore.set(0, "§7Активные питомцы: §6"+MyPetApi.getMyPetManager().getAllActiveMyPets().length);
        return lore;
    }

    @Override
    public boolean petCmd(final Player p, final Survivor sv) {
        final MyPetPlayer mpp = MyPetApi.getPlayerManager().isMyPetPlayer(p) ? MyPetApi.getPlayerManager().getMyPetPlayer(p) : MyPetApi.getPlayerManager().registerMyPetPlayer(p);
        final WorldGroup wg = WorldGroup.getGroupByWorld(p.getWorld());
        if (mpp.hasMyPet()) {
            p.sendMessage(Main.prefix + "§cУ вас уже есть питомец!");
            return false;
        }

        final InactiveMyPet imp = new InactiveMyPet(mpp);
        switch (sv.role) {
            case АРБАЛЕТЧИК:
                imp.setPetType(MyPetType.Fox);
                imp.setSkilltree(new Skilltree("Archer"));
                imp.setPetName(sv.role.color + "Хитрый Зверь");
                break;
            case АССАСИН:
                imp.setPetType(MyPetType.CaveSpider);
                imp.setSkilltree(new Skilltree("Assasin"));
                imp.setPetName(sv.role.color + "Смерть (кродеться)");
                break;
            case ВАМПИР:
                imp.setPetType(MyPetType.Bat);
                imp.setSkilltree(new Skilltree("Vampire"));
                imp.setPetName(sv.role.color + "Кровосися");
                break;
            case КАМЕННОКОЖИЙ:
                imp.setPetType(MyPetType.Silverfish);
                imp.setSkilltree(new Skilltree("Tank"));
                imp.setPetName(sv.role.color + "Оживший Камень");
                break;
            case МАГ:
                imp.setPetType(MyPetType.Allay);
                imp.setSkilltree(new Skilltree("Mage"));
                imp.setPetName(sv.role.color + "Зачарованый Сосуд");
                break;
            case ПИРОМАН:
                imp.setPetType(MyPetType.MagmaCube);
                imp.setSkilltree(new Skilltree("Pyro"));
                imp.setPetName(sv.role.color + "Сгусток Магмы");
                break;
            case ФАНТОМ:
                imp.setPetType(MyPetType.Phantom);
                imp.setSkilltree(new Skilltree("Phantom"));
                imp.setPetName(sv.role.color + "Покоритель Небес");
                break;
            case НЕКРОМАНТ:
                imp.setPetType(MyPetType.Strider);
                imp.setSkilltree(new Skilltree("Necro"));
                imp.setPetName(sv.role.color + "Сгусток Плоти");
                break;
            case ПАЛАДИН:
            default:
                imp.setPetType(MyPetType.Wolf);
                imp.setSkilltree(new Skilltree("Paladin"));
                imp.setPetName(sv.role.color + "Оружепесец");
                break;
        }
        imp.setWorldGroup(wg.getName());
        imp.getOwner().setMyPetForWorldGroup(wg.getName(), imp.getUUID());
        //p.sendMessage("i-" + imp.getInfo());
        //p.sendMessage("d-" + imp.getInfo().getCompoundData());

        MyPetApi.getPlugin().getRepository().addMyPet((StoredMyPet) imp, new RepositoryCallback<Boolean>() {
            @Override
            public void callback(final Boolean value) {
                p.sendMessage(imp.getPetName() + " §fтеперь ваш питомец!");
                MyPetApi.getMyPetManager().activateMyPet((StoredMyPet) imp).ifPresent(MyPet::createEntity);
            }
        });
        return true;
    }

    @Override
    public ClickableItem getMenuItem(final Player p) {
        final MyPetPlayer mpp = MyPetApi.getPlayerManager().isMyPetPlayer(p) ? MyPetApi.getPlayerManager().getMyPetPlayer(p) : MyPetApi.getPlayerManager().registerMyPetPlayer(p);

        if (mpp.hasMyPet()) {
            final MyPet mp = mpp.getMyPet();
            return ClickableItem.of(new ItemBuilder(ItemType.RABBIT_HIDE).name("§e<obf>k</obf>§6 Твой Питомец §e<obf>k")
                    .lore("")
                    .lore("§7Имя: " + mp.getPetName())
                    .lore("§7Уровень: §6" + mp.getExperience().getLevel())
                    .lore("§7HP: §c" + (int) mp.getHealth() + "§7/§c" + (int) mp.getMaxHealth())
                    .lore("§7Голод: §e" + (int) mp.getSaturation())
                    .lore("")
                    .lore("§6Клик §7- призвать")
                    .lore("§6ПКМ §7- отозвать")
                    .lore("§e/petinfo §7 - более информации")
                    .build(), e -> {
                        switch (e.getClick()) {
                            case LEFT:
                            case SHIFT_LEFT:
                            default:
                                p.performCommand("pc");
                                break;
                            case RIGHT:
                            case SHIFT_RIGHT:
                                p.performCommand("psa");
                                break;
                        }
                        p.closeInventory();
                    });
        } else {
            return ClickableItem.of(new ItemBuilder(ItemType.RABBIT_HIDE).name("§e<obf>k</obf>§6 Твой Питомец §e<obf>k")
                    .lore("")
                    .lore("§6Клик §7- создать питомца")
                    .build(), e -> {
                        p.closeInventory();
                        p.performCommand("skill pet");
                    });
        }
    }

    @EventHandler
    public void onPetKill(final MyPetDamageEvent e) {
        if (e.getTarget() instanceof Mob) {
            final LivingEntity le = (LivingEntity) e.getTarget();
            if (le.getHealth() <= e.getDamage()) {
                SM.getSurvivor(e.getOwner().getPlayer()).addXp(e.getOwner().getPlayer(), (int) (le.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * 0.1d));
            }
        }
    }

    @Override
    public void removePet(Player p) {
        final MyPetPlayer mpp = MyPetApi.getPlayerManager().isMyPetPlayer(p) ? MyPetApi.getPlayerManager().getMyPetPlayer(p) : MyPetApi.getPlayerManager().registerMyPetPlayer(p);
        if (mpp.hasMyPet()) {
            mpp.getMyPet().removePet();
        }
    }

*/}
