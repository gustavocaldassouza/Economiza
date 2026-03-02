package com.example.economiza.domain.usecase;

import com.example.economiza.domain.model.Frequency;
import com.example.economiza.domain.model.RecurringPayment;
import com.example.economiza.domain.model.Transaction;
import com.example.economiza.domain.repository.RecurringPaymentRepository;
import com.example.economiza.domain.repository.TransactionRepository;

import java.util.Calendar;
import java.util.List;

/**
 * Scans all active recurring payments and, for each one whose
 * {@code nextDueDate}
 * is on or before today, automatically posts a {@link Transaction} and advances
 * {@code nextDueDate} to the next occurrence.
 *
 * <p>
 * This use case must run on a <b>background thread</b> (it performs synchronous
 * database I/O). Call it once after the vault is unlocked — typically from
 * {@link com.example.economiza.EconomizaApp#initDependencies}.
 */
public class ProcessRecurringPaymentsUseCase {

    private final RecurringPaymentRepository recurringRepo;
    private final TransactionRepository transactionRepo;

    public ProcessRecurringPaymentsUseCase(RecurringPaymentRepository recurringRepo,
            TransactionRepository transactionRepo) {
        this.recurringRepo = recurringRepo;
        this.transactionRepo = transactionRepo;
    }

    /**
     * Executes the auto-posting logic.
     *
     * @return the number of transactions that were automatically posted.
     */
    public int execute() {
        List<RecurringPayment> active = recurringRepo.getActiveRecurringPaymentsSync();
        if (active == null || active.isEmpty())
            return 0;

        long nowMs = System.currentTimeMillis();
        int posted = 0;

        for (RecurringPayment payment : active) {
            // Keep posting until nextDueDate is in the future (catches months of back-log)
            while (payment.nextDueDate <= nowMs) {
                // 1. Create and save an expense transaction
                Transaction tx = new Transaction();
                tx.description = payment.description;
                tx.amount = payment.amount;
                tx.categoryId = payment.categoryId;
                tx.isIncome = false;
                tx.timestamp = payment.nextDueDate;
                transactionRepo.insert(tx);
                posted++;

                // 2. Advance nextDueDate by one frequency period
                payment.nextDueDate = advanceDate(payment.nextDueDate, payment.frequency);
            }

            // 3. Persist the updated nextDueDate
            recurringRepo.update(payment);
        }

        return posted;
    }

    // ── Date advancement helpers ─────────────────────────────────────────────

    private long advanceDate(long fromMs, Frequency frequency) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(fromMs);

        switch (frequency) {
            case DAILY:
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case WEEKLY:
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case BIWEEKLY:
                cal.add(Calendar.WEEK_OF_YEAR, 2);
                break;
            case YEARLY:
                cal.add(Calendar.YEAR, 1);
                break;
            case MONTHLY:
            default:
                cal.add(Calendar.MONTH, 1);
                break;
        }
        return cal.getTimeInMillis();
    }
}
