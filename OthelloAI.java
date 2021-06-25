import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;

public class OthelloAI {
  private final static int BLACK = 1;
  private final static int WHITE = -1;

  private Socket socket;
  private BufferedReader br;
  private PrintWriter pw;
  private String username;
  private BitBoard board;
  private int myColor;
  private int turn;
  private Solver solver;
  private long start, end;
  public OthelloAI(String host, int port, String username) {
    this.username = username;
    try {
      socket = new Socket(host,port);
      br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      pw = new PrintWriter(socket.getOutputStream());
    } catch(Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    mainLoop();
  }
  private void put() {
    start = System.currentTimeMillis();
    solver.update(board);
    long z = solver.solve(start);
    end = System.currentTimeMillis();
    if (z == -1) {
      System.out.println("timeout or error");
    } else {
      Point p = BitBoard.toCoord(z);
      pw.println("PUT " + p.y + " " + p.x);
      pw.flush();
      System.out.println("put: (" + p.x + ", " + p.y + ") [" + ((end - start) / 1000.0) + "s]");
    }
  }

  private void mainLoop() {
    try {
      pw.println("NICK " + username);
      pw.flush();
      StringTokenizer stn = new StringTokenizer(br.readLine() ," " ,false);
      stn.nextToken(); //START message
      myColor = (Integer.parseInt(stn.nextToken()) + 1) / 2;
      turn = WHITE;
      System.out.println("Player: " + (myColor != 0 ? "black" : "white"));
      System.out.println("BLACK: x, WHITE: o, BLANK: -");
      board = new BitBoard();
      solver = new Solver(myColor, 9000);
      while (true) {
        String message = br.readLine();
        stn = new StringTokenizer(message, " " ,false);
        String com = stn.nextToken();
        
        if (com.equals("SAY")) {
          String user = stn.nextToken();
          String mes = stn.nextToken();
          System.out.println(user + " " + mes);
          // System.out.println(message);
          continue;
        }
        if (com.equals("BOARD")) {
          // System.out.println(message);
          long blackBB = 0, whiteBB = 0;
          long mask = 0x8000000000000000L;
          int n;
          long change = board.blackBB | board.whiteBB;
          Point p;
          while (mask != 0) {
            n = Integer.parseInt(stn.nextToken());
            if (n == BLACK) {
              blackBB = blackBB | mask;
            } else if (n == WHITE) {
              whiteBB = whiteBB | mask;
            }
            mask = mask >>> 1;
          }
          board.update(blackBB, whiteBB);
          change ^= board.blackBB | board.whiteBB;
          if (change != 0) {
            p = BitBoard.toCoord(change);
            System.out.println(((turn + 1) / 2 == myColor ? "Player" + (myColor != 0 ? "[x]" : "[o]") 
                                                          : "Opponent" + (myColor != 0 ? "[o]" : "[x]")
                                                          ) + " puts (" + p.x + ", " + p.y + ")");
          }
          System.out.println(board.toString());
          continue;
        }
        if (com.equals("END")) {
          System.out.println(message);
          break;
        }
        if (com.equals("CLOSE")) {
          System.out.println(message);
          break;
        }
        if (com.equals("TURN")) {
          // System.out.println(message);
          int _turn = Integer.parseInt(stn.nextToken());
          int c = (_turn + 1) / 2;
          turn *= -1;
          // パスの検知
          if (turn != _turn) {
            System.out.println((turn + 1) / 2 == myColor ? ("Player" + (myColor != 0 ? "[x]" : "[o]") + " passed") : ("Opponent" + (myColor != 0 ? "[o]" : "[x]") + " passed"));
            board.pass();
            turn *= -1;
          }
          
          if (c == myColor) {
            System.out.println("Player" + (myColor != 0 ? "[x]" : "[o]") + " Turn: " + board.index);
            put();
          } else {
            System.out.println("Opponent" + (myColor != 0 ? "[o]" : "[x]") + " Turn: " + board.index);
          }
          
          // board.index++;
          continue;
        }
        if (com.equals("ERROR")) {
          System.out.println(message);
          int err = Integer.parseInt(stn.nextToken());
          switch (err) {
            case 1:
              System.out.println("書式が間違っています");
              break;
            case 2:
              System.out.println("PUT命令で指定されたマス目に石を置けません");
              break;
            case 3:
              System.out.println("相手のターンにPUT命令が送られました");
              break;
            case 4:
              System.out.println("命令を処理できません");
              break;
            default:
              break;
          }
        }
      }
    } catch(IOException e) {
      System.exit(0);
    }
  }

  public static void main(String args[]) {
    new OthelloAI(args[0],Integer.parseInt(args[1]),args[2]);
  }

}
