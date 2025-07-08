package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;

public class X_and_O {
    // Game Components
    private JFrame window = new JFrame("X AND O Game");
    private JFrame welcomeScreen = new JFrame("Welcome to Tic-Tac-Toe");
    private JFrame scoreboardScreen = new JFrame("Scoreboard");

    // Game Buttons
    private JButton[] gameButtons = new JButton[9];
    private JPanel gamePanel = new JPanel(new GridLayout(3, 3));

    // Player Info
    private JTextField player1Field = new JTextField(15);
    private JTextField player2Field = new JTextField(15);
    private String player1Name = "Player 1";
    private String player2Name = "Player 2";

    // Game Logic
    private ArrayList<Integer> playerOneMoves = new ArrayList<>();
    private ArrayList<Integer> playerTwoMoves = new ArrayList<>();
    private boolean playerOneTurn = true;
    private int roundsPlayed = 0;
    private int player1Wins = 0;
    private int player2Wins = 0;
    private int draws = 0;

    // Score Tracking
    private static final int MAX_ROUNDS = 3;
    private static final int MAX_PLAYERS = 5;
    private static final String SCORE_FILE = "tictactoe_scores.dat";
    private LinkedHashMap<String, Integer> playerScores = new LinkedHashMap<String, Integer>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Integer> eldest) {
            return size() > MAX_PLAYERS;
        }
    };

    public X_and_O() {
        loadScores();
        createWelcomeScreen();
    }

    private void createWelcomeScreen() {
        welcomeScreen.setLayout(new BorderLayout(10, 10));
        welcomeScreen.getContentPane().setBackground(new Color(240, 240, 240));

        // Title Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(240, 240, 240));
        JLabel titleLabel = new JLabel("Welcome to our Tic-Tac-Toe Game!", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Gothic", Font.BOLD, 24));
        titleLabel.setForeground(new Color(70, 130, 180));
        titlePanel.add(titleLabel);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBackground(new Color(240, 240, 240));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel player1Label = new JLabel("(X) Name:");
        JLabel player2Label = new JLabel("(O) Name:");
        player1Field.setFont(new Font("Arial", Font.PLAIN, 16));
        player2Field.setFont(new Font("Arial", Font.PLAIN, 16));
        inputPanel.add(player1Label);
        inputPanel.add(player1Field);
        inputPanel.add(player2Label);
        inputPanel.add(player2Field);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(new Color(240, 240, 240));

        JButton startButton = createStyledButton("Start Game", new Color(70, 130, 180));
        startButton.addActionListener(e -> startGame());

        JButton rulesButton = createStyledButton("View Rules", new Color(178, 34, 34)); // Crimson
        rulesButton.addActionListener(e -> showRulesScreen());

        JButton scoreboardButton = createStyledButton("View Scoreboard", new Color(100, 150, 100));
        scoreboardButton.addActionListener(e -> showScoreboard());

        buttonPanel.add(startButton);
        buttonPanel.add(rulesButton);
        buttonPanel.add(scoreboardButton);

        welcomeScreen.add(titlePanel, BorderLayout.NORTH);
        welcomeScreen.add(inputPanel, BorderLayout.CENTER);
        welcomeScreen.add(buttonPanel, BorderLayout.SOUTH);
        welcomeScreen.setSize(500, 300);
        welcomeScreen.setLocationRelativeTo(null);
        welcomeScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        welcomeScreen.setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    private void showRulesScreen() {
        JFrame rulesFrame = new JFrame("Game Rules");
        rulesFrame.setLayout(new BorderLayout(10, 10));
        rulesFrame.getContentPane().setBackground(new Color(240, 240, 240));

        JTextArea rulesText = new JTextArea(
                "🎯 Rules of X and O (Tic-Tac-Toe):\n\n" +
                        "1. Two players take turns marking spaces.\n" +
                        "2. One plays as 'X', the other as 'O'.\n" +
                        "3. The first to align 3 marks in a row wins.\n" +
                        "4. Rows, columns, or diagonals count.\n" +
                        "5. Game plays up to 3 rounds.\n" +
                        "6. Highest score after 3 rounds wins."
        );
        rulesText.setEditable(false);
        rulesText.setFont(new Font("Arial", Font.PLAIN, 16));
        rulesText.setBackground(new Color(240, 240, 240));
        rulesText.setLineWrap(true);
        rulesText.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(rulesText);
        JButton backButton = createStyledButton("Back to Welcome", new Color(70, 130, 180));
        backButton.addActionListener(e -> {
            rulesFrame.dispose();
            if (!welcomeScreen.isVisible()) {
                welcomeScreen.setVisible(true);
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(240, 240, 240));
        bottomPanel.add(backButton);

        rulesFrame.add(scrollPane, BorderLayout.CENTER);
        rulesFrame.add(bottomPanel, BorderLayout.SOUTH);
        rulesFrame.setSize(500, 400);
        rulesFrame.setLocationRelativeTo(null);
        rulesFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        rulesFrame.setVisible(true);
    }

    private void startGame() {
        if (!player1Field.getText().isEmpty()) {
            player1Name = player1Field.getText();
        }
        if (!player2Field.getText().isEmpty()) {
            player2Name = player2Field.getText();
        }

        playerScores.putIfAbsent(player1Name, 0);
        playerScores.putIfAbsent(player2Name, 0);

        welcomeScreen.dispose();
        initializeGame();
    }

    private void initializeGame() {
        window.getContentPane().removeAll();
        gamePanel.removeAll();

        for (int i = 0; i < 9; i++) {
            gameButtons[i] = new JButton();
            gameButtons[i].setFont(new Font("Arial", Font.BOLD, 60));
            gameButtons[i].setBackground(new Color(220, 220, 220));
            gameButtons[i].setName(String.valueOf(i + 1));
            gameButtons[i].addActionListener(new ButtonClickListener());
            gamePanel.add(gameButtons[i]);
        }

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        infoPanel.setBackground(new Color(240, 240, 240));
        JLabel roundLabel = new JLabel("Round: " + (roundsPlayed + 1) + "/" + MAX_ROUNDS);
        roundLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel turnLabel = new JLabel("Current Turn: " + (playerOneTurn ? player1Name + " (X)" : player2Name + " (O)"));
        turnLabel.setFont(new Font("Arial", Font.BOLD, 16));
        infoPanel.add(roundLabel);
        infoPanel.add(turnLabel);

        window.setLayout(new BorderLayout());
        window.add(infoPanel, BorderLayout.NORTH);
        window.add(gamePanel, BorderLayout.CENTER);
        window.setSize(550, 600);
        window.setLocationRelativeTo(null);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
    }

    private class ButtonClickListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JButton clickedButton = (JButton) e.getSource();
            int buttonNumber = Integer.parseInt(clickedButton.getName());

            if (playerOneTurn) {
                playerOneMoves.add(buttonNumber);
                clickedButton.setText("X");
                clickedButton.setForeground(new Color(70, 130, 180));
            } else {
                playerTwoMoves.add(buttonNumber);
                clickedButton.setText("O");
                clickedButton.setForeground(new Color(220, 20, 60));
            }

            clickedButton.setEnabled(false);
            playerOneTurn = !playerOneTurn;
            updateTurnDisplay();
            checkWinner();
        }
    }

    private void updateTurnDisplay() {
        Component[] components = window.getContentPane().getComponents();
        for (Component comp : components) {
            if (comp instanceof JPanel) {
                Component[] subComps = ((JPanel) comp).getComponents();
                for (Component subComp : subComps) {
                    if (subComp instanceof JLabel && ((JLabel) subComp).getText().startsWith("Current Turn:")) {
                        ((JLabel) subComp).setText("Current Turn: " +
                                (playerOneTurn ? player1Name + " (X)" : player2Name + " (O)"));
                    }
                }
            }
        }
    }

    private void checkWinner() {
        int[][] winningCombinations = {
                {1, 2, 3}, {4, 5, 6}, {7, 8, 9},
                {1, 4, 7}, {2, 5, 8}, {3, 6, 9},
                {1, 5, 9}, {3, 5, 7}
        };
        for (int[] combo : winningCombinations) {
            if (playerOneMoves.contains(combo[0]) && playerOneMoves.contains(combo[1]) && playerOneMoves.contains(combo[2])) {
                player1Wins++;
                playerScores.put(player1Name, playerScores.get(player1Name) + 1);
                endRound(player1Name + " (X) wins this round!");
                return;
            }
            if (playerTwoMoves.contains(combo[0]) && playerTwoMoves.contains(combo[1]) && playerTwoMoves.contains(combo[2])) {
                player2Wins++;
                playerScores.put(player2Name, playerScores.get(player2Name) + 1);
                endRound(player2Name + " (O) wins this round!");
                return;
            }
        }
        if (playerOneMoves.size() + playerTwoMoves.size() == 9) {
            draws++;
            endRound("This round is a draw!");
        }
    }

    private void endRound(String message) {
        roundsPlayed++;
        saveScores();

        if (roundsPlayed >= MAX_ROUNDS) {
            String championshipResult;
            if (player1Wins > player2Wins) {
                championshipResult = player1Name + " wins the championship " + player1Wins + "-" + player2Wins +
                        (draws > 0 ? " with " + draws + " draws" : "") + "!";
            } else if (player2Wins > player1Wins) {
                championshipResult = player2Name + " wins the championship " + player2Wins + "-" + player1Wins +
                        (draws > 0 ? " with " + draws + " draws" : "") + "!";
            } else {
                championshipResult = "The championship is tied " + player1Wins + "-" + player2Wins +
                        (draws > 0 ? " with " + draws + " draws" : "") + "!";
            }

            Object[] options = {"Return to Home", "Scoreboard", "Exit"};
            int choice = JOptionPane.showOptionDialog(window,
                    message + "\n" + championshipResult + "\nWhat would you like to do?",
                    "Championship Complete",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                window.dispose();
                resetChampionship();
                player1Field.setText("");
                player2Field.setText("");
                welcomeScreen.setVisible(true);
            } else if (choice == JOptionPane.NO_OPTION) {
                showScoreboard();
                window.dispose();
                resetChampionship();
                player1Field.setText("");
                player2Field.setText("");
                welcomeScreen.setVisible(true);
            } else {
                System.exit(0);
            }
        } else {
            Object[] options = {"Next Round", "View Scoreboard"};
            int choice = JOptionPane.showOptionDialog(window,
                    message + "\nCurrent Score: " + player1Name + " " + player1Wins + " - " +
                            player2Wins + " " + player2Name + (draws > 0 ? "\nDraws: " + draws : ""),
                    "Round Over",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == JOptionPane.YES_OPTION) {
                resetRound();
            } else {
                showScoreboard();
                resetRound();
            }
        }
    }

    private void resetRound() {
        playerOneMoves.clear();
        playerTwoMoves.clear();
        playerOneTurn = player1Wins == player2Wins || (player1Wins < player2Wins);
        for (JButton button : gameButtons) {
            button.setText("");
            button.setEnabled(true);
        }
        initializeGame();
    }

    private void resetChampionship() {
        roundsPlayed = 0;
        player1Wins = 0;
        player2Wins = 0;
        draws = 0;
        resetRound();
    }

    private void showScoreboard() {
        scoreboardScreen.getContentPane().removeAll();
        scoreboardScreen.setLayout(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));

        JLabel titleLabel = new JLabel("Tic-Tac-Toe Championship Hall of Fame", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(new Color(70, 130, 180));

        String[] columns = {"Rank", "Player", "Wins"};
        Object[][] data = new Object[playerScores.size()][3];

        java.util.List<Map.Entry<String, Integer>> sortedScores = new ArrayList<>(playerScores.entrySet());
        sortedScores.sort(Map.Entry.<String, Integer>comparingByValue().reversed());

        for (int i = 0; i < sortedScores.size(); i++) {
            data[i][0] = i + 1;
            data[i][1] = sortedScores.get(i).getKey();
            data[i][2] = sortedScores.get(i).getValue();
        }

        JTable scoreTable = new JTable(data, columns);
        scoreTable.setFont(new Font("Arial", Font.PLAIN, 14));
        scoreTable.setEnabled(false);
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(scoreTable);

        JButton backButton = createStyledButton("Back to Game", new Color(70, 130, 180));
        backButton.addActionListener(e -> scoreboardScreen.dispose());

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(backButton, BorderLayout.SOUTH);

        scoreboardScreen.add(mainPanel);
        scoreboardScreen.setSize(400, 400);
        scoreboardScreen.setLocationRelativeTo(null);
        scoreboardScreen.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        scoreboardScreen.setVisible(true);
    }

    private void saveScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SCORE_FILE))) {
            oos.writeObject(playerScores);
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadScores() {
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SCORE_FILE))) {
                playerScores = (LinkedHashMap<String, Integer>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading scores: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new X_and_O());
    }
}