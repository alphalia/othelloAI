import java.awt.*;
import java.util.*;

public class BitBoard {
  public final static int BLACK = 1;
  public final static int WHITE = 0;
  public final static int[][] GRID_SCORE = {
    {  30, -12,  0,  -1,  -1,   0, -12,  30},
    { -12, -15, -3,  -3,  -3,  -3, -15, -12},
    {   0,  -3,  0,  -1,  -1,   0,  -3,   0},
    {  -1,  -3, -1,  -1,  -1,  -1,  -3,  -1},
    {  -1,  -3, -1,  -1,  -1,  -1,  -3,  -1},
    {   0,  -3,  0,  -1,  -1,   0,  -3,   0},
    { -12, -15, -3,  -3,  -3,  -3, -15, -12},
    {  30, -12,  0,  -1,  -1,   0, -12,  30},
  };
  public int color;   // 黒番1or白番0
  public int index;   // 現在何手目か
  public int pass;    // パスが起こった回数
  // 盤面
  public long blackBB;
  public long whiteBB;
  // 盤面を初期化
  public BitBoard() {
    this.blackBB = 0x0000001008000000L;
    this.whiteBB = 0x0000000810000000L;
    this.color = 0; this.index = 0; this.pass = 0;
  }
  // シャローコピー
  public BitBoard(BitBoard board) {
    this.blackBB = board.blackBB;
    this.whiteBB = board.whiteBB;
    this.color = board.color;
    this.index = board.index;
    this.pass = board.pass;
  }
  
  // boardによって盤面を更新
  public void update(long blackBB, long whiteBB) {
    this.blackBB = blackBB;
    this.whiteBB = whiteBB;
    this.index = 61 - getBlanks();
    this.color = (this.index + this.pass) % 2;
  }

  public void advance() {
    this.index++;
    this.color ^= 1;
  }
  public void pass() {
    this.pass++;
    this.color ^= 1;
  }
  public int getBlacks() {
    return Long.bitCount(this.blackBB);
  }
  public int getWhites() {
    return Long.bitCount(this.whiteBB);
  }
  public int getBlanks() {
    return Long.bitCount(~(this.blackBB | this.whiteBB));
  }
  
  // 石反転パターンを用いて石をひっくり返す．
  public BitBoard flip(long z, long rev) {
    BitBoard board = this.clone();
    if (board.color != WHITE) {
      board.blackBB ^= z | rev;
      board.whiteBB ^= rev;
    } else {
      board.whiteBB ^= z | rev;
      board.blackBB ^= rev;
    }

    return board;
  }
  // 座標zに石を置けたら石反転パターンを返す．置けないなら，パターンなし(0)を返す．
  public long flips(long z) {
    //着手箇所が空きマスでない場合
    if (((this.blackBB | this.whiteBB) & z) != 0) {
      return 0;
    }

    long rev; // 石反転パターン
    long ret = 0;
    long mask;    // 相手石の存在確認マスク
    int count;
    long playerBB = this.color != WHITE ? this.blackBB : this.whiteBB;
    long opponentBB = this.color != WHITE ? this.whiteBB : this.blackBB;
    for (int i = 0; i < 8; i++) {
      count = 0;
      rev = 0;
      mask = transfer(z, i);
      // 相手石が連続する間，続ける
      while (mask != 0 && (mask & opponentBB) != 0) {
        rev |= mask;
        mask = transfer(mask, i);
        count++;
      }
      if (count != 0 && (mask & playerBB) != 0) { // 挟めたら，ひっくり返す
        ret |= rev;
      }
    }
    
    return ret;
  }
  // dir方向に遷移させる
  public static long transfer(long z, int dir) {
    switch (dir) {
      case 0: // 上
        return (z << 8) & 0xFFFFFFFFFFFFFFFFL;
      case 1: // 右上
        return (z << 7) & 0x7F7F7F7F7F7F7F7FL;
      case 2: // 右
        return (z >>> 1) & 0x7F7F7F7F7F7F7F7FL;
      case 3: // 右下
        return (z >>> 9) & 0x7F7F7F7F7F7F7F7FL;
      case 4: // 下
        return (z >>> 8) & 0xFFFFFFFFFFFFFFFFL;
      case 5: // 左下
        return (z >>> 7) & 0xFEFEFEFEFEFEFEFEL;
      case 6: // 左
        return (z << 1) & 0xFEFEFEFEFEFEFEFEL;
      case 7: // 左上
        return (z << 9) & 0xFEFEFEFEFEFEFEFEL;
      default:
        return 0;
    }
  }
  // maskを指定して，dir方向にシフトさせる
  public static long transfer(long z, long mask, int dir) {
    switch (dir) {
      case 0: // 上
        return (z << 8) & mask;
      case 1: // 右上
        return (z << 7) & mask;
      case 2: // 右
        return (z >>> 1) & mask;
      case 3: // 右下
        return (z >>> 9) & mask;
      case 4: // 下
        return (z >>> 8) & mask;
      case 5: // 左下
        return (z >>> 7) & mask;
      case 6: // 左
        return (z << 1) & mask;
      case 7: // 左上
        return (z << 9) & mask;
      default:
        return 0;
    }
  }
  // 合法手ボード
  public long mobility() {
    long playerBB = this.color != WHITE ? this.blackBB : this.whiteBB;
    long opponentBB = this.color != WHITE ? this.whiteBB : this.blackBB;
    long blankBB = ~(this.blackBB | this.whiteBB);
    long hwb = opponentBB & 0x7e7e7e7e7e7e7e7eL;
    long vwb = opponentBB & 0x00FFFFFFFFFFFF00L;
    long awb = opponentBB & 0x007e7e7e7e7e7e00L;
    long[] watchBB = {vwb, awb, hwb, awb, vwb, awb, hwb, awb};
    long tmp;
    long ret = 0;
    // 8方向チェック
    for (int i = 0; i < 8; i++) {
      tmp = transfer(playerBB, watchBB[i], i);
      tmp |= transfer(tmp, watchBB[i], i);
      tmp |= transfer(tmp, watchBB[i], i);
      tmp |= transfer(tmp, watchBB[i], i);
      tmp |= transfer(tmp, watchBB[i], i);
      tmp |= transfer(tmp, watchBB[i], i);
      ret |= transfer(tmp, blankBB, i);
    }
    
    return ret;
  }
  
