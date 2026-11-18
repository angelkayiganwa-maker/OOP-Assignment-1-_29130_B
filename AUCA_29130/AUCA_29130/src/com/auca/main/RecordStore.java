package com.auca.main;

import com.auca.abstracts.Person;
import com.auca.administrator.Administrator;
import com.auca.assessment.Assessment;
import com.auca.bookloan.BookLoan;
import com.auca.classroom.Classroom;
import com.auca.course.Course;
import com.auca.department.Department;
import com.auca.enrollment.Enrollment;
import com.auca.feepayment.FeePayment;
import com.auca.interfaces.Identifiable;
import com.auca.librarybook.LibraryBook;
import com.auca.student.Student;
import com.auca.teacher.Teacher;

import java.util.ArrayList;

import static com.auca.main.Constants.*;


public final class RecordStore {

    private RecordStore() {
    }

    static ArrayList<Department> departments = new ArrayList<>();

    static ArrayList<Student> students = new ArrayList<>();

    static ArrayList<Teacher> teachers = new ArrayList<>();

    static ArrayList<Administrator> administrators = new ArrayList<>();

    static ArrayList<Course> courses = new ArrayList<>();

    static ArrayList<Classroom> classrooms = new ArrayList<>();

    static ArrayList<Enrollment> enrollments = new ArrayList<>();

    static ArrayList<Assessment> assessments = new ArrayList<>();

    static ArrayList<FeePayment> payments = new ArrayList<>();

    static ArrayList<LibraryBook> books = new ArrayList<>();

    static ArrayList<BookLoan> loans = new ArrayList<>();

    public static ArrayList<Identifiable> recordsOfType(int type) {

        ArrayList<Identifiable> list = new ArrayList<>();

        switch (type) {
            case DEPARTMENT:
                list.addAll(departments);
                break;
            case STUDENT:
                list.addAll(students);
                break;
            case TEACHER:
                list.addAll(teachers);
                break;
            case ADMINISTRATOR:
                list.addAll(administrators);
                break;
            case COURSE:
                list.addAll(courses);
                break;
            case CLASSROOM:
                list.addAll(classrooms);
                break;
            case ENROLLMENT:
                list.addAll(enrollments);
                break;
            case ASSESSMENT:
                list.addAll(assessments);
                break;
            case PAYMENT:
                list.addAll(payments);
                break;
            case BOOK:
                list.addAll(books);
                break;
            case LOAN:
                list.addAll(loans);
                break;
            default:
                break;
        }

        return list;
    }

    public static ArrayList<Identifiable> getAllRecords() {

        ArrayList<Identifiable> records = new ArrayList<>();

        for (int type = DEPARTMENT; type <= LOAN; type++) {
            records.addAll(recordsOfType(type));
        }

        return records;
    }

    public static ArrayList<Person> getAllPeople() {

        ArrayList<Person> people = new ArrayList<>();

        people.addAll(students);
        people.addAll(teachers);
        people.addAll(administrators);

        return people;
    }

    public static Identifiable findRecord(int type, String id) {

        for (Identifiable record : recordsOfType(type)) {
            if (record.getId().equalsIgnoreCase(id)) {
                return record;
            }
        }

        return null;
    }

    public static Department findDepartment(String id) {
        return (Department) findRecord(DEPARTMENT, id);
    }

    public static Student findStudent(String id) {
        return (Student) findRecord(STUDENT, id);
    }

    public static Teacher findTeacher(String id) {
        return (Teacher) findRecord(TEACHER, id);
    }

    public static Course findCourse(String id) {
        return (Course) findRecord(COURSE, id);
    }

    public static Enrollment findEnrollment(String id) {
        return (Enrollment) findRecord(ENROLLMENT, id);
    }

    public static LibraryBook findBook(String id) {
        return (LibraryBook) findRecord(BOOK, id);
    }

    public static String label(int type, Identifiable record) {

        String id = "[" + record.getId() + "] ";

        switch (type) {
            case STUDENT:
                Student student = (Student) record;
                return id + student.getDisplayName() + " (" + student.getStudentNumber() + ")";
            case TEACHER:
                Teacher teacher = (Teacher) record;
                return id + teacher.getDisplayName() + " - " + teacher.getDepartment().getDepartmentName();
            case COURSE:
                Course course = (Course) record;
                return id + course.getCourseCode() + " - " + course.getCourseTitle();
            case ENROLLMENT:
                Enrollment enrollment = (Enrollment) record;
                return id + enrollment.getStudent().getDisplayName() + " - "
                        + enrollment.getCourse().getCourseTitle()
                        + " (" + enrollment.getSemester() + ", " + enrollment.getEnrollmentStatus() + ")";
            case BOOK:
                LibraryBook book = (LibraryBook) record;
                return id + book.getTitle() + " (" + book.getAvailableCopies() + " available)";
            case LOAN:
                BookLoan loan = (BookLoan) record;
                return id + loan.getStudent().getDisplayName() + " - " + loan.getBook().getTitle()
                        + " (due " + loan.getDueDate() + ")";
            default:
                return id + record.getDisplayName();
        }
    }

    public static String describe(int type, String id) {

        Identifiable record = findRecord(type, id);

        if (record == null) {
            return "(none)";
        }

        return label(type, record);
    }
}
