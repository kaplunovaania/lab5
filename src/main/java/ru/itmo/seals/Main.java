package ru.itmo.seals;

import ru.itmo.seals.service.Command;
import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
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
            if (input.equals("exit")) {
                System.out.println("Выход.");
                break;
            }
            if (input.equals("help")) {
                printHelp();
                continue;
            }

            try {
                processCommand(input, handler, currentUsername, scanner);
            } catch (Exception e) {
                System.err.println("Ошибка: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void processCommand(String input, Command handler, String currentUsername, Scanner scanner) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "task_add" -> {
                if (parts.length == 1) {
                    handler.taskAddInteractive(scanner, currentUsername);
                } else {
                    String priority = "MEDIUM";
                    String deadline = "";

                    boolean[] isParam = new boolean[parts.length];
                    boolean foundPriority = false;
                    boolean foundDeadline = false;

                    for (int i = parts.length - 1; i >= 1; i--) {
                        String part = parts[i];
                        String partUpper = part.toUpperCase();

                        if (!foundDeadline && part.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            deadline = part;
                            isParam[i] = true;
                            foundDeadline = true;
                        }
                        else if (!foundPriority &&
                                (partUpper.equals("LOW") || partUpper.equals("MEDIUM") || partUpper.equals("HIGH"))) {
                            priority = partUpper;
                            isParam[i] = true;
                            foundPriority = true;
                        }
                    }

                    StringBuilder textBuilder = new StringBuilder();
                    for (int i = 1; i < parts.length; i++) {
                        if (!isParam[i]) {
                            if (textBuilder.length() > 0) {
                                textBuilder.append(" ");
                            }
                            textBuilder.append(parts[i]);
                        }
                    }
                    String text = textBuilder.toString();

                    handler.taskAdd(text, priority, deadline, currentUsername);
                }
            }
            case "task_list" -> handler.taskList();
            case "task_show" -> {
                if (parts.length < 2) {
                    System.err.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskShow(taskId);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }
            case "task_update" -> {
                if (parts.length < 4) {
                    System.err.println("Ошибка: task_update <id> <field>=<value>");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    String[] fieldVal = parts[2].split("=", 2);
                    if (fieldVal.length != 2) {
                        System.err.println("Ошибка: формат field=value");
                        return;
                    }
                    handler.taskUpdate(taskId, fieldVal[0], fieldVal[1]);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }
            case "task_done" -> {
                if (parts.length < 2) {
                    System.err.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskDone(taskId);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }
            case "task_assign" -> {
                if (parts.length < 3) {
                    System.err.println("Ошибка: task_assign <task_id> <username>");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskAssign(taskId, parts[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }
            case "task_delete" -> {
                if (parts.length < 2) {
                    System.err.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.taskDelete(taskId);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }
            case "check_add" -> {
                if (parts.length < 3) {
                    System.err.println("Ошибка: check_add <task_id> <text>");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.checkAdd(taskId, parts[2]);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }
            case "check_list" -> {
                if (parts.length < 2) {
                    System.err.println("Ошибка: укажите task_id");
                    return;
                }
                try {
                    long taskId = Long.parseLong(parts[1]);
                    handler.checkList(taskId);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }
            case "check_toggle" -> {
                if (parts.length < 2) {
                    System.err.println("Ошибка: укажите item_id");
                    return;
                }
                try {
                    long itemId = Long.parseLong(parts[1]);
                    handler.checkToggle(itemId);
                } catch (NumberFormatException e) {
                    System.err.println("Ошибка: id должен быть числом");
                }
            }

            default -> System.err.println("Ошибка: неизвестная команда '" + command + "'");
        }
    }

    private static void printHelp() {
        System.out.println("""
            Команды:
              task_add <text> [priority] [deadline]        - Создать задачу
              task_list                                  - Список задач
              task_show <task_id>                        - Показать задачу
              task_update <id> <field>=<value>           - Обновить задачу
              task_done <task_id>                        - Завершить задачу
              task_assign <task_id> <username>           - Назначить задачу
              task_delete <task_id>                      - Удалить задачу
              check_add <task_id> <text>                 - Добавить пункт чеклиста
              check_list <task_id>                       - Список пунктов чеклиста
              check_toggle <item_id>                     - Переключить пункт
              exit                                       - Выход
              help                                       - Эта справка
            """);
    }
}
