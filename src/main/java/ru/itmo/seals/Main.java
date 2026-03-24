package ru.itmo.seals;

import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.command.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskCollectionManager taskManager = new TaskCollectionManager();
        ChecklistCollectionManager checklistManager = new ChecklistCollectionManager();
        Scanner scanner = new Scanner(System.in);

        Map<String, Command> commands = new HashMap<>();
        commands.put("task_add", new TaskAdd(taskManager));
        commands.put("task_list", new TaskList(taskManager));
        commands.put("task_show", new TaskShow(taskManager, checklistManager));
        commands.put("task_update", new TaskUpdate(taskManager));
        commands.put("task_done", new TaskDone(taskManager));
        commands.put("task_assign", new TaskAssign(taskManager));
        commands.put("task_delete", new TaskDelete(taskManager));
        commands.put("check_add", new CheckAdd(taskManager, checklistManager));
        commands.put("check_list", new CheckList(taskManager, checklistManager));
        commands.put("check_toggle", new CheckToggle(checklistManager));

        System.out.println("Task & Checklist Manager. Введите 'help' для списка команд.");

        label:
        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            switch (input) {
                case "":
                    continue;
                case "exit":
                case "quit":
                    break label;
                case "help":
                    printHelp();
                    continue;
            }

            String[] parts = input.split("\\s+", 10);
            String cmdName = parts[0].toLowerCase();
            Command command = commands.get(cmdName);

            if (command != null) {
                String[] cmdArgs = new String[parts.length - 1];
                if (parts.length > 1) {
                    System.arraycopy(parts, 1, cmdArgs, 0, parts.length - 1);
                }
                try {
                    command.execute(cmdArgs, scanner);
                } catch (Exception e) {
                    System.out.println("Oшибка: " + e.getMessage());
                }
            } else {
                System.out.println("Неизвестная команда: " + cmdName);
            }
        }
        scanner.close();
    }

    private static void printHelp() {
        System.out.println("""
            Команды:
              task_add [текст] [приоритет(LOW|HIGH|MEDIUM] [дедлайн]
              task_list
              task_show <id>
              task_update <id> <поле (text, priority, deadline)>=<значение>
              task_done <id>
              task_assign <id> <username>
              task_delete <id>
              check_add <id> <текст>
              check_list <id>
              check_toggle <id>
              exit
            """);
    }
}