package kopfgeldjaeger.ui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import kopfgeldjaeger.agents.AuthorityAgent;
import kopfgeldjaeger.agents.BountyHunterAgent;
import kopfgeldjaeger.agents.CityEnvironmentAgent;
import kopfgeldjaeger.agents.Position;
import kopfgeldjaeger.agents.PrisonAgent;
import kopfgeldjaeger.agents.ShopAgent;
import kopfgeldjaeger.agents.TargetAgent;

import kopfgeldjaeger.logic.BountyTarget;
import kopfgeldjaeger.logic.CombatEngine;
import kopfgeldjaeger.logic.HunterState;
import kopfgeldjaeger.logic.PrisonState;
import kopfgeldjaeger.logic.TargetType;
import kopfgeldjaeger.logic.Upgrade;

import java.util.ArrayList;
import java.util.List;

public class GameApplication extends Application
{
    // -------------------------------------------------
    // KARTE
    // -------------------------------------------------

    private static final double WIDTH = 1200;
    private static final double HEIGHT = 800;

    private static final double PLAYER_RADIUS = 20;
    private static final double TARGET_RADIUS = 20;

    private static final double BASE_SPEED = 10;
    private static final double MIN_TARGET_DISTANCE = 180;

    // -------------------------------------------------
    // SPIELER / ZIEL
    // -------------------------------------------------

    private Circle hunterCircle;
    private Circle targetCircle;

    private Group hunterSkin;
    private Group targetSkin;

    // -------------------------------------------------
    // GEBÄUDE & HINDERNISSE
    // -------------------------------------------------

    private Rectangle shopBuilding;
    private Rectangle authorityBuilding;
    private Rectangle prisonBuilding;

    private final List<Rectangle> obstacles = new ArrayList<>();

    // -------------------------------------------------
    // HUD
    // -------------------------------------------------

    private Text hunterHealthText;
    private Text targetHealthText;
    private Text moneyText;
    private Text targetInfoText;
    private Text statusText;
    private Text prisonInfoText;

    // -------------------------------------------------
    // AGENTEN & KAMPFLOGIK
    // -------------------------------------------------

    private CityEnvironmentAgent cityAgent;
    private BountyHunterAgent hunterAgent;
    private TargetAgent currentTargetAgent;
    private AuthorityAgent authorityAgent;
    private ShopAgent shopAgent;
    private PrisonAgent prisonAgent;

    private CombatEngine combatEngine;

    private boolean inCombat = false;
    private boolean gameOver = false;
    private boolean healAvailable = false;
    private boolean shopOpen = false;
    private boolean prisonOpen = false;

    // -------------------------------------------------
    // START
    // -------------------------------------------------

