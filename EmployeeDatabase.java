package milestonepayroll;

import java.io.*;
import java.util.*;

public class EmployeeDatabase {
    private static final String FILE_PATH = "src/milestonepayroll/employees.csv";

    public static void addEmployee(String id, String birthday, String position, double hourlyRate) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(id + "," + birthday + "," + position + "," + hourlyRate);
            bw.newLine();
        } catch (IOException e) {
            System.out.println("Error writing employee data.");
        }
    }

    public static List<String[]> loadEmployees() {
        List<String[]> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line.split(","));
            }
        } catch (IOException e) {
            System.out.println("Error loading employees.");
        }
        return list;
    }

    public static boolean deleteEmployee(String id) {
        List<String[]> all = loadEmployees();
        boolean removed = all.removeIf(e -> e[0].equals(id));
        if (removed) saveAll(all);
        return removed;
    }

    public static boolean updateEmployee(String id, String[] updated) {
        List<String[]> all = loadEmployees();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i)[0].equals(id)) {
                all.set(i, updated);
                saveAll(all);
                return true;
            }
        }
        return false;
    }

    private static void saveAll(List<String[]> list) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (String[] e : list) {
                bw.write(String.join(",", e));
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving employees.");
        }
    }
}

