package unibo.ing.warp.view;

/**
 * Created with IntelliJ IDEA.
 * User: lorenzodonini
 * Date: 10/11/13
 * Time: 17:45
 */

/**
 * Public interface needed by the caller in order to receive a result when the
 * DialogFragment is dismissed. The methods need to be implemented manually.
 * To be able to receive these callbacks, the class which creates the DialogFragment needs
 * to either implement this interface, or to call the setDialogFragmentListener(), creating
 * a new callListener at runtime, and therefore implementing the onDialogPositiveClick and
 * onDialogNegativeClick methods inline.
 */
public interface DialogFragmentListener {
    public void onDialogPositiveClick(PasswordDialogFragment dialog);
    public void onDialogNegativeClick(PasswordDialogFragment dialog);
}
