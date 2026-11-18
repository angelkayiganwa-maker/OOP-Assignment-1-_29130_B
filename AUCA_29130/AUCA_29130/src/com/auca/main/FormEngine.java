package com.auca.main;

import com.auca.administrator.Administrator;
import com.auca.assessment.Assessment;
import com.auca.bookloan.BookLoan;
import com.auca.classroom.Classroom;
import com.auca.course.Course;
import com.auca.department.Department;
import com.auca.enrollment.Enrollment;
import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.ValidationException;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import com.auca.teacher.Teacher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

import static com.auca.main.ConsoleIO.*;
import static com.auca.main.Constants.*;
import static com.auca.main.FieldDefinitions.*;
import static com.auca.main.RecordStore.*;
import static com.auca.main.TextUtils.*;
import static com.auca.main.Validator.*;


public final class FormEngine {

    private FormEngine() {
    }

    public static boolean fixInvalidFields(int type, String[] v, String selfId) {

        while (true) {
            int bad = findInvalidField(type, v, selfId);

            if (bad < 0) {
                return true;
            }

            System.out.println("! " + lastError);
            System.out.println("  Please correct field " + two(bad + 1) + " now (type CANCEL to undo the change).");

            String result = askField(type, bad, v, selfId);

            if (!result.equals("OK")) {
                return false;
            }
        }
    }

    public static String askField(int type, int i, String[] v, String selfId) {

        String prompt = two(i + 1) + " " + fieldLabels(type)[i];
        char kind = KINDS[type].charAt(i);

        if (kind == 'S') {
            return askRelatedRecord(type, i, v, selfId);
        }

        if (kind == 'C') {
            return askChoice(type, i, v, selfId, prompt);
        }

        return askText(type, i, v, selfId, prompt);
    }

    public static String askText(int type, int i, String[] v, String selfId, String prompt) {

        while (true) {
            String shown = prompt + " : ";

            if (!v[i].isEmpty()) {
                shown = prompt + " (current: " + v[i] + ") : ";
            }

            String line = readString(shown);

            if (line.equalsIgnoreCase("CANCEL")) {
                return "CANCEL";
            }

            if (line.equalsIgnoreCase("BACK")) {
                return "BACK";
            }

            if (line.isEmpty() && !v[i].isEmpty()) {
                return "OK";
            }

            try {
                v[i] = validateField(type, i, line, v, selfId);
                return "OK";
            } catch (ValidationException | DuplicateRecordException | BookUnavailableException e) {
                System.out.println("! " + e.getMessage());
                System.out.println("  Please re-enter field " + two(i + 1) + ".");
            }
        }
    }

    public static String askChoice(int type, int i, String[] v, String selfId, String prompt) {

        String[] options = fieldChoices(type, i);
        String hint = "[";

        for (int k = 0; k < options.length; k++) {
            if (k > 0) {
                hint = hint + " / ";
            }

            hint = hint + (k + 1) + " " + options[k];
        }

        hint = hint + "]";

        while (true) {
            String shown = prompt + " " + hint + " : ";

            if (!v[i].isEmpty()) {
                shown = prompt + " " + hint + " (current: " + v[i] + ") : ";
            }

            String line = readString(shown);

            if (line.equalsIgnoreCase("CANCEL")) {
                return "CANCEL";
            }

            if (line.equalsIgnoreCase("BACK")) {
                return "BACK";
            }

            if (line.isEmpty() && !v[i].isEmpty()) {
                return "OK";
            }

            String selected = "";

            try {
                int number = Integer.parseInt(line);

                if (number >= 1 && number <= options.length) {
                    selected = options[number - 1];
                }
            } catch (NumberFormatException e) {
                selected = line;
            }

            try {
                v[i] = validateField(type, i, selected, v, selfId);
                return "OK";
            } catch (ValidationException e) {
                System.out.println("! Invalid choice. Enter a number from 1 to " + options.length + ".");
                System.out.println("  Please re-enter field " + two(i + 1) + ".");
            } catch (DuplicateRecordException | BookUnavailableException e) {
                System.out.println("! " + e.getMessage());
                System.out.println("  Please re-enter field " + two(i + 1) + ".");
            }
        }
    }

    public static void fillRelatedOptions(int kind, int type, String[] v,
                                          ArrayList<String> labels, ArrayList<String> ids) {

        labels.clear();
        ids.clear();

        for (Identifiable record : recordsOfType(kind)) {

            if (kind == TEACHER && type == COURSE) {
                Teacher teacher = (Teacher) record;

                if (!teacher.getDepartment().getId().equalsIgnoreCase(v[4])) {
                    continue;
                }
            }

            if (kind == ENROLLMENT && !record.isActive()) {
                continue;
            }

            labels.add(label(kind, record));
            ids.add(record.getId());
        }
    }

