package com.money.management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.money.management.model.MoneyTransaction;

public interface TransactionRepository extends JpaRepository<MoneyTransaction, Long> {
    List<MoneyTransaction> findByUser_IdOrderByDateDescIdDesc(Long userId);

    Optional<MoneyTransaction> findByIdAndUser_Id(Long id, Long userId);
}
