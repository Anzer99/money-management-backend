package com.money.management.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.money.management.model.MoneyTransaction;
import com.money.management.model.TransactionType;
import com.money.management.model.User;
import com.money.management.repository.TransactionRepository;
import com.money.management.repository.UserRepository;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private static final String SESSION_USER_ID = "userId";

    private final TransactionRepository transactions;
    private final UserRepository users;

    public TransactionController(TransactionRepository transactions, UserRepository users) {
        this.transactions = transactions;
        this.users = users;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<TransactionView> list(HttpServletRequest request) {
        Long userId = requireUserId(request);
        return transactions.findByUser_IdOrderByDateDescIdDesc(userId).stream()
                .map(TransactionController::toView)
                .toList();
    }

    @PostMapping
    @Transactional
    public TransactionView create(@RequestBody TransactionRequest body, HttpServletRequest request) {
        Long userId = requireUserId(request);
        validate(body);
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in again."));
        MoneyTransaction saved = transactions.save(new MoneyTransaction(user, body.type(), body.amount(),
                body.category().trim(), cleanDescription(body.description()), body.date()));
        return toView(saved);
    }

    @PutMapping("/{id}")
    @Transactional
    public TransactionView update(@PathVariable Long id, @RequestBody TransactionRequest body,
                                  HttpServletRequest request) {
        Long userId = requireUserId(request);
        validate(body);
        MoneyTransaction existing = transactions.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));
        existing.update(body.type(), body.amount(), body.category().trim(),
                cleanDescription(body.description()), body.date());
        return toView(existing);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void delete(@PathVariable Long id, HttpServletRequest request,
                       jakarta.servlet.http.HttpServletResponse response) {
        Long userId = requireUserId(request);
        MoneyTransaction existing = transactions.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found."));
        transactions.delete(existing);
        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT);
    }

    private Long requireUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute(SESSION_USER_ID) instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please sign in.");
        }
        return userId;
    }

    private void validate(TransactionRequest body) {
        if (body == null || body.type() == null || body.amount() == null
                || body.category() == null || body.category().isBlank() || body.date() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Type, amount, category, and date are required.");
        }
        if (body.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero.");
        }
        if (body.amount().scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount can have at most two decimal places.");
        }
        if (body.category().trim().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category must be 40 characters or fewer.");
        }
        if (body.description() != null && body.description().length() > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description must be 200 characters or fewer.");
        }
    }

    private String cleanDescription(String description) {
        return description == null || description.isBlank() ? null : description.trim();
    }

    private static TransactionView toView(MoneyTransaction transaction) {
        return new TransactionView(transaction.getId(), transaction.getType(), transaction.getAmount(),
                transaction.getCategory(), transaction.getDescription(), transaction.getDate());
    }

    public record TransactionRequest(TransactionType type, BigDecimal amount, String category,
                                     String description, LocalDate date) {
    }

    public record TransactionView(Long id, TransactionType type, BigDecimal amount, String category,
                                  String description, LocalDate date) {
    }
}
