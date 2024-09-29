package ru.romindous.skills.tasks;

public enum TaskType {

    DEFEND(100, 200),
    MOVE(360, 200),
    KILL(360, 200),
    MINE(640, 200);

    public final int time;
    public final int exp;

    TaskType(final int time, final int exp) {
        this.time = time;
        this.exp = exp;
    }
}
