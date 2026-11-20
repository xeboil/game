package kopfgeldjaeger.logic;

public class CombatEngine
{
    public enum Result
    {
        IN_PROGRESS,
        TARGET_CAPTURED,
        HUNTER_DEFEATED,
        TARGET_ESCAPED
    }

    private long startTimeNanos;
    private boolean timerStarted;
    private final long timeLimitNanos;

    public CombatEngine(int timeLimitSeconds)
    {
        if (timeLimitSeconds <= 0)
        {
            throw new IllegalArgumentException("Das Zeitlimit muss größer als 0 sein.");
        }


        this.timeLimitNanos = timeLimitSeconds * 1_000_000_000L;
    }

    public Result playRound(Combatant hunter, Combatant target)
    {
        if (target.isDefeated())
        {
            return Result.TARGET_CAPTURED;
        }

        if (hunter.isDefeated())
        {
            return Result.HUNTER_DEFEATED;
        }
        if (!timerStarted)
        {
            startTimeNanos = System.nanoTime();
            timerStarted = true;
        }
        if (System.nanoTime() - startTimeNanos >= timeLimitNanos)
        {
            return Result.TARGET_ESCAPED;
        }

        // Der Kopfgeldjäger greift zuerst an.
        target.takeDamage(hunter.getDamage());

        if (target.isDefeated())
        {
            return Result.TARGET_CAPTURED;
        }

        // Das Ziel schlägt zurück.
        hunter.takeDamage(target.getDamage());

        if (hunter.isDefeated())
        {
            return Result.HUNTER_DEFEATED;
        }

        return Result.IN_PROGRESS;
    }
}