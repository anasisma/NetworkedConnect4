import javax.swing.*;

public class GUI extends JFrame {

    private final Board board;

    GUI() {
        setTitle("Connect 4");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);
        board = new Board();
        add(board);
        pack();
        setVisible(true);
    }

    public static void main(String[] args) {
        GUI g = new GUI();
        Game game = g.board.getGame();
        game.connectToServer();
        game.startReceiving();
    }

}