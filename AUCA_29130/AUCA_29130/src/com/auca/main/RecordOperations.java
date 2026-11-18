package com.auca.main;

import com.auca.bookloan.BookLoan;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;
import com.auca.interfaces.Identifiable;
import com.auca.interfaces.Registrable;
import com.auca.librarybook.LibraryBook;
import com.auca.teacher.Teacher;

import java.time.LocalDate;
import java.util.ArrayList;

import static com.auca.main.ConsoleIO.*;
import static com.auca.main.Constants.*;
import static com.auca.main.FieldDefinitions.*;
import static com.auca.main.FormEngine.*;
import static com.auca.main.RecordMapper.*;
import static com.auca.main.RecordStore.*;
import static com.auca.main.TextUtils.*;
import static com.auca.main.Validator.*;


public final class RecordOperations {

    private RecordOperations() {
    }

    public static void returnBook()
            throws RecordNotFoundException, ValidationException {

        System.out.println("\n========== RETURN LIBRARY BOOK ==========");

        ArrayList<BookLoan> activeLoans = new ArrayList<>();

        for (BookLoan loan : loans) {
            if (loan.isActive()) {
                activeLoans.add(loan);
            }
        }

        if (activeLoans.isEmpty()) {
            throw new RecordNotFoundException("There is no active loan to return.");
        }

        System.out.println("Active loans:");

        for (int i = 0; i < activeLoans.size(); i++) {
            System.out.println((i + 1) + ". " + label(LOAN, activeLoans.get(i)));
        }

        System.out.println("0. Cancel");

        int choice = readNumber("Select loan: ");

        if (choice == 0) {
            System.out.println("Return cancelled.");
            return;
        }

        if (choice < 1 || choice > activeLoans.size()) {
            throw new RecordNotFoundException("Invalid loan selection.");
        }

        BookLoan loan = activeLoans.get(choice - 1);
        LocalDate returnedDate = null;

        while (returnedDate == null) {
            String line = readString("Returned date (LocalDate, YYYY-MM-DD, CANCEL to stop): ");

            if (line.equalsIgnoreCase("CANCEL")) {
                System.out.println("Return cancelled. Stock was not changed.");
                return;
            }

            try {
                LocalDate date = parseDate(line);

                if (date.isAfter(LocalDate.now())) {
                    throw new ValidationException("Returned date cannot be in the future.");
                }

                if (date.isBefore(loan.getBorrowedDate())) {
                    throw new ValidationException("Returned date cannot be before the borrowed date ("
                            + loan.getBorrowedDate() + ").");
                }

                returnedDate = date;
            } catch (ValidationException e) {
                System.out.println("! " + e.getMessage());
            }
        }

        System.out.println("\nReturn summary: " + label(LOAN, loan) + " returned on " + returnedDate);
        System.out.println("1. Confirm return   0. Cancel");

        if (readNumber("Select option: ") != 1) {
            System.out.println("Return cancelled. Stock was not changed.");
            return;
        }

        LibraryBook book = loan.getBook();

        if (book.getAvailableCopies() >= book.getTotalCopies()) {
            throw new ValidationException("Stock records show all copies are already in the library.");
        }

        loan.setReturnedDate(returnedDate);
        loan.validate();
        book.setAvailableCopies(book.getAvailableCopies() + 1);

        System.out.println("\n+ Loan " + loan.getLoanId() + " returned successfully.");
        System.out.println("  Available copies of " + book.getTitle() + ": " + book.getAvailableCopies()
                + " of " + book.getTotalCopies());
        loan.printDetails();
    }

