package local.epul4a.fotoshare.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestionnaire global des exceptions pour l'application.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gere l'exception de depassement de taille de fichier.
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(MaxUploadSizeExceededException exc,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
            "Le fichier est trop volumineux. La taille maximale autorisee est de 10 MB.");

        return "redirect:/photos/upload";
    }
}

