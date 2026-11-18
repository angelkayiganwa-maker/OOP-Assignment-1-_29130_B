package com.auca.main;

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

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.auca.main.Constants.*;
import static com.auca.main.RecordStore.*;
import static com.auca.main.TextUtils.*;


public final class RecordMapper {

    private RecordMapper() {
    }

    public static String[] valuesOf(int type, Identifiable record) {

        switch (type) {
            case DEPARTMENT:
                Department department = (Department) record;
                String head = "";
                if (department.getHeadTeacher() != null) {
                    head = department.getHeadTeacher().getId();
                }
                return new String[]{department.getDepartmentId(), department.getDepartmentName(),
                        department.getDepartmentCode(), department.getOfficeLocation(),
                        department.getContactEmail(), head};
            case STUDENT:
                Student student = (Student) record;
                return new String[]{student.getPersonId(), student.getFirstName(), student.getLastName(),
                        student.getEmail(), student.getPhoneNumber(), student.getDateOfBirth().toString(),
                        student.getMemberNumber(), student.getDepartment().getDepartmentId(),
                        student.getJoinDate().toString(), student.getAcademicStatus(),
                        student.getInstitutionalEmail(), student.getAdvisorName(),
                        student.getStudentNumber(), student.getProgramName(),
                        student.getAdmissionDate().toString(), String.valueOf(student.getStudyYear()),
                        student.getGuardianPhone(), yesNo(student.isScholarshipStatus())};
            case TEACHER:
                Teacher teacher = (Teacher) record;
                return new String[]{teacher.getPersonId(), teacher.getFirstName(), teacher.getLastName(),
                        teacher.getEmail(), teacher.getPhoneNumber(), teacher.getDateOfBirth().toString(),
                        teacher.getMemberNumber(), teacher.getDepartment().getDepartmentId(),
                        teacher.getJoinDate().toString(), teacher.getAcademicStatus(),
                        teacher.getInstitutionalEmail(), teacher.getAdvisorName(),
                        teacher.getEmployeeNumber(), teacher.getSpecialization(),
                        teacher.getHireDate().toString(), teacher.getSalary().toPlainString(),
                        teacher.getOfficeNumber(), teacher.getEmploymentType()};
            case ADMINISTRATOR:
                Administrator administrator = (Administrator) record;
                return new String[]{administrator.getPersonId(), administrator.getFirstName(),
                        administrator.getLastName(), administrator.getEmail(),
                        administrator.getPhoneNumber(), administrator.getDateOfBirth().toString(),
                        administrator.getStaffNumber(), administrator.getJobTitle(),
                        administrator.getAssignedOffice(), administrator.getEmploymentDate().toString(),
                        String.valueOf(administrator.getAccessLevel()), yesNo(administrator.isActive())};
            case COURSE:
                Course course = (Course) record;
                return new String[]{course.getCourseId(), course.getCourseCode(), course.getCourseTitle(),
                        String.valueOf(course.getCreditHours()), course.getDepartment().getDepartmentId(),
                        course.getAssignedTeacher().getId()};
            case CLASSROOM:
                Classroom classroom = (Classroom) record;
                return new String[]{classroom.getClassroomId(), classroom.getBuildingName(),
                        classroom.getRoomNumber(), String.valueOf(classroom.getCapacity()),
                        String.valueOf(classroom.getFloorNumber()), yesNo(classroom.isAvailable())};
            case ENROLLMENT:
                Enrollment enrollment = (Enrollment) record;
                return new String[]{enrollment.getEnrollmentId(), enrollment.getStudent().getId(),
                        enrollment.getCourse().getId(), enrollment.getEnrollmentDate().toString(),
                        enrollment.getSemester(), enrollment.getEnrollmentStatus()};
            case ASSESSMENT:
                Assessment assessment = (Assessment) record;
                return new String[]{assessment.getAssessmentId(), assessment.getEnrollment().getId(),
                        assessment.getAssessmentType(), String.valueOf(assessment.getMaximumScore()),
                        String.valueOf(assessment.getObtainedScore()),
                        assessment.getAssessmentDate().toString()};
            case PAYMENT:
                FeePayment payment = (FeePayment) record;
                return new String[]{payment.getPaymentId(), payment.getStudent().getId(),
                        payment.getAmount().toPlainString(), payment.getPaymentDate().toString(),
                        payment.getPaymentMethod(), payment.getReceiptNumber()};
            case BOOK:
                LibraryBook book = (LibraryBook) record;
                return new String[]{book.getBookId(), book.getIsbn(), book.getTitle(), book.getAuthor(),
                        String.valueOf(book.getTotalCopies()), String.valueOf(book.getAvailableCopies())};
            default:
                BookLoan loan = (BookLoan) record;
                String returned = "";
                if (loan.getReturnedDate() != null) {
                    returned = loan.getReturnedDate().toString();
                }
                return new String[]{loan.getLoanId(), loan.getStudent().getId(), loan.getBook().getId(),
                        loan.getBorrowedDate().toString(), loan.getDueDate().toString(), returned};
        }
    }

