package unibo.ing.warp.view;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import unibo.ing.warp.R;

/**
 * Created by cronic90 on 08/10/13.
 */
public class PasswordDialogFragment extends DialogFragment {
    private EditText passwordText;
    private DialogFragmentListener mListener;
    private String mKeyType;
    public final static String WPA_KEY = "WPA";
    public final static String WPA2_KEY = "WPA2";
    public final static String WEP_KEY = "WEP";
    private final static String TITLE = " key";

    public PasswordDialogFragment(String keyType)
    {
        mKeyType=keyType;
        setArguments(new Bundle());
    }

    /**
     * Getter method. Should be called only after a PositiveClick callback, performed
     * on the attached Listeners. Returns the String inside the EditText field, containing
     * the password entered by the user
     *
     * @return  Returns the password entered by the user inside the Dialog as a string
     */
    public String getPassword()
    {
        Editable text = passwordText.getText();
        return (text != null) ? text.toString() : "";
    }

    /**
     * Getter method. Usually called after a PositiveClick callback, performed on the
     * attached Listeners. Returns the current Key type the Dialog is asking a password for
     *
     * @return  Returns the key type associated to the password the user is entering
     */
    public String getKeyType()
    {
        return mKeyType;
    }

    /**
     * Setter method. Called in case the Dialog wasn't directly attached to an activity.
     * The user can decide to attach another class that is implementing the
     * DialogFragmentListener interface. Once attached, a listener will automatically
     * receive the proper callback method invocation once the Dialog triggers an event.
     *
     * @param listener  The class implementing the DialogFragmentListener interface
     */
    public void setDialogFragmentListener(DialogFragmentListener listener)
    {
        mListener=listener;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try{
            if(mListener==null)
            {
                mListener=(DialogFragmentListener)activity;
            }
        }
        catch(ClassCastException e)
        {
            /*Catching an exception caused by the calling activity, since this has
            not implemented the DialogFragmentListener. In case the calling class
            doesn't create a DialogFragmentListener inline, the fragment won't call
            any callback methods.
            */
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        int style = DialogFragment.STYLE_NORMAL;
        int theme = android.R.style.Theme_Holo_Dialog;
        setStyle(style,theme);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Button okButton, cancelButton;
        View view = inflater.inflate(R.layout.dialog_password, container, false);
        if(view == null)
        {
            return null;
        }

        passwordText = (EditText)view.findViewById(R.id.password_editText);
        final Editable text = passwordText.getText();
        passwordText.requestFocus();

        CheckBox showPasswordCheckbox = (CheckBox)view.findViewById(R.id.showPassword_checkbox);
        showPasswordCheckbox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b)
                    {
                if(compoundButton.isChecked())
                {
                    passwordText.setInputType(
                    InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    passwordText.setSelection((text != null)? text.length() : 0);
                }
                else
                {
                    passwordText.setInputType(129);
                    passwordText.setSelection((text != null)? text.length() : 0);
                }
            }
                      });

        okButton = (Button)view.findViewById(R.id.ok_btn);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener!=null)
                {
                    //We just call the callback method in case the listener is attached
                    mListener.onDialogPositiveClick(PasswordDialogFragment.this);
                }
            }
        });
        cancelButton = (Button)view.findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mListener!=null)
                {
                    //We just call the callback method in case the listener is attached
                    mListener.onDialogNegativeClick(PasswordDialogFragment.this);
                    dismiss();
                }
            }
        });

        if(getActivity()!=null)
        {
            getActivity().setTitle(mKeyType + TITLE);
        }
        return view;
    }
}
