package kopfgeldjaeger.logic;

public class CombatDemo
{
    public static void main(String[] args)
    {
        // Testwerte für den Jäger; die Gruppe muss die Startwerte noch festlegen.
        HunterState hunter = new HunterState("Kopfgeldjäger", 60, 12, 0);

        // Erstellt einen Dieb mit den Werten aus TargetType.
        BountyTarget target = TargetType.THIEF.createTarget();

        // Das Zeitlimit kommt ebenfalls vom ausgewählten Zieltyp.
        CombatEngine engine =
                new CombatEngine(target.getTimeLimitSeconds());

        CombatEngine.Result result = CombatEngine.Result.IN_PROGRESS;

        System.out.println("Kampf gegen: " + target.getName());

        while (result == CombatEngine.Result.IN_PROGRESS)
        {
            result = engine.playRound(hunter, target);

            System.out.println("Jäger: " + hunter.getHealth() + " LP");
            System.out.println("Ziel: " + target.getHealth() + " LP");
            System.out.println("---");
        }

        System.out.println("Kampfergebnis: " + result);

        if (result == CombatEngine.Result.TARGET_CAPTURED)
        {
            hunter.deliverTarget(target);

            System.out.println("Kopfgeld: " + target.getBounty() + " Euro");
            System.out.println("Geld des Jägers: " + hunter.getMoney() + " Euro");

            try
            {
                hunter.deliverTarget(target);
                System.out.println("FEHLER: Das Ziel wurde zweimal bezahlt.");
            }
            catch (IllegalStateException e)
            {
                System.out.println("Zweitauszahlung verhindert.");
            }

            System.out.println(
                    "Geld nach dem zweiten Versuch: " + hunter.getMoney() + " Euro");
            boolean batonPurchased = hunter.purchaseUpgrade(Upgrade.BATON);
            System.out.println("Schlagstock gekauft: " + batonPurchased);
            System.out.println("Schaden des Jägers: " + hunter.getDamage());
            System.out.println("Restgeld: " + hunter.getMoney() + " Euro");

            boolean vestPurchased = hunter.purchaseUpgrade(Upgrade.VEST);
            System.out.println("Schutzweste mit Restgeld gekauft: " + vestPurchased);
            System.out.println("Maximale Lebenspunkte: " + hunter.getMaxHealth());
        }
    }
}