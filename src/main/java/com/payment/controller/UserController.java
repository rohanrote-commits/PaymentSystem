package com.payment.controller;

import com.payment.dto.ReplaceEmailDto;
import com.payment.dto.ReplaceMobile;
import com.payment.entity.User;
import com.payment.exceptions.UserNotFoundException;
import com.payment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Tag(name = "Users", description = "Endpoints for managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
        summary = "Get user by ID",
        description = "Fetch user details using the unique account ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @GetMapping("/getUser/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) throws UserNotFoundException {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(
        summary = "Update user mobile number",
        description = "Replace the mobile number of a user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Mobile number updated successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Mobile number already exists")
        }
    )
    @PutMapping("/mobile")
    public ResponseEntity<User> updateUserMobile(@RequestBody ReplaceMobile mobile)
            throws UserNotFoundException{
        return ResponseEntity.ok(userService.replaceMobileNo(mobile));
    }

    @Operation(
        summary = "Update user email",
        description = "Replace the email address of a user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Email updated successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "400", description = "Email already registered")
        }
    )
    @PutMapping("/email")
    public ResponseEntity<User> updateUserEmail(@RequestBody ReplaceEmailDto emailDto)
            throws UserNotFoundException  {
        return ResponseEntity.ok(userService.replaceEmail(emailDto));
    }

    @Operation(
        summary = "Register a new user",
        description = "Adds a new user to the system",
        responses = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Mobile number or email already exists")
        }
    )
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) {
        userService.addUser(user);
        return ResponseEntity.ok(user);
    }
}
