package ru.itmo.seals.ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import ru.itmo.seals.service.UserService;

public class LoginController {

    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private UserService userService;
    private Stage stage;

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (userService.login(login, password)) {
            messageLabel.setText("");
            stage.close();
        } else {
            messageLabel.setText("Invalid login or password");
        }
    }

    @FXML
    private void handleRegister() {
        String login = loginField.getText();
        String password = passwordField.getText();

        if (userService.register(login, password)) {
            messageLabel.setText("Registration successful! Please login.");
        } else {
            messageLabel.setText("Registration failed");
        }
    }
}