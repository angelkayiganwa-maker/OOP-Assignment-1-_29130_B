package com.auca.main;

import com.auca.department.Department;
import com.auca.enrollment.Enrollment;
import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.teacher.Teacher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static com.auca.main.Constants.*;
import static com.auca.main.FieldDefinitions.*;
import static com.auca.main.RecordMapper.*;
import static com.auca.main.RecordStore.*;
import static com.auca.main.TextUtils.*;


public final class Validator {

    private Validator() {
    }

    static String lastError = "";

    public static String checkLength(String s, int min, int max, String name)
            throws ValidationException {

        String text = "";

        if (s != null) {
            text = s.trim();
        }

        if (text.isEmpty()) {
            throw new ValidationException(name + " cannot be blank.");
        }

        if (text.length() < min || text.length() > max) {
            throw new ValidationException(name + " must be between " + min + " and " + max + " characters.");
        }

        return text;
    }

    public static String checkEmail(String s, String name) throws ValidationException {

        String text = checkLength(s, 5, Registrable.MAX_EMAIL_LENGTH, name);

        if (!text.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException(name + " is not a valid email address (example: alice@example.com).");
        }

        return text;
    }

    public static String checkPhone(String s, String name) throws ValidationException {

        String text = checkLength(s, 1, 30, name);

        if (!text.matches("\\+?[0-9]{" + Registrable.MIN_PHONE_LENGTH + "," + Registrable.MAX_PHONE_LENGTH + "}")) {
            throw new ValidationException(name + " must contain " + Registrable.MIN_PHONE_LENGTH + " to "
                    + Registrable.MAX_PHONE_LENGTH + " digits, with an optional leading +.");
        }

        return text;
    }

