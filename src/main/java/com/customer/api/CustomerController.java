package com.customer.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
public class CustomerController {

    @Autowired
    private CustomerService service;

    @GetMapping(value = "/customers")
    public ResponseEntity<List<CustomerPersistent>> getCustomers() {

        List<CustomerPersistent> customers = service.getCustomers();

        return new ResponseEntity<>(customers,HttpStatus.OK);

    }

    @GetMapping(value = "/customer/{id}")
    public ResponseEntity<Optional<CustomerPersistent>> getCustomerById(@PathVariable Integer id) {

        Optional<CustomerPersistent> customer = service.getCustomerById(id);

        return new ResponseEntity<>(customer, HttpStatus.OK);

    }

    @PostMapping(value = "/customer/create")
    public ResponseEntity<String> createCustomer(@RequestBody CustomerPersistent customer) {

        service.createCustomer(customer);

        return new ResponseEntity<>("Created Success",HttpStatus.CREATED);
    }

    @PutMapping(value = "/customer/update/{id}")
    public ResponseEntity<Optional<CustomerPersistent>> updateCustomer(@PathVariable Integer id, @RequestBody CustomerPersistent customer) {

        Optional<CustomerPersistent> customerUpdated = service.updateCustomer(customer, id);

        return new ResponseEntity<>(customerUpdated, HttpStatus.OK);
    }

    @DeleteMapping(value = "/customer/delete/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Integer id) {

        service.deleteCustomer(id);

        return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }


}
