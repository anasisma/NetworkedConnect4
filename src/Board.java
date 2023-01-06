import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Board extends JPanel implements MouseListener {
    int slotSize = 70;
    private final Color[][] grid = new Color[6][7];

    private final Game game;

    private final Color Circles = new Color(229, 208, 220);
    private final Color BG = new Color(205, 179, 219);
    private final Color Purple = new Color(138, 105, 166);
    private final Color Pink = new Color(201, 126, 169);

    public Board() {
        game = new Game();
        setPreferredSize(new Dimension(720, 800));
        addMouseListener(this);

        for (int col = 0; col < grid.length; col++) {
            for (int row = 0; row < grid[0].length; row++) {
                grid[col][row] = Circles;
            }
        }

    }

    public Game getGame() {
        return game;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setColor(BG);
        g2d.fillRect(0, 0, getSize().width, getSize().height);

        drawCells(g);

        g2d.setFont(new Font("Verdana", Font.PLAIN, 50));
        g2d.setColor(Color.WHITE);
        g2d.drawString("Connect 4", 230, 60);
        g2d.setFont(new Font("Verdana", Font.PLAIN, 90));
        g2d.drawString(String.valueOf(game.getPinkScore()), 170, 700);
        g2d.drawString(String.valueOf(game.getPurpScore()), 490, 700);
        g2d.setFont(new Font("Verdana", Font.PLAIN, 30));
        g2d.drawString("Pink's Score:", 100, 620);
        g2d.drawString("Purple's Score:", 395, 620);

        startTimer();

        if (game.winnerExists()) {
            g2d.setColor(BG);
            g2d.fillRect(180, 120, 360, 250);
            g2d.setColor(Color.black);
            g2d.drawRect(180, 120, 360, 250);
            g2d.setColor(Color.WHITE);
            if (game.didPinkWin()) {
                g2d.drawString("Pink Wins!", 280, 170);
                printMenu(g);
            } else {
                g2d.drawString("Purple Wins!", 270, 170);
                printMenu(g);
            }
        } else if (game.quitting()) {
            g2d.setColor(BG);
            g2d.fillRect(160, 120, 400, 65);
            g2d.setColor(Color.black);
            g2d.drawRect(160, 120, 400, 65);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Opponent disconnected", 182, 165);
        } else if (game.error()) {
            g2d.setColor(BG);
            g2d.fillRect(160, 120, 400, 65);
            g2d.setColor(Color.black);
            g2d.drawRect(160, 120, 400, 65);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Server error occured!", 190, 165);
        }
    }

    public void drawCells(Graphics g) {
        int startX = 85;
        int startY = 85;
        int currX = startX;
        int currY = startY;

        Graphics2D g2d = (Graphics2D) g;
        for (Color[] colors : grid) {
            for (int col = 0; col < grid[0].length; col++) {
                g2d.setColor(colors[col]);
                g2d.fillOval(currX, currY, slotSize, slotSize);
                g2d.setColor(Color.black);
                g2d.drawOval(currX, currY, slotSize, slotSize);
                g2d.setColor(BG);
                currX += slotSize + 10;
            }
            currX = startX;
            currY += slotSize + 10;
        }
    }

    public void printMenu(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Circles);
        g2d.fillRect(200, 200, 320, 65);
        g2d.fillRect(200, 285, 320, 65);
        g2d.setColor(Color.black);
        g2d.drawRect(200, 200, 320, 65);
        g2d.drawRect(200, 285, 320, 65);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Play Again", 280, 242);
        g2d.drawString("Quit", 315, 327);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (!game.winnerExists() && !game.quitting() && !game.error()) {
            int col = (e.getX() - 80) / (slotSize + 10);
//            int player = game.getPlayer();
            int row = game.setPlay(col);
            if (row != -1) {
                getCellColors();
                repaint();
            }
        } else {
            if (game.winnerExists()) {
                int posX = e.getX();
                int posY = e.getY();
                if (posX >= 200 && posX <= 420) {
                    if (posY >= 200 && posY <= 265) {
                        game.sendRestart();
                        updateBoard();
                    } else if (posY >= 285 && posY <= 350) {
                        game.quit();
                        System.exit(0);
                    }
                }
            }
        }
    }

    public void getCellColors() {
        int[][] board = game.getBoard();
        for (int col = 0; col < board.length; col++) {
            for (int row = 0; row < board[0].length; row++) {
                if (board[col][row] == 0)
                    grid[col][row] = Circles;
                else if (board[col][row] == 1)
                    grid[col][row] = Purple;
                else if (board[col][row] == 2)
                    grid[col][row] = Pink;
            }
        }
    }

    public void startTimer() {
        Timer timer = new Timer(100, e -> SwingUtilities.invokeLater(() -> {
            updateBoard();
            repaint();
        }));
        timer.start();
    }

    public void updateBoard() {
        getCellColors();
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }
}