    @Override
    public void start(Stage stage)
    {
        Pane root = new Pane();

        // Hintergrund
        Rectangle background = new Rectangle(WIDTH, HEIGHT);
        background.setFill(Color.web("#667F59"));
        root.getChildren().add(background);

        // Straßen
        createRoads(root);
        addRoadMarkings(root);

        // Gebäude
        createShop(root);
        createAuthority(root);
        createPrison(root);
        createResidentialBuildings(root);

        // Natur / Dekoration
        createNature(root);

        // Agenten initialisieren
        initAgents();

        // Unsichtbare Hitbox Spieler
        hunterCircle = new Circle(
                hunterAgent.getPosition().getX(),
                hunterAgent.getPosition().getY(),
                PLAYER_RADIUS
        );
        hunterCircle.setFill(Color.TRANSPARENT);

        // Jäger-Skin
        hunterSkin = createHunterSkin();
        updateHunterSkinPosition();

        // Unsichtbare Hitbox Ziel
        targetCircle = new Circle(TARGET_RADIUS);
        targetCircle.setFill(Color.TRANSPARENT);

        // Ziel-Skin
        targetSkin = new Group();

        // HUD
        hunterHealthText = new Text(20, 30, "");
        targetHealthText = new Text(20, 55, "");
        moneyText = new Text(20, 80, "");
        targetInfoText = new Text(20, 105, "");
        statusText = new Text(20, 135, "");
        prisonInfoText = new Text(700, 30, "");

        hunterHealthText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        moneyText.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        root.getChildren().addAll(
                targetCircle,
                targetSkin,
                hunterCircle,
                hunterSkin,
                hunterHealthText,
                targetHealthText,
                moneyText,
                targetInfoText,
                statusText,
                prisonInfoText
        );

        // Erstes Ziel über Umgebungsagent spawnen
        spawnTarget();
        updateHud();

        // Gefängnis-Einnahmen über Gefängnisagent
        startPrisonIncomeTimer();

        Scene scene = new Scene(root, WIDTH, HEIGHT);

        // Tastatursteuerung
        scene.setOnKeyPressed(event ->
        {
            if (gameOver)
            {
                if (event.getCode() == KeyCode.R)
                {
                    restartGame();
                }
                return;
            }

            if (shopOpen)
            {
                if (event.getCode() == KeyCode.DIGIT1)
                {
                    buyUpgrade(Upgrade.BATON);
                    return;
                }
                if (event.getCode() == KeyCode.DIGIT2)
                {
                    buyUpgrade(Upgrade.VEST);
                    return;
                }
                if (event.getCode() == KeyCode.DIGIT3)
                {
                    buyUpgrade(Upgrade.BICYCLE);
                    return;
                }
                if (event.getCode() == KeyCode.ESCAPE)
                {
                    shopOpen = false;
                    statusText.setText("Shop geschlossen.");
                    return;
                }
                return;
            }

            if (prisonOpen)
            {
                if (event.getCode() == KeyCode.ESCAPE)
                {
                    prisonOpen = false;
                    statusText.setText("Gefängnis geschlossen.");
                    return;
                }
                if (event.getCode() == KeyCode.DIGIT1)
                {
                    if (!prisonAgent.getPrisonState().isPurchased())
                    {
                        buyPrison();
                    }
                    else
                    {
                        imprisonCurrentTarget();
                    }
                    return;
                }
                if (event.getCode() == KeyCode.DIGIT2)
                {
                    buyPrisonCell();
                    return;
                }
                return;
            }

            if (inCombat)
            {
                if (event.getCode() == KeyCode.SPACE)
                {
                    playCombatRound();
                }
                return;
            }

            if (event.getCode() == KeyCode.E)
            {
                if (isNearPrison())
                {
                    openPrison();
                    return;
                }
                if (isNearAuthority())
                {
                    deliverTargetAtAuthority();
                    return;
                }
                if (isNearShop())
                {
                    openShop();
                    return;
                }
            }

            if (event.getCode() == KeyCode.N)
            {
                requestNewTarget();
                return;
            }

            if (event.getCode() == KeyCode.H && healAvailable)
            {
                hunterAgent.heal(10);
                healAvailable = false;
                statusText.setText("Pause gemacht: +10 Leben.");
                updateHud();
                return;
            }

            double currentSpeed = BASE_SPEED * hunterAgent.getState().getMovementMultiplier();

            if (event.getCode() == KeyCode.W)
            {
                tryMove(0, -currentSpeed);
            }
            if (event.getCode() == KeyCode.S)
            {
                tryMove(0, currentSpeed);
            }
            if (event.getCode() == KeyCode.A)
            {
                tryMove(-currentSpeed, 0);
            }
            if (event.getCode() == KeyCode.D)
            {
                tryMove(currentSpeed, 0);
            }

            checkCollision();
            updateLocationHint();
        });

        stage.setTitle("Kopfgeldjäger - Stadt (Agenten-Modus)");
        stage.setScene(scene);
        stage.show();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        root.requestFocus();
    }

    // -------------------------------------------------
    // AGENTEN INITIALISIEREN
    // -------------------------------------------------

    private void initAgents()
    {
        cityAgent = new CityEnvironmentAgent(WIDTH, HEIGHT, 150, PLAYER_RADIUS);
        for (Rectangle obs : obstacles)
        {
            cityAgent.addObstacle(obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight());
        }

        HunterState hunterState = new HunterState("Kopfgeldjäger", 60, 12, 0);
        hunterAgent = new BountyHunterAgent(hunterState, new Position(410, 470));

        authorityAgent = new AuthorityAgent();
        shopAgent = new ShopAgent();
        prisonAgent = new PrisonAgent();

        System.out.println("[System] Alle Agenten initialisiert.");
    }

    // -------------------------------------------------
    // STRASSEN & GEBÄUDE
    // -------------------------------------------------

