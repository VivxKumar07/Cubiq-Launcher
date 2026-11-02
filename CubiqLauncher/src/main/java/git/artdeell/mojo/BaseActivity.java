package git.artdeell.mojo;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.init(this);
        Tools.getDisplayMetrics(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Tools.setInsetsMode(this, true, false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Tools.init(this);
        Tools.setInsetsMode(this, true, false);
        Tools.getDisplayMetrics(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Tools.setInsetsMode(this, true, false);
        }
    }
}
