package org.example;

import org.example.dao.DepartmentDAO;
import org.example.dao.EmployeeDAO;
import org.example.models.Department;
import org.example.models.Employee;
import org.example.util.*;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws SQLException {
        Scanner scanner = new Scanner(System.in);
        DepartmentDAO departmentDAO = new DepartmentDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();

        while (true) {
            System.out.println("\n================== ГОЛОВНЕ МЕНЮ ==================");
            System.out.println("1 - Вивести інформацію про БД");
            System.out.println("2 - Додати департамент");
            System.out.println("3 - Додати співробітника");
            System.out.println("4 - Редагувати департамент");
            System.out.println("5 - Редагувати співробітника");
            System.out.println("6 - Видалити співробітника");
            System.out.println("7 - Видалити департамент (з усіма співробітниками)");
            System.out.println("8 - Отримати всіх співробітників департаменту");
            System.out.println("9 - Перевести співробітника в інший департамент");
            System.out.println("10 - Перевести кількох співробітників (Batch Update)");
            System.out.println("11 - Показати рейтинг співробітників за зарплатою");
            System.out.println("12 - Завантажити 10 000 співробітників (звичайний INSERT)");
            System.out.println("13 - Завантажити 10 000 співробітників (batch INSERT)");
            System.out.println("14 - Пагінація (перегляд по 20 співробітників)");
            System.out.println("15 - Резервне копіювання (Backup в CSV)");
            System.out.println("16 - Відновлення з CSV (Restore)");

            System.out.println("0 - Вихід");
            System.out.print("\nОберіть опцію: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    DatabaseUtils.printDatabaseInfo();
                    break;
                case 2:
                    addDepartmentMenu(scanner, departmentDAO);
                    break;
                case 3:
                    addEmployeeMenu(scanner, employeeDAO);
                    break;
                case 4:
                    updateDepartmentMenu(scanner, departmentDAO);
                    break;
                case 5:
                    updateEmployeeMenu(scanner, employeeDAO);
                    break;
                case 6:
                    deleteEmployeeMenu(scanner, employeeDAO);
                    break;
                case 7:
                    deleteDepartmentMenu(scanner, departmentDAO);
                    break;
                case 8:
                    getEmployeesByDepartmentMenu(scanner, departmentDAO);
                    break;
                case 9:
                    transferEmployee(scanner, employeeDAO);
                    break;
                case 10:
                    transferMultipleEmployees(scanner, employeeDAO);
                    break;
                case 11:
                    showEmployeeSalaryRanking(employeeDAO);
                    break;
                case 12:
                    System.out.println("Додавання 10 000 співробітників (звичайний INSERT)...");
                    EmployeeLoader.insertEmployeesIndividually(10_000);
                    break;
                case 13:
                    System.out.println("Додавання 10 000 співробітників (batch INSERT)...");
                    EmployeeLoader.insertEmployeesBatch(10_000);
                    break;
                case 14:
                    paginateEmployees(scanner);
                    break;
                case 15:
                    System.out.println("Створення резервної копії...");
                    DatabaseBackup.backupToCSV();
                    break;
                case 16:
                    System.out.println("Відновлення з резервної копії...");
                    DatabaseRestore.restoreFromCSV();
                    break;
                case 0:
                    System.out.println("Вихід...");
                    scanner.close();
                    return;
                default:
                    System.out.println("❌ Невідома команда, спробуйте ще раз.");
            }
        }
    }

    private static void deleteEmployeeMenu(Scanner scanner, EmployeeDAO employeeDAO) throws SQLException {
        System.out.print("Введіть ID співробітника, якого потрібно видалити: ");
        int id = scanner.nextInt();

        employeeDAO.deleteEmployee(id);
        System.out.println("✅ Співробітник видалений!");
    }

    private static void showEmployeeSalaryRanking(EmployeeDAO employeeDAO) {
        try {
            List<String> ranking = employeeDAO.getEmployeeSalaryRanking();
            System.out.println("\n🏆 Рейтинг співробітників за зарплатою:");
            ranking.forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("❌ Помилка: " + e.getMessage());
        }
    }

    private static void addDepartmentMenu(Scanner scanner, DepartmentDAO departmentDAO) throws SQLException {
        System.out.println("\n📝 Додавання нового департаменту (введіть `0` для виходу)");

        System.out.print("Введіть назву департаменту: ");
        String name = scanner.nextLine();
        if (name.equals("0")) {
            System.out.println("🔙 Повернення в головне меню...");
            return;
        }

        System.out.print("Введіть бюджет департаменту: ");
        if (!scanner.hasNextDouble()) {
            System.out.println("❌ Помилка: потрібно ввести число. Операція скасована.");
            scanner.nextLine();
            return;
        }
        double budget = scanner.nextDouble();
        scanner.nextLine();

        departmentDAO.addDepartment(name, budget);
        System.out.println("✅ Департамент додано успішно!");
    }


    private static void addEmployeeMenu(Scanner scanner, EmployeeDAO employeeDAO) throws SQLException {
        System.out.println("\n📝 Додавання нового співробітника (введіть `0` для виходу)");

        // Ввод имени сотрудника
        System.out.print("Введіть ім'я співробітника: ");
        String name = scanner.nextLine();
        if (name.equals("0")) {
            System.out.println("🔙 Повернення в головне меню...");
            return;
        }

        // Ввод ID департамента
        System.out.print("Введіть ID департаменту: ");
        if (!scanner.hasNextInt()) {
            System.out.println("❌ Помилка: потрібно ввести число (ID департаменту). Операція скасована.");
            scanner.nextLine();
            return;
        }
        int departmentId = scanner.nextInt();
        if (departmentId == 0) {
            System.out.println("🔙 Повернення в головне меню...");
            return;
        }

        // Ввод зарплаты сотрудника
        System.out.print("Введіть зарплату співробітника: ");
        if (!scanner.hasNextDouble()) {
            System.out.println("❌ Помилка: потрібно ввести число (зарплата). Операція скасована.");
            scanner.nextLine();
            return;
        }
        double salary = scanner.nextDouble();
        scanner.nextLine();

        employeeDAO.addEmployee(name, salary, departmentId);
        System.out.println("✅ Співробітника додано успішно!");
    }

    private static void updateDepartmentMenu(Scanner scanner, DepartmentDAO departmentDAO) throws SQLException {
        System.out.print("Введіть ID департаменту, який потрібно оновити: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Введіть нову назву департаменту: ");
        String name = scanner.nextLine();
        System.out.print("Введіть новий бюджет департаменту: ");
        double budget = scanner.nextDouble();

        departmentDAO.updateDepartment(id,name,budget);
        System.out.println("✅ Департамент оновлено успішно!");
    }

    private static void updateEmployeeMenu(Scanner scanner, EmployeeDAO employeeDAO) throws SQLException {
        System.out.print("Введіть ID співробітника, якого потрібно оновити: ");
        int id = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Введіть нове ім'я співробітника: ");
        String name = scanner.nextLine();
        System.out.print("Введіть новий ID департаменту: ");
        int departmentId = scanner.nextInt();
        System.out.print("Введіть нову зарплату співробітника: ");
        double salary = scanner.nextDouble();

        employeeDAO.updateEmployee(id, name, salary, departmentId);
        System.out.println("✅ Інформацію про співробітника оновлено!");
    }

    private static void deleteDepartmentMenu(Scanner scanner, DepartmentDAO departmentDAO) throws SQLException {
        System.out.print("Введіть ID департаменту, який потрібно видалити: ");
        int id = scanner.nextInt();

        departmentDAO.deleteDepartment(id);
        System.out.println("✅ Департамент і всі його співробітники видалені!");
    }

    private static void getEmployeesByDepartmentMenu(Scanner scanner, DepartmentDAO departmentDAO) throws SQLException {
        System.out.print("Введіть ID департаменту, для якого потрібно вивести співробітників: ");
        int departmentId = scanner.nextInt();

        List<String> department = departmentDAO.getEmployeesByDepartment(departmentId);
        if (department.isEmpty()) {
            System.out.println("❌ У цьому департаменті немає співробітників.");
        } else {
            System.out.println("\n👥 Співробітники департаменту:");
            for (String dep : department) {
                System.out.println(dep);
            }
        }
    }

    private static void transferEmployee(Scanner scanner, EmployeeDAO employeeDAO) {
        System.out.print("Введіть ID співробітника: ");
        int employeeId = scanner.nextInt();
        System.out.print("Введіть ID нового департаменту: ");
        int newDepartmentId = scanner.nextInt();

        try {
            employeeDAO.transferEmployee(employeeId, newDepartmentId);
            System.out.println("✅ Співробітника переведено!");
        } catch (SQLException e) {
            System.out.println("❌ Помилка: " + e.getMessage());
        }
    }

    private static void transferMultipleEmployees(Scanner scanner, EmployeeDAO employeeDAO) {
        System.out.print("Скільки співробітників потрібно перевести? ");
        int count = scanner.nextInt();
        List<Integer> employeeIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            System.out.print("Введіть ID співробітника #" + (i + 1) + ": ");
            employeeIds.add(scanner.nextInt());
        }

        System.out.print("Введіть ID нового департаменту: ");
        int newDepartmentId = scanner.nextInt();

        try {
            employeeDAO.transferMultipleEmployees(employeeIds, newDepartmentId);
            System.out.println("✅ Успішний масовий перевід!");
        } catch (SQLException e) {
            System.out.println("❌ Помилка: " + e.getMessage());
        }
    }

    private static void paginateEmployees(Scanner scanner) {
        int page = 1;
        int pageSize = 20;

        while (true) {
            System.out.println("\nСторінка: " + page);
            List<String> employees = EmployeePagination.getEmployeesPage(page, pageSize);
            if (employees.isEmpty()) {
                System.out.println("❌ Більше немає співробітників.");
                break;
            }
            employees.forEach(System.out::println);

            System.out.println("\nНатисніть Enter для наступної сторінки або введіть `0` для виходу...");
            String input = scanner.nextLine();
            if (input.equals("0")) break;
            page++;
        }
    }
}
