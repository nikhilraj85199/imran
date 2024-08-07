package com.mymart.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.mymart.model.Category;
import com.mymart.model.carousel;
import com.mymart.model.carouselDto;
import com.mymart.repository.carouselRepository;
import com.mymart.service.CategoryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/Admin")
public class carouselController {

    @Autowired
    private carouselRepository repo;
    
    private final CategoryService categoryService;
    
    @Autowired
    public carouselController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    
    @GetMapping("/Admincarousel")
    public String showcarouselList(Model model) {
        List<carousel> carousel = repo.findAll();
        model.addAttribute("carousel", carousel);
        return "admin/Admincarousel";
    }

    @GetMapping("/Createcarousel")
    public String showCreatecarousel(Model model) {
        carouselDto carouselDto = new carouselDto();
        model.addAttribute("carouselDto", carouselDto);
        
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/Createcarousel";
    }

    @PostMapping("/Createcarousel")
    public String createcarousel(
            @Valid @ModelAttribute carouselDto carouselDto,
            BindingResult result,
            Model model) {
        
        if(carouselDto.getImageFile().isEmpty()) {
            result.addError(new FieldError("carouselDto", "imageFile", "The image file is required"));
        }
        
        if(result.hasErrors()) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            return "admin/Createcarousel";
        }

        MultipartFile image = carouselDto.getImageFile();
        String storageFileName = image.getOriginalFilename();
        
        try {
            String uploadDir = "public/images/";
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            try(InputStream inputStream = image.getInputStream()) {
                Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch(Exception ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
        
        carousel carousel = new carousel();
        carousel.setImageFileName(storageFileName);
        carousel.setCategory(carouselDto.getCategory());
        repo.save(carousel);
        return "redirect:/Admin/Admincarousel";
    }
    
    @GetMapping("/editcarousel")
    public String showEditcarousel(Model model, @RequestParam int id) {
        try {
            carousel carousel = repo.findById(id).get();
            model.addAttribute("carousel", carousel);
            carouselDto carouselDto = new carouselDto();
            model.addAttribute("carouselDto", carouselDto);
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return "redirect:/Admin/Admincarousel";
        }
        return "admin/Editcarousel";    
    }

    @PostMapping("/editcarousel")
    public String updatecarousel(
            Model model,
            @RequestParam int id,
            @Valid @ModelAttribute carouselDto carouselDto,
            BindingResult result) {
        
        try {
            carousel carousel = repo.findById(id).get();
            model.addAttribute("carousel", carousel);

            if(result.hasErrors()) {
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
                return "admin/Editcarousel";
            }

            if(!carouselDto.getImageFile().isEmpty()) {
                String uploadDir = "public/images/";
                Path oldImagePath = Paths.get(uploadDir + carousel.getImageFileName());
                try {
                    Files.delete(oldImagePath);
                } catch(Exception e) {
                    System.out.println("Exception: " + e.getMessage());
                }

                MultipartFile image = carouselDto.getImageFile();
                String storageFileName = image.getOriginalFilename();
                try(InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, Paths.get(uploadDir + storageFileName), StandardCopyOption.REPLACE_EXISTING);        
                }
                
                carousel.setImageFileName(storageFileName);
            }
            
            carousel.setCategory(carouselDto.getCategory());
            repo.save(carousel);
            
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        
        return "redirect:/Admin/Admincarousel";
    }
    
    @GetMapping("/deletecarousel")
    public String deletecarousel(@RequestParam int id) {
        try {
            carousel carousel = repo.findById(id).get();
            Path imagePath = Paths.get("public/images/" + carousel.getImageFileName());
            try {
                Files.delete(imagePath);
            } catch(Exception e) {
                System.out.println("Exception: " + e.getMessage());
            }
            repo.delete(carousel);
        } catch(Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        
        return "redirect:/Admin/Admincarousel";
    }
}