    public static String askRelatedRecord(int type, int i, String[] v, String selfId) {

        int kind = relatedKind(type, i);
        ArrayList<String> labels = new ArrayList<>();
        ArrayList<String> ids = new ArrayList<>();

        while (true) {
            fillRelatedOptions(kind, type, v, labels, ids);

            String kindName = NAMES[kind];
            System.out.println();
            System.out.println("+----------------- CHOOSE " + kindName.toUpperCase() + " -----------------+");

            if (ids.isEmpty()) {
                System.out.println("| No " + kindName.toLowerCase() + " is available to choose yet.");

                if (kind == TEACHER && type == COURSE) {
                    System.out.println("| Only teachers of the selected department are listed.");
                }
            }

            for (int k = 0; k < ids.size(); k++) {
                System.out.println("| " + (k + 1) + " " + labels.get(k));
            }

            System.out.println("| " + (ids.size() + 1) + " [+] Register a new " + kindName.toLowerCase());
            System.out.println("| 0 Cancel " + NAMES[type].toLowerCase() + " registration");

            if (!v[i].isEmpty()) {
                System.out.println("| ENTER keeps: " + describe(kind, v[i]));
            }

            System.out.println("+------------------------------------------------------------+");

            String line = readString(two(i + 1) + " " + shortLabel(fieldLabels(type)[i]) + " - Select option: ");

            if (line.equalsIgnoreCase("BACK")) {
                return "BACK";
            }

            if (line.equalsIgnoreCase("CANCEL")) {
                return "CANCEL";
            }

            if (line.isEmpty() && !v[i].isEmpty()) {
                return "OK";
            }

            int number;

            try {
                number = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("! Please enter a number from the list.");
                continue;
            }

            if (number == 0) {
                return "CANCEL";
            }

            if (number >= 1 && number <= ids.size()) {
                try {
                    v[i] = validateField(type, i, ids.get(number - 1), v, selfId);
                    return "OK";
                } catch (ValidationException | DuplicateRecordException | BookUnavailableException e) {
                    System.out.println("! " + e.getMessage());
                    continue;
                }
            }

            if (number == ids.size() + 1) {
                registerNested(kind, NAMES[type].toLowerCase() + " registration");
                continue;
            }

            System.out.println("! Invalid selection. Choose a number shown in the list.");
        }
    }

    public static void registerNested(int kind, String returnTo) {

        try {
            String savedId = runForm(kind);

            if (savedId != null) {
                System.out.println("\n" + NAMES[kind] + " " + savedId + " saved. Returning to " + returnTo + "...");
            } else {
                System.out.println("\nRegistration cancelled. Returning to " + returnTo + "...");
            }
        } catch (PrerequisiteMissingException e) {
            System.out.println("\n! " + e.getMessage());
            System.out.println("  Returning to " + returnTo + "...");
        }
    }

    public static int countAvailable(int kind) {

        int count = 0;

        for (Identifiable record : recordsOfType(kind)) {
            if (kind == ENROLLMENT && !record.isActive()) {
                continue;
            }

            count++;
        }

        return count;
    }

    public static boolean ensureRecords(int kind, String purpose) {

        while (countAvailable(kind) == 0) {

            String name = NAMES[kind].toLowerCase();

            if (kind == ENROLLMENT) {
                name = "active enrollment";
            }

            System.out.println();
            System.out.println("No " + name + " exists. Register a " + name + " before " + purpose + ".");
            System.out.println("1. Register " + NAMES[kind].toLowerCase() + " now");
            System.out.println("2. Return to menu");

            int choice = readNumber("Select option: ");

            if (choice == 1) {
                registerNested(kind, "the original task");
            } else if (choice == 2 || choice == 0) {
                return false;
            } else {
                System.out.println("! Invalid option. Enter 1 or 2.");
            }
        }

        return true;
    }

    public static boolean checkPrerequisites(int type) {

        switch (type) {
            case COURSE:
                return ensureRecords(DEPARTMENT, "registering a course")
                        && ensureRecords(TEACHER, "registering a course");
            case ENROLLMENT:
                return ensureRecords(STUDENT, "enrolling a student")
                        && ensureRecords(COURSE, "enrolling a student");
            case ASSESSMENT:
                return ensureRecords(ENROLLMENT, "recording an assessment");
            case PAYMENT:
                return ensureRecords(STUDENT, "recording a fee payment");
            case LOAN:
                return ensureRecords(STUDENT, "borrowing a library book")
                        && ensureRecords(BOOK, "borrowing a library book");
            default:
                return true;
        }

    }

