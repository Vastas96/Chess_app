package board;

/**
 * Created by Vytenis on 2017.10.10.
 */

public class Undo {
    public int move;
    public int castlePerm;
    public int enPas;
    public int fiftyMove;

    public Undo(int move, int castlePerm, int enPas, int fiftyMove){
        this.move = move;
        this.castlePerm = castlePerm;
        this.enPas = enPas;
        this.fiftyMove = fiftyMove;
    }
}
