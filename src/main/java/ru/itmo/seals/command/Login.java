package ru.itmo.seals.command;

import ru.itmo.seals.service.UserService;
import java.util.Scanner;

public class Login extends Command {
    private final UserService userService;

    public Login(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void execute(String[] args, Scanner scanner) {
        if (args.length < 2) {
            System.out.println("login <login> <password>");
            return;
        }

        String login = args[0];
        String password = args[1];

        userService.login(login, password);
    }
}