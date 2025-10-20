package apartment.example.backend.controller;

import apartment.example.backend.dto.LoginRequest;
import apartment.example.backend.dto.LoginResponse;
import apartment.example.backend.dto.RegisterRequest;
import apartment.example.backend.dto.RegisterResponse;
import apartment.example.backend.entity.User;
import apartment.example.backend.repository.UserRepository;
import apartment.example.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Why: สร้าง Endpoint สำหรับการ Login
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            // Why: ใช้ AuthenticationManager เพื่อตรวจสอบ username/password ที่ส่งมา
            // ถ้าไม่ถูกต้อง ส่วนนี้จะโยน Exception ออกมาทันที
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            // Why: ถ้าการยืนยันตัวตนล้มเหลว ให้ส่ง HTTP Status 401 Unauthorized กลับไป
            // ซึ่งเป็นวิธีที่ถูกต้องในการแจ้งว่าข้อมูล Login ไม่ถูกต้อง
            return ResponseEntity.status(401).body("Incorrect username or password");
        }

        // Why: ถ้า authenticate ผ่าน ให้โหลดข้อมูล UserDetails
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        // Cast to User to get role information
        final User user = (User) userDetails;
        // Why: สร้าง JWT Token จาก UserDetails
        final String token = jwtUtil.generateToken(userDetails);

        // Why: ส่ง Token, role, และ username กลับไปให้ Client พร้อมกับ HTTP Status 200 OK
        return ResponseEntity.ok(new LoginResponse(token, user.getRole().toString(), user.getUsername()));
    }

    // Register new user endpoint
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Registration request received:");
            System.out.println("Username: " + registerRequest.getUsername());
            System.out.println("Email: " + registerRequest.getEmail());
            System.out.println("Password length: " + (registerRequest.getPassword() != null ? registerRequest.getPassword().length() : "null"));
            
            // Check if username already exists
            if (!userRepository.findByUsername(registerRequest.getUsername()).isEmpty()) {
                System.out.println("Username already exists: " + registerRequest.getUsername());
                return ResponseEntity.status(400).body("Username already exists");
            }

            // Check if email already exists
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                System.out.println("Email already exists: " + registerRequest.getEmail());
                return ResponseEntity.status(400).body("Email already exists");
            }

            // Create new user with USER role
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setEmail(registerRequest.getEmail());
            newUser.setRole(User.Role.USER); // New users get USER role by default

            // Save user to database
            User savedUser = userRepository.save(newUser);
            System.out.println("User registered successfully: " + savedUser.getUsername());

            return ResponseEntity.ok(new RegisterResponse("User registered successfully", newUser.getUsername()));

        } catch (Exception e) {
            System.out.println("Registration failed with exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Registration failed: " + e.getMessage());
        }
    }
}