package com.vctek.orderservice.feignclient;

import com.vctek.dto.request.AssignCardParameter;
import com.vctek.dto.request.CheckValidCardParameter;
import com.vctek.health.VersionClient;
import com.vctek.kafka.data.loyalty.TransactionData;
import com.vctek.kafka.data.loyalty.TransactionRequest;
import com.vctek.orderservice.feignclient.dto.LoyaltyCardData;
import com.vctek.orderservice.feignclient.dto.RewardSettingData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@FeignClient(name = "${vctek.microservices.loyalty:loyalty-service}")
public interface LoyaltyClient extends VersionClient {

    @PostMapping("/cards/is-valid")
    Boolean isValidCardNumber(@RequestBody CheckValidCardParameter checkValidCardParam);

    @PostMapping("/customers/assign-card")
    ResponseEntity assignCard(@RequestBody AssignCardParameter assignCardParameter);

    @PostMapping("/cards/is-apply")
    Boolean isAppliedCardNumber(@RequestBody CheckValidCardParameter parameter);

    @PostMapping("/transaction/reward")
    TransactionData reward(@RequestBody TransactionRequest transactionRequest);

    @PostMapping("/transaction/reward-by-phone")
    TransactionData rewardByPhone(@RequestBody TransactionRequest transactionRequest);

    @PutMapping("/transaction/reward/{invoiceNumber}")
    TransactionData updateReward(@RequestBody TransactionRequest transactionRequest, @PathVariable("invoiceNumber") String invoiceNumber);

    @GetMapping("/cards/{cardNumber}")
    LoyaltyCardData getDetailByCardNumber(@PathVariable("cardNumber") String cardNumber,  @RequestParam("companyId") Long companyId);

    @PostMapping("/transaction/redeem")
    TransactionData redeem(@RequestBody TransactionRequest transactionRequest);

    @PutMapping("/transaction/redeem/{invoiceNumber}")
    TransactionData updateRedeem(@RequestBody TransactionRequest transactionRequest, @PathVariable("invoiceNumber") String invoiceNumber);

    @PutMapping("/transaction/redeem/{invoiceNumber}/pending")
    TransactionData updatePendingRedeem(@RequestBody TransactionRequest transactionRequest, @PathVariable("invoiceNumber") String invoiceNumber);

    @PostMapping("/transaction/revert")
    TransactionData revert(@RequestBody TransactionRequest transactionRequest);

    @PostMapping("/transaction/refund")
    TransactionData refund(@RequestBody TransactionRequest transactionRequest);

    @PutMapping("/transaction/revert/{invoiceNumber}")
    TransactionData updateRevert(@RequestBody TransactionRequest transactionRequest, @PathVariable("invoiceNumber") String invoiceNumber);

    @PutMapping("/transaction/refund/{invoiceNumber}")
    TransactionData updateRefund(@RequestBody TransactionRequest transactionRequest, @PathVariable("invoiceNumber") String invoiceNumber);

    @PostMapping("/transaction/{invoiceNumber}/history")
    TransactionData findByInvoiceNumberAndCompanyIdAndType(@RequestBody TransactionRequest transactionRequest,
                                                           @PathVariable("invoiceNumber") String invoiceNumber);
    @GetMapping("/reward-settings")
    RewardSettingData findRewardUnit(@RequestParam("companyId") Long companyId);

    @PostMapping("/transaction/convert-point")
    Double convertAmountToPoint(@RequestBody TransactionRequest transactionRequest);

    @PutMapping("/transaction/redeem/{invoiceNumber}/cancel")
    TransactionData cancelRedeem(@RequestBody TransactionRequest transactionRequest, @PathVariable("invoiceNumber") String invoiceNumber);

    @PostMapping("/transaction/redeem/pending")
    TransactionData createRedeemPending(@RequestBody TransactionRequest transactionRequest);

    @PutMapping("/transaction/redeem/{invoiceNumber}/complete-pending")
    TransactionData completePendingRedeem(@RequestBody TransactionRequest request,
                                          @PathVariable("invoiceNumber") String invoiceNumber);

}
