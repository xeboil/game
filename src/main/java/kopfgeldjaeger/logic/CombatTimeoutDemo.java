package kopfgeldjaeger.logic;

public class CombatTimeoutDemo
{
    public static void main(String[] args) throws InterruptedException
    {
        Combatant hunter = new Combatant("Kopfgeldjäger", 60, 12);
        Combatant target = new Combatant("Sonderziel", 200, 25);

        CombatEngine engine = new CombatEngine(1);

        // Für diesen Test warten wir absichtlich länger als eine Sekunde.
        Thread.sleep(1100);

        CombatEngine.Result result = engine.playRound(hunter, target);
        System.out.println("Kampfergebnis: " + result);
    }
}