package com.dztech.rayder.controller;

import com.dztech.rayder.dto.CreateDriverRequest;
import com.dztech.rayder.dto.DriverRequestAcknowledgement;
import com.dztech.rayder.dto.DriverRequestDetails;
import com.dztech.rayder.security.AuthenticatedUserProvider;
import com.dztech.rayder.service.DriverRequestService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/driver-requests")
public class DriverRequestController {

    private final DriverRequestService driverRequestService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DriverRequestController(
            DriverRequestService driverRequestService,
            AuthenticatedUserProvider authenticatedUserProvider) {
        this.driverRequestService = driverRequestService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @PostMapping
    public ResponseEntity<DriverRequestAcknowledgement> createRequest(
            @RequestBody @Valid CreateDriverRequest request) {
        Long userId = authenticatedUserProvider.getCurrentUserId();
        DriverRequestDetails details = driverRequestService.createDriverRequest(userId, request);
        DriverRequestAcknowledgement response =
                new DriverRequestAcknowledgement(true, "Driver request submitted", details);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