    private void createRoads(Pane root)
    {
        Color sidewalkColor = Color.web("#BDBDBD");
        Color roadColor = Color.web("#454545");

        Rectangle sidewalk1 = new Rectangle(0, 265, WIDTH, 130);
        sidewalk1.setFill(sidewalkColor);

        Rectangle sidewalk2 = new Rectangle(0, 545, WIDTH, 130);
        sidewalk2.setFill(sidewalkColor);

        Rectangle sidewalk3 = new Rectangle(345, 0, 130, HEIGHT);
        sidewalk3.setFill(sidewalkColor);

        Rectangle sidewalk4 = new Rectangle(805, 0, 130, HEIGHT);
        sidewalk4.setFill(sidewalkColor);

        Rectangle road1 = new Rectangle(0, 280, WIDTH, 100);
        road1.setFill(roadColor);

        Rectangle road2 = new Rectangle(0, 560, WIDTH, 100);
        road2.setFill(roadColor);

        Rectangle road3 = new Rectangle(360, 0, 100, HEIGHT);
        road3.setFill(roadColor);

        Rectangle road4 = new Rectangle(820, 0, 100, HEIGHT);
        road4.setFill(roadColor);

        root.getChildren().addAll(
                sidewalk1, sidewalk2, sidewalk3, sidewalk4,
                road1, road2, road3, road4
        );
    }

    private void addRoadMarkings(Pane root)
    {
        Color lineColor = Color.web("#EEE7C7");
        addHorizontalDashes(root, 330, lineColor);
        addHorizontalDashes(root, 610, lineColor);
        addVerticalDashes(root, 410, lineColor);
        addVerticalDashes(root, 870, lineColor);
    }

    private void addHorizontalDashes(Pane root, double y, Color color)
    {
        addHorizontalDashSegment(root, 10, 350, y, color);
        addHorizontalDashSegment(root, 470, 810, y, color);
        addHorizontalDashSegment(root, 930, WIDTH, y, color);
    }

    private void addHorizontalDashSegment(Pane root, double startX, double endX, double y, Color color)
    {
        for (double x = startX; x < endX; x += 46)
        {
            Rectangle dash = new Rectangle(x, y, 28, 5);
            dash.setFill(color);
            root.getChildren().add(dash);
        }
    }

    private void addVerticalDashes(Pane root, double x, Color color)
    {
        addVerticalDashSegment(root, 150, 270, x, color);
        addVerticalDashSegment(root, 390, 550, x, color);
        addVerticalDashSegment(root, 670, HEIGHT, x, color);
    }

    private void addVerticalDashSegment(Pane root, double startY, double endY, double x, Color color)
    {
        for (double y = startY; y < endY; y += 46)
        {
            Rectangle dash = new Rectangle(x, y, 5, 28);
            dash.setFill(color);
            root.getChildren().add(dash);
        }
    }

    private void createShop(Pane root)
    {
        double x = 985;
        double y = 690;
        double width = 170;
        double height = 85;

        shopBuilding = new Rectangle(x, y, width, height);
        shopBuilding.setFill(Color.web("#E5C49B"));
        shopBuilding.setStroke(Color.web("#5A3928"));
        shopBuilding.setStrokeWidth(3);
        obstacles.add(shopBuilding);

        Rectangle roof = new Rectangle(x - 5, y - 12, width + 10, 20);
        roof.setFill(Color.web("#6D3528"));

        Rectangle window1 = new Rectangle(x + 15, y + 32, 42, 30);
        window1.setFill(Color.web("#A6D8E7"));
        window1.setStroke(Color.web("#4F4F4F"));

        Rectangle window2 = new Rectangle(x + 112, y + 32, 42, 30);
        window2.setFill(Color.web("#A6D8E7"));
        window2.setStroke(Color.web("#4F4F4F"));

        Rectangle door = new Rectangle(x + 70, y + 35, 30, 50);
        door.setFill(Color.web("#59402F"));

        Rectangle awning = new Rectangle(x + 8, y + 15, width - 16, 13);
        awning.setFill(Color.web("#B73535"));

        Text sign = new Text(x + 55, y + 13, "SHOP");
        sign.setFill(Color.WHITE);
        sign.setFont(Font.font("Arial", FontWeight.BOLD, 17));

        root.getChildren().addAll(shopBuilding, roof, window1, window2, door, awning, sign);
    }

