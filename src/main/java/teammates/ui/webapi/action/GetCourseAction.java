package teammates.ui.webapi.action;

import org.apache.http.HttpStatus;

import teammates.common.datatransfer.attributes.CourseAttributes;
import teammates.common.exception.UnauthorizedAccessException;
import teammates.common.util.Const;
import teammates.ui.webapi.output.CourseData;

/**
 * Get a course for an instructor or student.
 */
public class GetCourseAction extends Action {

    @Override
    protected AuthType getMinAuthLevel() {
        return AuthType.LOGGED_IN;
    }

    @Override
    public void checkSpecificAccessControl() {
        if (userInfo.isAdmin) {
            return;
        }

        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        String entityType = getNonNullRequestParamValue(Const.ParamsNames.ENTITY_TYPE);

        if (userInfo.isInstructor && Const.EntityType.INSTRUCTOR.equals(entityType)) {
            gateKeeper.verifyAccessible(
                    logic.getInstructorForGoogleId(courseId, userInfo.getId()),
                    logic.getCourse(courseId));
            return;
        }

        if (userInfo.isStudent && Const.EntityType.STUDENT.equals(entityType)) {
            CourseAttributes course = logic.getCourse(courseId);
            gateKeeper.verifyAccessible(logic.getStudentForGoogleId(courseId, userInfo.getId()), course);
            return;
        }

        throw new UnauthorizedAccessException("Student or instructor account is required to access this resource.");
    }

    @Override
    public ActionResult execute() {
        String courseId = getNonNullRequestParamValue(Const.ParamsNames.COURSE_ID);
        CourseAttributes courseAttributes = logic.getCourse(courseId);
        if (courseAttributes == null) {
            return new JsonResult("No course with id: " + courseId, HttpStatus.SC_NOT_FOUND);
        }
        return new JsonResult(new CourseData(courseAttributes));
    }
}
