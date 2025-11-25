package com.pm.billingservice.billing_service.grpc;
import net.devh.boot.grpc.server.service.GrpcService;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import io.grpc.stub.StreamObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biling.BillingRequest;
import billing.BillingResponse;


@GrpcService
public class BillingGrpcService extends BillingServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(BillingGrpcService.class);
    

    @Override
    public void createBillingAccount(BillingRequest billingRequest, StreamObserver<BillingResponse> responseObserver) {

        log.info("creatingBillingAccount request received {}", billingRequest.toString());

        // Business Logic - eg save to db, perform calculations etc

        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId("12345")
                .setStatus("ACTIVE")
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();

    }
}
