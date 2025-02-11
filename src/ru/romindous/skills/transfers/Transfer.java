package ru.romindous.skills.transfers;

import java.lang.ref.WeakReference;
import org.bukkit.Location;
import org.bukkit.World;
import ru.komiss77.modules.player.PM;
import ru.romindous.skills.survs.SM;
import ru.romindous.skills.survs.Survivor;

public interface Transfer {
	
	int distSQ = 100;
	WeakReference<Transfer> etr = new WeakReference<>(null);
	
	int getTId();
	
	int getSouls();
	
	int getMaxSouls();

	Location getLoc(final World w);
	
	TransferType getTransType();

	boolean addTo(final Transfer to);
	
	boolean hasTo(final Transfer to);

	boolean rmvTo(final Transfer to);
	
//	public boolean addFrom(final Transfer from);
	
//	public boolean rmvFrom(final Transfer from);
	
	boolean transferSouls(final World in);
	
	void changeSouls(final int amt);
	
	void tetherTo(final Transfer to, final World w);
	
	void transferTick(final World in);

	static boolean validate(final Transfer tr) {
        return switch (tr) {
            case final Survivor sv -> PM.getOplayer(sv.id) != null;
            case final CuBlock cb -> SM.cublocks.containsKey(cb.getCube().getEntityId());
            case null, default -> false;
        };
    }
	
	@Override
    String toString();
}
