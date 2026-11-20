package kopfgeldjaeger.logic;

public class UpgradeDemo
{
    public static void main(String[] args)
    {
        // 1000 Euro sind hier nur Testgeld:
        // 300 + 300 + 400 Euro reichen genau für alle drei Verbesserungen.
        HunterState hunter =
                new HunterState("Testjäger", 60, 12, 1000);

        System.out.println(
                "Schlagstock gekauft: "
                        + hunter.purchaseUpgrade(Upgrade.BATON));

        System.out.println(
                "Schutzweste gekauft: "
                        + hunter.purchaseUpgrade(Upgrade.VEST));

        System.out.println(
                "Fahrrad gekauft: "
                        + hunter.purchaseUpgrade(Upgrade.BICYCLE));

        System.out.println("Schaden: " + hunter.getDamage());
        System.out.println("Maximale Lebenspunkte: " + hunter.getMaxHealth());
        System.out.println(
                "Bewegungsfaktor: " + hunter.getMovementMultiplier());
        System.out.println("Restgeld: " + hunter.getMoney() + " Euro");

        System.out.println(
                "Schutzweste noch einmal gekauft: "
                        + hunter.purchaseUpgrade(Upgrade.VEST));
    }
}