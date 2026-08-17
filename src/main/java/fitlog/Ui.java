package fitlog;

/**
 * Receives categorised feedback from FitLog operations.
 */
public interface Ui {
    /**
     * Displays neutral information.
     *
     * @param message the information to display
     */
    void showInfo(String message);

    /**
     * Displays an example command using presentation distinct from normal information.
     *
     * @param message the labelled example to display
     */
    void showExample(String message);

    /**
     * Displays confirmation of a successful mutation.
     *
     * @param message the confirmation to display
     */
    void showSuccess(String message);

    /**
     * Displays a validation or command error.
     *
     * @param message the error to display
     */
    void showError(String message);

    /**
     * Displays a non-fatal warning.
     *
     * @param message the warning to display
     */
    void showWarning(String message);

    /**
     * Displays a personal-record notification.
     *
     * @param message the personal-record message to display
     */
    void showPersonalRecord(String message);
}
