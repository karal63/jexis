package com.jexis.jexis_backend.stripe.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jexis.jexis_backend.common.web.error.DomainException;
import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.net.RequestOptions;

/**
 * StripeController
 *
 * REST controller in the presentation layer responsible for exposing
 * stripe-related HTTP endpoints.
 *
 * It handles request routing, input validation, and response mapping,
 * delegating all business logic execution to dedicated stripe use case
 * services (application layer).
 *
 * This class does not contain domain logic; its role is limited to
 * orchestrating request/response flow between the client and the
 * application layer.
 *
 * @Author: Leo
 */
@Controller
@RequestMapping("")
public class StripeController {

    private final StripeClient client;

    public StripeController(StripeClient client) {
        this.client = client;
    }

    @PostMapping("test")
    public ResponseEntity<Account> test() {
        try {
            Account account = client.v1().accounts().retrieve("acct_1TafqCLGRmU4Cdd");
            return ResponseEntity.status(200).body(account);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}

// test Exception
// test if same effect with no try catch