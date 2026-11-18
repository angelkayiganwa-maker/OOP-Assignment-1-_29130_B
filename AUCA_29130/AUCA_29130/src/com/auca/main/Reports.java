package com.auca.main;

import com.auca.abstracts.AcademicMember;
import com.auca.abstracts.Person;
import com.auca.assessment.Assessment;
import com.auca.enrollment.Enrollment;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;

import java.math.BigDecimal;
import java.util.ArrayList;

import static com.auca.main.ConsoleIO.*;
import static com.auca.main.Constants.*;
import static com.auca.main.RecordStore.*;


public final class Reports {

    private Reports() {
    }

    public static void viewAllRecords() {

        System.out.println("\n========== ALL REGISTERED RECORDS ==========");

        for (int type = DEPARTMENT; type <= LOAN; type++) {

            ArrayList<Identifiable> list = recordsOfType(type);

            System.out.println("\n---------- " + NAMES[type].toUpperCase() + " (" + list.size() + ") ----------");

            if (list.isEmpty()) {
                System.out.println("No " + NAMES[type].toLowerCase() + " records registered.");
            }

            for (Identifiable record : list) {
                ((Registrable) record).printDetails();
                System.out.println("-------------------------");
            }
        }

        readString("\nPress ENTER to return to the menu...");
    }

    public static void searchById() {

        System.out.println("\n========== SEARCH RECORD BY ID ==========");

        String id = readString("Enter record ID (blank to cancel): ");

        if (id.isEmpty()) {
            System.out.println("Search cancelled.");
            return;
        }

        boolean found = false;

        for (int type = DEPARTMENT; type <= LOAN; type++) {
            for (Identifiable record : recordsOfType(type)) {
                if (record.getId().equalsIgnoreCase(id)) {
                    found = true;
                    System.out.println("\nRecord found (" + NAMES[type] + "): " + label(type, record));
                    ((Registrable) record).printDetails();
                }
            }
        }

        if (!found) {
            System.out.println("No record found with ID: " + id);
        }
    }

    public static Student pickStudent() throws RecordNotFoundException {

        if (students.isEmpty()) {
            throw new RecordNotFoundException("No student is registered yet.");
        }

        System.out.println("\nStudents:");

        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + label(STUDENT, students.get(i)));
        }

        System.out.println("0. Cancel");

        int choice = readNumber("Select student: ");

        if (choice == 0) {
            return null;
        }

        if (choice < 1 || choice > students.size()) {
            throw new RecordNotFoundException("Invalid student selection.");
        }

        return students.get(choice - 1);
    }

    public static void showStudentAcademicSummary() throws RecordNotFoundException {

        System.out.println("\n========== STUDENT ACADEMIC SUMMARY ==========");

        Student student = pickStudent();

        if (student == null) {
            System.out.println("Cancelled.");
            return;
        }

        student.printDetails();

        System.out.println("\n---------- ENROLLMENTS ----------");

        int enrollmentCount = 0;

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudent().equals(student)) {
                enrollmentCount++;
                System.out.println(enrollmentCount + ". " + label(ENROLLMENT, enrollment)
                        + " - " + enrollment.getCourse().getCreditHours() + " credit hours");
            }
        }

        if (enrollmentCount == 0) {
            System.out.println("No enrollments recorded for this student.");
        }

        System.out.println("\n---------- ASSESSMENTS ----------");

        double totalObtained = 0;
        double totalMaximum = 0;
        int assessmentCount = 0;

        for (Assessment assessment : assessments) {
            if (assessment.getEnrollment().getStudent().equals(student)) {
                assessmentCount++;
                totalObtained = totalObtained + assessment.getObtainedScore();
                totalMaximum = totalMaximum + assessment.getMaximumScore();

                double percentage = assessment.getObtainedScore() * 100 / assessment.getMaximumScore();

                System.out.println(assessmentCount + ". " + assessment.getAssessmentType()
                        + " in " + assessment.getEnrollment().getCourse().getCourseTitle()
                        + ": " + assessment.getObtainedScore() + "/" + assessment.getMaximumScore()
                        + " (" + String.format("%.1f", percentage) + "%)");
            }
        }

        if (assessmentCount == 0) {
            System.out.println("No assessments recorded for this student.");
        } else {
            System.out.println("Overall: " + totalObtained + "/" + totalMaximum
                    + " (" + String.format("%.1f", totalObtained * 100 / totalMaximum) + "%)");
        }

        readString("\nPress ENTER to return to the menu...");
    }

    public static void showPaymentHistory() throws RecordNotFoundException {

        System.out.println("\n========== STUDENT PAYMENT HISTORY ==========");

        Student student = pickStudent();

        if (student == null) {
            System.out.println("Cancelled.");
            return;
        }

        BigDecimal total = BigDecimal.ZERO;
        int count = 0;

        for (FeePayment payment : payments) {
            if (payment.getStudent().equals(student)) {
                count++;
                total = total.add(payment.getAmount());
                payment.printDetails();
                System.out.println("-------------------------");
            }
        }

        if (count == 0) {
            System.out.println("No payment records found for " + student.getDisplayName() + ".");
        }

        System.out.println("Payments made: " + count);
        System.out.println("Total paid: " + total);

        readString("\nPress ENTER to return to the menu...");
    }

    public static void showAvailableBooks() {

        System.out.println("\n========== AVAILABLE LIBRARY BOOKS ==========");

        int count = 0;

        for (LibraryBook book : books) {
            if (book.getAvailableCopies() > 0) {
                count++;
                book.printDetails();
                System.out.println("-------------------------");
            }
        }

        if (count == 0) {
            System.out.println("No book has an available copy right now.");
        }

        readString("\nPress ENTER to return to the menu...");
    }

    public static void demonstratePolymorphism() {

        System.out.println("\n========== POLYMORPHISM DEMONSTRATION ==========");

        ArrayList<Person> people = getAllPeople();

        System.out.println("\n---------- PEOPLE (Person references) ----------");

        if (people.isEmpty()) {
            System.out.println("No people registered.");
        }

        for (Person person : people) {

            System.out.println("\nName: " + person.getDisplayName());
            System.out.println("ID: " + person.getId());
            System.out.println("Role: " + person.getRoleDescription());
            System.out.println("Active: " + person.isActive());

            if (person instanceof AcademicMember) {
                AcademicMember member = (AcademicMember) person;
                System.out.println("Academic category: " + member.getAcademicCategory());
                System.out.println("Department: " + member.getDepartment().getDepartmentName());
            }

            System.out.println("Summary: " + person.getRegistrationSummary());
        }

        System.out.println("\n---------- ALL RECORDS (Identifiable references) ----------");

        ArrayList<Identifiable> records = getAllRecords();

        if (records.isEmpty()) {
            System.out.println("No records registered.");
        }

        for (Identifiable record : records) {
            System.out.println("\nID: " + record.getId());
            System.out.println("Name: " + record.getDisplayName());
            System.out.println("Active: " + record.isActive());

            if (record instanceof Registrable) {
                Registrable registrable = (Registrable) record;
                System.out.println("Summary: " + registrable.getRegistrationSummary());
            }
        }

        readString("\nPress ENTER to return to the menu...");
    }
}
