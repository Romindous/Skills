package ru.romindous.skills.transfers.modules;

import org.bukkit.World;
import org.bukkit.entity.Slime;

import ru.romindous.skills.transfers.CuBType;
import ru.romindous.skills.transfers.CuBlock;

public class Relay extends CuBlock {

	public Relay(final Slime cube, final boolean load) {
		super(cube, CuBType.RELAY, load);
	}
	
	@Override
	public String getUpdName() {
		return "§dРеле Трансмиссий";
	}
	
	@Override
	public void transferTick(final World in) {
		super.transferTick(in);
	}
}
