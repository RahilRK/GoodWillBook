package com.hksofttronix.goodwillbook;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.beautycoder.pflockscreen.PFFLockScreenConfiguration;
import com.beautycoder.pflockscreen.fragments.PFLockScreenFragment;
import com.beautycoder.pflockscreen.security.PFResult;
import com.beautycoder.pflockscreen.security.PFSecurityManager;
import com.beautycoder.pflockscreen.security.callbacks.PFPinCodeHelperCallback;
import com.beautycoder.pflockscreen.viewmodels.PFPinCodeViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.hksofttronix.goodwillbook.AddBusiness.Addbusiness;
import com.hksofttronix.goodwillbook.MainActivity.MainActivity;

public class LockScreen extends AppCompatActivity {

    String TAG = this.getClass().getSimpleName();
    AppCompatActivity activity = LockScreen.this;

    Globalclass globalclass;

    String get_goto = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);
        getSupportActionBar().hide();

        globalclass = Globalclass.getInstance(activity);

        get_goto = getIntent().getStringExtra("goto");

        if(get_goto.equalsIgnoreCase("createcode"))
        {
            manageORcreatecode();
        }
        else
        {
            showLockScreenFragment();
        }
    }

    void showLockScreenFragment() {
        new PFPinCodeViewModel().isPinCodeEncryptionKeyExist().observe(
                this,
                new Observer<PFResult<Boolean>>() {
                    @Override
                    public void onChanged(@Nullable PFResult<Boolean> result) {
                        if (result == null) {
                            return;
                        }
                        if (result.getError() != null) {
                            globalclass.toast_short("Can not get pin code info");
                            return;
                        }
                        showLockScreenFragment(result.getResult());
                    }
                }
        );
    }

    void showLockScreenFragment(boolean isPinExist) {
        final PFFLockScreenConfiguration.Builder builder = new PFFLockScreenConfiguration.Builder(this)
                .setTitle(isPinExist ? "Unlock with your pin code" : "Create Code")
                .setCodeLength(4)
                .setLeftButton("Can't remeber")
                .setNewCodeValidation(true)
                .setNewCodeValidationTitle("Please input code again")
                .setUseFingerprint(false);
        final PFLockScreenFragment fragment = new PFLockScreenFragment();

        fragment.setOnLeftButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                globalclass.toast_short("Left button pressed");
                showAskVerificationDialogue(activity,"We will send you otp to verify you mobilenumber!");
            }
        });

        builder.setMode(isPinExist
                ? PFFLockScreenConfiguration.MODE_AUTH
                : PFFLockScreenConfiguration.MODE_CREATE);
        if (isPinExist) {
            fragment.setEncodedPinCode(globalclass.getStringData(Globalclass.lockcode));
            fragment.setLoginListener(mLoginListener);
        }

        fragment.setConfiguration(builder.build());
        fragment.setCodeCreateListener(mCodeCreateListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_view, fragment).commit();

    }

    final PFLockScreenFragment.OnPFLockScreenCodeCreateListener mCodeCreateListener =
            new PFLockScreenFragment.OnPFLockScreenCodeCreateListener() {
                @Override
                public void onCodeCreated(String encodedCode) {
                    globalclass.toast_short("App lock has been created successfully!");
                    globalclass.setStringData(Globalclass.lockcode,encodedCode);

                    if(get_goto.equalsIgnoreCase("createcode"))
                    {
                        startActivity(new Intent(activity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                    else
                    {
                        onBackPressed();
                    }
                }

                @Override
                public void onNewCodeValidationFailed() {
                    globalclass.toast_short("Code should be same as before!");
                }
            };

    final PFLockScreenFragment.OnPFLockScreenLoginListener mLoginListener =
            new PFLockScreenFragment.OnPFLockScreenLoginListener() {

                @Override
                public void onCodeInputSuccessful() {
                    if(get_goto.equalsIgnoreCase("MainActivity"))
                    {
                        startActivity(new Intent(activity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                    else if(get_goto.equalsIgnoreCase("Addbusiness"))
                    {
                        startActivity(new Intent(activity, Addbusiness.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                    else if(get_goto.equalsIgnoreCase("managecode"))
                    {
                        manageORcreatecode();
                    }
                }

                @Override
                public void onFingerprintSuccessful() {
                    globalclass.toast_short("Fingerprint successfull");
                    if(get_goto.equalsIgnoreCase("MainActivity"))
                    {
                        startActivity(new Intent(activity, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                    else
                    {
                        startActivity(new Intent(activity, Addbusiness.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    }
                }

                @Override
                public void onPinLoginFailed() {
                    globalclass.toast_short("Incorrect code!");
                }

                @Override
                public void onFingerprintLoginFailed() {
                    globalclass.toast_short("Fingerprint failed");
                }
            };

    void manageORcreatecode()
    {
        PFSecurityManager.getInstance().getPinCodeHelper().delete(new PFPinCodeHelperCallback<Boolean>() {
            @Override
            public void onResult(PFResult<Boolean> result)
            {
                globalclass.toast_long("Set new app lock!");
                globalclass.setStringData(Globalclass.lockcode,"");
                showLockScreenFragment(false); //create code again
            }
        });
    }

    void showAskVerificationDialogue(final Activity activity, String title)
    {
        new MaterialAlertDialogBuilder(activity, R.style.RoundShapeTheme)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("Send otp", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        globalclass.setStringData("from",TAG);
                        Intent intent = new Intent(activity, VerifyOtp.class);
                        intent.putExtra("mobilenumber",globalclass.getStringData("mobilenumber"));
                        startActivity(intent);
                        finish();
                    }
                })
                .setNeutralButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                })
                .show();
    }
}