  // 盤面の評価値を計算する(序盤から中盤にかけて有効)
  // 手番側から見て，差が正であれば優勢，負であれば劣勢，0なら引き分け
  public int evaluate() {
    long mask = 0x8000000000000000L;
    int c = 2 * this.color - 1; // 黒1, 白-1
    int s = 0;
    for (int i = 0; i < 8; i++) {
      for (int j = 0; j < 8; j++) {
        if ((mask & this.blackBB) != 0) {
          s += BitBoard.GRID_SCORE[i][j];
        } else if ((mask & this.whiteBB) != 0) {
          s -= BitBoard.GRID_SCORE[i][j];
        }
        mask = mask >>> 1;
      }
    }

    return s * c;
  }
  // zにおける評価値を返す
  public static int evalOf(long z) {
    int n = Long.numberOfLeadingZeros(z);

    return BitBoard.GRID_SCORE[n / 8][n % 8];
  }
  // 座標をビットに変換
  public static long toBit(Point p) {
    long z = 0x8000000000000000L;
    // 座標の位置までシフト
    return z >>> (p.x + p.y * 8);
  }
  // ビットを座標に変換
  public static Point toCoord(long z) {
    int n = Long.numberOfLeadingZeros(z);
    
    return new Point(n % 8, n / 8);
  }
  // 座標の文字列表現を返す
  public static String view(long z) {
    long mask = 0x8000000000000000L;
    String ret = "";
    for (int i = 0; i < 64; i++) {
      if ((mask & z) != 0) {
        ret += "* ";
      } else {
        ret += "- ";
      }
      if ((i + 1) % 8 == 0) {
        ret += "\n";
      }
      mask = mask >>> 1;
    }
    return ret;
  }
  
  // 文字列表現を出力(行列形式で出力，-: blank，o: black，x: white)
  @Override
  public String toString() {
    long mask = 0x8000000000000000L;
    String ret = "";
    for (int i = 0; i < 64; i++) {
      if ((mask & blackBB) != 0) {
        ret += "x ";
      } else if ((mask & whiteBB) != 0) {
        ret += "o ";
      } else {
        ret += "- ";
      }
      if ((i + 1) % 8 == 0) {
        ret += "\n";
      }
      mask = mask >>> 1;
    }
    return ret;
  }
  @Override
  public BitBoard clone() {
    return new BitBoard(this);
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    
    if (obj instanceof BitBoard) {
      BitBoard board = (BitBoard)obj;
      // 盤面が同じなら等価
      if (this.blackBB == board.blackBB && this.whiteBB == board.whiteBB) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }
  @Override
  public int hashCode() {
    return Long.valueOf(blackBB).hashCode() + Long.valueOf(whiteBB).hashCode();
  }
}