    public static void applyValues(int type, Identifiable record, String[] v) {

        switch (type) {
            case DEPARTMENT:
                Department department = (Department) record;
                department.setDepartmentName(v[1]);
                department.setDepartmentCode(v[2]);
                department.setOfficeLocation(v[3]);
                department.setContactEmail(v[4]);
                department.setHeadTeacher(findTeacher(v[5]));
                break;
            case STUDENT:
                Student student = (Student) record;
                student.setFirstName(v[1]);
                student.setLastName(v[2]);
                student.setEmail(v[3]);
                student.setPhoneNumber(v[4]);
                student.setDateOfBirth(LocalDate.parse(v[5]));
                student.setMemberNumber(v[6]);
                student.setDepartment(findDepartment(v[7]));
                student.setJoinDate(LocalDate.parse(v[8]));
                student.setAcademicStatus(v[9]);
                student.setInstitutionalEmail(v[10]);
                student.setAdvisorName(v[11]);
                student.setStudentNumber(v[12]);
                student.setProgramName(v[13]);
                student.setAdmissionDate(LocalDate.parse(v[14]));
                student.setStudyYear(Integer.parseInt(v[15]));
                student.setGuardianPhone(v[16]);
                student.setScholarshipStatus(v[17].equals("YES"));
                break;
            case TEACHER:
                Teacher teacher = (Teacher) record;
                teacher.setFirstName(v[1]);
                teacher.setLastName(v[2]);
                teacher.setEmail(v[3]);
                teacher.setPhoneNumber(v[4]);
                teacher.setDateOfBirth(LocalDate.parse(v[5]));
                teacher.setMemberNumber(v[6]);
                teacher.setDepartment(findDepartment(v[7]));
                teacher.setJoinDate(LocalDate.parse(v[8]));
                teacher.setAcademicStatus(v[9]);
                teacher.setInstitutionalEmail(v[10]);
                teacher.setAdvisorName(v[11]);
                teacher.setEmployeeNumber(v[12]);
                teacher.setSpecialization(v[13]);
                teacher.setHireDate(LocalDate.parse(v[14]));
                teacher.setSalary(new BigDecimal(v[15]));
                teacher.setOfficeNumber(v[16]);
                teacher.setEmploymentType(v[17]);
                break;
            case ADMINISTRATOR:
                Administrator administrator = (Administrator) record;
                administrator.setFirstName(v[1]);
                administrator.setLastName(v[2]);
                administrator.setEmail(v[3]);
                administrator.setPhoneNumber(v[4]);
                administrator.setDateOfBirth(LocalDate.parse(v[5]));
                administrator.setStaffNumber(v[6]);
                administrator.setJobTitle(v[7]);
                administrator.setAssignedOffice(v[8]);
                administrator.setEmploymentDate(LocalDate.parse(v[9]));
                administrator.setAccessLevel(Integer.parseInt(v[10]));
                administrator.setActive(v[11].equals("YES"));
                break;
            case COURSE:
                Course course = (Course) record;
                course.setCourseCode(v[1]);
                course.setCourseTitle(v[2]);
                course.setCreditHours(Integer.parseInt(v[3]));
                course.setDepartment(findDepartment(v[4]));
                course.setAssignedTeacher(findTeacher(v[5]));
                break;
            case CLASSROOM:
                Classroom classroom = (Classroom) record;
                classroom.setBuildingName(v[1]);
                classroom.setRoomNumber(v[2]);
                classroom.setCapacity(Integer.parseInt(v[3]));
                classroom.setFloorNumber(Integer.parseInt(v[4]));
                classroom.setAvailable(v[5].equals("YES"));
                break;
            case ENROLLMENT:
                Enrollment enrollment = (Enrollment) record;
                enrollment.setEnrollmentDate(LocalDate.parse(v[3]));
                enrollment.setSemester(v[4]);
                enrollment.setEnrollmentStatus(v[5]);
                break;
            case ASSESSMENT:
                Assessment assessment = (Assessment) record;
                assessment.setAssessmentType(v[2]);
                assessment.setMaximumScore(Double.parseDouble(v[3]));
                assessment.setObtainedScore(Double.parseDouble(v[4]));
                assessment.setAssessmentDate(LocalDate.parse(v[5]));
                break;
            case PAYMENT:
                FeePayment payment = (FeePayment) record;
                payment.setAmount(new BigDecimal(v[2]));
                payment.setPaymentDate(LocalDate.parse(v[3]));
                payment.setPaymentMethod(v[4]);
                payment.setReceiptNumber(v[5]);
                break;
            case BOOK:
                LibraryBook book = (LibraryBook) record;
                book.setIsbn(v[1]);
                book.setTitle(v[2]);
                book.setAuthor(v[3]);
                break;
            default:
                BookLoan loan = (BookLoan) record;
                loan.setBorrowedDate(LocalDate.parse(v[3]));
                loan.setDueDate(LocalDate.parse(v[4]));
                break;
        }
    }
}
