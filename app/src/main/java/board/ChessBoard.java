package board;

import android.content.Intent;

import com.example.vytenis.chess_app.GameActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Vytenis on 2017.10.10.
 */

public class ChessBoard {
    public static final int EMPTY = 0;
    public static final int WP = 1;
    public static final int WN = 2;
    public static final int WB = 3;
    public static final int WR = 4;
    public static final int WQ = 5;
    public static final int WK = 6;
    public static final int BP = 7;
    public static final int BN = 8;
    public static final int BB = 9;
    public static final int BR = 10;
    public static final int BQ = 11;
    public static final int BK = 12;

    public static final int A1 = 0, B1 = 1, C1 = 2, D1 = 3, E1 = 4, F1 = 5, G1 = 6, H1 = 7;
    public static final int A2 = 8, B2 = 9, C2 = 10, D2 = 11, E2 = 12, F2 = 13, G2 = 14, H2 = 15;
    public static final int A7 = 48, B7 = 49, C7 = 50, D7 = 51, E7 = 52, F7 = 53, G7 = 54, H7 = 55;
    public static final int A8 = 56, B8 = 57, C8 = 58, D8 = 59, E8 = 60, F8 = 61, G8 = 62, H8 = 63;
    public static final int NO_SQ = 64;

    public static final int RANK_1 = 0;
    public static final int RANK_2 = 1;
    public static final int RANK_3 = 2;
    public static final int RANK_4 = 3;
    public static final int RANK_5 = 4;
    public static final int RANK_6 = 5;
    public static final int RANK_7 = 6;
    public static final int RANK_8 = 7;

    public static final int FILE_A = 0;
    public static final int FILE_B = 1;
    public static final int FILE_C = 2;
    public static final int FILE_D = 3;
    public static final int FILE_E = 4;
    public static final int FILE_F = 5;
    public static final int FILE_G = 6;
    public static final int FILE_H = 7;

    public static final int WKCA = 1;
    public static final int WQCA = 2;
    public static final int BKCA = 4;
    public static final int BQCA = 1;

    public static final int WHITE = 0;
    public static final int BLACK = 1;

    /* FLAGS */

    public static final int MFLAGEP = 0x40000;
    public static final int MFLAGPS = 0x80000;
    public static final int MFLAGCA = 0x1000000;

    public static final int MFLAGCAP = 0x7C000;
    public static final int MFLAGPROM = 0xF00000;

    private int square[] = new int[64];
    private MoveList mlist = new MoveList();
    private ArrayList<Undo> history = new ArrayList<Undo>();

    private int kingSq[] = new int[2];
    private int pceNum[] = new int[13];
    private int pList[][] = new int[13][10];
    private int side;
    private boolean isGame;
    private int enPas;
    private int fiftyMove;
    private int ply;
    private int hisPly;
    private int castlePerm;

    private GameActivity activity;

    public ChessBoard(GameActivity activity){
        this.activity = activity;
    }

    void setGame(boolean state){
        this.isGame = state;
    }

    void setSide(int side){
        this.side = side;
    }

    public int pceColor(int pce){
        if(pce > 6){
            return 1;
        }
        return 0;
    }

    private String sq2String(int sq){
        int file = SQ2F(sq);
        int rank = SQ2R(sq);

        char f = (char) ('a' + file);
        char r = (char) ('1' + rank);

        return new StringBuilder().append(f).append(r).toString();
    }

    private String pce2String(int pce){
        switch (pce){
            case 1:
                return "wp";
            case 2:
                return "wn";
            case 3:
                return "wb";
            case 4:
                return "wr";
            case 5:
                return "wq";
            case 6:
                return "wk";
            case 7:
                return "bp";
            case 8:
                return "bn";
            case 9:
                return "bb";
            case 10:
                return "br";
            case 11:
                return "bq";
            case 12:
                return "bk";
            default:
                return null;
        }
    }

    final int KnDir[][] = { {-1,-2}, {-2,-1}, {-2,1}, {-1,2}, {1,2}, {2,1}, {2,-1}, {1,-2}  };
    final int RkDir[][] = { {-1,0}, {0,1}, {1,0}, {0,-1} };
    final int BiDir[][] = { {-1,1}, {1,1}, {1,-1}, {-1,-1} };
    final int KiDir[][] = { {-1,0}, {0,1}, {1,0}, {0,-1}, {-1,1}, {1,1}, {1,-1}, {-1,-1} };

    final boolean IsKn[] = { false, false, true, false, false, false, false, false, true, false, false, false, false };
    final boolean IsKi[] = { false, false, false, false, false, false, true, false, false, false, false, false, true };
    final boolean IsRQ[] = { false, false, false, false, true, true, false, false, false, false, true, true, false };
    final boolean IsBQ[] = { false, false, false, true, false, true, false, false, false, true, false, true, false };