    public static LocalDate parseDate(String s) throws ValidationException {

        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date: use YYYY-MM-DD (example: 2004-05-16).");
        }
    }

    public static String checkPastDate(String s, String name) throws ValidationException {

        LocalDate date = parseDate(s);

        if (!date.isBefore(LocalDate.now())) {
            throw new ValidationException(name + " must be in the past.");
        }

        return date.toString();
    }

    public static String checkNotFutureDate(String s, String name) throws ValidationException {

        LocalDate date = parseDate(s);

        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException(name + " cannot be in the future.");
        }

        return date.toString();
    }

    public static String checkInt(String s, int min, int max, String name) throws ValidationException {

        try {
            int number = Integer.parseInt(s.trim());

            if (number < min || number > max) {
                throw new ValidationException(name + " must be between " + min + " and " + max + ".");
            }

            return String.valueOf(number);
        } catch (NumberFormatException e) {
            throw new ValidationException(name + " must be a whole number.");
        }
    }

    public static String checkMoney(String s, String name) throws ValidationException {

        try {
            BigDecimal amount = new BigDecimal(s.trim());

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException(name + " must be greater than zero.");
            }

            return amount.toPlainString();
        } catch (NumberFormatException e) {
            throw new ValidationException(name + " must be a valid amount (example: 150000.50).");
        }
    }

    public static double parseScore(String s, String name) throws ValidationException {

        try {
            double score = Double.parseDouble(s.trim());

            if (Double.isNaN(score) || Double.isInfinite(score)) {
                throw new ValidationException(name + " must be a valid number.");
            }

            return score;
        } catch (NumberFormatException e) {
            throw new ValidationException(name + " must be a valid number (example: 78.5).");
        }
    }

    public static String checkChoice(int type, int i, String value) throws ValidationException {

        String[] options = fieldChoices(type, i);

        for (String option : options) {
            if (option.equalsIgnoreCase(value)) {
                return option;
            }
        }

        throw new ValidationException("Choose one of the listed options.");
    }

    public static boolean valueTaken(int type, int i, String value, String selfId) {

        for (Identifiable record : recordsOfType(type)) {
            if (record.getId().equalsIgnoreCase(selfId)) {
                continue;
            }

            String[] values = valuesOf(type, record);

            if (values[i].equalsIgnoreCase(value)) {
                return true;
            }
        }

        return false;
    }

    public static boolean personValueTaken(int i, String value, String selfId) {

        return valueTaken(STUDENT, i, value, selfId)
                || valueTaken(TEACHER, i, value, selfId)
                || valueTaken(ADMINISTRATOR, i, value, selfId);
    }

    public static boolean memberValueTaken(int i, String value, String selfId) {

        return valueTaken(STUDENT, i, value, selfId)
                || valueTaken(TEACHER, i, value, selfId);
    }

    public static boolean activeEnrollmentExists(String studentId, String courseId,
                                                 String semester, String selfId) {

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getId().equalsIgnoreCase(selfId)) {
                continue;
            }

            if (enrollment.isActive()
                    && enrollment.getStudent().getId().equalsIgnoreCase(studentId)
                    && enrollment.getCourse().getId().equalsIgnoreCase(courseId)
                    && enrollment.getSemester().equalsIgnoreCase(semester)) {
                return true;
            }
        }

        return false;
    }

    public static String validateField(int type, int i, String s, String[] v, String selfId)
            throws ValidationException, DuplicateRecordException, BookUnavailableException {

        char kind = KINDS[type].charAt(i);

        if (kind == 'D') {
            return s;
        }

        String name = shortLabel(fieldLabels(type)[i]);

        if (s == null || s.trim().isEmpty()) {
            throw new ValidationException(name + " cannot be blank.");
        }

        String value = s.trim();

        if (kind == 'C') {
            value = checkChoice(type, i, value);
        } else if (kind == 'S') {
            int related = relatedKind(type, i);

            if (findRecord(related, value) == null) {
                throw new ValidationException("Select an existing " + NAMES[related].toLowerCase()
                        + " from the list.");
            }
        } else {
            value = validateTextField(type, i, value, selfId, name);
        }

        checkRelations(type, i, value, v, selfId);

        return value;
    }

    public static String validateTextField(int type, int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        if ((type == STUDENT || type == TEACHER || type == ADMINISTRATOR) && i < 6) {
            return validatePersonField(type, i, value, selfId);
        }

        if ((type == STUDENT || type == TEACHER) && i >= 6 && i < 12) {
            return validateMemberField(i, value, selfId);
        }

        switch (type) {
            case DEPARTMENT:
                return validateDepartmentField(i, value, selfId, name);
            case STUDENT:
                return validateStudentField(i, value, selfId, name);
            case TEACHER:
                return validateTeacherField(i, value, selfId, name);
            case ADMINISTRATOR:
                return validateAdministratorField(i, value, selfId, name);
            case COURSE:
                return validateCourseField(i, value, selfId, name);
            case CLASSROOM:
                return validateClassroomField(i, value, selfId, name);
            case ENROLLMENT:
                return validateEnrollmentField(i, value, selfId, name);
            case ASSESSMENT:
                return validateAssessmentField(i, value, selfId, name);
            case PAYMENT:
                return validatePaymentField(i, value, selfId, name);
            case BOOK:
                return validateBookField(i, value, selfId, name);
            default:
                return validateLoanField(i, value, selfId, name);
        }
    }

    public static String validatePersonField(int type, int i, String value, String selfId)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, Identifiable.MIN_ID_LENGTH, Identifiable.MAX_ID_LENGTH, "Person ID");
                if (personValueTaken(0, id, selfId)) {
                    throw new DuplicateRecordException("Person ID " + id + " already exists. Please enter a different ID.");
                }
                return id;
            case 1:
                return checkLength(value, Registrable.MIN_NAME_LENGTH, Registrable.MAX_NAME_LENGTH, "First name");
            case 2:
                return checkLength(value, Registrable.MIN_NAME_LENGTH, Registrable.MAX_NAME_LENGTH, "Last name");
            case 3:
                String email = checkEmail(value, "Email");
                if (personValueTaken(3, email, selfId)) {
                    throw new DuplicateRecordException("Email " + email + " is already used by another person.");
                }
                return email;
            case 4:
                return checkPhone(value, "Phone");
            default:
                return checkPastDate(value, "Date of birth");
        }
    }

    public static String validateMemberField(int i, String value, String selfId)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 6:
                String number = checkLength(value, 4, 30, "Member number");
                if (memberValueTaken(6, number, selfId)) {
                    throw new DuplicateRecordException("Member number " + number + " already exists.");
                }
                return number;
            case 8:
                return checkNotFutureDate(value, "Join date");
            case 10:
                String email = checkEmail(value, "Institutional email");
                if (memberValueTaken(10, email, selfId)) {
                    throw new DuplicateRecordException("Institutional email " + email + " already exists.");
                }
                return email;
            default:
                return checkLength(value, 2, 120, "Advisor name");
        }
    }

    public static String validateDepartmentField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, 3, 30, name);
                if (valueTaken(DEPARTMENT, 0, id, selfId)) {
                    throw new DuplicateRecordException("Department ID " + id + " already exists.");
                }
                return id;
            case 1:
                return checkLength(value, 2, 60, name);
            case 2:
                String code = checkLength(value, 2, 20, name);
                if (valueTaken(DEPARTMENT, 2, code, selfId)) {
                    throw new DuplicateRecordException("Department code " + code + " already exists.");
                }
                return code;
            case 3:
                return checkLength(value, 2, 60, name);
            default:
                return checkEmail(value, name);
        }
    }

    public static String validateStudentField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 12:
                String number = checkLength(value, 4, 30, name);
                if (valueTaken(STUDENT, 12, number, selfId)) {
                    throw new DuplicateRecordException("Student number " + number + " already exists.");
                }
                return number;
            case 13:
                return checkLength(value, 2, 60, name);
            case 14:
                return checkNotFutureDate(value, name);
            case 15:
                return checkInt(value, 1, 6, name);
            default:
                return checkPhone(value, name);
        }
    }

    public static String validateTeacherField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 12:
                String number = checkLength(value, 4, 30, name);
                if (valueTaken(TEACHER, 12, number, selfId)) {
                    throw new DuplicateRecordException("Employee number " + number + " already exists.");
                }
                return number;
            case 13:
                return checkLength(value, 2, 60, name);
            case 14:
                return checkNotFutureDate(value, name);
            case 15:
                return checkMoney(value, name);
            default:
                return checkLength(value, 1, 20, name);
        }
    }

    public static String validateAdministratorField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 6:
                String number = checkLength(value, 4, 30, name);
                if (valueTaken(ADMINISTRATOR, 6, number, selfId)) {
                    throw new DuplicateRecordException("Staff number " + number + " already exists.");
                }
                return number;
            case 7:
                return checkLength(value, 2, 60, name);
            case 8:
                return checkLength(value, 2, 60, name);
            case 9:
                return checkNotFutureDate(value, name);
            default:
                return checkInt(value, 1, 5, name);
        }
    }

    public static String validateCourseField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, 3, 30, name);
                if (valueTaken(COURSE, 0, id, selfId)) {
                    throw new DuplicateRecordException("Course ID " + id + " already exists.");
                }
                return id;
            case 1:
                String code = checkLength(value, 2, 20, name);
                if (valueTaken(COURSE, 1, code, selfId)) {
                    throw new DuplicateRecordException("Course code " + code + " already exists.");
                }
                return code;
            case 2:
                return checkLength(value, 2, 60, name);
            default:
                return checkInt(value, 1, 6, name);
        }
    }

    public static String validateClassroomField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, 3, 30, name);
                if (valueTaken(CLASSROOM, 0, id, selfId)) {
                    throw new DuplicateRecordException("Classroom ID " + id + " already exists.");
                }
                return id;
            case 1:
                return checkLength(value, 2, 60, name);
            case 2:
                return checkLength(value, 1, 20, name);
            case 3:
                return checkInt(value, 1, 100000, name);
            default:
                return checkInt(value, 0, 200, name);
        }
    }

    public static String validateEnrollmentField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        if (i == 0) {
            String id = checkLength(value, 3, 30, name);
            if (valueTaken(ENROLLMENT, 0, id, selfId)) {
                throw new DuplicateRecordException("Enrollment ID " + id + " already exists.");
            }
            return id;
        }

        return checkNotFutureDate(value, name);
    }

    public static String validateAssessmentField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, 3, 30, name);
                if (valueTaken(ASSESSMENT, 0, id, selfId)) {
                    throw new DuplicateRecordException("Assessment ID " + id + " already exists.");
                }
                return id;
            case 3:
                double maximum = parseScore(value, name);
                if (maximum <= 0) {
                    throw new ValidationException(name + " must be greater than zero.");
                }
                return String.valueOf(maximum);
            case 4:
                double obtained = parseScore(value, name);
                if (obtained < 0) {
                    throw new ValidationException(name + " cannot be negative.");
                }
                return String.valueOf(obtained);
            default:
                return checkNotFutureDate(value, name);
        }
    }

    public static String validatePaymentField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, 3, 30, name);
                if (valueTaken(PAYMENT, 0, id, selfId)) {
                    throw new DuplicateRecordException("Payment ID " + id + " already exists.");
                }
                return id;
            case 2:
                return checkMoney(value, name);
            case 3:
                return checkNotFutureDate(value, name);
            default:
                String receipt = checkLength(value, 3, 30, name);
                if (valueTaken(PAYMENT, 5, receipt, selfId)) {
                    throw new DuplicateRecordException("Receipt number " + receipt + " already exists.");
                }
                return receipt;
        }
    }

    public static String validateBookField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, 3, 30, name);
                if (valueTaken(BOOK, 0, id, selfId)) {
                    throw new DuplicateRecordException("Book ID " + id + " already exists.");
                }
                return id;
            case 1:
                String isbn = checkLength(value, 10, 17, name);
                if (!isbn.matches("[0-9Xx-]+")) {
                    throw new ValidationException(name + " may contain only digits, hyphens and X.");
                }
                if (valueTaken(BOOK, 1, isbn, selfId)) {
                    throw new DuplicateRecordException("ISBN " + isbn + " already exists.");
                }
                return isbn;
            case 2:
                return checkLength(value, 2, 60, name);
            case 3:
                return checkLength(value, 2, 60, name);
            default:
                return checkInt(value, 0, 100000, name);
        }
    }

    public static String validateLoanField(int i, String value, String selfId, String name)
            throws ValidationException, DuplicateRecordException {

        switch (i) {
            case 0:
                String id = checkLength(value, 3, 30, name);
                if (valueTaken(LOAN, 0, id, selfId)) {
                    throw new DuplicateRecordException("Loan ID " + id + " already exists.");
                }
                return id;
            case 3:
                return checkNotFutureDate(value, name);
            default:
                return parseDate(value).toString();
        }
    }

    public static void checkRelations(int type, int i, String value, String[] v, String selfId)
            throws ValidationException, DuplicateRecordException, BookUnavailableException {

        if (type == COURSE && i == 5) {
            Department department = findDepartment(v[4]);
            Teacher teacher = findTeacher(value);

            if (department == null) {
                throw new ValidationException("Choose the department before the teacher.");
            }

            if (!teacher.getDepartment().getId().equalsIgnoreCase(department.getId())) {
                throw new ValidationException("Teacher must belong to the selected department.");
            }
        }

        if (type == ENROLLMENT && (i == 4 || i == 5)) {
            boolean activeNew = true;

            if (i == 5) {
                activeNew = value.equals("ACTIVE");
            } else if (v[5].equals("COMPLETED") || v[5].equals("CANCELLED")) {
                activeNew = false;
            }

            if (activeNew && !v[1].isEmpty() && !v[2].isEmpty()) {
                String semester = v[4];

                if (i == 4) {
                    semester = value;
                }

                if (!semester.isEmpty() && activeEnrollmentExists(v[1], v[2], semester, selfId)) {
                    throw new DuplicateRecordException("This student already has an active enrollment in this course for "
                            + semester + ".");
                }
            }
        }

        if (type == ASSESSMENT && i == 1 && selfId.isEmpty()) {
            Enrollment enrollment = findEnrollment(value);

            if (!enrollment.isActive()) {
                throw new ValidationException("Only an active enrollment can be assessed.");
            }
        }

        if (type == ASSESSMENT && i == 4 && !v[3].isEmpty()) {
            if (Double.parseDouble(value) > Double.parseDouble(v[3])) {
                throw new ValidationException("Obtained score cannot be greater than the maximum score.");
            }
        }

        if (type == BOOK && i == 5 && !v[4].isEmpty()) {
            if (Integer.parseInt(value) > Integer.parseInt(v[4])) {
                throw new ValidationException("Available copies cannot exceed total copies.");
            }
        }

        if (type == LOAN && i == 2 && selfId.isEmpty()) {
            LibraryBook book = findBook(value);

            if (book.getAvailableCopies() <= 0) {
                throw new BookUnavailableException("No copy of \"" + book.getTitle()
                        + "\" is available to borrow.");
            }
        }

        if (type == LOAN && i == 4 && !v[3].isEmpty()) {
            if (!LocalDate.parse(value).isAfter(LocalDate.parse(v[3]))) {
                throw new ValidationException("Due date must be after the borrowed date.");
            }
        }
    }

    public static int findInvalidField(int type, String[] v, String selfId) {

        for (int i = 0; i < v.length; i++) {
            if (KINDS[type].charAt(i) == 'D') {
                continue;
            }

            try {
                validateField(type, i, v[i], v, selfId);
            } catch (ValidationException | DuplicateRecordException | BookUnavailableException e) {
                lastError = "Field " + two(i + 1) + ": " + e.getMessage();
                return i;
            }
        }

        return -1;
    }
}
