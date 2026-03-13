package ru.itmo.seals;

import ru.itmo.seals.model.Task;
import ru.itmo.seals.model.TaskPriority;
import ru.itmo.seals.model.TaskStatus;
import ru.itmo.seals.service.Command;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;

import java.time.Instant;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        TaskCollectionManager taskManager = new TaskCollectionManager();
        ChecklistCollectionManager checklistManager = new ChecklistCollectionManager();
        Command handler = new Command(taskManager, checklistManager);

        Scanner scanner = new Scanner(System.in);
        String currentUsername = "SYSTEM";

        System.out.println("Task & Checklist Manager. Введите 'help' для списка команд.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) continue;
            if (input.equals("exit") || input.equals("quit")) {
                System.out.println("Выход...");
                break;
            }
            if (input.equals("help")) {
                printHelp();
                continue;
            }

            try {
                processCommand(input, handler, currentUsername, scanner);
            } catch (Exception e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void processCommand(String input, Command handler, String currentUsername, Scanner scanner) {
        String[] parts = input.split("\\s+", 10);
        String command = parts[0].toLowerCase();

        switch (command) {
            case "task_add" -> {
                if (parts.length == 1) {
                    handler.taskAddInteractive(scanner, currentUsername);
                } else {
                    String text = parts[1];
                    String priority = parts.length > 2 ? parts[2] : "MEDIUM";
                    String deadline = parts.length > 3 ? parts[3] : "";
                    handler.taskAdd(text, priority, deadline, currentUsername);
                }
            }
            case "task_list" -> handler.taskList();
            case "task_show" -> {
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskShow(taskId);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }
            case "task_update" -> {
                if (parts.length < 4) {
                    System.out.println("Ошибка: task_update <id> <field>=<value>");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    String[] fieldVal = parts[2].split("=", 2);
                    if (fieldVal.length != 2) {
                        System.out.println("Ошибка: формат field=value");
                        return;
                    }
                    handler.taskUpdate(taskId, fieldVal[0], fieldVal[1]);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }
            case "task_done" -> {
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskDone(taskId);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }
            case "task_assign" -> {
                if (parts.length < 3) {
                    System.out.println("Ошибка: task_assign <task_id> <username>");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskAssign(taskId, parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }
            case "task_delete" -> {
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskDelete(taskId);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }
            case "check_add" -> {
                if (parts.length < 3) {
                    System.out.println("Ошибка: check_add <task_id> <text>");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.checkAdd(taskId, parts[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }
            case "check_list" -> {
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.checkList(taskId);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }
            case "check_toggle" -> {
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите item_id");
                    return;
                }
                try {
                    long itemId = Long.parseLong(parts[1]);
                    handler.checkToggle(itemId);
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка: id должен быть числом");
                }
            }

            default -> System.out.println("Ошибка: неизвестная команда '" + command + "'");
        }
    }

    private static void printHelp() {
        System.out.println("""
            Команды:
              task_add <text> [priority] [deadline]     - Создать задачу
              task_list                                  - Список задач
              task_show <task_id>                        - Показать задачу
              task_update <id> <field>=<value>           - Обновить задачу
              task_done <task_id>                        - Завершить задачу
              task_assign <task_id> <username>           - Назначить задачу
              task_delete <task_id>                      - Удалить задачу
              check_add <task_id> <text>                 - Добавить пункт чеклиста
              check_list <task_id>                       - Список пунктов чеклиста
              check_toggle <item_id>                     - Переключить пункт
              exit / quit                                - Выход
              help                                       - Эта справка
            """);
    }
}
