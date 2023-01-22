package org.acme.schooltimetabling.solver;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.inject.Inject;

import org.acme.schooltimetabling.bootstrap.DemoDataGenerator;
import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

class TimeTableConstraintProviderTest {

    private static List<Lesson> lessons;

    private static List<Timeslot> timeslots;

    private static final Room ROOM1 = new Room(1, "Room1");
    private static final Room ROOM2 = new Room(2, "Room2");
    private static final Timeslot BEFORE_EXAM = new Timeslot(1, LocalDate.of(2023, 2, 16), LocalTime.NOON);
    private static final Timeslot AFTER_EXAM = new Timeslot(2, LocalDate.of(2023, 3, 4), LocalTime.NOON);
    private static final Timeslot DURING_EXAM = new Timeslot(3, LocalDate.of(2023, 2, 18), LocalTime.NOON.plusHours(1));
//    private static final Timeslot TIMESLOT4 = new Timeslot(4, DayOfWeek.TUESDAY, LocalTime.NOON.plusHours(3));

    ConstraintVerifier<TimeTableConstraintProvider, TimeTable> constraintVerifier = ConstraintVerifier.build(
            new TimeTableConstraintProvider(), TimeTable.class, Lesson.class);

    @BeforeAll
    public static void init() {
        lessons = DemoDataGenerator.generateLessons();
        timeslots = DemoDataGenerator.generateTimeslots();
    }

    @Test
    void teacherConflict() {
        String conflictingTeacher = "Teacher1";
        Lesson firstLesson = new Lesson(1, "Subject1", conflictingTeacher, "Group1", BEFORE_EXAM, ROOM1);
        Lesson conflictingLesson = new Lesson(2, "Subject2", conflictingTeacher, "Group2", BEFORE_EXAM, ROOM2);
        Lesson nonConflictingLesson = new Lesson(3, "Subject3", "Teacher2", "Group3", AFTER_EXAM, ROOM1);
        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void studentGroupConflict() {
        String conflictingGroup = "Group1";
        Lesson firstLesson = new Lesson(1, "Subject1", "Teacher1", conflictingGroup, BEFORE_EXAM, ROOM1);
        Lesson conflictingLesson = new Lesson(2, "Subject2", "Teacher2", conflictingGroup, BEFORE_EXAM, ROOM2);
        Lesson nonConflictingLesson = new Lesson(3, "Subject3", "Teacher3", "Group3", AFTER_EXAM, ROOM1);
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGroupConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }

//    @Test
//    void teacherTimeEfficiency() {
//        String teacher = "Teacher1";
//        Lesson singleLessonOnMonday = new Lesson(1, "Subject1", teacher, "Group1", TIMESLOT1, ROOM1);
//        Lesson firstTuesdayLesson = new Lesson(2, "Subject2", teacher, "Group2", TIMESLOT2, ROOM1);
//        Lesson secondTuesdayLesson = new Lesson(3, "Subject3", teacher, "Group3", TIMESLOT3, ROOM1);
//        Lesson thirdTuesdayLessonWithGap = new Lesson(4, "Subject4", teacher, "Group4", TIMESLOT4, ROOM1);
//        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherTimeEfficiency)
//                .given(singleLessonOnMonday, firstTuesdayLesson, secondTuesdayLesson, thirdTuesdayLessonWithGap)
//                .rewardsWith(1); // Second tuesday lesson immediately follows the first.
//    }

    @Test
    void workTimeTest() {
        String conflictingGroup = "Group1";
        Lesson firstLesson = new Lesson(1, "Subject1", "Teacher1", conflictingGroup, BEFORE_EXAM, ROOM1);
        Lesson conflictingLesson = new Lesson(2, "Subject2", "Teacher2", conflictingGroup, BEFORE_EXAM, ROOM2);
        Lesson nonConflictingLesson = new Lesson(3, "Subject3", "Teacher3", "Group3", AFTER_EXAM, ROOM1);
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGroupConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);
    }


}
