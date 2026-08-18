package fitlog;

import java.util.ArrayList;
import java.util.List;

/** Captures all UI feedback so command tests can assert messages without a real UI. */
final class TestUi implements Ui {
    private final List<String> messages = new ArrayList<>();

    @Override
    public void showInfo(String message) {
        messages.add(message);
    }

    @Override
    public void showExample(String message) {
        messages.add(message);
    }

    @Override
    public void showSuccess(String message) {
        messages.add(message);
    }

    @Override
    public void showError(String message) {
        messages.add(message);
    }

    @Override
    public void showWarning(String message) {
        messages.add(message);
    }

    @Override
    public void showPersonalRecord(String message) {
        messages.add(message);
    }

    List<String> messages() {
        return List.copyOf(messages);
    }

    String onlyMessage() {
        if (messages.size() != 1) {
            throw new IllegalStateException("Expected exactly one message but received " + messages.size());
        }
        return messages.get(0);
    }

    void clear() {
        messages.clear();
    }
}