    public static void printBanner(int type, int step) {

        String[] titles = STEP_TITLES[type];
        String line = "+------------------------------------------------------------+";

        System.out.println();
        System.out.println(line);
        System.out.println(boxLine("AUCA_ID / SCHOOL MANAGEMENT"));
        System.out.println(boxLine(String.format("%-46s%12s", NAMES[type].toUpperCase() + " REGISTRATION",
                "Step " + step + " of 4")));
        System.out.println(line);
        System.out.println(boxLine(String.format("%-29s%-29s", "[1] " + titles[0], "[2] " + titles[1])));
        System.out.println(boxLine(String.format("%-29s%-29s", "[3] " + titles[2], "[4] Review & confirm")));
        System.out.println(line);
        System.out.println(boxLine("Enter the requested value. Type BACK to return;"));
        System.out.println(boxLine("CANCEL to discard the unsaved " + NAMES[type].toLowerCase() + "."));
        System.out.println(line);
    }

    public static void printReview(int type, String[] v) {

        String[] titles = STEP_TITLES[type];
        int[] starts = STEP_STARTS[type];
        String[] labels = fieldLabels(type);

        System.out.println();
        System.out.println("+---------------- REVIEW " + NAMES[type].toUpperCase() + " ----------------+");

        for (int i = 0; i < v.length; i++) {
            for (int s = 0; s < 3; s++) {
                if (starts[s] == i) {
                    System.out.println("| " + titles[s].toUpperCase());
                }
            }

            System.out.println("|   " + two(i + 1) + " " + String.format("%-22s", shortLabel(labels[i]))
                    + ": " + displayValue(type, i, v));
        }

        System.out.println("+------------------------------------------------------------+");
        System.out.println("| [1] Save " + NAMES[type].toLowerCase() + "   [2] Edit field   [0] Cancel");
        System.out.println("+------------------------------------------------------------+");
    }

    public static int collectStep(int type, String[] v, int from, int to) {

        int i = from;

        while (i < to) {

            if (KINDS[type].charAt(i) == 'D') {
                System.out.println(two(i + 1) + " " + shortLabel(fieldLabels(type)[i]) + " : "
                        + displayValue(type, i, v));
                i++;
                continue;
            }

            String result = askField(type, i, v, "");

            if (result.equals("CANCEL")) {
                return 2;
            }

            if (result.equals("BACK")) {
                if (i == from) {
                    return 1;
                }

                i--;

                while (i > from && KINDS[type].charAt(i) == 'D') {
                    i--;
                }
            } else {
                i++;
            }
        }

        return 0;
    }

    public static String runForm(int type) throws PrerequisiteMissingException {

        if (!checkPrerequisites(type)) {
            throw new PrerequisiteMissingException("A required record is missing, so the "
                    + NAMES[type].toLowerCase() + " registration was stopped.");
        }

        int fieldCount = KINDS[type].length();
        String[] v = new String[fieldCount];

        for (int i = 0; i < fieldCount; i++) {
            v[i] = "";
        }

        int[] starts = STEP_STARTS[type];
        int step = 0;

        while (step < 3) {

            printBanner(type, step + 1);
            System.out.println("[STEP " + (step + 1) + "/4] " + STEP_TITLES[type][step].toUpperCase());

            int to = fieldCount;

            if (step < 2) {
                to = starts[step + 1];
            }

            int result = collectStep(type, v, starts[step], to);

            if (result == 2) {
                System.out.println("\nRegistration cancelled. Nothing was saved.");
                return null;
            }

            if (result == 1) {
                if (step == 0) {
                    System.out.println("You are already at the first step.");
                } else {
                    step--;
                }
            } else {
                step++;
            }
        }

        return reviewLoop(type, v);
    }

    public static String reviewLoop(int type, String[] v) {

        while (true) {

            printBanner(type, 4);
            printReview(type, v);

            int choice = readNumber("Select option: ");

            if (choice == 0) {
                System.out.println("\nRegistration cancelled. Nothing was saved.");
                return null;
            }

            if (choice == 2) {
                editField(type, v);
                continue;
            }

            if (choice != 1) {
                System.out.println("! Invalid option. Enter 1, 2 or 0.");
                continue;
            }

            if (!fixInvalidFields(type, v, "")) {
                continue;
            }

            try {
                String id = saveRecord(type, v);
                System.out.println("\n+ " + NAMES[type] + " " + id + " registered successfully.");
                return id;
            } catch (ValidationException | BookUnavailableException | DuplicateRecordException e) {
                System.out.println("\n! Could not save: " + e.getMessage());
                System.out.println("  Use Edit field to correct the record.");
            }
        }
    }

