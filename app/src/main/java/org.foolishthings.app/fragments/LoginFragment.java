package org.foolishthings.app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.foolishthings.app.R;
import org.foolishthings.app.interfaces.LogInStateListener;
import org.foolishthings.app.model.LocalUser;
import org.foolishthings.app.utility.FontUtils;
import org.foolishthings.app.utility.Fonts;
import org.foolishthings.app.utility.Utils;
import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import info.hoang8f.android.segmented.SegmentedGroup;

public class LoginFragment extends Fragment implements RadioGroup.OnCheckedChangeListener
{
    private SegmentedGroup loginSegment;
    private RelativeLayout signUpLayout, loginLayout;
    private EditText etEmail, etFirstName, etLastName, etPassword, etEmailLogIn, etPasswordLogin;
    private Button bSignUp, bLogin;
    private ProgressDialog pDialog;
    private LogInStateListener onLogInListener;

    public static Fragment newInstance()
    {
        return new LoginFragment();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        onLogInListener = (LogInStateListener) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_login, container, false);
        loginSegment = (SegmentedGroup) v.findViewById(R.id.segmented);
        loginSegment.setTintColor(getResources().getColor(R.color.primary_color));

        signUpLayout = (RelativeLayout) v.findViewById(R.id.sg_signup);
        loginLayout = (RelativeLayout) v.findViewById(R.id.sg_login);

        etEmail = (EditText) v.findViewById(R.id.et_email);
        etFirstName = (EditText) v.findViewById(R.id.et_first_name);
        etLastName = (EditText) v.findViewById(R.id.et_last_name);
        etPassword = (EditText) v.findViewById(R.id.et_password);

        bSignUp = (Button) v.findViewById(R.id.b_continue);
        bSignUp.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onSignUpClicked();
            }
        });

        etEmailLogIn = (EditText) v.findViewById(R.id.et_email_login);
        etPasswordLogin = (EditText) v.findViewById(R.id.et_password_log_in);
        bLogin = (Button) v.findViewById(R.id.b_login);
        bLogin.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onLogInClicked();
            }
        });

        v.findViewById(R.id.forgot_password_text_view).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                onForgotPasswordClicked();
            }
        });

        loginSegment.setOnCheckedChangeListener(this);
        FontUtils.getInstance().overrideFonts(v, Fonts.LIGHT);
        return v;
    }

    public void onSignUpClicked()
    {
        if (etEmailLogIn.getText().equals("") || etPasswordLogin.getText().equals(""))
        {
            Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_LONG).show();
            return;
        }

        pDialog = Utils.createProgressDialog(getActivity());
        pDialog.show();

//        final ParseUser user = new ParseUser();
//        user.setEmail(etEmail.getText().toString().trim());
//        user.setUsername(etEmail.getText().toString().toString());
//        user.setPassword(etPassword.getText().toString());
//        user.put("appCompany", LocalUser.getInstance().getParentCompany());
//        user.put("fullName", etFirstName.getText().toString() + " " + etLastName.getText().toString());
//        user.signUpInBackground(new SignUpCallback()
        final String username = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString();
        ParseUser.logInInBackground(username, password, new LogInCallback() // Try to log the user in if they've used our app before
        {
            @Override
            public void done(ParseUser parseUser, ParseException e)
            {
                if (e != null) // Need to create a new user
                {
                    final ParseUser user = new ParseUser();
                    user.setEmail(username);
                    user.setUsername(username);
                    user.setPassword(password);
                    user.put("appCompany", LocalUser.getInstance().getParentCompany());
                    user.put("fullName", etFirstName.getText().toString() + " " + etLastName.getText().toString());
                    user.signUpInBackground(new SignUpCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (getActivity() != null)
                            {
                                pDialog.dismiss();
                                if (e == null)
                                {
                                    updateUserData();
                                }
                                else
                                {
                                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
                }
                else
                {
                    if (getActivity() != null)
                        pDialog.dismiss();
                    updateUserData();
                }
            }
        });
    }
    public void updateUserData()
    {
        ParseQuery<ParseObject> userDataQuery = new ParseQuery<ParseObject>("UserData");
        userDataQuery.whereEqualTo("user", ParseUser.getCurrentUser());
        userDataQuery.whereEqualTo("appParentCompany", LocalUser.getInstance().getParentCompany());
        userDataQuery.getFirstInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                pDialog.dismiss();
                if (parseObject == null)
                {
                    ParseObject userData = new ParseObject("UserData");
                    userData.put("user", ParseUser.getCurrentUser());
                    userData.put("appParentCompany", LocalUser.getInstance().getParentCompany());
                    userData.put("rewardPoints", new Integer(0));
                    userData.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e == null)
                            {
                                if (getActivity() != null)
                                    onUserSignedIn();
                            }
                            else
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else
                {
                    onUserSignedIn();
                }
            }
        });
    }

    public void onLogInClicked()
    {
        Utils.hideKeyboard(getActivity());
        boolean validationError = false;
        StringBuilder validationErrorMessage = new StringBuilder(getResources().getString(R.string.error_intro));

        if (isEmpty(etEmailLogIn))
        {
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.error_blank_email));
        }

        if (isEmpty(etPasswordLogin))
        {
            if (validationError)
            {
                validationErrorMessage.append(getResources().getString(R.string.error_join));
            }
            validationError = true;
            validationErrorMessage.append(getResources().getString(R.string.error_blank_password));
        }

        validationErrorMessage.append(getResources().getString(R.string.error_end));
        if (validationError)
        {
            Toast.makeText(getActivity(), validationErrorMessage.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        pDialog = Utils.createProgressDialog(getActivity());
        pDialog.show();

        String email = etEmailLogIn.getText().toString();
        String password = etPasswordLogin.getText().toString();
        ParseUser.logInInBackground(email, password, new LogInCallback()
        {
            @Override
            public void done(final ParseUser user, final ParseException logInException)
            {
                if (pDialog.isShowing())
                    pDialog.dismiss();
                if (logInException == null)
                {
                    user.getRelation("parentCompanies").add(LocalUser.getInstance().getParentCompany());
                    user.saveInBackground();

                    updateUserData();
                }
                else
                {
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), logInException.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void onUserSignedIn()
    {
        Toast.makeText(getActivity(), "Successfully logged in.", Toast.LENGTH_LONG).show();
        onLogInListener.onLogInToggled(false);
    }

    public void onForgotPasswordClicked()
    {
        View resetPasswordView = View.inflate(getActivity(), R.layout.forgot_password_layout, null);
        final EditText resetEmailEditText = (EditText) resetPasswordView.findViewById(R.id.forgot_password_edit_text);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Forgot Password?").setView(resetPasswordView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                final String email = resetEmailEditText.getText().toString();
                if (!email.equals(""))
                {
                    ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            if (e == null)
                            {
                                if (getActivity() != null)
                                {
                                    Toast.makeText(getActivity(), "Sent reset email to: " + email, Toast.LENGTH_LONG).show();
                                }
                            }
                            else
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else
                {
                    Toast.makeText(getActivity(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId)
    {
        switch (checkedId)
        {
            case R.id.rb_signup:
                signUpLayout.setVisibility(View.VISIBLE);
                loginLayout.setVisibility(View.GONE);
                break;
            case R.id.rb_login:
                loginLayout.setVisibility(View.VISIBLE);
                signUpLayout.setVisibility(View.GONE);
                break;
        }
    }

    private boolean isEmpty(EditText etText)
    {
        return etText.getText().toString().trim().length() <= 0;
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        if (pDialog != null && pDialog.isShowing())
            pDialog.cancel();
    }
}
