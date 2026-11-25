# Attendance Tracker

A console-based Java application that records daily attendance of students, calculates attendance percentages and ranks students by percentage.

| | |
|---|---|
| **Student** | Mohammad Ayan |
| **ERP ID** | RU-25-10828 |
| **Course** | B.Tech Computer Science & Engineering with Data Science |
| **Subject** | Data Structures and Algorithms (DSA) |
| **PS No.** | 2 |
| **University** | Rungta International Skills University, Bhilai, Chhattisgarh |
| **Session** | 2026-27 |

## Features

- Add students with automatically generated roll numbers (R101, R102, R103, ...)
- Mark daily attendance (`P` = Present, `A` = Absent); re-marking a day replaces the old value
- Search a student by roll number (HashMap)
- View present days, recorded days and attendance percentage
- Sort students by attendance percentage, highest first (Bubble Sort, stable)
- Save to and load from `attendance.txt` automatically

## Data Structures Used

- `int[][] attendance` - rows = students, columns = days  
  `1` = Present, `0` = Absent, `-1` = Not recorded / not enrolled yet (never counted)
- `HashMap<String, Integer>` - roll number -> row index
- `String[] rollNumbers`, `String[] studentNames` - details of each student (by row)

Attendance Percentage = (Total Present Days / Total Recorded Days) x 100

## Requirements

- JDK 8 or above
- Any terminal (Command Prompt, PowerShell, Linux/macOS terminal)

## How to Compile and Run

```
javac AttendanceTracker.java
java AttendanceTracker
```

Run the program from the folder where you want `attendance.txt` to be created.

## Menu

```
========== ATTENDANCE TRACKER ==========

1. Add Student
2. Mark Attendance
3. Sort Students by Attendance Percentage
4. View Attendance Percentage
5. Exit

========================================
```

Data is saved when you choose **5. Exit**.

## Example Session

```
Enter your choice: 1
Enter student name: Karan
Student added. Roll: R101 | Karan

Enter your choice: 2
Enter roll number: R101
Enter day number (1-100): 1
Enter status (P = Present, A = Absent): P
Attendance marked for Day 1.

Enter your choice: 4
Enter roll number: R101
R101 | Karan | Present: 1/1 | 100.0%

Enter your choice: 2
Enter roll number: R555
Roll number R555 not found.
```

## Test Cases

| # | Input | Expected Output |
|---|-------|-----------------|
| 1 | Option 1, name `Karan` | `Student added. Roll: R101 \| Karan` |
| 2 | R101 present on Day 1, Option 4, `R101` | `R101 \| Karan \| Present: 1/1 \| 100.0%` |
| 3 | Option 2, roll `R555` | `Roll number R555 not found.` |
| 4 | Add Meera; Day 2: R101 = A, R102 = P; Option 3 | `1. R102 \| Meera \| Present: 1/1 \| 100.0%` then `2. R101 \| Karan \| Present: 1/2 \| 50.0%` |

## File Format (`attendance.txt`)

```
NEXT_ROLL=103
R101|Karan|1,0
R102|Meera|-1,1
```

- First line: the next roll number to use
- Each student line: `RollNumber|Name|day values` (1 = Present, 0 = Absent, -1 = Not recorded)
- A missing file starts empty; damaged lines are skipped with a warning

## Time Complexity

N = number of students, D = number of day columns in use.

| Operation | Complexity |
|-----------|------------|
| Add student | O(1) average |
| Search by roll number | O(1) average |
| Mark attendance | O(1) average |
| Percentage of one student | O(D) |
| Sort students (Bubble Sort) | O(N^2), plus O(N x D) for percentages |
| Load data | O(N) (MAX_DAYS is a fixed constant) |
| Save data | O(N x D) |

## Limits

Defined as constants at the top of `AttendanceTracker.java`: `MAX_STUDENTS = 100`, `MAX_DAYS = 100`. Change them to resize the arrays.

## Files

- `AttendanceTracker.java` - complete source code (single file)
- `Attendance_Tracker_Interim_Report.pdf` - project interim report
- `attendance.txt` - created automatically when you exit the program
