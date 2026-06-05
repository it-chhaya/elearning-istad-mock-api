package co.istad.chhaya.elearning.features.course;

import co.istad.chhaya.elearning.features.course.dto.CourseResponse;
import co.istad.chhaya.elearning.features.course.dto.CreateCourseRequest;

public interface CourseService {

    // Create a new course
    CourseResponse createCourse(CreateCourseRequest createCourseRequest);

}
