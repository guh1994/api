package com.customer.api.service;

import com.customer.api.model.PersistentCustomer;
import com.customer.api.repository.CustomerRepository;
import com.customer.api.rest.RestCustomer;
import com.customer.api.validator.RestEntityResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceImplTest {

    public static final String NAME = "Gustavo";
    public static final String EMAIL = "gustavo@gmail.com";
    public static final int CUSTOMER_ID = 1;
    @InjectMocks
    private CustomerServiceImpl subject;

    @Mock
    private CustomerRepository repository;

    @Mock
    private PersistentCustomer persistentCustomer;

    @Mock
    private PersistentCustomer updatedCustomer;

    @BeforeEach
    public void setup() {
        when(persistentCustomer.name()).thenReturn(NAME);
        when(persistentCustomer.email()).thenReturn(EMAIL);
        when(repository.findAll()).thenReturn(List.of(persistentCustomer));
        when(repository.findCustomerById(CUSTOMER_ID)).thenReturn(persistentCustomer);
        when(repository.save(any())).thenReturn(persistentCustomer);
        when(repository.findById(CUSTOMER_ID)).thenReturn(Optional.of(persistentCustomer));
        when(repository.findCustomerByEmail(EMAIL)).thenReturn(persistentCustomer);

    }

    @Test
    public void shouldGetCustomers() {

        List<RestCustomer> customers = subject.getCustomers();

        assertEquals(1, customers.size());
        RestCustomer restCustomer = customers.get(0);

        assertEquals(NAME, restCustomer.name());
        assertEquals(EMAIL, restCustomer.email());
    }

    @Test
    public void shouldGetEmptyCustomers() {
        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<RestCustomer> customers = subject.getCustomers();
        assertEquals(0, customers.size());
    }

    @Test
    public void shouldGetCustomerById() {
        RestEntityResponse<RestCustomer> restEntityResponse = subject.getCustomerByEmail(persistentCustomer.email());

        RestCustomer restCustomer = restEntityResponse.entity();

        assertEquals(NAME, restCustomer.name());
        assertEquals(EMAIL, restCustomer.email());
    }


    @Test
    public void shouldGetErrorMessageWhenCustomerNotFound() {

        RestEntityResponse<RestCustomer> restEntityResponse = subject.getCustomerByEmail("gustavo@hotmail.com");

        assertNull(restEntityResponse.entity());
        assertEquals(List.of("Customer not found"), restEntityResponse.messages());
    }

    @Test
    public void shouldCreateCustomer() {

        RestCustomer customer = new RestCustomer(persistentCustomer.name(), persistentCustomer.email());

        RestEntityResponse<RestCustomer> restEntityResponse = subject.createCustomer(customer);

        assertNotNull(restEntityResponse.entity());
        assertEquals(RestEntityResponse.createSuccess(customer), restEntityResponse);

    }

    @Test
    public void shouldValidateIfEmailCustomerExistsBeforeCreate() {
        when(repository.existsByEmail(persistentCustomer.email())).thenReturn(true);

        RestCustomer restCustomer = new RestCustomer(persistentCustomer.name(), persistentCustomer.email());
        RestEntityResponse<RestCustomer> restEntityResponse = subject.createCustomer(restCustomer);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("Customer allready exist with this email"), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());

    }

    @Test
    public void shouldReturnErrorMessageWhenCustomerNameIsNullIWhenCreateCustomer() {

        RestCustomer restCustomer = new RestCustomer(null, persistentCustomer.email());
        RestEntityResponse<RestCustomer> restEntityResponse = subject.createCustomer(restCustomer);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O nome está vazio"), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());
    }

    @Test
    public void ShouldReturnErrorMessageWhenCustomerEmailIsNullWhenCreateCustomer() {

        RestCustomer restCustomer = new RestCustomer(persistentCustomer.name(), null);
        RestEntityResponse<RestCustomer> restEntityResponse = subject.createCustomer(restCustomer);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O email está vazio"), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());
    }

    @Test
    public void shouldReturnErrorMessageWhenCustomerEmailIsInvalid() {
        RestCustomer restCustomer = new RestCustomer(persistentCustomer.name(), "roberto");
        RestEntityResponse<RestCustomer> restEntityResponse = subject.createCustomer(restCustomer);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O email está inválido"), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());

    }

    @Test
    public void shouldReturnErrorMessageWhenCustomerNameIsInvalid() {
        RestCustomer restCustomer = new RestCustomer("ab", persistentCustomer.email());
        RestEntityResponse<RestCustomer> restEntityResponse = subject.createCustomer(restCustomer);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O nome está inválido"), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());

    }

    @Test
    public void shouldReturnErrorMessageWhenCustomerIsNullWhenCreateCustomer() {
        RestEntityResponse<RestCustomer> restEntityResponse = subject.createCustomer(null);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O Payload está nulo."), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());

    }

    @Test
    public void shouldUpdateCustomer() {

        RestCustomer customer = new RestCustomer("Roberto", "roberto@gmail.com");

        when(repository.save(persistentCustomer)).thenReturn(updatedCustomer);
        when(updatedCustomer.name()).thenReturn(customer.name());
        when(updatedCustomer.email()).thenReturn(customer.email());

        RestEntityResponse<RestCustomer> restEntityResponse = subject.updateCustomer(customer, persistentCustomer.email());

        verify(persistentCustomer).update(customer);
        verify(repository).save(persistentCustomer);

        assertTrue(restEntityResponse.success());
        assertEquals(customer, restEntityResponse.entity());
        assertTrue(restEntityResponse.messages().isEmpty());
    }

    @Test
    public void shouldReturnErrorMessageIfCustomerNotExistsWhenUpdateCustomer() {
        RestCustomer restCustomer = new RestCustomer(persistentCustomer.name(), persistentCustomer.email());

        RestEntityResponse<RestCustomer> restEntityResponse = subject.updateCustomer(restCustomer, "gustavo@hotmail.com");

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("Customer with email %s not exists".formatted("gustavo@hotmail.com")), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());
    }

    @Test
    public void shouldReturnErrorMessageIfCustomerAndIdIsNull() {

        RestEntityResponse<RestCustomer> restEntityResponse = subject.updateCustomer(null, null);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O Payload está nulo."), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());
    }

    @Test
    public void shouldReturnErrorMessageIfCustomerEmailIsNull() {

        RestCustomer restCustomer = new RestCustomer(persistentCustomer.name(), null);
        RestEntityResponse<RestCustomer> restEntityResponse = subject.updateCustomer(restCustomer, null);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O email está vazio"), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());
    }

    @Test
    public void shouldReturnErrorMessageIfCustomerNameIsNull() {

        RestCustomer restCustomer = new RestCustomer(null, persistentCustomer.email());
        RestEntityResponse<RestCustomer> restEntityResponse = subject.updateCustomer(restCustomer, null);

        assertFalse(restEntityResponse.success());
        assertEquals(List.of("O nome está vazio"), restEntityResponse.messages());
        assertNull(restEntityResponse.entity());
    }


    @Test
    public void shouldDeleteCustomer() {
        RestEntityResponse<RestCustomer> restEntityResponse = subject.deleteCustomer(CUSTOMER_ID);

        assertNull(restEntityResponse.entity());
        assertEquals(List.of("Customer Deleted with success"), restEntityResponse.messages());
        Mockito.verify(repository).deleteById(CUSTOMER_ID);

    }

    @Test
    public void shouldReturnErrorMessagesWhenCustomerIdIsNullInDeletion() {
        RestEntityResponse<RestCustomer> restEntityResponse = subject.deleteCustomer(null);

        assertEquals(List.of("Customer deletion id is null."), restEntityResponse.messages());
        Mockito.verify(repository, never()).deleteById(any());
    }


    @Test
    public void shouldThrowException() {

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> subject.throwExceptionForTest());

        assertTrue(runtimeException.getMessage().contains("Test..."));

    }


}
