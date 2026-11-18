package com.auca.main;


public final class Constants {

    private Constants() {
    }

    static final int DEPARTMENT = 1;

    static final int STUDENT = 2;

    static final int TEACHER = 3;

    static final int ADMINISTRATOR = 4;

    static final int COURSE = 5;

    static final int CLASSROOM = 6;

    static final int ENROLLMENT = 7;

    static final int ASSESSMENT = 8;

    static final int PAYMENT = 9;

    static final int BOOK = 10;

    static final int LOAN = 11;

    static final String[] NAMES = {"", "Department", "Student", "Teacher", "Administrator",
            "Course", "Classroom", "Enrollment", "Assessment", "Fee payment",
            "Library book", "Book loan"};

    static final String[] KINDS = {"", "TTTTTD", "TTTTTTTSTCTTTTTTTC", "TTTTTTTSTCTTTTTTTC",
            "TTTTTTTTTTTC", "TTTTSS", "TTTTTC", "TSSTCC", "TSCTTT", "TSTTCT", "TTTTTT", "TSSTTD"};

    static final int[][] STEP_STARTS = {{}, {0, 2, 4}, {0, 6, 12}, {0, 6, 12}, {0, 6, 9},
            {0, 2, 4}, {0, 2, 4}, {0, 2, 4}, {0, 2, 4}, {0, 2, 4}, {0, 2, 4}, {0, 2, 4}};

    static final String[][] STEP_TITLES = {
            {},
            {"Basic details", "Location details", "Contact details"},
            {"Personal details", "Academic details", "Student details"},
            {"Personal details", "Academic details", "Teacher details"},
            {"Personal details", "Staff details", "Employment details"},
            {"Course identity", "Course details", "Course relations"},
            {"Room identity", "Room location", "Capacity details"},
            {"Identity & student", "Course & date", "Semester & status"},
            {"Identity & enrollment", "Type & maximum", "Result & date"},
            {"Identity & student", "Amount & date", "Method & receipt"},
            {"Identity & ISBN", "Title & author", "Copies"},
            {"Identity & student", "Book & borrow date", "Due & return"}};

    static final String[] YES_NO = {"YES", "NO"};

    static final String[] ACADEMIC_STATUS = {"ACTIVE", "INACTIVE"};

    static final String[] EMPLOYMENT_TYPES = {"FULL_TIME", "PART_TIME", "CONTRACT"};

    static final String[] ENROLLMENT_STATUS = {"ACTIVE", "COMPLETED", "CANCELLED"};

    static final String[] SEMESTERS = {"FALL", "SPRING", "SUMMER"};

    static final String[] ASSESSMENT_TYPES = {"QUIZ", "ASSIGNMENT", "MIDTERM", "FINAL_EXAM", "PROJECT"};

    static final String[] PAYMENT_METHODS = {"CASH", "MOBILE_MONEY", "BANK_TRANSFER", "CARD"};
}
