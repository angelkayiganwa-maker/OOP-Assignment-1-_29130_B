package com.auca.main;

import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;

import static com.auca.main.Constants.*;
import static com.auca.main.RecordStore.*;
import static com.auca.main.TextUtils.*;


public final class FieldDefinitions {

    private FieldDefinitions() {
    }

    public static boolean isMutable(int type, int i) {

        if (i == 0) {
            return false;
        }

        if (type == ENROLLMENT && (i == 1 || i == 2)) {
            return false;
        }

        if (type == ASSESSMENT && i == 1) {
            return false;
        }

        if (type == PAYMENT && i == 1) {
            return false;
        }

        if (type == BOOK && (i == 4 || i == 5)) {
            return false;
        }

        if (type == LOAN && (i == 1 || i == 2 || i == 5)) {
            return false;
        }

        return true;
    }

    public static String[] fieldLabels(int type) {

        String nameRule = Registrable.MIN_NAME_LENGTH + "-" + Registrable.MAX_NAME_LENGTH + " chars";
        String phoneRule = Registrable.MIN_PHONE_LENGTH + "-" + Registrable.MAX_PHONE_LENGTH + " digits, optional +";

        String[] person = {
                "Person ID [String | unique | " + Identifiable.MIN_ID_LENGTH + "-" + Identifiable.MAX_ID_LENGTH + " chars]",
                "First name [String | " + nameRule + "]",
                "Last name [String | " + nameRule + "]",
                "Email [String | unique | email]",
                "Phone [String | " + phoneRule + "]",
                "Date of birth [LocalDate | YYYY-MM-DD]"};

        String[] member = {
                "Member number [String | unique | 4-30 chars]",
                "Department [Department | select]",
                "Join date [LocalDate | YYYY-MM-DD]",
                "Academic status",
                "Institutional email [String | unique | email]",
                "Advisor name [String | 2-120 chars]"};

        switch (type) {
            case DEPARTMENT:
                return new String[]{
                        "Department ID [String | unique | 3-30 chars]",
                        "Department name [String | 2-60 chars]",
                        "Department code [String | unique | 2-20 chars]",
                        "Office location [String | 2-60 chars]",
                        "Contact email [String | email]",
                        "Head teacher [Teacher | assigned later]"};
            case STUDENT:
                return merge(merge(person, member), new String[]{
                        "Student number [String | unique | 4-30 chars]",
                        "Program name [String | 2-60 chars]",
                        "Admission date [LocalDate | YYYY-MM-DD]",
                        "Study year [int | 1-6]",
                        "Guardian phone [String | " + phoneRule + "]",
                        "Scholarship"});
            case TEACHER:
                return merge(merge(person, member), new String[]{
                        "Employee number [String | unique | 4-30 chars]",
                        "Specialization [String | 2-60 chars]",
                        "Hire date [LocalDate | YYYY-MM-DD]",
                        "Salary [BigDecimal | greater than 0]",
                        "Office number [String | 1-20 chars]",
                        "Employment type"});
            case ADMINISTRATOR:
                return merge(person, new String[]{
                        "Staff number [String | unique | 4-30 chars]",
                        "Job title [String | 2-60 chars]",
                        "Assigned office [String | 2-60 chars]",
                        "Employment date [LocalDate | YYYY-MM-DD]",
                        "Access level [int | 1-5]",
                        "Active"});
            case COURSE:
                return new String[]{
                        "Course ID [String | unique | 3-30 chars]",
                        "Course code [String | unique | 2-20 chars]",
                        "Course title [String | 2-60 chars]",
                        "Credit hours [int | 1-6]",
                        "Department [Department | select]",
                        "Assigned teacher [Teacher | select]"};
            case CLASSROOM:
                return new String[]{
                        "Classroom ID [String | unique | 3-30 chars]",
                        "Building name [String | 2-60 chars]",
                        "Room number [String | 1-20 chars]",
                        "Capacity [int | greater than 0]",
                        "Floor number [int | 0 or more]",
                        "Available"};
            case ENROLLMENT:
                return new String[]{
                        "Enrollment ID [String | unique | 3-30 chars]",
                        "Student [Student | select]",
                        "Course [Course | select]",
                        "Enrollment date [LocalDate | YYYY-MM-DD]",
                        "Semester",
                        "Enrollment status"};
            case ASSESSMENT:
                return new String[]{
                        "Assessment ID [String | unique | 3-30 chars]",
                        "Enrollment [Enrollment | select active]",
                        "Assessment type",
                        "Maximum score [double | greater than 0]",
                        "Obtained score [double | 0 to maximum]",
                        "Assessment date [LocalDate | YYYY-MM-DD]"};
            case PAYMENT:
                return new String[]{
                        "Payment ID [String | unique | 3-30 chars]",
                        "Student [Student | select]",
                        "Amount [BigDecimal | greater than 0]",
                        "Payment date [LocalDate | YYYY-MM-DD]",
                        "Payment method",
                        "Receipt number [String | unique | 3-30 chars]"};
            case BOOK:
                return new String[]{
                        "Book ID [String | unique | 3-30 chars]",
                        "ISBN [String | unique | 10-17 digits or -]",
                        "Title [String | 2-60 chars]",
                        "Author [String | 2-60 chars]",
                        "Total copies [int | 0 or more]",
                        "Available copies [int | 0 to total copies]"};
            default:
                return new String[]{
                        "Loan ID [String | unique | 3-30 chars]",
                        "Student [Student | select]",
                        "Book [LibraryBook | select available]",
                        "Borrowed date [LocalDate | YYYY-MM-DD]",
                        "Due date [LocalDate | after borrowed date]",
                        "Returned date [LocalDate | set on return]"};
        }
    }