    private void createAuthority(Pane root)
    {
        double x = 50;
        double y = 690;
        double width = 180;
        double height = 85;

        authorityBuilding = new Rectangle(x, y, width, height);
        authorityBuilding.setFill(Color.web("#D6D9D8"));
        authorityBuilding.setStroke(Color.web("#465867"));
        authorityBuilding.setStrokeWidth(3);
        obstacles.add(authorityBuilding);

        Rectangle roof = new Rectangle(x - 5, y - 12, width + 10, 18);
        roof.setFill(Color.web("#536A7A"));

        Rectangle door = new Rectangle(x + 72, y + 38, 36, 47);
        door.setFill(Color.web("#30495D"));

        Rectangle column1 = new Rectangle(x + 35, y + 25, 12, 60);
        column1.setFill(Color.web("#EEEEEE"));

        Rectangle column2 = new Rectangle(x + 133, y + 25, 12, 60);
        column2.setFill(Color.web("#EEEEEE"));

        Rectangle window1 = new Rectangle(x + 10, y + 35, 20, 25);
        window1.setFill(Color.web("#9CC7D9"));

        Rectangle window2 = new Rectangle(x + 150, y + 35, 20, 25);
        window2.setFill(Color.web("#9CC7D9"));

        Text sign = new Text(x + 46, y + 18, "BEHÖRDE");
        sign.setFill(Color.web("#243846"));
        sign.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        root.getChildren().addAll(authorityBuilding, roof, door, column1, column2, window1, window2, sign);
    }

    private void createPrison(Pane root)
    {
        double x = 50;
        double y = 165;
        double width = 170;
        double height = 80;

        prisonBuilding = new Rectangle(x, y, width, height);
        prisonBuilding.setFill(Color.web("#686868"));
        prisonBuilding.setStroke(Color.web("#292929"));
        prisonBuilding.setStrokeWidth(4);
        obstacles.add(prisonBuilding);

        Rectangle roof = new Rectangle(x - 4, y - 10, width + 8, 16);
        roof.setFill(Color.web("#303030"));

        Rectangle door = new Rectangle(x + 68, y + 34, 34, 46);
        door.setFill(Color.web("#262626"));

        Rectangle window1 = new Rectangle(x + 18, y + 30, 30, 25);
        window1.setFill(Color.web("#9FA7A8"));

        for (int i = 0; i < 3; i++)
        {
            Rectangle bar = new Rectangle(x + 23 + i * 9, y + 30, 3, 25);
            bar.setFill(Color.BLACK);
            root.getChildren().add(bar);
        }

        Rectangle window2 = new Rectangle(x + 122, y + 30, 30, 25);
        window2.setFill(Color.web("#9FA7A8"));

        for (int i = 0; i < 3; i++)
        {
            Rectangle bar = new Rectangle(x + 127 + i * 9, y + 30, 3, 25);
            bar.setFill(Color.BLACK);
            root.getChildren().add(bar);
        }

        Text sign = new Text(x + 43, y + 18, "GEFÄNGNIS");
        sign.setFill(Color.WHITE);
        sign.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        root.getChildren().addAll(prisonBuilding, roof, window1, window2, door, sign);
    }

    private void createResidentialBuildings(Pane root)
    {
        addHouse(root, 245, 165, 90, 80);
        addHouse(root, 510, 165, 120, 80);
        addHouse(root, 655, 165, 120, 80);
        addHouse(root, 965, 165, 100, 80);
        addHouse(root, 1080, 165, 100, 80);

        addHouse(root, 60, 420, 110, 95);
        addHouse(root, 205, 420, 110, 95);
        addHouse(root, 500, 420, 120, 95);
        addHouse(root, 655, 420, 120, 95);
        addHouse(root, 960, 420, 100, 95);
        addHouse(root, 1080, 420, 100, 95);

        addHouse(root, 250, 690, 85, 85);
        addHouse(root, 500, 690, 120, 85);
        addHouse(root, 655, 690, 120, 85);
    }

    private void addHouse(Pane root, double x, double y, double width, double height)
    {
        String[] houseColors = {"#D8C5A5", "#C99B77", "#D5B895", "#B98A70"};
        String houseColor = houseColors[obstacles.size() % houseColors.length];

        Rectangle house = new Rectangle(x, y, width, height);
        house.setFill(Color.web(houseColor));
        house.setStroke(Color.web("#49392F"));
        house.setStrokeWidth(3);

        Rectangle roof = new Rectangle(x, y, width, 18);
        roof.setFill(Color.web("#704A3A"));

        Rectangle door = new Rectangle(x + width / 2 - 9, y + height - 30, 18, 30);
        door.setFill(Color.web("#5A3825"));

        Rectangle window1 = new Rectangle(x + 15, y + 35, 18, 16);
        window1.setFill(Color.web("#A9D8E8"));
        window1.setStroke(Color.web("#444444"));

        Rectangle window2 = new Rectangle(x + width - 33, y + 35, 18, 16);
        window2.setFill(Color.web("#A9D8E8"));
        window2.setStroke(Color.web("#444444"));

        root.getChildren().addAll(house, roof, door, window1, window2);
        obstacles.add(house);
    }

