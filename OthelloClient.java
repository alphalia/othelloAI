import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class OthelloClient extends JFrame{
  final static int BLACK = 1;
  final static int WHITE = -1;

  private Socket socket;
  private BufferedReader br;
  private PrintWriter pw;
  private byte color;
  private byte[][] board = new byte[8][8];
  private JTextField tf;
  private JTextArea ta;
  private JLabel label;
  private OthelloCanvas canvas;
  private String username = "";

  public OthelloClient(String host, int port, String username) {
    this.username = username;
    //setTitle(username);
    try{
      socket = new Socket(host,port);
      br = new BufferedReader(new InputStreamReader
          (socket.getInputStream()));
      pw = new PrintWriter(socket.getOutputStream());
    }catch(Exception e){
      e.printStackTrace();
      System.exit(1);
    }
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e)  {
        System.exit(0);
      }
    });
    tf = new JTextField(40);
    ta = new JTextArea(18,40);
    ta.setLineWrap(true);
    ta.setEditable(false);
    label = new JLabel();

    this.setSize(640,320);

    tf.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        if(tf.getText().equals("quit")){
          System.exit(0);
        }
        //sayMessage(tf.getText());
        pw.println("SAY "+tf.getText());
        pw.flush();
        tf.setText("");
      }
    }
    );

    JPanel mainp = (JPanel)getContentPane();
    JPanel ep = new JPanel();
    JPanel wp = new JPanel();
    canvas = new OthelloCanvas(this);

    GridLayout gl = new GridLayout(1,2);
    gl.setHgap(5);
    mainp.setLayout(gl);
    ep.setLayout(new BorderLayout());
    ep.add(new JScrollPane(ta),BorderLayout.CENTER);
    ep.add(tf,BorderLayout.SOUTH);
    wp.setLayout(new BorderLayout());
    wp.add(label,BorderLayout.SOUTH);
    wp.add(canvas,BorderLayout.CENTER);
    mainp.add(wp);
    mainp.add(ep);

    setVisible(true);

    mainLoop();
  }

  void putPiece(int x, int y){
    pw.println("PUT "+x+" "+y);
    pw.flush();
  }

  byte[][] getBoard(){
    return board;
  }

  private void mainLoop(){
    try{
      pw.println("NICK "+username);
      pw.flush();
      StringTokenizer stn = new StringTokenizer(br.readLine()," ",false);
      stn.nextToken(); //START message
      color = Byte.parseByte(stn.nextToken());
      if(color==BLACK){
        setTitle(username+"(BLACK)");
      }else{
        setTitle(username+"(WHITE)");
      }
      while(true){
        String message = br.readLine();
        stn = new StringTokenizer(message," ",false);
        String com = stn.nextToken();
        if(com.equals("SAY")){
          setMessage(message.substring(4));
          continue;
        }

        if(com.equals("BOARD")){
          for(int i=0;i<8;i++){
            for(int j=0;j<8;j++){
              board[i][j] = Byte.parseByte(stn.nextToken());
            }
          }
          canvas.repaint();
          continue;
        }if(com.equals("END")){
          //System.out.println(message);
          label.setText(message);
          //setMessage("==System==:"+message);

          continue;
        }if(com.equals("CLOSE")){
          label.setText(message);
          return;

        }if(com.equals("TURN")){
          byte c = Byte.parseByte(stn.nextToken());
          if(c==color){
            label.setText("Your Turn");
          }else{
            label.setText("Enemy Turn");
          }
          continue;
        }

        System.out.println(message);
      }
    }catch(IOException e){
      System.exit(0);
    }
  }

  private void setMessage(String str){
    ta.append(str+"\n");
    int len = ta.getText().length();
    ta.setCaretPosition(len); 
  }

  public static void main(String args[]) {
    new OthelloClient(args[0],Integer.parseInt(args[1]),args[2]);
  }
}


class OthelloCanvas extends JPanel {
  private final static int startx = 20;
  private final static int starty = 10;
  private final static int gap = 30;
  private OthelloClient oc;
  private byte[][] board;

  OthelloCanvas(OthelloClient oc){
    this.oc = oc;
    this.board=oc.getBoard();
    this.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        putPiece((p.x-startx)/gap,(p.y-starty)/gap);
      }
    });
  }

  private void putPiece(int x, int y){
    oc.putPiece(x,y);
  }


  public void paintComponent(Graphics g){
    g.setColor(new Color(0,180,0));
    g.fillRect(startx,starty,gap*8,gap*8);

    g.setColor(Color.BLACK);
    for(int i=0;i<9;i++){
      g.drawLine(startx,starty+i*gap,startx+8*gap,starty+i*gap);
      g.drawLine(startx+i*gap,starty,startx+i*gap,starty+8*gap);
    }
    for(int i=0;i<8;i++){
      for(int j=0;j<8;j++){
        if(board[i][j]==OthelloClient.BLACK){
          g.setColor(Color.BLACK);
          g.fillOval(startx+gap*i,starty+gap*j,gap,gap);
        }else if(board[i][j]==OthelloClient.WHITE){
          g.setColor(Color.WHITE);
          g.fillOval(startx+gap*i,starty+gap*j,gap,gap);
        }
      }
    }
  }
}
