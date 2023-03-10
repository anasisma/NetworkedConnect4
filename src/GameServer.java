import java.io.*;
import java.net.*;

public class GameServer {

    private ServerSocket ss;

    private ServerSideConnection player1;
    private ServerSideConnection player2;

    private int turnsMade;
    private final int maxTurns = 42;
    private final int maxPlayers = 2;
    private int numPlayers;

    private int pinkScore;
    private int purpScore;
    private int[][] board;
    private boolean purpWon = false;
    private boolean pinkWon = false;
    private boolean serverError;

    public GameServer() {
        System.out.println(" --------- Server started --------- ");
        numPlayers = 0;
        turnsMade = 0;
        board = new int[6][7];

        pinkScore = 0;
        purpScore = 0;

        serverError = false;

        try {
            ss = new ServerSocket(10101);
        } catch (IOException e) {
            System.out.println("IOException in GameServer constructor");
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections...");

            while (numPlayers < maxPlayers && !serverError) {
                Socket s = ss.accept();

                numPlayers++;
                System.out.println("Player " + numPlayers + " has joined!");

                ServerSideConnection ssc = new ServerSideConnection(s, numPlayers);

                if (numPlayers == 1) {
                    player1 = ssc;
                } else {
                    player2 = ssc;
                    player1.dataOut.writeUTF("p");
                }

                Thread t = new Thread(ssc);
                t.start();
            }
            System.out.println("All players have joined.");

        } catch (IOException e) {
            serverError = true;
        }
    }

    public void restart() {
        pinkWon = purpWon = false;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = 0;
            }
        }
    }

    public void purpWon() {
        purpScore++;
        purpWon = true;
    }

    public void pinkWon() {
        pinkScore++;
        pinkWon = true;
    }

    private boolean getWinHor(int col, int row, int player) {
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col + i][row] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col - i][row] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private boolean getWinVert(int col, int row, int player) {
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col][row + i] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col][row - i] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    private boolean getWinDiag(int col, int row, int player) {
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col + i][row + i] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col - i][row + i] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col + i][row - i] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        try {
            int max = 1;
            for (int i = 1; i < 4; i++) {
                if (board[col - i][row - i] == player) {
                    max++;
                    if (max == 4) return true;
                }
            }
        } catch (Exception ignore) {
        }
        return false;
    }

    public boolean getWin(int col, int row, int player) {
        return getWinHor(col, row, player) || getWinVert(col, row, player) || getWinDiag(col, row, player);
    }

    public void setPlay(int k, int i, int player) {
        board[k][i] = player;
        turnsMade++;
    }

    private class ServerSideConnection implements Runnable {

        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private int playerID;

        public ServerSideConnection(Socket s, int id) {
            socket = s;
            playerID = id;

            try {

                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());

            } catch (IOException e) {
                serverError = true;
            }
        }

        public void run() {
            try {

                dataOut.writeInt(playerID);
                dataOut.writeInt(numPlayers);
                dataOut.flush();

                while (turnsMade < maxTurns && !serverError) {
                    String receivedData = dataIn.readUTF();

                    char last = receivedData.charAt(receivedData.length() - 1);
                    if (last == 'q') {
                        player1.sendDataToClient("q");
                        player2.sendDataToClient("q");
                        break;
                    } else {
                        if (last == 'r') {
                            restart();
                            player1.sendDataToClient("r");
                            player2.sendDataToClient("r");
                        } else if (last == 'i') {
                            continue;
                        } else {
                            if (playerID == 1) {
                                String[] charsSplit = receivedData.split("-");
                                if (last == 'm') {
                                    setPlay(Integer.parseInt(charsSplit[0]), Integer.parseInt(charsSplit[1]), playerID);
                                }
                                if (getWin(Integer.parseInt(charsSplit[0]), Integer.parseInt(charsSplit[1]), playerID)) {
                                    purpWon();
                                    receivedData += String.valueOf(purpScore);
                                    receivedData += String.valueOf(playerID);
                                    receivedData += "w";
                                    player1.sendDataToClient(purpScore + "" + playerID + "w");
                                }
                                player2.sendDataToClient(receivedData);

                            } else if (playerID == 2) {

                                String[] charsSplit = receivedData.split("-");
                                if (last == 'm') {
                                    setPlay(Integer.parseInt(charsSplit[0]), Integer.parseInt(charsSplit[1]), playerID);
                                }
                                if (getWin(Integer.parseInt(charsSplit[0]), Integer.parseInt(charsSplit[1]), playerID)) {
                                    pinkWon();
                                    receivedData += String.valueOf(pinkScore);
                                    receivedData += String.valueOf(playerID);
                                    receivedData += "w";
                                    player2.sendDataToClient(pinkScore + "" + playerID + "w");
                                }
                                player1.sendDataToClient(receivedData);
                            }
                        }
                    }
                }

                player1.closeConnection();
                player2.closeConnection();
                ss.close();

            } catch (Exception e) {
                serverError = true;
            }
        }

        public void sendDataToClient(String s) {
            try {
                dataOut.writeUTF(s);
                dataOut.flush();
            } catch (Exception e) {
                serverError = true;
            }
        }

        public void closeConnection() {
            try {
                socket.close();
            } catch (IOException e) {
                serverError = true;
            }
        }
    }

    public static void main(String[] args) {
        GameServer gs = new GameServer();
        gs.acceptConnections();
    }
}