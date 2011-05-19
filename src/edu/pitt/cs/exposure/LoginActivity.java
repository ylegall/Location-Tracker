package edu.pitt.cs.exposure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/**
 * Allows the user to enter a username and password.
 * 
 * @author ylegall
 */
public class LoginActivity extends BaseActivity implements OnClickListener {

	public static final int EMPTY_USER = 1;
	public static final int EMPTY_PASS = 2;

	private EditText userEdit, passEdit;
	private Button submitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		userEdit = (EditText) findViewById(R.id.user_edit);
		passEdit = (EditText) findViewById(R.id.pass_edit);
		submitButton = (Button) findViewById(R.id.submit_button);
		submitButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.submit_button:
				Log.i(TAG, "LoginActivity: submit clicked");
				if (userEdit.getText().length() == 0) {
					showDialog(EMPTY_USER);
				} else if (passEdit.getText().length() == 0) {
					showDialog(EMPTY_PASS);
				} else {
					setResultAndFinish();
				}
				break;
		}
	}

	private void setResultAndFinish() {
		Log.i(TAG, "LoginActivity: setting result and finishing.");
		Intent data = new Intent();
		data.putExtra("username", userEdit.getText().toString());
		data.putExtra("password", userEdit.getText().toString());
		setResult(RESULT_OK, data);
		finish();
	}

	protected Dialog onCreateDialog(int id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		switch (id) {
			case EMPTY_USER:
				Log.w(TAG, "LoginActivity: empty username");
				builder.setMessage("The username cannot be empty.");
				break;
			case EMPTY_PASS:
				Log.w(TAG, "LoginActivity: empty password");
				builder.setMessage("The password cannot be empty.");
				break;
		}
		return builder.create();
	}

}
