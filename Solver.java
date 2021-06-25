import java.util.*;

public class Solver {
  int myColor;
  long limit;
  public BitBoard curr;
  public Solver(int myColor, long limit) {
    this.myColor = myColor;
    this.limit = limit;
    this.curr = new BitBoard();
  }
  public void update(BitBoard curr) {
    this.curr = curr;
  }
  public long solve(long start) {
    long mob = this.curr.mobility();
    long answer = -1, mask = 0x8000000000000000L;
    int eval, max = Integer.MIN_VALUE;
    System.out.println("CURRENT SCORE: " + curr.evaluate());

    for (int i = 0; i < 64; i++) {
      if (System.currentTimeMillis() - start > limit) return -1;
      long z = mask >>> i;
      if ((mob & z) != 0) {
        eval = BitBoard.evalOf(z);
        if (eval > max) {
          max = eval;
          answer = z;
        }
      }
    }

    return answer;
  }
}