    private void createNature(Pane root)
    {
        addTree(root, 25, 215);
        addTree(root, 485, 215);
        addTree(root, 945, 215);

        addTree(root, 25, 475);
        addTree(root, 485, 475);
        addTree(root, 945, 475);

        addTree(root, 25, 735);
        addTree(root, 475, 735);
        addTree(root, 950, 735);
    }

    private void addTree(Pane root, double x, double y)
    {
        Rectangle obstacle = new Rectangle(x - 13, y - 13, 26, 30);
        obstacles.add(obstacle);

        Rectangle trunk = new Rectangle(x - 4, y + 5, 8, 17);
        trunk.setFill(Color.web("#6C492E"));

        Circle crownMain = new Circle(x, y, 15);
        crownMain.setFill(Color.web("#246B36"));

        Circle crownLeft = new Circle(x - 9, y + 3, 10);
        crownLeft.setFill(Color.web("#2F7D43"));

        Circle crownRight = new Circle(x + 9, y + 3, 10);
        crownRight.setFill(Color.web("#2F7D43"));

        Circle crownTop = new Circle(x, y - 7, 10);
        crownTop.setFill(Color.web("#39894C"));

        root.getChildren().addAll(trunk, crownMain, crownLeft, crownRight, crownTop);
    }

    // -------------------------------------------------
    // SKINS
    // -------------------------------------------------

    private Group createHunterSkin()
    {
        Group skin = new Group();

        Rectangle leftLeg = new Rectangle(-9, 8, 7, 13);
        leftLeg.setFill(Color.web("#252525"));

        Rectangle rightLeg = new Rectangle(2, 8, 7, 13);
        rightLeg.setFill(Color.web("#252525"));

        Rectangle body = new Rectangle(-11, -10, 22, 23);
        body.setArcWidth(6);
        body.setArcHeight(6);
        body.setFill(Color.web("#284E78"));

        Rectangle leftArm = new Rectangle(-16, -7, 5, 18);
        leftArm.setFill(Color.web("#C9956D"));

        Rectangle rightArm = new Rectangle(11, -7, 5, 18);
        rightArm.setFill(Color.web("#C9956D"));

        Circle head = new Circle(0, -17, 8);
        head.setFill(Color.web("#D1A078"));

        Rectangle hat = new Rectangle(-9, -27, 18, 7);
        hat.setFill(Color.web("#4A3326"));

        Rectangle hatBrim = new Rectangle(-13, -21, 26, 3);
        hatBrim.setFill(Color.web("#4A3326"));

        Rectangle belt = new Rectangle(-11, 5, 22, 4);
        belt.setFill(Color.web("#3B2C23"));

        skin.getChildren().addAll(leftLeg, rightLeg, leftArm, rightArm, body, belt, head, hat, hatBrim);
        return skin;
    }

    private void createTargetSkin(TargetType type)
    {
        targetSkin.getChildren().clear();

        Color clothingColor;
        boolean special = false;

        switch (type)
        {
            case PETTY_CRIMINAL:
                clothingColor = Color.web("#C83B3B");
                break;
            case THIEF:
                clothingColor = Color.web("#8E44AD");
                break;
            case ROBBER:
                clothingColor = Color.web("#69308C");
                break;
            case DANGEROUS_FUGITIVE:
                clothingColor = Color.web("#3B145F");
                break;
            case SPECIAL_TARGET:
                clothingColor = Color.web("#211030");
                special = true;
                break;
            default:
                clothingColor = Color.RED;
                break;
        }

        Rectangle leftLeg = new Rectangle(-8, 7, 6, 13);
        leftLeg.setFill(Color.web("#252525"));

        Rectangle rightLeg = new Rectangle(2, 7, 6, 13);
        rightLeg.setFill(Color.web("#252525"));

        Rectangle body = new Rectangle(-11, -10, 22, 22);
        body.setArcWidth(5);
        body.setArcHeight(5);
        body.setFill(clothingColor);

        Rectangle leftArm = new Rectangle(-16, -7, 5, 17);
        leftArm.setFill(clothingColor);

        Rectangle rightArm = new Rectangle(11, -7, 5, 17);
        rightArm.setFill(clothingColor);

        Circle head = new Circle(0, -17, 8);
        head.setFill(Color.web("#C99470"));

        Rectangle mask = new Rectangle(-8, -20, 16, 5);
        mask.setFill(Color.web("#242424"));

        targetSkin.getChildren().addAll(leftLeg, rightLeg, leftArm, rightArm, body, head, mask);

        if (special)
        {
            Circle goldRing = new Circle(0, 0, 24);
            goldRing.setFill(Color.TRANSPARENT);
            goldRing.setStroke(Color.GOLD);
            goldRing.setStrokeWidth(3);
            targetSkin.getChildren().add(0, goldRing);
        }
    }

