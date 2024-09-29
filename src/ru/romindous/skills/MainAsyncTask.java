package ru.romindous.skills;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.komiss77.modules.player.PM;
import ru.komiss77.utils.ScreenUtil;


public class MainAsyncTask implements Runnable {
    
    protected static int proccesTime;

    @Override
    public void run() {

        final long start = System.currentTimeMillis();
//        Bat bat; Location bodyLoc; Location batLoc;
        
        for (final Player p : Bukkit.getOnlinePlayers()) {

            final Survivor sv = PM.getOplayer(p, Survivor.class);
            if (sv == null) {
                continue;
            }
            
            //каждый тик 
            if (sv.role != null) {
                /*if (sv.vampireBatTime>0) {
                    bat = VanilaModel.vampireBats.get(sv.nik);
                    if (bat!=null && !bat.isDead()) {
                        bodyLoc = p.getEyeLocation().add(0d, -1d, 0d);
                        batLoc = bat.getLocation();
                        if (bodyLoc.getX()!=batLoc.getX() || bodyLoc.getY()!=batLoc.getY() || bodyLoc.getZ()!=batLoc.getZ()) {
                            bat.teleportAsync(bodyLoc);
                        }
                    }
                }*/
            }
            
            

            //каждую секунду с рабросом по тикам для игроков
            if (sv.tickAsync % 20 == 0) { //p.getTicksLived() не подходит, выхватывает числа не по порядку
                // --- конец блока, где скилл!=null ---

                //подсказки игрокам
                /*if (sv.totalPlyTime<3000) { //через 30 сек., если глобально наиграл меньше 15 минут
                    switch (sv.currentPlyTime) {
                        case 30:
                            ScreenUtil.sendTitle(p, "", "§7одно §4❤ §7= §f"+HEALTH_DIVIDER*2+" §7НР", 40, 120, 60);
                            break;
                    }
                }*/
                if (sv.currentPlyTime == 60) {
                    ScreenUtil.sendTitle(p, "", "§fАльфа-тест §4Седны", 20, 40, 10);
                } else if (sv.currentPlyTime == 64) {
                    ScreenUtil.sendTitle(p, "§fВозможны", "§6рестарт §f, §cпотеря данных!", 20, 40, 10);
                }

                if (sv.currentPlyTime % 60 == 0) { //раз в минкту
                    //p.sendMessage("§fbleeds:"+sv.bleeds.size()+" arrowsHitInfo:"+sv.arrowsHitInfo.size());
                }

//                sv.totalPlyTime++;
                sv.currentPlyTime++;
                sv.currentLiveSec++;


            }
            // --- конец блока каждую секунду игрока ---

            sv.tickAsync++;

        }
        // --- конец блока каждый тик игрока ---

        
        
        proccesTime =  (int) (System.currentTimeMillis() - start);
    }
    
}
