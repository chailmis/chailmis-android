package org.clintonhealthaccess.lmis.app.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;

import org.clintonhealthaccess.lmis.app.R;
import org.clintonhealthaccess.lmis.app.LmisException;
import org.clintonhealthaccess.lmis.app.services.CommodityService;
import org.clintonhealthaccess.lmis.app.services.UserService;

import roboguice.activity.RoboActionBarActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import static android.view.View.OnClickListener;
import static android.widget.Toast.LENGTH_SHORT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.clintonhealthaccess.lmis.app.R.id;
import static org.clintonhealthaccess.lmis.app.R.layout;

public class RegisterActivity extends RoboActionBarActivity {
    @Inject
    private UserService userService;

    @Inject
    private CommodityService commodityService;

    @InjectView(id.textUsername)
    private TextView textUsername;

    @InjectView(id.textPassword)
    private TextView textPassword;

    @InjectResource(R.string.registration_successful_message)
    private String registrationSuccessfulMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_register);

        Button registerButton = (Button) findViewById(id.buttonRegister);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = textUsername.getText().toString();
                String password = textPassword.getText().toString();
                if (isBlank(username)) {
                    textUsername.setError(getString(R.string.username_error));
                    return;
                }

                if (isBlank(password)) {
                    textPassword.setError(getString(R.string.password_error));
                    return;
                }

                doRegister(username, password);
            }
        });
    }

    private void doRegister(final String username, final String password) {
        AsyncTask<Void, Void, Boolean> registerTask = new AsyncTask<Void, Void, Boolean>() {
            private LmisException failureCause;

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    userService.register(username, password);
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                } catch (LmisException e) {
                    this.failureCause = e;
                    return false;
                }
                commodityService.initialise();
                return true;
            }

            @Override
            protected void onPostExecute(Boolean succeeded) {
                String toastMessage = succeeded ? registrationSuccessfulMessage : failureCause.getMessage();
                Toast.makeText(getApplicationContext(), toastMessage, LENGTH_SHORT).show();
            }
        };
        registerTask.execute();
    }
}
