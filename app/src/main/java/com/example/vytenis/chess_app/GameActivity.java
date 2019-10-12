package com.example.vytenis.chess_app;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.DragEvent;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import board.ChessBoard;

public class GameActivity extends Activity {

    private SocketService mBoundService;
    private boolean mIsBound;

    private ChessBoard board;
    private int state;
    private int inOptions = 0;

    private TextView mUsernameView;
    private TextView mOtherUsernameView;

    private LinearLayout mOptions;
    private android.widget.GridLayout mGame;
    private Button mMore;
    private TableLayout mTable;
    private TableRow mRow;

    private WaitMoveTask mWaitMove = null;
    private Vibrator vib;

    private String color;

    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBoundService = ((SocketService.LocalBinder)service).getService();
            System.out.println("Trying to access service");
            if(mBoundService != null){
                System.out.println("Service online!");
                mUsernameView.setText(mBoundService.getUsername());
                mOtherUsernameView.setText(mBoundService.getOtherUsername());
                if(color.equals("black")){
                    System.out.println("creating task");
                    mWaitMove = new WaitMoveTask(mBoundService);
                    mWaitMove.execute((Void) null);
                    System.out.println("created task");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBoundService = null;
        }

    };


    private void doBindService() {
        bindService(new Intent(GameActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        if(mBoundService!=null){
            mBoundService.IsBoundable();
        }
    }


    private void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        color = getIntent().getStringExtra("color");

        if(color.equals("white")){
            setContentView(R.layout.activity_game);
        }
        else{
            setContentView(R.layout.activity_game2);
        }

        doBindService();

        mOptions = (LinearLayout) findViewById(R.id.options);
        mGame = (android.widget.GridLayout) findViewById(R.id.game);
        mMore = (Button) findViewById(R.id.more);
        mTable = (TableLayout) findViewById(R.id.table);
        mMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inOptions = 1;
                mOptions.setVisibility(View.VISIBLE);
                mGame.setVisibility(View.GONE);
                mOtherUsernameView.setVisibility(View.GONE);
                mUsernameView.setVisibility(View.GONE);
                mMore.setVisibility(View.GONE);
            }
        });

        mUsernameView = (TextView)findViewById(R.id.username);
        mOtherUsernameView = (TextView) findViewById(R.id.otherUsername);

        vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        board = new ChessBoard(this);
        board.init();
        //board.parseFen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        initSquares();
        initPieces();
    }

    @Override
    public void onBackPressed() {
        if(inOptions == 1){
            inOptions = 0;
            mOptions.setVisibility(View.GONE);
            mGame.setVisibility(View.VISIBLE);
            mOtherUsernameView.setVisibility(View.VISIBLE);
            mUsernameView.setVisibility(View.VISIBLE);
            mMore.setVisibility(View.VISIBLE);
            return;
        }
        if(state == 1){
            Toast.makeText(this,"You are in game! Can't leave now :(", Toast.LENGTH_LONG).show();
            return;
        }
        super.onBackPressed();
    }
    /*
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu, popup.getMenu());
        popup.show();
    }
    */
    public void setState(int state){
        this.state = state;
    }

    private void initSquares(){
        for(int i=0; i<8; i++) {
            for(int j=0; j<8; j++) {
                int file = i + 'a';
                int row = j + '1';

                char cfile = (char)file;
                char crow = (char)row;

                String squareID = new StringBuilder().append(cfile).append(crow).toString();

                int resID = getResources().getIdentifier(squareID, "id", getPackageName());

                if(j % 2 == 0){
                    if(i % 2 == 0){
                        ((View) findViewById(resID)).setOnDragListener(new MyDragListener1());
                        continue;
                    }
                }
                else if(j % 2 != 0){
                    if(i % 2 != 0){
                        ((View) findViewById(resID)).setOnDragListener(new MyDragListener1());
                        continue;
                    }
                }

                ((View) findViewById(resID)).setOnDragListener(new MyDragListener());
            }
        }
    }

    public void initPieces(){
        for(int i = 0 ; i < 8 ; i++){
            int file = i + 'a';
            char cfile = (char)file;

            String squareID = new StringBuilder().append(cfile).append('7').toString();
            addPiece(squareID, "bp");

            squareID = new StringBuilder().append(cfile).append('2').toString();
            addPiece(squareID, "wp");
        }

        addPiece("a8", "br");
        addPiece("b8", "bn");
        addPiece("c8", "bb");
        addPiece("d8", "bq");
        addPiece("e8", "bk");
        addPiece("f8", "bb");
        addPiece("g8", "bn");
        addPiece("h8", "br");

        addPiece("a1", "wr");
        addPiece("b1", "wn");
        addPiece("c1", "wb");
        addPiece("d1", "wq");
        addPiece("e1", "wk");
        addPiece("f1", "wb");
        addPiece("g1", "wn");
        addPiece("h1", "wr");
    }

    public void addMove(String move){
        if(mRow == null){
            mRow = new TableRow(this);
        }
        if(mRow.getChildCount() == 2){
            Button moveView = new Button(this);
            moveView.setText(move);
            moveView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            mRow.addView(moveView);
        }
        else{
            mRow = new TableRow(this);
            Button plyView = new Button(this);
            plyView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            plyView.setText(Integer.toString(board.getPly()/2+1));
            mRow.addView(plyView);
            Button moveView = new Button(this);
            moveView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            moveView.setText(move);
            mRow.addView(moveView);
            mTable.addView(mRow);
        }
    }

    public void addPiece(String sq, String piece){
        int sqID = getResources().getIdentifier(sq, "id", getPackageName());
        FrameLayout container = (FrameLayout) findViewById(sqID);

        ImageView iv = new ImageView(this);
        int resID = getResources().getIdentifier(piece, "drawable", getPackageName());
        iv.setImageResource(resID);
        if(piece.charAt(0) == 'w' && color.equals("white")){
            iv.setOnTouchListener(new MyTouchListener());
        }
        else if(piece.charAt(0) == 'b' && color.equals("black")){
            iv.setOnTouchListener(new MyTouchListener());
        }
        container.addView(iv);
    }

    public void movePiece(String from, String to){
        System.out.println("Mocing piece form to"+from+to);
        int sqID = getResources().getIdentifier(from, "id", getPackageName());
        FrameLayout containerFrom = (FrameLayout) findViewById(sqID);

        ImageView piece = (ImageView) containerFrom.getChildAt(0);
        containerFrom.removeView(piece);

        sqID = getResources().getIdentifier(to, "id", getPackageName());
        FrameLayout containerTo = (FrameLayout) findViewById(sqID);

        containerTo.addView(piece);
    }

    public void clearPiece(String sq){
        int sqID = getResources().getIdentifier(sq, "id", getPackageName());
        FrameLayout containerSq = (FrameLayout) findViewById(sqID);
        containerSq.removeAllViews();
    }


    private final class MyTouchListener implements View.OnTouchListener {
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(
                        view);
                view.startDrag(data, shadowBuilder, view, 0);
                view.setVisibility(View.INVISIBLE);
                return true;
            } else {
                return false;
            }
        }
    }
    class MyDragListener1 implements View.OnDragListener {
        Drawable enterShape = getResources().getDrawable(R.drawable.shape1_droptarget);
        Drawable normalShape = getResources().getDrawable(R.drawable.shape1);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            //color = v.getBackground().getConstantState().equals(normalShape.getConstantState()) ? true : false;
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundDrawable(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundDrawable(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    View piece = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) piece.getParent();
                    FrameLayout container = (FrameLayout) v;

                    String from = owner.getResources().getResourceName(owner.getId());
                    String to = container.getResources().getResourceName(container.getId());
                    from = from.substring(from.length() - 2, from.length());
                    to = to.substring(to.length() - 2, to.length());
                    if(board.makeMove(board.parseMove(from+to))){
                        addMove(from+to);
                        piece.setVisibility(View.VISIBLE);
                        vib.vibrate(200);
                        if(mBoundService != null){
                            System.out.println("Made move! "+from+to);
                            if(!from.equals(to)){
                                mBoundService.sendMove(from+to);
                                mWaitMove = new WaitMoveTask(mBoundService);
                                mWaitMove.execute((Void) null);
                            }
                        }
                    }
                    piece.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    piece = (View) event.getLocalState();
                    piece.setVisibility(View.VISIBLE);
                    v.setBackgroundDrawable(normalShape);
                default:
                    break;
            }
            return true;
        }
    }

    class MyDragListener implements View.OnDragListener {
        Drawable enterShape = getResources().getDrawable(R.drawable.shape_droptarget);
        Drawable normalShape = getResources().getDrawable(R.drawable.shape);

        @Override
        public boolean onDrag(View v, DragEvent event) {
            //color = v.getBackground().getConstantState().equals(normalShape.getConstantState()) ? true : false;
            int action = event.getAction();
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    // do nothing
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundDrawable(enterShape);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackgroundDrawable(normalShape);
                    break;
                case DragEvent.ACTION_DROP:
                    // Dropped, reassign View to ViewGroup
                    View piece = (View) event.getLocalState();
                    ViewGroup owner = (ViewGroup) piece.getParent();
                    FrameLayout container = (FrameLayout) v;

                    String from = owner.getResources().getResourceName(owner.getId());
                    String to = container.getResources().getResourceName(container.getId());
                    from = from.substring(from.length() - 2, from.length());
                    to = to.substring(to.length() - 2, to.length());
                    if(board.makeMove(board.parseMove(from+to))){
                        addMove(from+to);
                        piece.setVisibility(View.VISIBLE);
                        vib.vibrate(200);
                        if(mBoundService != null){
                            if(!from.equals(to)){
                                mBoundService.sendMove(from+to);
                                mWaitMove = new WaitMoveTask(mBoundService);
                                mWaitMove.execute((Void) null);
                            }
                        }
                    }
                    piece.setVisibility(View.VISIBLE);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    v.setBackgroundDrawable(normalShape);
                default:
                    break;
            }
            return true;
        }
    }

    public class WaitMoveTask extends AsyncTask<Void, Void, Boolean> {
        private String move;

        private final SocketService mService;

        WaitMoveTask (SocketService service) {
            mService = service;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
//            mWaitMove = null;
            System.out.println("waiting for move");
            move = mService.waitForMove();
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean aBoolean) {
            mWaitMove = null;
            System.out.println("Got move: "+move);
            System.out.println("from: "+move.substring(0,2) + "to: "+move.substring(2,4));
            board.makeMove(board.parseMove(move));
            addMove(move);
            vib.vibrate(200);
//            finish();
            //MAKE MOVE HERE
        }
    }
}
