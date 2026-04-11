package ru.itmo.seals.command;

import ru.itmo.seals.service.UserService;
import java.util.Scanner;

public class Logout extends Command {
    private final UserService userService;

    public Logout(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        userService.logout();
    }
}
