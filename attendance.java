/*
 * ============================================================
 *  Project Title : Attendance Tracker (PS No. 2)
 *  Student       : Mohammad Ayan
 *  ERP ID        : RU-25-10828
 *  Course        : B.Tech Computer Science & Engineering with Data Science
 *  Subject       : Data Structures and Algorithms (DSA)
 *  Language      : Java (JDK 8 or above), console application
 * ============================================================
 *
 *  Data structures used:
 *    1. int[][] attendance           -> rows = students, columns = days
 *                                       1 = Present, 0 = Absent, -1 = Not recorded
 *    2. HashMap<String, Integer>     -> roll number -> row index in the arrays
 *    3. String[] rollNumbers         -> roll number of each student (by row)
 *    4. String[] studentNames        -> name of each student (by row)
 *
 *  Data file: attendance.txt (loaded at start, saved on Exit)
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

public class AttendanceTracker {

    // ---------------------------------------------------------
    // Constants (change MAX_STUDENTS / MAX_DAYS to resize arrays)
    // ---------------------------------------------------------
    static final int MAX_STUDENTS = 100;        // maximum number of students (rows)
    static final int MAX_DAYS = 100;            // maximum number of days (columns)

    static final int PRESENT = 1;               // value stored for Present
    static final int ABSENT = 0;                // value stored for Absent
    static final int NOT_RECORDED = -1;         // value stored when nothing is recorded

    static final int FIRST_ROLL_NUMBER = 101;   // first roll number is R101
    static final String DATA_FILE = "attendance.txt";

    // ---------------------------------------------------------
    // Main data (all stored in arrays + one HashMap)
    // ---------------------------------------------------------
    static String[] rollNumbers = new String[MAX_STUDENTS];     // e.g. "R101"
    static String[] studentNames = new String[MAX_STUDENTS];    // e.g. "Karan"
    static int[][] attendance = new int[MAX_STUDENTS][MAX_DAYS]; // 1 / 0 / -1

    // Key = roll number, Value = row index in the arrays above
    static HashMap<String, Integer> rollToRow = new HashMap<String, Integer>();

    static int studentCount = 0;                    // how many students are stored
    static int totalDays = 0;                       // highest day number recorded so far
    static int nextRollNumber = FIRST_ROLL_NUMBER;  // number used for the next new roll

    static Scanner scanner = new Scanner(System.in);

    // =========================================================
    // main()
    // =========================================================
    public static void main(String[] args) {
        initializeAttendance();   // fill the whole 2D array with -1
        loadData();               // load attendance.txt if it exists

        boolean running = true;
        while (running) {
            displayMenu();
            String choice = readLine("Enter your choice: ");

            if (choice == null) {
                // Input ended (for example, piped input finished): save and stop.
                System.out.println();
                saveAndReport();
                break;
            }

            switch (choice) {
                case "1":
                    addStudent();
                    break;
                case "2":
                    markAttendance();
                    break;
                case "3":
                    sortByPercentage();
                    break;
                case "4":
                    viewPercentage();
                    break;
                case "5":
                    saveAndReport();
                    System.out.println("Thank you for using Attendance Tracker. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please enter a number from 1 to 5.");
            }
        }
        scanner.close();
    }

    // =========================================================
    // displayMenu()
    // =========================================================
    static void displayMenu() {
        System.out.println();
        System.out.println("========== ATTENDANCE TRACKER ==========");
        System.out.println();
        System.out.println("1. Add Student");
        System.out.println("2. Mark Attendance");
        System.out.println("3. Sort Students by Attendance Percentage");
        System.out.println("4. View Attendance Percentage");
        System.out.println("5. Exit");
        System.out.println();
        System.out.println("========================================");
    }

    // =========================================================
    // readLine(): prints a prompt and returns the trimmed line.
    // Returns null when there is no more input.
    // =========================================================
    static String readLine(String prompt) {
        System.out.print(prompt);
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }

    // =========================================================
    // initializeAttendance(): set EVERY cell to -1 (Not recorded).
    // Java's default int value is 0, which would wrongly mean "Absent".
    // =========================================================
    static void initializeAttendance() {
        for (int row = 0; row < MAX_STUDENTS; row++) {
            for (int day = 0; day < MAX_DAYS; day++) {
                attendance[row][day] = NOT_RECORDED;
            }
        }
    }

    // =========================================================
    // generateRollNumber(): returns R101, R102, R103, ...
    // The counter only moves forward, so a roll number is never reused.
    // =========================================================
    static String generateRollNumber() {
        String roll = "R" + nextRollNumber;
        while (rollToRow.containsKey(roll)) {   // extra safety against duplicates
            nextRollNumber++;
            roll = "R" + nextRollNumber;
        }
        nextRollNumber++;
        return roll;
    }

    // =========================================================
    // addStudent(): Option 1
    // =========================================================
    static void addStudent() {
        String name = readLine("Enter student name: ");
        if (name == null) {
            return;
        }

        if (name.isEmpty()) {
            System.out.println("Student name cannot be blank.");
            return;
        }
        if (name.indexOf('|') >= 0) {
            System.out.println("Student name cannot contain the '|' character.");
            return;
        }
        if (studentCount >= MAX_STUDENTS) {
            System.out.println("Cannot add student. Maximum capacity of "
                    + MAX_STUDENTS + " students is full.");
            return;
        }

        String roll = generateRollNumber();
        int row = studentCount;

        rollNumbers[row] = roll;
        studentNames[row] = name;
        // The row is already filled with -1, but we reset it to be safe.
        for (int day = 0; day < MAX_DAYS; day++) {
            attendance[row][day] = NOT_RECORDED;
        }
        rollToRow.put(roll, row);
        studentCount++;

        System.out.println("Student added. Roll: " + roll + " | " + name);
    }

    // =========================================================
    // searchStudent(): HashMap lookup, returns row index or -1.
    // =========================================================
    static int searchStudent(String roll) {
        Integer row = rollToRow.get(roll);   // average O(1)
        if (row == null) {
            return -1;
        }
        return row;
    }

    // =========================================================
    // markAttendance(): Option 2
    // =========================================================
    static void markAttendance() {
        String roll = readLine("Enter roll number: ");
        if (roll == null) {
            return;
        }
        roll = roll.toUpperCase();

        int row = searchStudent(roll);
        if (row == -1) {
            System.out.println("Roll number " + roll + " not found.");
            return;                              // nothing is modified
        }

        String dayText = readLine("Enter day number (1-" + MAX_DAYS + "): ");
        if (dayText == null) {
            return;
        }
        int day;
        try {
            day = Integer.parseInt(dayText);
        } catch (NumberFormatException e) {
            System.out.println("Invalid day number. Enter a whole number from 1 to " + MAX_DAYS + ".");
            return;
        }
        if (day < 1 || day > MAX_DAYS) {
            System.out.println("Invalid day number. Enter a whole number from 1 to " + MAX_DAYS + ".");
            return;
        }

        String status = readLine("Enter status (P = Present, A = Absent): ");
        if (status == null) {
            return;
        }
        status = status.toUpperCase();

        int value;
        if (status.equals("P")) {
            value = PRESENT;
        } else if (status.equals("A")) {
            value = ABSENT;
        } else {
            System.out.println("Invalid status. Enter only P (Present) or A (Absent).");
            return;
        }

        int column = day - 1;                       // Day 1 is column 0
        boolean alreadyRecorded = attendance[row][column] != NOT_RECORDED;

        // The cell is simply overwritten, so an updated day is never counted twice.
        attendance[row][column] = value;

        if (day > totalDays) {
            totalDays = day;                        // remember the highest day used
        }

        System.out.println("Attendance marked for Day " + day + ".");
        if (alreadyRecorded) {
            System.out.println("(Earlier record for this day was replaced, not counted twice.)");
        }
    }

    // =========================================================
    // countPresent() / countRecorded(): count only 1 and 0, skip -1
    // =========================================================
    static int countPresent(int row) {
        int present = 0;
        for (int day = 0; day < totalDays; day++) {
            if (attendance[row][day] == PRESENT) {
                present++;
            }
        }
        return present;
    }

    static int countRecorded(int row) {
        int recorded = 0;
        for (int day = 0; day < totalDays; day++) {
            if (attendance[row][day] != NOT_RECORDED) {   // 0 or 1 only
                recorded++;
            }
        }
        return recorded;
    }

    // =========================================================
    // calculatePercentage(): (present / recorded) * 100
    // Returns 0.0 when nothing is recorded (avoids division by zero).
    // =========================================================
    static double calculatePercentage(int row) {
        int recorded = countRecorded(row);
        if (recorded == 0) {
            return 0.0;
        }
        int present = countPresent(row);
        return (present * 100.0) / recorded;
    }

    // Formats to at most two decimals: 100.0%, 50.0%, 66.67%
    static String formatPercentage(double percentage) {
        String text = String.format(Locale.US, "%.2f", percentage);
        if (text.endsWith("0")) {
            text = text.substring(0, text.length() - 1);   // 100.00 -> 100.0
        }
        return text + "%";
    }

    // Builds "R101 | Karan | Present: 1/1 | 100.0%" for one student row.
    static String describeStudent(int row) {
        int recorded = countRecorded(row);
        String line = rollNumbers[row] + " | " + studentNames[row]
                + " | Present: " + countPresent(row) + "/" + recorded + " | ";
        if (recorded == 0) {
            return line + "No attendance recorded";
        }
        return line + formatPercentage(calculatePercentage(row));
    }

    // =========================================================
    // sortByPercentage(): Option 3 (Bubble Sort, descending)
    // =========================================================
    static void sortByPercentage() {
        if (studentCount == 0) {
            System.out.println("No students available to sort.");
            return;
        }

        // 1. Calculate every student's percentage once.
        double[] percentages = new double[studentCount];
        for (int row = 0; row < studentCount; row++) {
            percentages[row] = calculatePercentage(row);
        }

        // 2. 'order' holds row indexes. We sort THIS array, not the real data,
        //    so student records and HashMap mappings stay untouched.
        int[] order = new int[studentCount];
        for (int i = 0; i < studentCount; i++) {
            order[i] = i;
        }

        // 3. Bubble Sort: swap only when left < right (strict), so students
        //    with equal percentages keep their original insertion order.
        for (int pass = 0; pass < studentCount - 1; pass++) {
            boolean swapped = false;
            for (int j = 0; j < studentCount - 1 - pass; j++) {
                if (percentages[order[j]] < percentages[order[j + 1]]) {
                    int temp = order[j];
                    order[j] = order[j + 1];
                    order[j + 1] = temp;
                    swapped = true;
                }
            }
            if (!swapped) {
                break;       // already sorted
            }
        }

        // 4. Display highest percentage first.
        for (int position = 0; position < studentCount; position++) {
            System.out.println((position + 1) + ". " + describeStudent(order[position]));
        }
    }

    // =========================================================
    // viewPercentage(): Option 4
    // =========================================================
    static void viewPercentage() {
        String roll = readLine("Enter roll number: ");
        if (roll == null) {
            return;
        }
        roll = roll.toUpperCase();

        int row = searchStudent(roll);
        if (row == -1) {
            System.out.println("Roll number " + roll + " not found.");
            return;
        }

        if (countRecorded(row) == 0) {
            System.out.println(rollNumbers[row] + " | " + studentNames[row]
                    + " | No attendance has been recorded yet.");
            return;
        }
        System.out.println(describeStudent(row));
    }

    // =========================================================
    // saveData(): writes attendance.txt. Returns true on success.
    //
    // File format:
    //   NEXT_ROLL=103
    //   R101|Karan|1,0
    //   R102|Meera|-1,1
    // =========================================================
    static boolean saveData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            writer.write("NEXT_ROLL=" + nextRollNumber);
            writer.newLine();

            for (int row = 0; row < studentCount; row++) {
                StringBuilder line = new StringBuilder();
                line.append(rollNumbers[row]).append("|").append(studentNames[row]).append("|");
                for (int day = 0; day < totalDays; day++) {
                    if (day > 0) {
                        line.append(",");
                    }
                    line.append(attendance[row][day]);
                }
                writer.write(line.toString());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Error: could not save data to " + DATA_FILE
                    + " (" + e.getMessage() + ")");
            return false;
        }
    }

    static void saveAndReport() {
        if (saveData()) {
            System.out.println("Data saved to " + DATA_FILE + ".");
        }
    }

    // =========================================================
    // loadData(): reads attendance.txt (if present) at start-up.
    // A missing file is fine; bad lines are skipped with a warning.
    // =========================================================
    static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("No saved data found. Starting with empty records.");
            return;
        }

        int savedNextRoll = FIRST_ROLL_NUMBER;
        int skippedLines = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("NEXT_ROLL=")) {
                    try {
                        savedNextRoll = Integer.parseInt(line.substring("NEXT_ROLL=".length()).trim());
                    } catch (NumberFormatException e) {
                        skippedLines++;
                    }
                } else if (!loadStudentLine(line)) {
                    skippedLines++;
                }
            }
        } catch (IOException e) {
            System.out.println("Error: could not read " + DATA_FILE + " (" + e.getMessage() + ")");
        }

        // Continue roll numbers after the largest number in use, so a
        // damaged NEXT_ROLL line can never cause a duplicate roll number.
        nextRollNumber = Math.max(savedNextRoll, FIRST_ROLL_NUMBER);
        for (int row = 0; row < studentCount; row++) {
            int used = Integer.parseInt(rollNumbers[row].substring(1));
            if (used >= nextRollNumber) {
                nextRollNumber = used + 1;
            }
        }

        // Work out the highest day that has any record.
        totalDays = 0;
        for (int row = 0; row < studentCount; row++) {
            for (int day = 0; day < MAX_DAYS; day++) {
                if (attendance[row][day] != NOT_RECORDED && day + 1 > totalDays) {
                    totalDays = day + 1;
                }
            }
        }

        System.out.println("Loaded " + studentCount + " student(s) from " + DATA_FILE + ".");
        if (skippedLines > 0) {
            System.out.println("Warning: " + skippedLines + " damaged line(s) were skipped.");
        }
    }

    // Reads one "R101|Karan|1,0" line. Returns false if the line is malformed.
    static boolean loadStudentLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 3) {
            return false;
        }

        String roll = parts[0].trim();
        String name = parts[1].trim();
        String values = parts[2].trim();

        // Roll must look like R + digits, name must not be blank.
        if (!roll.matches("R[0-9]{1,9}") || name.isEmpty()) {
            return false;
        }
        if (rollToRow.containsKey(roll) || studentCount >= MAX_STUDENTS) {
            return false;
        }

        // Read the attendance values into a temporary array first.
        int[] temp = new int[MAX_DAYS];
        for (int day = 0; day < MAX_DAYS; day++) {
            temp[day] = NOT_RECORDED;
        }
        if (!values.isEmpty()) {
            String[] cells = values.split(",", -1);
            if (cells.length > MAX_DAYS) {
                return false;
            }
            for (int day = 0; day < cells.length; day++) {
                String cell = cells[day].trim();
                if (cell.equals("1")) {
                    temp[day] = PRESENT;
                } else if (cell.equals("0")) {
                    temp[day] = ABSENT;
                } else if (cell.equals("-1")) {
                    temp[day] = NOT_RECORDED;
                } else {
                    return false;       // anything else is malformed
                }
            }
        }

        // The line is valid: store it and rebuild the HashMap entry.
        int row = studentCount;
        rollNumbers[row] = roll;
        studentNames[row] = name;
        for (int day = 0; day < MAX_DAYS; day++) {
            attendance[row][day] = temp[day];
        }
        rollToRow.put(roll, row);
        studentCount++;
        return true;
    }
}
