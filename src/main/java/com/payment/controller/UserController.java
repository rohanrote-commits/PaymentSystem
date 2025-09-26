package com.payment.controller;

import com.payment.dto.ReplaceEmailDto;
import com.payment.dto.ReplaceMobile;
import com.payment.entity.User;
import com.payment.exceptions.EmailAlreadyRegistered;
import com.payment.exceptions.MobileNumberAlreadyExist;
import com.payment.exceptions.UserNotFoundException;
import com.payment.service.UserService;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Data
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/getUser/{id}")
    public ResponseEntity<User> getUser(@PathVariable long id) throws UserNotFoundException {
        return new ResponseEntity<>(userService.getUserById(id), HttpStatus.OK);
    }



    @PutMapping("/mobile")
    public ResponseEntity<User> updateUserMobile(@RequestBody ReplaceMobile mobile) throws UserNotFoundException, MobileNumberAlreadyExist {
        return new ResponseEntity<>(userService.replaceMobileNo(mobile), HttpStatus.OK);
    }

    @PutMapping("/email")
    public ResponseEntity<User> updateUserEmail(@RequestBody ReplaceEmailDto emailDto) throws UserNotFoundException {
        return new ResponseEntity<>(userService.replaceEmail(emailDto), HttpStatus.OK);
    }

    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody User user) throws MobileNumberAlreadyExist, EmailAlreadyRegistered {
         userService.addUser(user);
        return new ResponseEntity<>(user,HttpStatus.OK);
    }

}