    private void updateHunterSkinPosition()
    {
        hunterSkin.setLayoutX(hunterCircle.getCenterX());
        hunterSkin.setLayoutY(hunterCircle.getCenterY());
    }

    private void updateTargetSkinPosition()
    {
        targetSkin.setLayoutX(targetCircle.getCenterX());
        targetSkin.setLayoutY(targetCircle.getCenterY());
    }

    // -------------------------------------------------
    // AGENTEN-AKTIONEN IN DER UI
    // -------------------------------------------------

    private void tryMove(double deltaX, double deltaY)
    {
        if (hunterAgent.moveBy(deltaX, deltaY, cityAgent))
        {
            hunterCircle.setCenterX(hunterAgent.getPosition().getX());
            hunterCircle.setCenterY(hunterAgent.getPosition().getY());
            updateHunterSkinPosition();
        }
    }

    private void spawnTarget()
    {
        currentTargetAgent = cityAgent.spawnRandomTarget(hunterAgent, MIN_TARGET_DISTANCE);
        BountyTarget target = currentTargetAgent.getTarget();

        createTargetSkin(currentTargetAgent.getType());

        targetCircle.setCenterX(currentTargetAgent.getPosition().getX());
        targetCircle.setCenterY(currentTargetAgent.getPosition().getY());
        targetCircle.setVisible(true);
        targetSkin.setVisible(true);
        updateTargetSkinPosition();

        System.out.println("[Umgebungsagent] Zielpersonenagent gespawnt: "
                + target.getName() + " an " + currentTargetAgent.getPosition());

        targetInfoText.setText(
                "Gesuchtes Ziel: " + target.getName() + " | Kopfgeld: " + target.getBounty() + " EUR"
        );

        if (healAvailable)
        {
            statusText.setText("H = +10 Leben | N = neues Ziel");
        }
        else
        {
            statusText.setText("Verfolge das Ziel. N = neues Ziel");
        }
    }

    private void requestNewTarget()
    {
        if (inCombat || gameOver)
        {
            return;
        }

        if (hunterAgent.hasCarriedTarget())
        {
            statusText.setText("Bring dein gefangenes Ziel zuerst weg.");
            return;
        }

        if (currentTargetAgent != null)
        {
            currentTargetAgent.deactivate();
        }

        targetCircle.setVisible(false);
        targetSkin.setVisible(false);

        spawnTarget();
        statusText.setText("Neues Ziel vom Umgebungsagenten erhalten.");
    }

    private boolean isNearBuilding(Rectangle building, double limit)
    {
        double centerX = building.getX() + building.getWidth() / 2;
        double centerY = building.getY() + building.getHeight() / 2;

        double dx = hunterAgent.getPosition().getX() - centerX;
        double dy = hunterAgent.getPosition().getY() - centerY;

        return Math.sqrt(dx * dx + dy * dy) < limit;
    }

    private boolean isNearShop()
    {
        return isNearBuilding(shopBuilding, 125);
    }

    private boolean isNearAuthority()
    {
        return isNearBuilding(authorityBuilding, 130);
    }

    private boolean isNearPrison()
    {
        return isNearBuilding(prisonBuilding, 125);
    }

    private void openShop()
    {
        shopOpen = true;
        statusText.setText(
                "SHOP: 1 = Schlagstock (300 EUR) | 2 = Weste (300 EUR) | 3 = Fahrrad (400 EUR) | ESC = Schließen"
        );
    }

