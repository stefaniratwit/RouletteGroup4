import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.*;

interface Playable {
    void playTurn(int bet, String[] selectedNumbers);
}

abstract class Game {
    protected int bankRoll;
    protected int spinCount;

    public Game(int bankRoll) {
        this.bankRoll = bankRoll;
        this.spinCount = 0;
    }

    public abstract void playTurn(int bet, String[] selectedNumbers);

    public int getBankRoll() {
        return bankRoll;
    }

    public int getSpinCount() {
        return spinCount;
    }

    public abstract void showSummary();
}

class Roulette extends Game implements Playable {
    private List<Integer> spinHistory;
    private TextArea outputArea;

    public Roulette(int bankRoll, TextArea outputArea) {
        super(bankRoll);
        this.outputArea = outputArea;
        spinHistory = new ArrayList<>();
    }

    @Override
    public void playTurn(int bet, String[] selectedNumbers) {
        int betWinnings = getPayout(bet, selectedNumbers.length);
        if (betWinnings == -1) {
            outputArea.appendText("Invalid number of selections. Try 1, 2, 3, 4, 6, 12, or 18 numbers.\n");
            return;
        }

        if (bankRoll >= bet) {
            bankRoll -= bet;
            int spinNumber = spin();
            spinHistory.add(spinNumber);
            String spinDisplay = (spinNumber == 37) ? "00" : String.valueOf(spinNumber);
            outputArea.appendText("\nThe ball landed on: " + spinDisplay + "\n");
            outputArea.appendText(toString() + "\n");

            if (isInList(spinNumber, selectedNumbers)) {
                outputArea.appendText("You picked the winning number " + spinDisplay + " and win $" + betWinnings + "\n");
                bankRoll += betWinnings;
            } else {
                outputArea.appendText("You did not pick the winning number.\n");
            }
        } else {
            outputArea.appendText("You do not have enough money to make this bet.\n");
        }
    }

    private boolean isInList(int winningNum, String[] list) {
        for (String s : list) {
            s = s.trim();
            if ((winningNum == 37 && s.equals("00")) || (s.matches("\\d+") && winningNum == Integer.parseInt(s))) {
                return true;
            }
        }
        return false;
    }

    private int getPayout(int bet, int numbersBet) {
        return switch (numbersBet) {
            case 1 -> bet * 36;
            case 2 -> bet * 18;
            case 3 -> bet * 12;
            case 4 -> bet * 9;
            case 6 -> bet * 6;
            case 12 -> bet * 3;
            case 18 -> bet * 2;
            default -> -1;
        };
    }

    private int spin() {
        spinCount++;
        return (int) (Math.random() * 38);
    }

    @Override
    public void showSummary() {
        outputArea.appendText("\nGame Summary:\n");
        outputArea.appendText("Total spins: " + spinCount + "\n");
        outputArea.appendText("Final bankroll: $" + bankRoll + "\n");
        outputArea.appendText("Spin results: ");
        for (int i = 0; i < spinHistory.size(); i++) {
            int spin = spinHistory.get(i);
            outputArea.appendText((spin == 37 ? "00" : String.valueOf(spin)) +
                    (i < spinHistory.size() - 1 ? ", " : "\n"));
        }
    }

    @Override
    public String toString() {
        return "Bank Roll = $" + this.bankRoll + "\nSpin Count = " + this.spinCount;
    }
}

public class RouletteApp extends Application {
    private Roulette roulette;
    private TextField bankrollInput = new TextField();
    private TextField betInput = new TextField();
    private TextField numbersInput = new TextField();
    private TextArea outputArea = new TextArea();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Roulette Game");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button startButton = new Button("Start Game");
        Button playTurnButton = new Button("Play Turn");
        Button summaryButton = new Button("Show Summary");

        layout.getChildren().addAll(
                new Label("Starting Bankroll:"), bankrollInput,
                startButton,
                new Separator(),
                new Label("Bet Amount:"), betInput,
                new Label("Select Numbers (comma-separated) 00; 1-36:"), numbersInput,
                playTurnButton,
                summaryButton,
                new Label("Game Output:"),
                outputArea
        );

        outputArea.setEditable(false);

        startButton.setOnAction(e -> {
            try {
                int bankroll = Integer.parseInt(bankrollInput.getText());
                roulette = new Roulette(bankroll, outputArea);
                outputArea.clear();
                outputArea.appendText("Game started with bankroll: $" + bankroll + "\n");
            } catch (NumberFormatException ex) {
                outputArea.appendText("Please enter a valid number for bankroll.\n");
            }
        });

        playTurnButton.setOnAction(e -> {
            if (roulette == null) {
                outputArea.appendText("Please start the game first.\n");
                return;
            }

            try {
                int bet = Integer.parseInt(betInput.getText());
                String[] numbers = numbersInput.getText().split(",");

                List<String> cleaned = new ArrayList<>();
                for (String s : numbers) {
                    s = s.trim();
                    if (s.equals("00")) {
                        cleaned.add(s);
                    } else if (s.matches("\\d+")) {
                        int num = Integer.parseInt(s);
                        if (num >= 1 && num <= 36) {
                            cleaned.add(s);
                        } else {
                            outputArea.appendText("Invalid number: " + num + ". Please enter numbers between 1 and 36 or '00'.\n");
                            return;
                        }
                    } else {
                        outputArea.appendText("Invalid input: '" + s + "'. Only digits and '00' are allowed.\n");
                        return;
                    }
                }

                int count = cleaned.size();
                Set<Integer> validBets = Set.of(1, 2, 3, 4, 6, 12, 18);
                if (!validBets.contains(count)) {
                    outputArea.appendText("Invalid number of selections (" + count + "). Please select 1, 2, 3, 4, 6, 12, or 18 numbers.\n");
                    return;
                }

                roulette.playTurn(bet, cleaned.toArray(new String[0]));

            } catch (NumberFormatException ex) {
                outputArea.appendText("Please enter a valid number for the bet.\n");
            }
        });

        summaryButton.setOnAction(e -> {
            if (roulette != null) {
                roulette.showSummary();
            }
        });

        Scene scene = new Scene(layout, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
