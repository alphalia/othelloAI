import java.util.*;
import java.awt.*;

public class MonteCarlo {
  public long limit;  // 検索打ち切り時間
  public int myColor;
  public BitBoard curr;
  public MonteCarlo(BitBoard curr, int myColor, long limit) {
    this.myColor = myColor;
    this.curr = curr;
    this.limit = limit;
  }
  // solve
  public long solve(long start) {
    Node root = new Node(this.myColor, this.curr);
    int res = play(root, this.curr, 0, start, 0);
    System.out.print("RESULT ===================================\nexpect = " + res + "\n" + MonteCarlo.result(root));
    for (Node child : root.children) {
      if (child.score == res) return child.z;
    }

    return root.children.get(new Random().nextInt(root.children.size())).z;
  }
  // シミュレート値を返す
  public int play(Node node, BitBoard curr, int depth, long start, int passed) {
    if (depth > 15) return node.score;
    long mask = 0x8000000000000000L, _z;
    long rev;
    BitBoard next;
    long mob = node.curr.mobility();
    if (mob != 0) {
      passed = 0;
      for (int i = 0; i < 64; i++) {
        if (System.currentTimeMillis() - start > limit) return node.score;
        _z = mask >>> i;
        
        if ((mob & _z) != 0) {
          // _zに着手した時にひっくり返せる石
          rev = node.curr.flips(_z);
          // ひっくり返した後の盤面
          next = node.curr.flip(_z, rev);
          // ターンを進める
          next.advance();
          next.judge(_z);
          // ノードに追加
          node.children.add(new Node(_z, 0, this.myColor, next));
        }
      }
    } else {
      if (System.currentTimeMillis() - start > limit) return node.score;
      passed |= (node.curr.color + 1);
      // パス
      next = node.curr.clone();
      next.pass();
      // ノードに追加
      if (passed == 3) return 0;
      else return play(node, next, depth + 1, start, passed);
    }

    int res;
    // シミュレート
    // 1000回くらい
    for (int i = 0; i < 10000; i++) {
      for (Node child : node.children) {
        if (System.currentTimeMillis() - start > limit) return node.score;
        res = playout(child.curr, start);
        // System.out.println(res);
        child.score += res > 0 ? 1 : 0;
      }
    }
    Point p = BitBoard.toCoord(node.z);
    System.out.print("(" + p.x + ", " + p.y + ") -> [depth = " + depth + "]\n" + MonteCarlo.result(node));

    Collections.sort(node.children, new Comparator<Node>() {
      @Override
      public int compare(Node o1, Node o2) {
        return o2.score - o1.score;
      }
    });
    
    // more than or eq 70%
    for (Node child : node.children) {
      if (child.score > 8000) continue;
      child.score = play(child, child.curr, depth + 1, start, passed);
    }

    return Collections.max(node.children, new Comparator<Node>() {
      @Override
      public int compare(Node o1, Node o2) {
        return o1.score - o2.score;
      }
    }).score;
  }
  // ゲーム終了までシミュレートし，勝敗判定を行う
  public int playout(BitBoard board, long start) {
    long mask = 0x8000000000000000L, _z = 0;
    int passed = 0;
    long rev = 0;
    long mob;
    long maxz, tmp;
    int size, cnt, r;
    Random rnd = new Random();

    while (board.index <= 60) {
      // System.out.println("index" + board.index);
      if (System.currentTimeMillis() - start > limit) return 0;
      cnt = 0;
      maxz = 0;
      tmp = 0;
      mob = board.mobility();
      long playerBB = board.color != 0 ? board.blackBB : board.whiteBB;
      if (mob != 0) {
        size = Long.bitCount(mob);
        // System.out.println(size);
        r = rnd.nextInt(size);
        for (int i = 0; i < 64; i++) {
          _z = mask >>> i;
          if ((mob & _z) != 0) {
            // corner
            if ((0x8100000000000081L & _z) != 0) {
              maxz = _z;
              break;
            }
            // 確定石を広げる
            if ((playerBB & 0x8100000000000081L) != 0 && board.fillAroundCorner(mob) != -1) {
              maxz = _z;
              break;
            }
            if (cnt == r) tmp = _z;
            cnt++;
          }
        }
        if (maxz == 0) {
          maxz = tmp;
        }
        rev = board.flips(maxz);
        board = board.flip(maxz, rev);
        board.advance();
        passed = 0;
      } else {
        board.pass();
        passed |= (board.color + 1);
        if (passed == 3) break;
        // System.out.println(board.toString());
        // System.out.println(board.color == 1 ? "black passed!" : "white passed!");
      }
    }
    // System.out.println("game is over: black " + board.getBlacks() + ", white " + board.getWhites());
    // System.out.println(board.toString());

    return myColor != 0 ? board.getBlacks() - board.getWhites() : board.getWhites() - board.getBlacks();
  }
  public static String result(Node node) {
    String ret = "";
    Point p;
    for (Node child: node.children) {
      p = BitBoard.toCoord(child.z);
      ret += "(" + p.x + ", " + p.y + "): score = " + child.score + "/10000\n";
    }

    return ret;
  }

}