    private void buyUpgrade(Upgrade upgrade)
    {
        boolean purchased = shopAgent.sellUpgrade(hunterAgent, upgrade);
        System.out.println("[Shopagent] Verkauf " + upgrade.getDisplayName() + ": " + purchased);

        if (purchased)
        {
            statusText.setText(upgrade.getDisplayName() + " gekauft!");
        }
        else
        {
            statusText.setText("Kauf nicht möglich (zu wenig Geld oder bereits gekauft).");
        }

        updateHud();
    }

    private void openPrison()
    {
        prisonOpen = true;
        PrisonState prisonState = prisonAgent.getPrisonState();

        if (!prisonState.isPurchased())
        {
            statusText.setText(
                    "GEFÄNGNIS: 1 = Gefängnis kaufen (" + prisonState.getPrisonPrice() + " EUR) | ESC = Schließen"
            );
            return;
        }

        statusText.setText(
                "GEFÄNGNIS: 1 = Sonderziel einsperren | 2 = Zelle kaufen ("
                        + prisonState.getCellPrice() + " EUR) | ESC = Schließen"
        );
    }

    private void buyPrison()
    {
        boolean success = prisonAgent.buyPrison(hunterAgent);
        System.out.println("[Gefängnisagent] Gefängnis gekauft: " + success);

        if (success)
        {
            statusText.setText("Gefängnis gekauft! "
                    + prisonAgent.getPrisonState().getCapacity() + " Zellen verfügbar.");
        }
        else
        {
            statusText.setText("Gefängnis konnte nicht gekauft werden.");
        }

        updateHud();
    }

    private void buyPrisonCell()
    {
        if (!prisonAgent.getPrisonState().isPurchased())
        {
            statusText.setText("Du besitzt noch kein Gefängnis.");
            return;
        }

        boolean success = prisonAgent.buyCell(hunterAgent);
        System.out.println("[Gefängnisagent] Zusätzliche Zelle gekauft: " + success);

        if (success)
        {
            statusText.setText("Neue Zelle gekauft. Kapazität: "
                    + prisonAgent.getPrisonState().getCapacity());
        }
        else
        {
            statusText.setText("Nicht genug Geld für eine neue Zelle.");
        }

        updateHud();
    }

    private void imprisonCurrentTarget()
    {
        if (!hunterAgent.hasCarriedTarget())
        {
            statusText.setText("Du hast kein gefangenes Ziel dabei.");
            return;
        }

        if (currentTargetAgent == null || !currentTargetAgent.isSpecialTarget())
        {
            statusText.setText("Nur Sonderziele können hier eingesperrt werden.");
            return;
        }

        if (prisonAgent.getPrisonState().isFull())
        {
            statusText.setText("Das Gefängnis ist voll.");
            return;
        }

        boolean success = prisonAgent.imprisonSpecialTarget(hunterAgent, currentTargetAgent);
        System.out.println("[Gefängnisagent] Sonderziel eingesperrt: " + success);

        if (!success)
        {
            statusText.setText("Das Ziel konnte nicht eingesperrt werden.");
            return;
        }

        spawnTarget();

        PrisonState p = prisonAgent.getPrisonState();
        statusText.setText("Sonderziel eingesperrt! " + p.getPrisoners() + " / " + p.getCapacity() + " Zellen belegt.");
        updateHud();
    }

    private void deliverTargetAtAuthority()
    {
        int bounty = authorityAgent.processHandover(hunterAgent);
        System.out.println("[Behördenagent] Übergabe verarbeitet, Auszahlung: +" + bounty + " EUR");

        if (bounty <= 0)
        {
            statusText.setText("Du hast kein gefangenes Ziel.");
            return;
        }

        spawnTarget();
        statusText.setText("Ziel abgeliefert: +" + bounty + " EUR");
        updateHud();
    }

    private void updateLocationHint()
    {
        if (isNearPrison())
        {
            statusText.setText("Gefängnis: E = öffnen");
            return;
        }

        if (hunterAgent.hasCarriedTarget() && isNearAuthority())
        {
            statusText.setText("Behörde: E = Ziel abliefern");
            return;
        }

        if (isNearShop())
        {
            statusText.setText("Shop: E = öffnen");
        }
    }

    private void checkCollision()
    {
        if (inCombat || currentTargetAgent == null || !currentTargetAgent.isActive())
        {
            return;
        }

        if (cityAgent.checkEncounter(hunterAgent, currentTargetAgent))
        {
            System.out.println("[Umgebungsagent] Begegnung zwischen Kopfgeldjäger und Zielperson erkannt!");
            startCombat();
        }
    }

