package com.jpmc.midascore.kafka;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-core"
    )
    @Transactional
    public void listen(Transaction transaction) {
        // Task 2: Listen + deserialize ONLY
        // System.out.println("Received transaction: " + transaction);

        //Task 3: Validate + Store InMemory

        //fetch user from repository -> Use Optional because the user may be null
        Optional<UserRecord> senderOpt =
                Optional.ofNullable(userRepository.findById(transaction.getSenderId()));

        Optional<UserRecord> receiverOpt =
                Optional.ofNullable(userRepository.findById(transaction.getRecipientId()));


        if(senderOpt.isEmpty() || receiverOpt.isEmpty()) return;

        UserRecord sender = senderOpt.get();
        UserRecord receiver = receiverOpt.get();

        //Check Balance
        if(sender.getBalance() < transaction.getAmount()) return ;

        //update balance
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        receiver.setBalance(receiver.getBalance() + transaction.getAmount());

        //Save Transactions
        TransactionRecord record = new TransactionRecord();
        record.setAmount(transaction.getAmount());
        record.setSender(sender);
        record.setRecipient(receiver);

        transactionRepository.save(record);
        userRepository.save(sender);
        userRepository.save(receiver);

        // Temporary for Finding the balance -> Issue with the debugger method.
//        UserRecord waldorf = userRepository.findByName("waldorf");
//        if (waldorf != null) {
//            System.out.println("WALDORF_BALANCE=" + waldorf.getBalance());
//        }


    }
}
