import java.net.*;
import java.io.*;

public class Game {
    private int[][] board;

    private int pinkScore;
    private int purpScore;

    private boolean pinkWon;
    private boolean winnerExists;

    private int playerID;
    private int otherPlayer;
    private int numPlayers;

    private boolean yourTurn;

    private boolean quit;
    private boolean serverError;

    ClientSideConnection csc;

    public Game() {
        board = new int[6][7];
        pinkScore = 0;
        purpScore = 0;
        pinkWon = false;
        winnerExists = false;
        numPlayers = 0;
        yourTurn = false;
        otherPlayer = 0;
        quit = false;
        serverError = false;
    }

    public void connectToServer() {
        csc = new ClientSideConnection();
    }

    public void startReceiving() {
        Thread t = new Thread(() -> {
            while (!csc.isClosed() && !serverError) {
                csc.receiveServerData();
            }
        });
        t.start();
    }

    private class ClientSideConnection {

        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;

        public ClientSideConnection() {
            System.out.println(" ------- Client started ------- ");
            try {
                socket = new Socket("localhost", 10101);
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());

                playerID = dataIn.readInt();
                if (playerID == 1)
                    otherPlayer = 2;
                else
                    otherPlayer = 1;
                System.out.println("Connected to server as player " + playerID);

                numPlayers = dataIn.readInt();

                if (playerID == 1) {
                    yourTurn = true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isClosed() {
            return socket.isClosed();
        }

        public void sendServerData(String s) {
            try {
                char last = s.charAt(s.length() - 1);

                if (s.length() == 1) {
                    if (last == 'q') {
                        dataOut.writeUTF(s);
                        dataOut.close();
                        dataIn.close();
                        socket.close();
                    }
                    dataOut.writeUTF(s);
                }
                else if (s.charAt(s.length() - 1) == 'm') {
                    dataOut.writeUTF(s);
                    dataOut.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void receiveServerData() {
            String n;
            try {
                n = dataIn.readUTF();
                char last = n.charAt(n.length() - 1);
                char secLast = '0';
                if (n.length() >= 2)
                    secLast = n.charAt(n.length() - 2);
                char thrdLast = '0';
                if (n.length() >= 3) {
                    thrdLast = n.charAt(n.length() - 3);
                }
                char frthLast = '0';
                if (n.length() >= 4) {
                    frthLast = n.charAt(n.length() - 4);
                }
                if (last == 'w') {
                    winnerExists = true;
                    if (secLast == '1') {
                        purpScore = Integer.parseInt(String.valueOf(thrdLast));
                    } else {
                        pinkScore = Integer.parseInt(String.valueOf(thrdLast));
                    }
                } else if (last == 'p') {
                    numPlayers = 2;
                } else if (last == 'q') {
                    restart();
                    System.out.println("Other user quit :(");
                    quit = true;
                    numPlayers = 1;
                } else if (last == 'r') {
                    restart();
                }
                if (last == 'm' || frthLast == 'm') {
                    String[] charsSplit = n.split("-");
                    board[Integer.parseInt(charsSplit[0])][Integer.parseInt(charsSplit[1])] = otherPlayer;
                    yourTurn = true;
                }
            } catch (Exception e) {
                serverError = true;
            }
        }
    }

    public boolean quitting() {
        return quit;
    }

    public boolean error() {
        return serverError;
    }

    public boolean didPinkWin() {
        return pinkWon;
    }

    public int getPinkScore() {
        return pinkScore;
    }

    //
    public int getPurpScore() {
        return purpScore;
    }

    public boolean winnerExists() {
        return winnerExists;
    }

    public int[][] getBoard() {
        return board;
    }

    public int setPlay(int i) {
        int k = -1;

        if (yourTurn && numPlayers == 2) {
            for (int[] ints : board) {
                if (ints[i] == 0)
                    k++;
            }
            String charsString = "" + k + "-" + i + "-" + "m";

            board[k][i] = playerID;

            csc.sendServerData(charsString);
            yourTurn = false;
        } else {
            System.out.println("Waiting for other player before proceeding.");
        }
        return k;
    }

    public void restart() {
        board = new int[6][7];
        pinkWon = false;
        winnerExists = false;
        if (playerID == 1)
            yourTurn = true;
        else
            yourTurn = false;
        csc.sendServerData("i");
    }

    public void sendRestart() {
        csc.sendServerData("r");
    }

    public void quit() {
        csc.sendServerData("q");
    }
}