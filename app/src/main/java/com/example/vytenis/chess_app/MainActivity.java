package com.example.vytenis.chess_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.os.StrictMode;

import static com.example.vytenis.chess_app.R.id.bottom;
import static com.example.vytenis.chess_app.R.id.textView;

public class MainActivity extends AppCompatActivity {

    private SocketService mBoundService;
    private boolean mIsBound;

    // UI references.
    private TextView mTextView;
    private Button seekButton;
    private Button inviteButton;
    private EditText mUsername;
    private View mProgressView;
    private View mMainView;
    private Button cancelButton;

    private UserSeekTask mSeekTask = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        //EDITED PART
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            mBoundService = ((SocketService.LocalBinder)service).getService();
            System.out.println("Trying to access service");
            if(mBoundService != null){
                mTextView.setText("You are logged in as " + mBoundService.getUsername() + "!");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            mBoundService = null;
        }

    };


    private void doBindService() {
        bindService(new Intent(MainActivity.this, SocketService.class), mConnection, Context.BIND_AUTO_CREATE);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setContentView(R.layout.activity_main);
        //startService(new Intent(MainActivity.this,SocketService.class));
        doBindService();

        initReferences();
        setListeners();

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void initReferences(){
        mTextView = (TextView)findViewById(R.id.textView);
        seekButton = (Button) findViewById(R.id.button);
        mProgressView = (View) findViewById(R.id.progressView);
        mMainView = (View) findViewById(R.id.mainView);
        cancelButton = (Button) findViewById(R.id.cancel);
        inviteButton = (Button) findViewById(R.id.invite);
        mUsername = (EditText) findViewById(R.id.username);
    }

    private void setListeners(){
        seekButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seekGame();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSeekTask.cancel(true);
            }
        });
    }

    private void seekGame(){
        if (mSeekTask != null) {
            return;
        }
        showProgress(true);
        mSeekTask = new UserSeekTask(mBoundService);
        mSeekTask.execute((Void) null);
    }

    public void finish(){
        doUnbindService();
        //Start game activity
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("color", mBoundService.getColor());
        startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mMainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            cancelButton.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            cancelButton.setVisibility(show ? View.VISIBLE : View.GONE);
            mMainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class UserSeekTask extends AsyncTask<Void, Void, Boolean> {
        private final SocketService mService;

        UserSeekTask(SocketService service) {
            mService = service;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            mService.sendMessage("2");
            System.out.println("You zdd seek!");
            if(mService.waitForAuthorization()){
                return true;
            }
            else{
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean aBoolean) {
            mSeekTask = null;
            showProgress(false);

            System.out.println("You zdd seek post!");

            finish();
        }

        @Override
        protected void onCancelled() {
            //Write somethinf else!
            super.onCancelled();
        }
    }
}
