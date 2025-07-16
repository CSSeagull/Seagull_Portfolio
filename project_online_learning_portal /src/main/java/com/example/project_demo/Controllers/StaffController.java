package com.example.project_demo.Controllers;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Staff;
import com.example.project_demo.Services.CourseMaterialService;
import com.example.project_demo.Services.CourseService;
import com.example.project_demo.Services.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
public class StaffController {

    private final StaffService staffService;
    private final CourseService courseService;
    private final CourseMaterialService courseMaterialService;


    @Autowired
    public StaffController(StaffService staffService, CourseService courseService, CourseMaterialService courseMaterialService) {
        this.staffService = staffService;
        this.courseService = courseService;
        this.courseMaterialService = courseMaterialService;
    }

    @GetMapping("/aboutus")
    public String aboutUsPage() {
        return "forward:/AboutUs.html";  // Forward to the static resource
    }



    @GetMapping("/calendar")
    public String Calendar() {
        return "forward:/Calendar.html";  // Forward to the static resource
    }

    @GetMapping("/team")
    public String TeamMembers() {
        return "forward:/TeamMembers.html";  // Forward to the static resource
    }

    @GetMapping("/plan")
    public String PlanFeatures() {
        return "forward:/PlannedFeatures.html";  // Forward to the static resource
    }

    @GetMapping("/com")
    public String Completed() {
        return "forward:/CompletedFeatures.html";  // Forward to the static resource
    }

    @GetMapping("/staff/{staffName}")
    public String staffDashboard(@PathVariable String staffName, Model model) {
        Optional<Staff> optionalStaff = staffService.getStaffByName(staffName);
        if (optionalStaff.isPresent()) {
            Staff staff = optionalStaff.get();
            List<Course> courses = courseService.getCoursesByStaff(staff);
            model.addAttribute("staff", staff);
            model.addAttribute("courses", courses);
            return "StaffHomePage";
        } else {
            model.addAttribute("errorMessage", "No staff found with name " + staffName);
            return "error";
        }
    }

    @GetMapping("/upload/{courseId}")
    public String uploadCourseMaterials(@PathVariable("courseId") String courseId, Model model) {
        model.addAttribute("courseId", courseId);
        return "upload";
    }

    @PostMapping("/upload/{courseId}")
    public String uploadFile(@PathVariable("courseId") String courseId,
                             @RequestParam("title") String title,
                             @RequestParam("file") MultipartFile file,
                             Model model) throws IOException {
        model.addAttribute("courseId", courseId);
        Course course = courseService.getCourseByCourseId(courseId);
        boolean materialExists = courseMaterialService.existsByCourseAndTitle(course, title);

        if (materialExists) {
            model.addAttribute("title_message", "title_is_already_in_use");
            System.out.println("title_is_already_in_use");
            return "upload";
        }
        courseMaterialService.saveCourseMaterial(title, course, file);
        return "upload";
    }
}
//test commit