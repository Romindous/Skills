package ru.romindous.skills.transfers.modules;

import org.bukkit.World;
import org.bukkit.entity.Slime;

import ru.romindous.skills.enums.CuBType;
import ru.romindous.skills.transfers.CuBlock;

public class Store extends CuBlock {

	public Store(final Slime cube, final boolean load) {
		super(cube, CuBType.STORE, load);
	}
	
	@Override
	public String getUpdName() {
		return "§5Стабильный Омут";
	}
	
	@Override
	public void transferTick(final World in) {
		super.transferTick(in);
	}
}
