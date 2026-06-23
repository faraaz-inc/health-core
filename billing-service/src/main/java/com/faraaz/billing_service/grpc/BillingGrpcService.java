package com.faraaz.billing_service.grpc;

import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {
    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);

    @Override
    public void createBillingAccount(billing.BillingRequest BillingRequest,
                                     StreamObserver<billing.BillingResponse> responseObserver) {
        log.info("Create billing account request received:{}", BillingRequest.toString());

        //Business logic - eg., save to DB, perform calculations etc.

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("Complete")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