    private void startCombat()
    {
        combatEngine = cityAgent.startCombat(currentTargetAgent);
        inCombat = true;
        statusText.setText("Kampf gestartet! LEERTASTE = Angriff");
        updateHud();
    }

    private void playCombatRound()
    {
        CombatEngine.Result result = combatEngine.playRound(
                hunterAgent.getState(),
                currentTargetAgent.getTarget()
        );

        updateHud();

        if (result == CombatEngine.Result.IN_PROGRESS)
        {
            statusText.setText("Kampf läuft... LEERTASTE = nächster Angriff");
            return;
        }

        if (result == CombatEngine.Result.TARGET_CAPTURED)
        {
            currentTargetAgent.deactivate();
            hunterAgent.captureTarget(currentTargetAgent.getTarget());
            System.out.println("[Kopfgeldjägeragent] Ziel festgenommen: "
                    + hunterAgent.getCarriedTarget().getName());

            targetCircle.setVisible(false);
            targetSkin.setVisible(false);
            inCombat = false;
            healAvailable = true;

            targetInfoText.setText("Gefangen: " + hunterAgent.getCarriedTarget().getName());

            if (currentTargetAgent.isSpecialTarget())
            {
                statusText.setText("Sonderziel gefangen! Behörde oder Gefängnis möglich.");
            }
            else
            {
                statusText.setText("Ziel gefangen! Bring es zur Behörde.");
            }

            updateHud();
            return;
        }

        if (result == CombatEngine.Result.TARGET_ESCAPED)
        {
            System.out.println("[Zielpersonenagent] Ziel ist entkommen!");
            currentTargetAgent.deactivate();
            targetCircle.setVisible(false);
            targetSkin.setVisible(false);
            inCombat = false;
            healAvailable = true;

            spawnTarget();
            return;
        }

        if (result == CombatEngine.Result.HUNTER_DEFEATED)
        {
            System.out.println("[Kampf] Kopfgeldjäger wurde besiegt.");
            currentTargetAgent.deactivate();
            targetCircle.setVisible(false);
            targetSkin.setVisible(false);
            gameOver = true;
            inCombat = false;
            healAvailable = false;
            hunterAgent.clearCarriedTarget();

            statusText.setText("GAME OVER - Drücke R zum Neustarten.");
            updateHud();
        }
    }

    private void startPrisonIncomeTimer()
    {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(20), event ->
                {
                    if (gameOver)
                    {
                        return;
                    }

                    int income = prisonAgent.collectPeriodicIncome(hunterAgent);
                    if (income > 0)
                    {
                        System.out.println("[Gefängnisagent] Regelmäßige Einnahmen ausgezahlt: +" + income + " EUR");
                        updateHud();

                        if (!inCombat && !shopOpen && !prisonOpen)
                        {
                            statusText.setText("Gefängnis-Einnahmen: +" + income + " EUR");
                        }
                    }
                })
        );

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void restartGame()
    {
        initAgents();

        hunterCircle.setCenterX(hunterAgent.getPosition().getX());
        hunterCircle.setCenterY(hunterAgent.getPosition().getY());
        updateHunterSkinPosition();

        gameOver = false;
        inCombat = false;
        healAvailable = false;
        shopOpen = false;
        prisonOpen = false;

        spawnTarget();
        updateHud();
    }

    private void updateHud()
    {
        HunterState state = hunterAgent.getState();

        hunterHealthText.setText("Jäger Leben: " + state.getHealth() + " / " + state.getMaxHealth());
        moneyText.setText("Geld: " + state.getMoney() + " EUR");

        if (currentTargetAgent != null && inCombat)
        {
            BountyTarget t = currentTargetAgent.getTarget();
            targetHealthText.setText("Ziel Leben: " + t.getHealth() + " / " + t.getMaxHealth());
        }
        else
        {
            targetHealthText.setText("Ziel Leben: -");
        }

        PrisonState p = prisonAgent.getPrisonState();
        if (!p.isPurchased())
        {
            prisonInfoText.setText("Gefängnis: nicht gekauft | " + p.getPrisonPrice() + " EUR");
        }
        else
        {
            prisonInfoText.setText(
                    "Gefängnis: " + p.getPrisoners() + " / " + p.getCapacity()
                            + " | +" + p.getIncomePerPrisoner() + " EUR/Gefangener"
            );
        }
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}