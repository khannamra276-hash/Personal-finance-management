package com.financeapp.service;

import com.financeapp.dao.CategoryDao;
import com.financeapp.dao.UserDao;
import com.financeapp.model.Category;
import com.financeapp.model.CategoryType;
import com.financeapp.model.User;
import com.financeapp.util.InputValidator;
import com.financeapp.util.Session;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDao userDao;
    private final CategoryDao categoryDao;

    public AuthService() {
        this.userDao = new UserDao();
        this.categoryDao = new CategoryDao();
    }

    public AuthService(UserDao userDao, CategoryDao categoryDao) {
        this.userDao = userDao;
        this.categoryDao = categoryDao;
    }

    public User register(String name, String email, String password) throws SQLException, IllegalArgumentException {
        if (!InputValidator.isNotEmpty(name)) {
            throw new IllegalArgumentException("Full name cannot be empty.");
        }
        if (!InputValidator.isValidEmail(email)) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (!InputValidator.isStrongPassword(password)) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        if (userDao.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(10));
        User newUser = new User(name.trim(), email.trim().toLowerCase(), hashedPassword);
        User createdUser = userDao.create(newUser);

        seedDefaultCategories(createdUser.getId());
        return createdUser;
    }

    public User login(String email, String password) throws SQLException, IllegalArgumentException {
        if (!InputValidator.isValidEmail(email) || !InputValidator.isNotEmpty(password)) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Optional<User> userOpt = userDao.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOpt.get();
        boolean passwordMatch = BCrypt.checkpw(password, user.getPasswordHash());
        if (!passwordMatch) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        Session.setCurrentUser(user);
        return user;
    }

    public void logout() {
        Session.clear();
    }

    private void seedDefaultCategories(Long userId) {
        String[] incomeCategories = {"Salary", "Freelance", "Investments", "Other Income"};
        String[] expenseCategories = {"Housing & Rent", "Groceries & Food", "Utilities & Bills", "Transportation", "Entertainment", "Healthcare", "Shopping"};

        for (String name : incomeCategories) {
            try {
                categoryDao.create(new Category(userId, name, CategoryType.INCOME));
            } catch (SQLException ignored) {
            }
        }
        for (String name : expenseCategories) {
            try {
                categoryDao.create(new Category(userId, name, CategoryType.EXPENSE));
            } catch (SQLException ignored) {
            }
        }
    }
}

