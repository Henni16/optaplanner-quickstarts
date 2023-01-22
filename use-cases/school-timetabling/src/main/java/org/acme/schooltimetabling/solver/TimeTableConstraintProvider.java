package org.acme.schooltimetabling.solver;

import org.acme.schooltimetabling.domain.Lesson;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.*;
import org.optaplanner.core.api.score.stream.bi.BiJoiner;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;

import static java.time.temporal.ChronoUnit.*;


public class TimeTableConstraintProvider implements ConstraintProvider {

    private Map<String, Double> teachers = new HashMap<>();

    public TimeTableConstraintProvider() {
        this.teachers.put("Sonja", 13.5);
        this.teachers.put("Barbara", 9d);
        this.teachers.put("Susanne", 13.5);
        this.teachers.put("Stefanie Puhl", 10.8);
        this.teachers.put("Tanja", 18d);
        this.teachers.put("Anette", 18d);
        this.teachers.put("Jasmin", 18d);
        this.teachers.put("Carolin", 10.8);
        this.teachers.put("Roland", 18d);
        this.teachers.put("Sabrina", 10.8);
        this.teachers.put("Andreas", 18d);
        this.teachers.put("Andrea", 18d);
        this.teachers.put("Norbert", 18d);
        this.teachers.put("Martin", 18d);
        this.teachers.put("Stefanie Hermann", 9d);
        this.teachers.put("Laura", 9d);
        this.teachers.put("Hanna", 18d);
        this.teachers.put("Dorothea", 15.3);
        this.teachers.put("Isabelle", 18d);
        this.teachers.put("Anna", 10.8);
        this.teachers.put("Michael", 18d);
        this.teachers.put("Barbel", 9d);
        this.teachers.put("Julia", 18d);
        this.teachers.put("Elena", 18d);
    }


    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                teacherConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                workTime(constraintFactory),
                examTime(constraintFactory),
                examConsecutive(constraintFactory),
                needConsecutive(constraintFactory),
                // Soft constraints
                teacherTimeEfficiency(constraintFactory),
                subjectClose(constraintFactory)
        };
    }

    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        // A teacher can teach at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getTeacher))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher conflict");
    }

    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        // A student can attend at most one lesson at the same time.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTimeslot),
                        Joiners.equal(Lesson::getStudentGroup))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group conflict");
    }

    Constraint teacherTimeEfficiency(ConstraintFactory constraintFactory) {
        // A teacher prefers to teach sequential lessons and dislikes gaps between lessons.
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
                        Joiners.equal((lesson) -> lesson.getTimeslot().getDate()))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0;
                })
                .reward(HardSoftScore.ONE_SOFT)
                .asConstraint("Teacher time efficiency");
    }

    Constraint subjectClose(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class,
                        Joiners.equal(Lesson::getSubject))
                .map((a,b) -> WEEKS.between(a.getTimeslot().getDate(), b.getTimeslot().getDate()))
                .filter(weeks -> weeks > 0)
                .penalize(HardSoftScore.ONE_SOFT, Long::intValue)
                .asConstraint("Subject Close");
    }

    Constraint workTime(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .groupBy(lesson -> lesson.getTimeslot().getDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR), Lesson::getTeacher, ConstraintCollectors.sum(lesson -> Math.toIntExact(HOURS.between(lesson.getTimeslot().getStartTime(), lesson.getTimeslot().getEndTime()))))
                .filter((week, teacher, hours) -> hours > teachers.get(teacher) + 2)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Maximum working hours per week");
    }

    Constraint examTime(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(Lesson.class)
                .filter(lesson -> lesson.getSubject().contains("Klausur"))
                .filter(lesson -> lesson.getTimeslot().getDate().isBefore(LocalDate.of(2023, 2, 17)) || lesson.getTimeslot().getDate().isAfter(LocalDate.of(2022, 3, 3)))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Exam dates");
    }

    Constraint examConsecutive(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class, Joiners.filtering((a, b) -> a.getSubject().contains("Klausur") && b.getSubject().contains("Klausur")))
                .filter((a,b) -> DAYS.between(a.getTimeslot().getDate(), b.getTimeslot().getDate()) < 2)
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Exam consecutive");
    }

    Constraint needConsecutive(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEachUniquePair(Lesson.class, Joiners.equal(Lesson::getSubject))
                .filter((a,b) -> a.getSubject().equals("13.4") || a.getSubject().equals("11.5"))
                .filter((lesson1, lesson2) -> {
                    Duration between = Duration.between(lesson1.getTimeslot().getEndTime(),
                            lesson2.getTimeslot().getStartTime());
                    return !lesson1.getTimeslot().getDate().equals(lesson2.getTimeslot().getDate()) ||(!between.isNegative() && between.compareTo(Duration.ofMinutes(30)) <= 0);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("13.4 consecutive");
    }


}
