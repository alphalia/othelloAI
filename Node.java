import java.util.*;
import java.awt.*;

public class Node {
  public long z;  // 石を置いた座標
  public BitBoard curr; // 石を置いた後の盤面
  public int score; // currに対する盤面の評価値
  public int myColor; // 自身の色
  public int result = -1; // 子ノードからの結果
  public ArrayList<Node> children = new ArrayList<Node>();
  public Node(int myColor, BitBoard curr) {
    this.z = -1;
    this.score = -1;
    this.curr = curr;
    this.myColor = myColor;
  }
  public Node(long z, int score, int myColor, BitBoard curr) {
    this.z = z;
    this.score = score;
    this.curr = curr;
    this.myColor = myColor;
  }
  public void setResult(int val) {
    this.result = val;
  }
  public int getResult() {
    return this.result;
  }
}