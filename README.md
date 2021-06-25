# othelloAI
ネットワーク対戦型オセロAIプログラム（Java）

# コンパイル方法
$ javac OthelloAI.java Solver.java BitBoard.java

# 起動方法
1. 自分の環境で対戦させる場合
  
  ターミナルが3つ必要．ターミナルの区別に $ ，% ，> を用いる．
  
  - サーバの起動
  
    $ java -jar OthelloServer.jar -port _port_

    -monitor を指定すると，GUIでモニタウインドウが立ち上がる．

    -timeout _timeout_ を指定するとタイムアウトを設定できる．

  - クライアントプログラムの接続
  
    % java OthelloAI localhost _port_ _nickname_

    \> java OthelloAI localhost _port_ _nickname_

2. 他人の環境に接続して対戦させる場合

  $ java OthelloAI _hostname_ _port_ _nickname_