    public static void updateRecord() throws RecordNotFoundException {

        System.out.println("\n========== UPDATE EXISTING RECORD ==========");

        for (int type = DEPARTMENT; type <= LOAN; type++) {
            System.out.println(type + ". " + NAMES[type]);
        }

        System.out.println("0. Cancel");

        int type = readNumber("Select record type: ");

        if (type == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        if (type < DEPARTMENT || type > LOAN) {
            throw new RecordNotFoundException("Invalid record type selection.");
        }

        ArrayList<Identifiable> list = recordsOfType(type);

        if (list.isEmpty()) {
            throw new RecordNotFoundException("No " + NAMES[type].toLowerCase() + " record exists to update.");
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.println((i + 1) + ". " + label(type, list.get(i)));
        }

        System.out.println("0. Cancel");

        int choice = readNumber("Select record: ");

        if (choice == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        if (choice < 1 || choice > list.size()) {
            throw new RecordNotFoundException("Invalid record selection.");
        }

        Identifiable record = list.get(choice - 1);
        String selfId = record.getId();
        String[] values = valuesOf(type, record);
        String[] original = copyOf(values);
        String[] labels = fieldLabels(type);
        boolean changed = false;

        while (true) {

            System.out.println("\n---------- UPDATE " + NAMES[type].toUpperCase() + " " + selfId + " ----------");

            for (int i = 0; i < values.length; i++) {
                String note = "";

                if (!isMutable(type, i) && !(type == DEPARTMENT && i == 5)) {
                    note = "  (locked)";
                }

                System.out.println(two(i + 1) + " " + String.format("%-22s", shortLabel(labels[i]))
                        + ": " + displayValue(type, i, values) + note);
            }

            int number = readNumber("Field number to edit (0 to finish): ");

            if (number == 0) {
                break;
            }

            if (number < 1 || number > values.length) {
                System.out.println("! Invalid field number.");
                continue;
            }

            int index = number - 1;

            boolean headTeacherField = type == DEPARTMENT && index == 5;

            if (!isMutable(type, index) && !headTeacherField) {
                System.out.println("! This field is locked and cannot be changed.");
                continue;
            }

            String[] backup = copyOf(values);
            String result;

            if (headTeacherField) {
                result = chooseHeadTeacher(values);
            } else {
                result = askField(type, index, values, selfId);
            }

            if (result.equals("OK")) {
                if (fixInvalidFields(type, values, selfId)) {
                    changed = true;
                } else {
                    for (int k = 0; k < values.length; k++) {
                        values[k] = backup[k];
                    }

                    System.out.println("Change undone. The old values were restored.");
                }
            } else {
                System.out.println("No change made to that field.");
            }
        }

        if (!changed) {
            System.out.println("No changes were made.");
            return;
        }

        System.out.println("\nThe record will be changed to:");

        for (int i = 0; i < values.length; i++) {
            System.out.println(two(i + 1) + " " + String.format("%-22s", shortLabel(labels[i]))
                    + ": " + displayValue(type, i, values));
        }

        System.out.println("1. Confirm and save changes   0. Discard changes");

        if (readNumber("Select option: ") != 1) {
            System.out.println("Changes discarded. The record was not modified.");
            return;
        }

        applyValues(type, record, values);

        try {
            ((Registrable) record).validate();
            System.out.println("\n+ " + NAMES[type] + " " + selfId + " updated successfully.");
            ((Registrable) record).printDetails();
        } catch (ValidationException e) {
            applyValues(type, record, original);
            System.out.println("\n! Update rejected: " + e.getMessage());
            System.out.println("  The original record was restored.");
        }
    }

    public static String chooseHeadTeacher(String[] values) {

        ArrayList<Teacher> candidates = new ArrayList<>();

        for (Teacher teacher : teachers) {
            if (teacher.getDepartment().getId().equalsIgnoreCase(values[0])) {
                candidates.add(teacher);
            }
        }

        if (candidates.isEmpty()) {
            System.out.println("! No teacher belongs to this department yet. Register a teacher in it first.");
            return "NONE";
        }

        System.out.println("\nTeachers of this department:");

        for (int i = 0; i < candidates.size(); i++) {
            System.out.println((i + 1) + ". " + label(TEACHER, candidates.get(i)));
        }

        System.out.println("0. No head teacher");

        int choice = readNumber("Select head teacher: ");

        if (choice == 0) {
            values[5] = "";
            return "OK";
        }

        if (choice < 1 || choice > candidates.size()) {
            System.out.println("! Invalid selection.");
            return "NONE";
        }

        values[5] = candidates.get(choice - 1).getId();
        return "OK";
    }
}
