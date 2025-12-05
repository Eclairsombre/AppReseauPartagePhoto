package local.epul4a.fotoshare.controller;

import jakarta.validation.Valid;
import local.epul4a.fotoshare.dto.UserRegistrationDto;
import local.epul4a.fotoshare.model.User;
import local.epul4a.fotoshare.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    // Page d'accueil
    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Afficher le formulaire d'inscription
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    // Traiter l'inscription
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie! Vous pouvez maintenant vous connecter.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            result.rejectValue(null, "error.user", e.getMessage());
            return "register";
        }
    }

    // Afficher le formulaire de connexion
    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                               @RequestParam(value = "logout", required = false) String logout,
                               Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Nom d'utilisateur ou mot de passe incorrect.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Vous avez été déconnecté avec succès.");
        }
        return "login";
    }
}