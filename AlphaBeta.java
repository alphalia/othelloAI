import java.util.*;
import java.awt.*;

public class AlphaBeta {
  public BitBoard curr;
  public int myColor;
  public long limit;
  public AlphaBeta(BitBoard curr, int myColor, long limit) {
    this.curr = curr;
    this.myColor = myColor;
    this.limit = limit;
  }
  // public AlphaBeta(long z, int score, int wins, int myColor, BitBoard board) {
  //   this.z = z;
  //   this.score = score;
  //   this.wins = wins;
  //   curr = board;
  //   this.myColor = myColor;
  // }
  // 引数に検索打ち切り時間を指定？
  public long solve(long start) {
    long mask = 0x8000000000000000L, _z, rev, ret = 0;
    int open;
    BitBoard next;
    Node root = new Node(this.myColor, this.curr);
    long mob = curr.mobility();
    // 着手可能な手を探索，開放度が低い順に並べる
    for (int i = 0; i < 64; i++) {
      _z = mask >>> i;
      
      if ((mob & _z) != 0) {
        rev = root.curr.flips(_z);
        next = root.curr.flip(_z, rev);
        open = root.curr.openingScore(_z);
        next.advance();
        root.children.add(new Node(_z, open, this.myColor, next));
      }
    }

    
    if (mob == 0) return -1;

    Collections.sort(root.children, new Comparator<Node>() {
      @Override
      public int compare(Node o1, Node o2) {
        // 降順に並べる
        return o1.score - o2.score;
      }
    });
    
    int best, max;
    long _ret = 0;
    Point p;
    // 反復深化
    DEEPENING: for (int depth = 4; ; depth += 2) {
      max = Integer.MIN_VALUE;
      for (Node child : root.children) {
        best = negaAlphaBeta(child, -1000000, 1000000, depth, 0, start);
        p = BitBoard.toCoord(child.z);
        System.out.println("(" + p.x + ", " + p.y + "): result = " + best + " [depth = " + (depth + 1) + "]");
        if (best != -1) {
          if (best > max) {
            max = best;
            _ret = child.z;
          }
        } else {
          break DEEPENING;
        }
      }
      ret = _ret;
      p = BitBoard.toCoord(_ret);
      System.out.println("found! - " + "(" + p.x + ", " + p.y + ")");
      if (root.curr.blanks < depth) break DEEPENING;
    }
    // System.out.println("mobilities =======================");
    // for (Node child : root.children) {
    //   p = BitBoard.toCoord(child.z);
    //   System.out.println("(" + p.x + ", " + p.y + "): result = " + child.result + "\n");
    // }

    return ret;
  }
  // 盤面をdepth手先まで読み，探索を行う
  // node: 検索するノード
  // alpha: 評価値の下限
  // beta: 評価値の上限
  // depth: 検索する深さ
  // passed: 両者がパスしているかどうか
  public int negaAlphaBeta(Node node, int alpha, int beta, int depth, int passed, long start) {
    // 盤面の手番側から見た評価値を返す(読む側が正，その相手側が負)
    if (node.curr.index == 60 || passed == 3) {
      return (this.curr.color * 2 - 1) * (node.curr.getBlacks() - node.curr.getWhites()) * 1000;
    } else if (depth == 0) {
      return node.score;
    }

    long mask = 0x8000000000000000L, _z;
    long rev;
    int open, diff = 0, cnrdiff, xpdiff, cpdiff, apdiff, bpdiff, eg, mobdiff;
    BitBoard next;
    long mob = node.curr.mobility(), mob2;
    if (mob != 0) {
      passed = 0;
      for (int i = 0; i < 64; i++) {
        if (System.currentTimeMillis() - start > limit) return -1;
        _z = mask >>> i;
        
        if ((mob & _z) != 0) {
          // _zに着手した時にひっくり返せる石
          rev = node.curr.flips(_z);
          // 自分の開放度
          open = node.curr.openingScore(_z);
          // ひっくり返した後の盤面
          next = node.curr.flip(_z, rev);
          // 自分の角と相手の角の数の差
          cnrdiff = next.corner();
          // X
          xpdiff = next.XPoint();
          // C
          cpdiff = next.CPoint();
          // A
          apdiff = next.APoint();
          // B
          bpdiff = next.BPoint();
          // ターンを進める
          next.advance();
          next.judge(_z);
          // 相手の着手可能な手
          mob2 = next.mobility();
          // 相手との石差
          if (node.curr.stage > 1) diff = node.curr.color != 0 ? node.curr.getBlacks() - node.curr.getWhites() : node.curr.getWhites() - node.curr.getBlacks();
          // egde
          eg = (0x3C0081818181003CL & _z) != 0 ? 1 : 0;
          // 着手可能数差
          mobdiff = Long.bitCount(mob) - Long.bitCount(mob2);
          // ノードに追加
          if (node.curr.stage > 1) {
            node.children.add(new Node(_z, mobdiff * 3000 - open * 500 + cnrdiff * 80000 + apdiff * 15000 + bpdiff * 10000 - xpdiff * 30000 - cpdiff * 30000 + (diff + 6) * 1000, this.myColor, next));
          } else {
            node.children.add(new Node(_z, mobdiff * 1000 - open * 1000 + cnrdiff * 80000 + apdiff * 15000 + bpdiff * 10000 - xpdiff * 30000 - cpdiff * 30000 + eg * 3000, this.myColor, next));
          }
          
          // System.out.println(BitBoard.toCoord(_z).toString() + "\n" + next.toString());
        }
      }
    } else {
      if (System.currentTimeMillis() - start > limit) return -1;
      passed |= (node.curr.color + 1);
      // パス
      next = node.curr.clone();
      next.pass();
      // 相手の着手可能な手
      mob2 = next.mobility();
      // 相手との石差
      diff = node.curr.color != 0 ? node.curr.getBlacks() - node.curr.getWhites() : node.curr.getWhites() - node.curr.getBlacks();
      // 着手可能数差
      mobdiff = Long.bitCount(mob2);
      // ノードに追加
      node.children.add(new Node(node.z, -mobdiff * 3000 + diff * 1000, this.myColor, next));
    }
    
    int value;
    Collections.sort(node.children, new Comparator<Node>() {
      @Override
      public int compare(Node o1, Node o2) {
        return o1.score - o2.score;
      }
    });
    // Point _p = BitBoard.toCoord(node.z);
    // System.out.println("(" + _p.x + ", " + _p.y + ") - depth: " + (4 - depth) + "------------------------");
    // Point p;
    for (Node child : node.children) {
      // p = BitBoard.toCoord(child.z);
      // System.out.println("(" + p.x + ", " + p.y + "): score = " + child.score);
      // System.out.println("alpha = " + alpha + ", beta = " + beta);
      // System.out.println(child.curr.toString());
      value = -negaAlphaBeta(child, -beta, -alpha, depth - 1, passed, start);
      // System.out.println(value);
      if (System.currentTimeMillis() - start > limit) return -1;
      if (value > alpha) alpha = value;
      if (alpha >= beta) {
        // System.out.println("(" + p.x + ", " + p.y + ") --> cut!");
        // System.out.println("END depth: " + (4 - depth) + "---------------------------------------");
        node.setResult(alpha);
        return alpha;
      }
    }
    // System.out.println("END depth: " + (4 - depth) + "---------------------------------------");

    node.setResult(alpha);
    return alpha;
  }

  
  public String result(ArrayList<Node> nodes) {
    String ret = "";
    Point p;
    for (Node node: nodes) {
      p = BitBoard.toCoord(node.z);
      ret += "(" + p.x + ", " + p.y + "): score = " + node.result + "\n";
    }

    return ret;
  }
  // ネガスカウト法
  // 序盤で使う
  public int negaScout(Node node, int alpha, int beta, int depth, int passed, long start) {
    // 両者パスor盤面埋めるor最深部なら探索終わり
    if (node.curr.index == 60 || passed == 3) {
      return (this.curr.color * 2 - 1) * (node.curr.getBlacks() - node.curr.getWhites()) * 1000;
    } else if (depth == 0) {
      return node.score;
    }

    long mask = 0x8000000000000000L, _z;
    long rev;
    int open, diff = 0, cnrdiff, xp, cp, xpdiff, cpdiff, eg, mobdiff;
    BitBoard next;
    long mob = node.curr.mobility(), mob2;
    if (mob != 0) {
      passed = 0;
      for (int i = 0; i < 64; i++) {
        if (System.currentTimeMillis() - start > limit) return -1;
        _z = mask >>> i;
        
        if ((mob & _z) != 0) {
          // _zに着手した時にひっくり返せる石
          rev = node.curr.flips(_z);
          // 自分の開放度
          open = node.curr.openingScore(_z);
          // ひっくり返した後の盤面
          next = node.curr.flip(_z, rev);
          // ターンを進める
          next.advance();
          next.judge(_z);
          // 相手の着手可能な手
          mob2 = next.mobility();
          // 相手との石差
          if (node.curr.stage > 1) diff = node.curr.color != 0 ? node.curr.getBlacks() - node.curr.getWhites() : node.curr.getWhites() - node.curr.getBlacks();
          // 自分の角と相手の角の数の差
          cnrdiff = next.corner();
          // X
          xp = (0x0042000000004200L & _z) != 0 ? 1 : -1;
          xpdiff = next.XPoint();
          // C
          cp = (0x4281000000008142L & _z) != 0 ? 1 : -1;
          cpdiff = next.CPoint();
          // egde
          eg = (0x3C0081818181003CL & _z) != 0 ? 1 : 0;
          // 着手可能数差
          mobdiff = Long.bitCount(mob) - Long.bitCount(mob2);
          // ノードに追加
          if (node.curr.stage > 1) {
            node.children.add(new Node(_z, mobdiff * 5000 - open * 200 - cnrdiff * 50000 - xp * 5000 - cp * 5000 + xpdiff * 15000 - cpdiff * 10000 + diff * 1000, this.myColor, next));
          } else {
            node.children.add(new Node(_z, mobdiff * 1000 - open * 1000 - cnrdiff * 50000 - xp * 5000 - cp * 5000 + xpdiff * 15000 - cpdiff * 10000 + eg * 3000, this.myColor, next));
          }
          
          // System.out.println(BitBoard.toCoord(_z).toString() + "\n" + next.toString());
        }
      }
    } else {
      if (System.currentTimeMillis() - start > limit) return -1;
      passed |= (node.curr.color + 1);
      // パス
      next = node.curr.clone();
      next.pass();
      // 相手の着手可能な手
      mob2 = next.mobility();
      // 相手との石差
      diff = node.curr.color != 0 ? node.curr.getBlacks() - node.curr.getWhites() : node.curr.getWhites() - node.curr.getBlacks();
      // 着手可能数差
      mobdiff = Long.bitCount(mob2);
      // ノードに追加
      node.children.add(new Node(node.z, -mobdiff * 3000 + diff * 1000, this.myColor, next));
    }

    // ネガスカウト
    Collections.sort(node.children, new Comparator<Node>() {
      @Override
      public int compare(Node o1, Node o2) {
        return o2.score - o1.score;
      }
    });

    int value = -negaScout(node.children.get(0), -beta, -alpha, depth - 1, passed, start);
    int max = value;
    
    if (beta <= value) {
      node.setResult(value);
      return value;  // カット
    }
    if (alpha < value) alpha = value;
    if (System.currentTimeMillis() - start > limit) return -1;

    for (Node child : node.children) {
      value = -negaScout(child, -alpha - 1, -alpha, depth -1, passed, start);  // Null Window Search
      if (System.currentTimeMillis() - start > limit) return -1;
      if (beta <= value) {
        node.setResult(value);
        return value;   // カット
      }
      if (alpha < value) {
        alpha = value;
        value = -negaScout(child, -beta, -alpha, depth - 1, passed, start);  // 通常のWindowで再探索
        if (System.currentTimeMillis() - start > limit) return -1;
        if (beta <= value) {
          node.setResult(value);
          return value;  // カット
        }
        if (alpha < value) alpha = value;
      }
      if (max < value) max = value;
    }
    node.setResult(max);
    return max; // 子ノードの最大値を返す(fail-soft)
  }
  // // 中盤で使う
  // public int negaScoutForMiddle(Node node, int alpha, int beta, int depth) {
  //   // ゲーム終了or最深部なら探索終わり
  //   if (node.curr.index == 60 || depth == 0) {
  //     return node.score;
  //   }