    public static void editField(int type, String[] v) {

        int number = readNumber("Field number to edit (01-" + two(v.length) + "): ");

        if (number < 1 || number > v.length) {
            System.out.println("! Invalid field number.");
            return;
        }

        int i = number - 1;

        if (KINDS[type].charAt(i) == 'D') {
            System.out.println("! This field is set automatically and cannot be edited here.");
            return;
        }

        System.out.println("Field " + two(number) + " " + shortLabel(fieldLabels(type)[i])
                + " - old value: " + displayValue(type, i, v));

        String[] backup = copyOf(v);
        String result = askField(type, i, v, "");

        if (!result.equals("OK")) {
            System.out.println("Edit cancelled. The old value was kept.");
            return;
        }

        if (!fixInvalidFields(type, v, "")) {
            for (int k = 0; k < v.length; k++) {
                v[k] = backup[k];
            }

            System.out.println("Change undone. The old values were restored.");
        }
    }

    public static String saveRecord(int type, String[] v)
            throws ValidationException, DuplicateRecordException, BookUnavailableException {

        switch (type) {
            case DEPARTMENT: {
                Department department = new Department(v[0], v[1], v[2], v[3], v[4], findTeacher(v[5]));
                department.validate();
                departments.add(department);
                break;
            }
            case STUDENT: {
                Student student = new Student(v[0], v[1], v[2], v[3], v[4], LocalDate.parse(v[5]),
                        v[6], findDepartment(v[7]), LocalDate.parse(v[8]), v[9], v[10], v[11],
                        v[12], v[13], LocalDate.parse(v[14]), Integer.parseInt(v[15]), v[16],
                        v[17].equals("YES"));
                student.validate();
                students.add(student);
                break;
            }
            case TEACHER: {
                Teacher teacher = new Teacher(v[0], v[1], v[2], v[3], v[4], LocalDate.parse(v[5]),
                        v[6], findDepartment(v[7]), LocalDate.parse(v[8]), v[9], v[10], v[11],
                        v[12], v[13], LocalDate.parse(v[14]), new BigDecimal(v[15]), v[16], v[17]);
                teacher.validate();
                teachers.add(teacher);
                break;
            }
            case ADMINISTRATOR: {
                Administrator administrator = new Administrator(v[0], v[1], v[2], v[3], v[4],
                        LocalDate.parse(v[5]), v[6], v[7], v[8], LocalDate.parse(v[9]),
                        Integer.parseInt(v[10]), v[11].equals("YES"));
                administrator.validate();
                administrators.add(administrator);
                break;
            }
            case COURSE: {
                Course course = new Course(v[0], v[1], v[2], Integer.parseInt(v[3]),
                        findDepartment(v[4]), findTeacher(v[5]));
                course.validate();
                courses.add(course);
                break;
            }
            case CLASSROOM: {
                Classroom classroom = new Classroom(v[0], v[1], v[2], Integer.parseInt(v[3]),
                        Integer.parseInt(v[4]), v[5].equals("YES"));
                classroom.validate();
                classrooms.add(classroom);
                break;
            }
            case ENROLLMENT: {
                Enrollment enrollment = new Enrollment(v[0], findStudent(v[1]), findCourse(v[2]),
                        LocalDate.parse(v[3]), v[4], v[5]);
                enrollment.validate();
                enrollments.add(enrollment);
                break;
            }
            case ASSESSMENT: {
                Assessment assessment = new Assessment(v[0], findEnrollment(v[1]), v[2],
                        Double.parseDouble(v[3]), Double.parseDouble(v[4]), LocalDate.parse(v[5]));
                assessment.validate();
                assessments.add(assessment);
                break;
            }
            case PAYMENT: {
                FeePayment payment = new FeePayment(v[0], findStudent(v[1]), new BigDecimal(v[2]),
                        LocalDate.parse(v[3]), v[4], v[5]);
                payment.validate();
                payments.add(payment);
                break;
            }
            case BOOK: {
                LibraryBook book = new LibraryBook(v[0], v[1], v[2], v[3],
                        Integer.parseInt(v[4]), Integer.parseInt(v[5]));
                book.validate();
                books.add(book);
                break;
            }
            default: {
                LibraryBook book = findBook(v[2]);
                BookLoan loan = new BookLoan(v[0], findStudent(v[1]), book, LocalDate.parse(v[3]),
                        LocalDate.parse(v[4]), null);
                loan.validate();

                if (book.getAvailableCopies() <= 0) {
                    throw new BookUnavailableException("No copy of \"" + book.getTitle()
                            + "\" is available to borrow.");
                }

                book.setAvailableCopies(book.getAvailableCopies() - 1);
                loans.add(loan);
                break;
            }
        }

        return v[0];
    }

    public static void registerFromMenu(int type) throws PrerequisiteMissingException {

        while (true) {
            String id = runForm(type);

            if (id == null) {
                return;
            }

            System.out.println("  [1] Register another  [2] View " + NAMES[type].toLowerCase() + "  [0] Main menu");
            int choice = readNumber("Select option: ");

            if (choice == 1) {
                continue;
            }

            if (choice == 2) {
                Identifiable record = findRecord(type, id);

                if (record != null) {
                    ((Registrable) record).printDetails();
                }
            }

            return;
        }
    }
}
