package com.crypto_wallet.service;

import java.time.LocalDate;

import com.crypto_wallet.dto.WalletEvaluationDTO;

/**
 * Service class for evaluating the performance of a user's wallet.
 * This class provides methods to calculate the total value of the wallet,
 * the best and worst performing assets, and their respective performances.
 */
public interface EvaluationService {

    WalletEvaluationDTO getWalletEvaluation(String email, LocalDate date);
}
