package local.epul4a.fotoshare.service;

import local.epul4a.fotoshare.dto.UserRegistrationDto;
import local.epul4a.fotoshare.model.ROLE_USER;
import local.epul4a.fotoshare.model.User;
import local.epul4a.fotoshare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Le nom d'utilisateur existe déjà");
        }

        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("L'email est déjà utilisé");
        }

        // Vérifier que les mots de passe correspondent
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        // Créer l'utilisateur
        User user = User.builder()
            .username(registrationDto.getUsername())
            .email(registrationDto.getEmail())
            .password_hash(passwordEncoder.encode(registrationDto.getPassword()))
            .role(ROLE_USER.USER)
            .enabled(true)
            .created_at(new Date())
            .build();

        return userRepository.save(user);
    }

    @Transactional
    public void enableUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public void disableUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    public boolean isUserEnabled(String username) {
        return userRepository.findByUsername(username)
            .map(User::isEnabled)
            .orElse(false);
    }
}