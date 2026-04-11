package ru.itmo.seals;

import ru.itmo.seals.service.TaskCollectionManager;
import ru.itmo.seals.service.ChecklistCollectionManager;
import ru.itmo.seals.command.*;
import ru.itmo.seals.storage.FileStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import ru.itmo.seals.service.UserService;


public class Main {
    public static void main(String[] args) {
        TaskCollectionManager taskManager = new TaskCollectionManager();
        ChecklistCollectionManager checklistManager = new ChecklistCollectionManager();
        FileStorage storage = new FileStorage();
        UserService userService = new UserService("users.json");

        if (args.length > 0) {
            System.out.println("Загрузка из " + args[0]);
            storage.load(args[0], taskManager, checklistManager);
        }
        Scanner scanner = new Scanner(System.in);

        Map<String, Command> commands = new HashMap<>();
        commands.put("task_add", new TaskAdd(taskManager, userService));
        commands.put("task_list", new TaskList(taskManager));
        commands.put("task_show", new TaskShow(taskManager, checklistManager));
        commands.put("task_update", new TaskUpdate(taskManager, userService));
        commands.put("task_done", new TaskDone(taskManager, userService));
        commands.put("task_assign", new TaskAssign(taskManager, userService));
        commands.put("task_delete", new TaskDelete(taskManager, userService));
        commands.put("check_add", new CheckAdd(taskManager, checklistManager, userService));
        commands.put("check_list", new CheckList(taskManager, checklistManager));
        commands.put("check_toggle", new CheckToggle(taskManager, checklistManager, userService));
        commands.put("save", new Save(taskManager, checklistManager));
        commands.put("load", new Load(taskManager, checklistManager));
        commands.put("register", new Register(userService));
        commands.put("login", new Login(userService));
        commands.put("logout", new Logout(userService));

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
        task_add [текст] [приоритет] [дедлайн (ГГГГ-ММ-ЧЧ]    - Создать новую задачу
        task_list                                             - Показать список всех задач
        task_show <id>                                        - Показать детали задачи
        task_update <id> <поле>=<значение>                    - Изменить поле задачи
        task_done <id>                                        - Отметить задачу выполненной
        task_assign <id> <username>                           - Назначить исполнителя
        task_delete <id>                                      - Удалить задачу
        check_add <task_id> <текст>                           - Добавить пункт чеклиста
        check_list <task_id>                                  - Показать чеклист задачи
        check_toggle <item_id>                                - Переключить статус пункта
        save <путь>                                           - Сохранить данные в файл JSON
        load <путь>                                           - Загрузить данные из файла JSON
        help                                                  - Показать помощь
        exit                                                  - Выход из программы
        register <login> <password>                           - Регистрация нового пользователя
        login <login> <password>                              - Вход в систему
        logout                                               1 - Выход из системы
        """);
    }
}