    public static String[] fieldChoices(int type, int i) {

        if ((type == STUDENT || type == TEACHER) && i == 9) {
            return ACADEMIC_STATUS;
        }

        if (type == STUDENT && i == 17) {
            return YES_NO;
        }

        if (type == TEACHER && i == 17) {
            return EMPLOYMENT_TYPES;
        }

        if (type == ADMINISTRATOR && i == 11) {
            return YES_NO;
        }

        if (type == CLASSROOM && i == 5) {
            return YES_NO;
        }

        if (type == ENROLLMENT && i == 4) {
            return SEMESTERS;
        }

        if (type == ENROLLMENT && i == 5) {
            return ENROLLMENT_STATUS;
        }

        if (type == ASSESSMENT && i == 2) {
            return ASSESSMENT_TYPES;
        }

        if (type == PAYMENT && i == 4) {
            return PAYMENT_METHODS;
        }

        return new String[0];
    }

    public static int relatedKind(int type, int i) {

        if ((type == STUDENT || type == TEACHER) && i == 7) {
            return DEPARTMENT;
        }

        if (type == COURSE && i == 4) {
            return DEPARTMENT;
        }

        if (type == COURSE && i == 5) {
            return TEACHER;
        }

        if (type == ENROLLMENT && i == 1) {
            return STUDENT;
        }

        if (type == ENROLLMENT && i == 2) {
            return COURSE;
        }

        if (type == ASSESSMENT && i == 1) {
            return ENROLLMENT;
        }

        if ((type == PAYMENT || type == LOAN) && i == 1) {
            return STUDENT;
        }

        return BOOK;
    }

    public static String derivedNote(int type) {

        if (type == DEPARTMENT) {
            return "Not assigned (assign later with Update Existing Record)";
        }

        return "Not returned (set by Return Library Book)";
    }

    public static String displayValue(int type, int i, String[] v) {

        char kind = KINDS[type].charAt(i);

        if (kind == 'D') {
            if (v[i].isEmpty()) {
                return derivedNote(type);
            }

            if (type == DEPARTMENT) {
                return describe(TEACHER, v[i]);
            }

            return v[i];
        }

        if (kind == 'S') {
            if (v[i].isEmpty()) {
                return "(not selected)";
            }

            return describe(relatedKind(type, i), v[i]);
        }

        if (v[i].isEmpty()) {
            return "(empty)";
        }

        return v[i];
    }
}
