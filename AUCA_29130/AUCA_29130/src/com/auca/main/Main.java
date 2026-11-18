package com.auca.main;

import com.auca.exceptions.BookUnavailableException;
import com.auca.exceptions.DuplicateRecordException;
import com.auca.exceptions.PrerequisiteMissingException;
import com.auca.exceptions.RecordNotFoundException;
import com.auca.exceptions.ValidationException;

import static com.auca.main.ConsoleIO.*;
import static com.auca.main.FormEngine.*;
import static com.auca.main.RecordOperations.*;
import static com.auca.main.Reports.*;


public class Main {

    public static void main(String[] args) {

        int choice;

        do {
            displayMenu();
            choice = readInt("Select an option: ");

            try {
                runMenuChoice(choice);
            } catch (ValidationException | DuplicateRecordException
                     | RecordNotFoundException | PrerequisiteMissingException
                     | BookUnavailableException e) {
                System.out.println("\n! " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("\n! Unexpected problem: " + e.getMessage());
                System.out.println("  Returning to the main menu.");
            }
        } while (choice != 0);
    }

    public static void runMenuChoice(int choice)
            throws ValidationException, DuplicateRecordException,
            RecordNotFoundException, PrerequisiteMissingException,
            BookUnavailableException {

        switch (choice) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
                registerFromMenu(choice);
                break;
            case 12:
                returnBook();
                break;
            case 13:
                viewAllRecords();
                break;
            case 14:
                searchById();
                break;
            case 15:
                updateRecord();
                break;
            case 16:
                showStudentAcademicSummary();
                break;
            case 17:
                showPaymentHistory();
                break;
            case 18:
                showAvailableBooks();
                break;
            case 19:
                demonstratePolymorphism();
                break;
            case 0:
                System.out.println("\nThank you for using AUCA_ID School Management System.");
                break;
            default:
                System.out.println("\n! Invalid menu choice. Enter a number from 0 to 19.");
        }
    }

    public static void displayMenu() {

        System.out.println();
        System.out.println("============== AUCA_ID SCHOOL MANAGEMENT ==============");
        System.out.println(String.format("%-34s%s", " 1. Register Department", "11. Borrow Library Book"));
        System.out.println(String.format("%-34s%s", " 2. Register Student", "12. Return Library Book"));
        System.out.println(String.format("%-34s%s", " 3. Register Teacher", "13. View All Records"));
        System.out.println(String.format("%-34s%s", " 4. Register Administrator", "14. Search Record by ID"));
        System.out.println(String.format("%-34s%s", " 5. Register Course", "15. Update Existing Record"));
        System.out.println(String.format("%-34s%s", " 6. Register Classroom", "16. Student Academic Summary"));
        System.out.println(String.format("%-34s%s", " 7. Enroll Student", "17. Student Payment History"));
        System.out.println(String.format("%-34s%s", " 8. Record Assessment", "18. View Available Books"));
        System.out.println(String.format("%-34s%s", " 9. Record Fee Payment", "19. Demonstrate Polymorphism"));
        System.out.println(String.format("%-34s%s", "10. Register Library Book", " 0. Exit"));
    }
}