    boolean sqAttacked(final int sq, final int side){
        int pce;
        int dir[] = new int[2];
        int tempsq[] = new int[2];
        int color;
        // pawns
        if(side == WHITE) {
            if(SQ2F(sq)+1 >= FILE_A && SQ2F(sq)+1 <= FILE_H && SQ2R(sq)-1 >= RANK_1 && SQ2R(sq)-1 <=RANK_8){
                if(square[FR2SQ(SQ2F(sq)+1,SQ2R(sq)-1)] == WP){
                    return true;
                }
            }
            if(SQ2F(sq)-1 >= FILE_A && SQ2F(sq)-1 <= FILE_H && SQ2R(sq)-1 >= RANK_1 && SQ2R(sq)-1 <=RANK_8){
                if(square[FR2SQ(SQ2F(sq)-1,SQ2R(sq)-1)] == WP){
                    return true;
                }
            }
        }
        else{
            if(SQ2F(sq)+1 >= FILE_A && SQ2F(sq)+1 <= FILE_H && SQ2R(sq)+1 >= RANK_1 && SQ2R(sq)+1 <=RANK_8){
                if(square[FR2SQ(SQ2F(sq)+1,SQ2R(sq)+1)] == BP){
                    return true;
                }
            }
            if(SQ2F(sq)-1 >= FILE_A && SQ2F(sq)-1 <= FILE_H && SQ2R(sq)+1 >= RANK_1 && SQ2R(sq)+1 <=RANK_8){
                if(square[FR2SQ(SQ2F(sq)-1,SQ2R(sq)+1)] == BP){
                    return true;
                }
            }
        }
        // knights
        for(int i = 0 ; i < 8 ; ++i){
            if(SQ2F(sq) + KnDir[i][0] >= FILE_A && SQ2F(sq) + KnDir[i][0] <= FILE_H && SQ2R(sq) + KnDir[i][1] >= RANK_1 && SQ2R(sq) + KnDir[i][1] <= RANK_8){
                pce = square[FR2SQ(SQ2F(sq) + KnDir[i][0],SQ2R(sq) + KnDir[i][1])];
                color = pceColor(pce);
                if(IsKn[pce] && color == side){
                    return true;
                }
            }
        }
        // rooks, queens
        for(int i = 0 ; i < 4; ++i){
            dir[0] = RkDir[i][0];
            dir[1] = RkDir[i][1];
            tempsq[0] = SQ2F(sq) + dir[0];
            tempsq[1] = SQ2R(sq) + dir[1];
            while(tempsq[0] >= FILE_A && tempsq[0] <= FILE_H && tempsq[1] >= RANK_1 && tempsq[1] <= RANK_8){
                if(square[FR2SQ(tempsq[0],tempsq[1])] != EMPTY){
                    pce = square[FR2SQ(tempsq[0],tempsq[1])];
                    color = pceColor(pce);
                    if(IsRQ[pce] && color == side){
                        return true;
                    }
                    break;
                }
                tempsq[0] += dir[0];
                tempsq[1] += dir[1];
            }
        }
        // bishops, queens
        for(int i = 0; i < 4; ++i){
            dir[0] = BiDir[i][0];
            dir[1] = BiDir[i][1];
            tempsq[0] = SQ2F(sq) + dir[0];
            tempsq[1] = SQ2R(sq) + dir[1];
            while(tempsq[0] >= FILE_A && tempsq[0] <= FILE_H && tempsq[1] >= RANK_1 && tempsq[1] <= RANK_8){
                if(square[FR2SQ(tempsq[0],tempsq[1])] != EMPTY) {
                    pce = square[FR2SQ(tempsq[0], tempsq[1])];
                    color = pceColor(pce);
                    if (IsBQ[pce] && color == side) {
                        return true;
                    }
                    break;
                }
                tempsq[0] += dir[0];
                tempsq[1] += dir[1];
            }
        }
        // kings
        for(int i = 0 ; i < 8 ; ++i){
            if(SQ2F(sq) + KiDir[i][0] >= FILE_A && SQ2F(sq) + KiDir[i][0] <= FILE_H && SQ2R(sq) + KiDir[i][1] >= RANK_1 && SQ2R(sq) + KiDir[i][1] <= RANK_8){
                if(square[FR2SQ(SQ2F(sq) + KiDir[i][0],SQ2R(sq) + KiDir[i][1])] != EMPTY){
                    pce = square[FR2SQ(SQ2F(sq) + KiDir[i][0],SQ2R(sq) + KiDir[i][1])];
                    color = pceColor(pce);
                    if(IsKi[pce] && color == side){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public boolean isCheck(){
        return sqAttacked(kingSq[side], (side^1));
    }
    public int getKingSq(){return kingSq[side];}
    boolean isCheckMate(){
        return false;
    }
    boolean isStaleMate(){
        return false;
    }
    boolean isSqOnBoard(final int sq){
        if(sq >= 0 || sq < NO_SQ){
            return true;
        }
        return false;
    }

    void updateListsMaterial(){
        int piece;
        int kingCount = 0;
        for(int i = 0 ; i < 64 ; i++){
            if(square[i] != EMPTY){
                //System.out.println(square[i]);
                piece = square[i];
                pList[piece][pceNum[piece]] = i;
                pceNum[piece]++;
                if(piece == WK){
                    kingSq[WHITE] = i;
                    kingCount++;
                }
                if(piece == BK){
                    kingSq[BLACK] = i;
                    kingCount++;
                }
            }
        }
    }

    void addQuietMove(final int move){
        mlist.move[mlist.mCount] = move;
        mlist.mCount++;
    }
    void addCaptureMove(final int move){
        mlist.move[mlist.mCount] = move;
        mlist.mCount++;
    }
    void addEnPassantMove(final int move){
        mlist.move[mlist.mCount] = move;
        mlist.mCount++;
    }
    void addWhitePawnCapMove(final int from, final int to, final int cap){
        if(SQ2R(from) == RANK_7){
            addCaptureMove(MOVE(from, to, cap, WQ, 0));
            addCaptureMove(MOVE(from, to, cap, WR, 0));
            addCaptureMove(MOVE(from, to, cap, WB, 0));
            addCaptureMove(MOVE(from, to, cap, WN, 0));
        }
        else{
            addCaptureMove(MOVE(from, to, cap, EMPTY, 0));
        }
    }
    void addWhitePawnMove(final int from, final int to){
        if(SQ2R(from) == RANK_7) {
            addQuietMove(MOVE(from, to, EMPTY, WQ, 0));
            addQuietMove(MOVE(from, to, EMPTY, WR, 0));
            addQuietMove(MOVE(from, to, EMPTY, WB, 0));
            addQuietMove(MOVE(from, to, EMPTY, WN, 0));
        }
        else{
            addQuietMove(MOVE(from, to, EMPTY, EMPTY, 0));
        }
    }
    void addBlackPawnCapMove(final int from, final int to, final int cap){
        if(SQ2R(from) == RANK_2){
            addCaptureMove(MOVE(from, to, cap, BQ, 0));
            addCaptureMove(MOVE(from, to, cap, BR, 0));
            addCaptureMove(MOVE(from, to, cap, BB, 0));
            addCaptureMove(MOVE(from, to, cap, BN, 0));
        }
        else{
            addCaptureMove(MOVE(from, to, cap, EMPTY, 0));
        }
    }
    void addBlackPawnMove(final int from, final int to){
        if(SQ2R(from) == RANK_2) {
            addQuietMove(MOVE(from, to, EMPTY, BQ, 0));
            addQuietMove(MOVE(from, to, EMPTY, BR, 0));
            addQuietMove(MOVE(from, to, EMPTY, BB, 0));
            addQuietMove(MOVE(from, to, EMPTY, BN, 0));
        }
        else{
            addQuietMove(MOVE(from, to, EMPTY, EMPTY, 0));
        }
    }
    void generateWhitePawnMoves(final int sq){
        int pce = EMPTY;
        if(square[FR2SQ(SQ2F(sq),SQ2R(sq) + 1)] == EMPTY){
            addWhitePawnMove(sq, sq+8);
            if(SQ2R(sq) == RANK_2 && square[FR2SQ(SQ2F(sq),SQ2R(sq) + 2)] == EMPTY){
                addQuietMove(MOVE(sq, sq+16, EMPTY, EMPTY, MFLAGPS));
            }
        }
        if(SQ2F(sq) + 1 <= FILE_H && SQ2R(sq) + 1 <= RANK_8){
            if(square[FR2SQ(SQ2F(sq) + 1,SQ2R(sq) + 1)] != EMPTY){
                if(pceColor(square[FR2SQ(SQ2F(sq) + 1,SQ2R(sq) + 1)]) == BLACK){
                    pce = square[FR2SQ(SQ2F(sq) + 1,SQ2R(sq) + 1)];
                    addWhitePawnCapMove(sq, sq+9, pce);
                }
            }
        }
        if(SQ2F(sq) - 1 >= FILE_A && SQ2R(sq) + 1 <= RANK_8){
            if(square[FR2SQ(SQ2F(sq) - 1,SQ2R(sq) + 1)] != EMPTY){
                if(pceColor(square[FR2SQ(SQ2F(sq) - 1,SQ2R(sq) + 1)]) == BLACK){
                    pce = square[FR2SQ(SQ2F(sq) - 1,SQ2R(sq) + 1)];
                    addWhitePawnCapMove(sq, sq+7, pce);
                }
            }
        }
        if(enPas != NO_SQ){
            if(sq + 9 == enPas && (SQ2F(sq) + 1) <= FILE_H){
                addEnPassantMove(MOVE(sq, sq+9, EMPTY, EMPTY, MFLAGEP));
            }
            if(sq + 7 == enPas){
                addEnPassantMove(MOVE(sq, sq+7, EMPTY, EMPTY, MFLAGEP));
            }
        }
    }
    void generateBlackPawnMoves(final int sq){
        int pce = EMPTY;
        if(square[FR2SQ(SQ2F(sq),SQ2R(sq) - 1)] == EMPTY){
            addBlackPawnMove(sq, sq-8);
            if(SQ2R(sq) == RANK_7 && square[FR2SQ(SQ2F(sq),SQ2R(sq) - 2)] == EMPTY){
                addQuietMove(MOVE(sq, sq-16, EMPTY, EMPTY, MFLAGPS));
            }
        }
        if(SQ2F(sq) - 1 >= FILE_A && SQ2R(sq) - 1 >= RANK_1){
            if(square[FR2SQ(SQ2F(sq) - 1,SQ2R(sq) - 1)] != EMPTY){
                if(pceColor(square[FR2SQ(SQ2F(sq) - 1,SQ2R(sq) - 1)]) == WHITE){
                    pce = square[FR2SQ(SQ2F(sq) - 1,SQ2R(sq) - 1)];
                    addBlackPawnCapMove(sq, sq-9, pce);
                }
            }
        }
        if(SQ2F(sq) + 1 <= FILE_H && SQ2R(sq) - 1 >= RANK_1){
            if(square[FR2SQ(SQ2F(sq) + 1,SQ2R(sq) - 1)] != EMPTY){
                if(pceColor(square[FR2SQ(SQ2F(sq) + 1,SQ2R(sq) - 1)]) == WHITE){
                    pce = square[FR2SQ(SQ2F(sq) + 1,SQ2R(sq) - 1)];
                    addBlackPawnCapMove(sq, sq-7, pce);
                }
            }
        }
        if(enPas != NO_SQ){
            if(sq - 9 == enPas && (SQ2F(sq) - 1) > FILE_A) {
                addEnPassantMove(MOVE(sq, sq-9, EMPTY, EMPTY, MFLAGEP));
            }
            if(sq - 7 == enPas) {
                addEnPassantMove(MOVE(sq, sq-7, EMPTY, EMPTY, MFLAGEP));
            }
        }
    }
    void generateCastling(){
        if(side == WHITE){
            /* White Castle */
            if( (castlePerm & WKCA) > 0) {
                if(square[F1] == EMPTY && square[G1] == EMPTY){
                    if(!sqAttacked(E1, BLACK) && !sqAttacked(F1, BLACK)){
                        addQuietMove(MOVE(E1, G1, EMPTY, EMPTY, MFLAGCA));
                    }
                }
            }
            if((castlePerm & WQCA) > 0) {
                if(square[D1] == EMPTY && square[C1] == EMPTY && square[B1] == EMPTY){
                    if(!sqAttacked(E1, BLACK) && !sqAttacked(D1, BLACK)){
                        addQuietMove(MOVE(E1, C1, EMPTY, EMPTY, MFLAGCA));
                    }
                }
            }
        }
        else{
            /* Black Castle */
            if((castlePerm & BKCA) > 0) {
                if(square[F8] == EMPTY && square[G8] == EMPTY){
                    if(!sqAttacked(E8, WHITE) && !sqAttacked(F8, WHITE)){
                        addQuietMove(MOVE(E8, G8, EMPTY, EMPTY, MFLAGCA));
                    }
                }
            }
            if((castlePerm & BQCA) > 0) {
                if(square[D8] == EMPTY && square[C8] == EMPTY && square[B8] == EMPTY){
                    if(!sqAttacked(E8, WHITE) && !sqAttacked(D8, WHITE)){
                        addQuietMove(MOVE(E8, C8, EMPTY, EMPTY, MFLAGCA));
                    }
                }
            }
        }
    }
    static final int LoopSlidePce[] = { WB, WR, WQ, 0, BB, BR, BQ, 0 };
    static final int LoopNonSlidePce[] = { WN, WK, 0, BN, BK, 0 };
    static final int LoopSlideIndex[] = { 0, 4 };
    static final int LoopNonSlideIndex[] = { 0, 3 };
    static final int PceDir[][][] = {
        { {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0} },
        { {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0} },
        { {-1,-2}, {-2,-1}, {-2,1}, {-1,2}, {1,2}, {2,1}, {2,-1}, {1,-2} },
        { {-1,1}, {1,1}, {1,-1}, {-1,-1}, {0,0}, {0,0}, {0,0}, {0,0} },
        { {-1,0}, {0,1}, {1,0}, {0,-1}, {0,0}, {0,0}, {0,0}, {0,0} },
        { {-1,0}, {0,1}, {1,0}, {0,-1}, {-1,1}, {1,1}, {1,-1}, {-1,-1} },
        { {-1,0}, {0,1}, {1,0}, {0,-1}, {-1,1}, {1,1}, {1,-1}, {-1,-1} },
        { {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0}, {0,0} },
        { {-1,-2}, {-2,-1}, {-2,1}, {-1,2}, {1,2}, {2,1}, {2,-1}, {1,-2} },
        { {-1,1}, {1,1}, {1,-1}, {-1,-1}, {0,0}, {0,0}, {0,0}, {0,0} },
        { {-1,0}, {0,1}, {1,0}, {0,-1}, {0,0}, {0,0}, {0,0}, {0,0} },
        { {-1,0}, {0,1}, {1,0}, {0,-1}, {-1,1}, {1,1}, {1,-1}, {-1,-1} },
        { {-1,0}, {0,1}, {1,0}, {0,-1}, {-1,1}, {1,1}, {1,-1}, {-1,-1} },
    };
    static final int NumDir[] = { 0, 0, 8, 4, 4, 8, 8, 0, 8, 4, 4, 8, 8 };
    void generateSlidingPieceMoves(final int sq){
        int pce;
        int dir[] = new int[2];
        int tempsq[] = new int[2];
        int color;
        int cap;
        pce = square[sq];
        for(int i = 0 ; i < NumDir[pce]; i++){
            dir[0] = PceDir[pce][i][0];
            dir[1] = PceDir[pce][i][1];
            tempsq[0] = SQ2F(sq) + dir[0];
            tempsq[1] = SQ2R(sq) + dir[1];
            while(tempsq[0] >= FILE_A && tempsq[0] <= FILE_H && tempsq[1] >= RANK_1 && tempsq[1] <= RANK_8){
                // BLACK ^ 1 == WHITE       WHITE ^ 1 == BLACK
                if(square[FR2SQ(tempsq[0],tempsq[1])] != EMPTY){
                    color = pceColor(square[FR2SQ(tempsq[0],tempsq[1])]);
                    if(color == (side ^ 1) ){
                        cap = square[FR2SQ(tempsq[0],tempsq[1])];
                        addCaptureMove(MOVE(sq, FR2SQ(tempsq[0], tempsq[1]), cap, EMPTY, 0));
                    }
                    break;
                }
                addQuietMove(MOVE(sq, FR2SQ(tempsq[0], tempsq[1]), EMPTY, EMPTY, 0));
                tempsq[0] += dir[0];
                tempsq[1] += dir[1];
            }
        }
    }
    void generateNonSlidingPieceMoves(final int sq){
        int pce;
        int dir[] = new int[2];
        int tempsq[] = new int[2];
        int color;
        int cap;
        pce = square[sq];
        for(int i = 0 ; i < NumDir[pce]; i++){
            dir[0] = PceDir[pce][i][0];
            dir[1] = PceDir[pce][i][1];
            tempsq[0] = SQ2F(sq) + dir[0];
            tempsq[1] = SQ2R(sq) + dir[1];
            if(tempsq[0] < FILE_A || tempsq[0] > FILE_H || tempsq[1] < RANK_1 || tempsq[1] > RANK_8){
                continue;
            }
            // BLACK ^ 1 == WHITE       WHITE ^ 1 == BLACK
            if(square[FR2SQ(tempsq[0],tempsq[1])] != EMPTY){
                color = pceColor(square[FR2SQ(tempsq[0],tempsq[1])]);
                if( color == (side ^ 1) ){
                    cap = square[FR2SQ(tempsq[0],tempsq[1])];
                    addCaptureMove(MOVE(sq, FR2SQ(tempsq[0], tempsq[1]), cap, EMPTY, 0));
                }
                continue;
            }
            addQuietMove(MOVE(sq, FR2SQ(tempsq[0], tempsq[1]), EMPTY, EMPTY, 0));
        }
    }
    void generateAllMoves(){
        mlist.mCount = 0;
        int pce = EMPTY;
        int sq = 0;
        int pceIndex = 0;
        if(side == WHITE){
            for(int i = 0 ; i < pceNum[WP] ; i++){
                sq = pList[WP][i];
                generateWhitePawnMoves(sq);
            }
        }
        else{
            for(int i = 0 ; i < pceNum[BP] ; i++){
                sq = pList[BP][i];
                generateBlackPawnMoves(sq);
            }
        }
        /* Loop for slide pieces */
        pceIndex = LoopSlideIndex[side];
        pce = LoopSlidePce[pceIndex++];
        while(pce != 0){
            for(int i = 0 ; i < pceNum[pce]; i++){
                sq = pList[pce][i];
                generateSlidingPieceMoves(sq);
            }
            pce = LoopSlidePce[pceIndex++];
        }
        /* Loop for non slide */
        pceIndex = LoopNonSlideIndex[side];
        pce = LoopNonSlidePce[pceIndex++];
        while( pce != 0) {
            for(int i = 0 ; i < pceNum[pce]; i++){
                sq = pList[pce][i];
                generateNonSlidingPieceMoves(sq);
            }
            pce = LoopNonSlidePce[pceIndex++];
        }
        /* Castling */
        generateCastling();
    }

    void clearPiece(final int sq){
        int tempPceNum = -1;
        int pce = square[sq];
        //delete square[SQ2F(sq)][SQ2R(sq)];
        square[sq] = EMPTY;
        for(int i = 0 ; i < pceNum[pce] ; ++i){
            if(pList[pce][i] == sq){
                tempPceNum = i;
                break;
            }
        }
        pceNum[pce]--;
        pList[pce][tempPceNum] = pList[pce][pceNum[pce]];
    }
    void addPiece(final int sq, final int pce){
        square[sq] = pce;
        pList[pce][pceNum[pce]] = sq;
        pceNum[pce]++;
    }
    void movePiece(final int from, final int to){
        int pce = square[from];
        square[to] = square[from];
        square[from] = EMPTY;
        for(int i = 0 ; i < pceNum[pce] ; ++i){
            if(pList[pce][i] == from){
                pList[pce][i] = to;
                break;
            }
        }
    }
    final int CastlePerm[] = {
        13, 15, 15, 15, 12, 15, 15, 14,
                15, 15, 15, 15, 15, 15, 15, 15,
                15, 15, 15, 15, 15, 15, 15, 15,
                15, 15, 15, 15, 15, 15, 15, 15,
                15, 15, 15, 15, 15, 15, 15, 15,
                15, 15, 15, 15, 15, 15, 15, 15,
                15, 15, 15, 15, 15, 15, 15, 15,
                7, 15, 15, 15,  3, 15, 15, 11
    };
    public boolean makeMove(final int move){

        int from = FROMSQ(move);
        int to = TOSQ(move);
        int side = getSide();

        if(from == to){
            return false;
        }

        if(history.size() != hisPly){
            history = (ArrayList<Undo>) history.subList(hisPly, history.size());
        }

        if((move & MFLAGEP) > 0){
            if(side == WHITE){
                clearPiece(to-8);
                activity.clearPiece(sq2String(to-8));
            }
            else{
                clearPiece(to+8);
                activity.clearPiece(sq2String(to+8));
            }
        }
        else if((move & MFLAGCA) > 0){
            switch(to){
                case C1:
                    movePiece(A1, D1);
                    activity.movePiece("a1", "d1");
                    break;
                case C8:
                    movePiece(A8, D8);
                    activity.movePiece("a8", "d8");
                    break;
                case G1:
                    movePiece(H1, F1);
                    activity.movePiece("h1", "f1");
                    break;
                case G8:
                    movePiece(H8, F8);
                    activity.movePiece("h8", "f8");
                    break;
                default:
                    break;
            }
        }

        Undo temp = new Undo(move, castlePerm, enPas, fiftyMove);
        history.add(temp);

        castlePerm &= CastlePerm[from];
        castlePerm &= CastlePerm[to];
        enPas = NO_SQ;

        int cap = CAPTURED(move);
        fiftyMove++;
        if(cap != EMPTY){
            clearPiece(to);
            activity.clearPiece(sq2String(to));
            fiftyMove = 0;
        }
        hisPly++;
        ply++;
        int pce = square[from];
        if(pce == WP || pce == BP){
            fiftyMove = 0;
            if((move & MFLAGPS) > 0){
                if(side == WHITE){
                    enPas = from+8;
                }
                else{
                    enPas = from-8;
                }
            }
        }
        movePiece(from, to);
        activity.movePiece(sq2String(from), sq2String(to));
        int prPce = PROMOTED(move);
        if(prPce != EMPTY){
            clearPiece(to);
            activity.clearPiece(sq2String(to));
            addPiece(to, prPce);
            activity.addPiece(sq2String(to), pce2String(prPce));
        }
        if(pce == WK || pce == BK){
            kingSq[side] = to;
        }
        this.side ^= 1;
        if(sqAttacked(kingSq[side], this.side)){
            takeMove();
            return false;
        }
        return true;
    }

    void takeMove(){
        if(history.size() == 0){
            return;
        }
        hisPly--;
        ply--;
        int move = history.get(hisPly).move;
        int from = FROMSQ(move);
        int to = TOSQ(move);

        castlePerm = history.get(hisPly).castlePerm;
        fiftyMove = history.get(hisPly).fiftyMove;
        enPas = history.get(hisPly).enPas;

        history.remove(hisPly);

        side ^= 1;

        if((MFLAGEP & move) > 0){
            if(side == WHITE){
                addPiece(to-8, BP);
                activity.addPiece(sq2String(to-8), "bp");
            }
            else{
                addPiece(to+8, WP);
                activity.addPiece(sq2String(to+8), "wp");
            }
        }
        else if((MFLAGCA & move) > 0){
            switch(to){
                case C1:
                    movePiece(D1, A1);
                    activity.movePiece("d1", "a1");
                    break;
                case C8:
                    movePiece(D8, A8);
                    activity.movePiece("d8", "a8");
                    break;
                case G1:
                    movePiece(F1, H1);
                    activity.movePiece("f1", "h1");
                    break;
                case G8:
                    movePiece(F8, H8);
                    activity.movePiece("f8", "h8");
                    break;
                default:
                    //assert(false);
                    break;
            }
        }
        movePiece(to, from);
        activity.movePiece(sq2String(to), sq2String(from));
        int pce = square[from];
        if(pce == WK || pce == BK){
            kingSq[side] = from;
        }
        int cap = CAPTURED(move);
        if(cap != EMPTY){
            addPiece(to, cap);
            activity.addPiece(sq2String(to), pce2String(cap));
        }
        if(PROMOTED(move) != EMPTY){
            int col = PROMOTED(move) / 7;
            clearPiece(from);
            activity.clearPiece(sq2String(from));
            addPiece(from, col == WHITE ? WP : BP);
            activity.addPiece(sq2String(from), col == WHITE ? "wp" : "bp");
        }
    }

    public void init(){
        for(int i = 0 ; i < 64 ; i++){
            square[i] =EMPTY;
        }
        square[A2] = WP;
        square[B2] = WP;
        square[C2] = WP;
        square[D2] = WP;
        square[E2] = WP;
        square[F2] = WP;
        square[G2] = WP;
        square[H2] = WP;

        square[A7] = BP;
        square[B7] = BP;
        square[C7] = BP;
        square[D7] = BP;
        square[E7] = BP;
        square[F7] = BP;
        square[G7] = BP;
        square[H7] = BP;

        square[A1] = WR;
        square[B1] = WN;
        square[C1] = WB;
        square[D1] = WQ;
        square[E1] = WK;
        square[F1] = WB;
        square[G1] = WN;
        square[H1] = WR;

        square[A8] = BR;
        square[B8] = BN;
        square[C8] = BB;
        square[D8] = BQ;
        square[E8] = BK;
        square[F8] = BB;
        square[G8] = BN;
        square[H8] = BR;

        side = WHITE;
        castlePerm = WKCA | WQCA | BKCA | BQCA;

        updateListsMaterial();

        activity.setState(1);
    }

    public int parseFen(String fen){
        try{
            if(fen.isEmpty()){
                throw new IOException("fen is empty!");
            }
            int  r = RANK_8;
            int  f = FILE_A;
            int  piece = 0;
            int  c = 0;
            int  i = 0;
            resetBoard();
            while ((r >= RANK_1) && i < fen.length()) {
                c = 1;
                switch (fen.charAt(i)) {
                    case 'p': piece = BP; break;
                    case 'r': piece = BR; break;
                    case 'n': piece = BN; break;
                    case 'b': piece = BB; break;
                    case 'k': piece = BK; break;
                    case 'q': piece = BQ; break;
                    case 'P': piece = WP; break;
                    case 'R': piece = WR; break;
                    case 'N': piece = WN; break;
                    case 'B': piece = WB; break;
                    case 'K': piece = WK; break;
                    case 'Q': piece = WQ; break;
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                        piece = EMPTY;
                        c = fen.charAt(i) - '0';
                        break;
                    case '/':
                    case ' ':
                        r--;
                        f = FILE_A;
                        i++;
                        continue;
                    default:
                        throw new IOException("Invalid argument in Fen");
                }
                for (int j = 0 ; j < c ; i++) {
                    if (piece != EMPTY) {
                        square[FR2SQ(f,r)] = piece;
                    }
                    f++;
                }
                i++;
            }
            if(fen.charAt(i) != 'w' && fen.charAt(i) != 'b'){
                throw new IOException("Side to move not given in Fen");
            }
            side = (fen.charAt(i) == 'w') ? WHITE : BLACK;
            i += 2;
            for (int j = 0 ; j < 4 ; j++) {
                if (fen.charAt(i) == ' ') {
                    break;
                }
                switch(fen.charAt(i)) {
                    case 'K': castlePerm |= WKCA; break;
                    case 'Q': castlePerm |= WQCA; break;
                    case 'k': castlePerm |= BKCA; break;
                    case 'q': castlePerm |= BQCA; break;
                    default:	     break;
                }
                i++;
            }
            i++;
            if(!(castlePerm>=0 && castlePerm <= 15)){
                throw new RuntimeException("Something is very wrong");
            }
            if (fen.charAt(i) != '-') {
                f = fen.charAt(i) - 'a';
                r = fen.charAt(i+1) - '1';
                enPas = FR2SQ(f,r);
            }
            updateListsMaterial();
            return 0;
        }
        catch(Exception e){
            System.out.println(e.getMessage());
            resetBoard();
            return 1;
        }
    }
    public int parseMove(String move){
        if(move.charAt(1) > '8' || move.charAt(1) < '1') return 0;
        if(move.charAt(3) > '8' || move.charAt(3) < '1') return 0;
        if(move.charAt(0) > 'h' || move.charAt(0) < 'a') return 0;
        if(move.charAt(2) > 'h' ||move.charAt(2) < 'a') return 0;
        int from = FR2SQ(move.charAt(0) - 'a', move.charAt(1) - '1');
        int to = FR2SQ(move.charAt(2) - 'a', move.charAt(3) - '1');
        if(!isSqOnBoard(from) || !isSqOnBoard(to)){
            return 0;
        }
        //assert(impl->isSqOnBoard(from) && impl->isSqOnBoard(to));
        generateAllMoves();
        int m = 0;
        int promPce = EMPTY;
        for(int i = 0 ; i < mlist.mCount ; ++i){
            m = mlist.move[i];
            if(FROMSQ(m) == from && TOSQ(m) == to){
                promPce = PROMOTED(m);
                if(promPce != EMPTY){
                    if(IsRQ[promPce] && !IsBQ[promPce] && move.charAt(4)=='r') {
                        return m;
                    } else if(!IsRQ[promPce] && IsBQ[promPce] && move.charAt(4)=='b') {
                        return m;
                    } else if(IsRQ[promPce] && IsBQ[promPce] && move.charAt(4)=='q') {
                        return m;
                    } else if(IsKn[promPce] && move.charAt(4)=='n') {
                        return m;
                    }
                    continue;
                }
                return m;
            }
        }
        return 0;
    }

    void resetBoard(){
        for(int i = 0 ; i < 64 ; i++){
            square[i] = EMPTY;
        }
        for(int i = 0 ; i < 13; i++) {
            pceNum[i] = 0;
        }
        kingSq[WHITE] = NO_SQ;
        kingSq[BLACK] = NO_SQ;
        side = 2;
        enPas = NO_SQ;
        fiftyMove = 0;
        ply = 0;
        hisPly = 0;
        castlePerm = 0;
    }

    public int getSide() {
        return side;
    }

    public int getPly() {
        return ply;
    }

    public boolean getGameState(){
        return isGame;
    }

    public MoveList getMoveList(){
        return mlist;
    }

    private int MOVE(int from, int to, int ca, int pro, int fl){
        return from | to << 7 | ca << 14 | pro << 20 | fl;
    }

    private int FROMSQ(int m){
        return m & 0x7F;
    }
    private int TOSQ(int m){
        return (m >> 7) & 0x7F;
    }
    private int CAPTURED(int m){
        return ( m >> 14) & 0xF;
    }
    private int PROMOTED(int m){
        return ( m >> 20) & 0xF;
    }

    int FR2SQ(int file, int rank){
        return file + rank*8;
    }

    int SQ2R(int sq){
        return sq/8;
    }
    int SQ2F(int sq){
        return sq%8;
    }

}