  //   long mask = 0x8000000000000000L, _z;
  //   long rev;
  //   int open, eval;
  //   BitBoard next;
  //   // 着手可能な手を探索
  //   for (int i = 0; i < 64; i++) {
  //     _z = mask >>> i;
  //     rev = node.curr.flips(_z);
      
  //     if (rev != 0) {
  //       next = node.curr.flip(_z, rev);
  //       open = node.curr.openingScore(_z);
  //       eval = next.evaluate();
  //       next.advance();
  //       node.children.add(new Node(_z, eval - open * 2, -1, this.myColor, next));
  //     }
  //   }
  //   // ネガスカウト
  //   Node child = node.children.poll();
  //   int value = -negaScoutForMiddle(child, -beta, -alpha, depth - 1);
  //   int max = value;
    
  //   if (beta <= value) return value;  // カット
  //   if (alpha < value) alpha = value;

  //   while (!node.children.isEmpty()) {
  //     Node ch = node.children.poll();
  //     value = -negaScoutForMiddle(ch, -alpha - 1, -alpha, depth -1);  // Null Window Search
  //     if (beta <= value) return value;  // カット
  //     if (alpha < value) {
  //       alpha = value;
  //       value = -negaScoutForMiddle(ch, -beta, -alpha, depth - 1);  // 通常のWindowで再探索
  //       if (beta <= value) return value;  // カット
  //       if (alpha < value) alpha = value;
  //     }
  //     if (max < value) max = value;
  //   }
  //   return max; // 子ノードの最大値を返す(fail-soft)
  // }
}