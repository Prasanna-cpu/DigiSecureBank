package com.digisecurebank.transaction_service.mapper;


import com.digisecurebank.transaction_service.dto.TransactionDTO;
import com.digisecurebank.transaction_service.entity.Transaction;

public class TransactionMapper {

    public static TransactionDTO mapToTransactionDTO(Transaction transaction){
        return new TransactionDTO(
                transaction.getId(),
                transaction.getSenderAccountNumber(),
                transaction.getReceiverAccountNumber(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getFailureReason(),
                transaction.getReferenceNumber()
        );
    }

    public static Transaction mapToTransaction(TransactionDTO transactionDTO){

        Transaction transaction = new Transaction();

        if(transactionDTO.getId() != null){
            transaction.setId(transactionDTO.getId());
        }

        if(transactionDTO.getReferenceNumber() != null){
            transaction.setReferenceNumber(transactionDTO.getReferenceNumber());
        }

        transaction.setReceiverAccountNumber(transactionDTO.getReceiverAccountNumber());
        transaction.setSenderAccountNumber(transactionDTO.getSenderAccountNumber());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setType(transactionDTO.getType());
        transaction.setStatus(transactionDTO.getStatus());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setFailureReason(transactionDTO.getFailureReason());

        return transaction;


    }

}
