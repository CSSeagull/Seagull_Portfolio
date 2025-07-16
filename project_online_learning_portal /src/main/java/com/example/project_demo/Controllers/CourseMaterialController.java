package com.example.project_demo.Controllers;


import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.CourseMaterial;
import com.example.project_demo.Services.CourseMaterialService;
import com.example.project_demo.Services.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Controller
public class CourseMaterialController {

    @Autowired
    private CourseMaterialService courseMaterialService;

    @Autowired
    private CourseService courseService;


    @GetMapping("/materials/{courseId}")
    public String getCourseMaterialsForUsers(@PathVariable String courseId, Model model) {
        List<CourseMaterial> material = courseMaterialService.findByCourse(courseService.getCourseByCourseId(courseId));
        model.addAttribute("material", material);
        model.addAttribute("courseId", courseId);
        return "CourseMaterialsPage";
    }

    @GetMapping("/staff/materials/{courseId}")
    public String getCourseMaterialsForStaff(@PathVariable String courseId, Model model) {
        List<CourseMaterial> material = courseMaterialService.findByCourse(courseService.getCourseByCourseId(courseId));
        model.addAttribute("material", material);
        model.addAttribute("courseId", courseId);
        return "CourseMaterialsStaff";
    }


    @GetMapping("/edit/{courseId}/{title}")
    public String editMaterialForm(@PathVariable String courseId, @PathVariable String title, Model model) {
        CourseMaterial material = courseMaterialService.findByCourseAndTitle(courseService.getCourseByCourseId(courseId), title);

        model.addAttribute("material", material);
        model.addAttribute("courseId", courseId);
        return "editCourseMaterial";
    }

    @PutMapping("/edit/{courseId}/{title}")
    public ResponseEntity<Void> editMaterial(@PathVariable String courseId,
                                             @PathVariable String title,
                                             @RequestParam("newTitle") String newTitle) {

        boolean materialExists = courseMaterialService.existsByCourseAndTitle(courseService.getCourseByCourseId(courseId), title);
        System.out.println(materialExists);
        if (!materialExists){
            return null;
        } else {
            courseMaterialService.updateTitle(courseService.getCourseByCourseId(courseId), title, newTitle);
        }
        return null;
    }

    @DeleteMapping("/staff/delete/{courseId}/{title}")
    public ResponseEntity<Void> deleteCourseMaterial(@PathVariable String courseId,
                                                     @PathVariable String title) {
        courseMaterialService.deleteByCourseAndTitle(courseService.getCourseByCourseId(courseId), title);
        return null;
    }

    @GetMapping("/view/{courseId}/{title}")
    public String viewFile(@PathVariable String courseId, @PathVariable String title, Model model) {
        CourseMaterial material = courseMaterialService.findByCourseAndTitle(courseService.getCourseByCourseId(courseId), title);

        if (material != null) {
            byte[] fileData = material.getFileData();
            String base64Encoded = Base64.getEncoder().encodeToString(fileData);

            model.addAttribute("pdfData", base64Encoded);
            model.addAttribute("title", material.getTitle());
            model.addAttribute("courseId", courseId);
        } else {
            model.addAttribute("message", "File not found");
        }
        return "viewFile";
    }

}