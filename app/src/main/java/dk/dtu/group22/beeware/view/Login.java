package dk.dtu.group22.beeware.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import dk.dtu.group22.beeware.R;

public class Login extends AppCompatActivity implements View.OnClickListener {

    private Button login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupToolbar();
        login_button = findViewById(R.id.login_button);
        login_button.setOnClickListener(this);
    }

    // Replaces action bar with toolbar
    public void setupToolbar() {
        // Sets the toolbar for the activity
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add 25 to the title's margin
        TextView toolbar_title = findViewById(R.id.toolbar_title);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        params.setMarginEnd(+ 25);
        toolbar_title.setLayoutParams(params);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        toolbar_title.setText("Log in");
    }

    @Override
    public void onClick(View view) {
        if (view == login_button) {
            Intent intent = new Intent(this, SubscriptionsOverview.class);
            // Flag makes the back stack ignored when changing view
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }
}
