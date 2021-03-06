/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.repository.integration;

import com.microsoft.azure.spring.data.cosmosdb.core.CosmosTemplate;
import com.microsoft.azure.spring.data.cosmosdb.domain.Customer;
import com.microsoft.azure.spring.data.cosmosdb.repository.TestRepositoryConfig;
import com.microsoft.azure.spring.data.cosmosdb.repository.repository.CustomerRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import lombok.NonNull;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class CustomerRepositoryIT {

    private static final String USER_NAME_0 = "username-0";
    private static final String USER_NAME_1 = "username-1";
    private static final String FAKE_USER_NAME = "username-fake";

    private static final Long USER_AGE_0 = 34L;
    private static final Long USER_AGE_1 = 45L;

    private static final String CUSTOMER_ID_0 = "id-0";
    private static final String CUSTOMER_ID_1 = "id-1";
    private static final String CUSTOMER_ID_2 = "id-2";

    private static final Long CUSTOMER_LEVEL_0 = 1L;
    private static final Long CUSTOMER_LEVEL_1 = 2L;

    private static final Customer.User USER_0 = new Customer.User(USER_NAME_0, USER_AGE_0);
    private static final Customer.User USER_1 = new Customer.User(USER_NAME_1, USER_AGE_1);
    private static final Customer.User USER_2 = new Customer.User(USER_NAME_0, USER_AGE_1);

    private static final Customer CUSTOMER_0 = new Customer(CUSTOMER_ID_0, CUSTOMER_LEVEL_0, USER_0);
    private static final Customer CUSTOMER_1 = new Customer(CUSTOMER_ID_1, CUSTOMER_LEVEL_1, USER_1);
    private static final Customer CUSTOMER_2 = new Customer(CUSTOMER_ID_2, CUSTOMER_LEVEL_1, USER_2);

    private static final CosmosEntityInformation<Customer, String> entityInformation =
            new CosmosEntityInformation<>(Customer.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CustomerRepository repository;

    @Autowired
    private CosmosTemplate template;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        this.repository.saveAll(Arrays.asList(CUSTOMER_0, CUSTOMER_1, CUSTOMER_2));
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        this.repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    private void assertCustomerListEquals(@NonNull List<Customer> customers, @NonNull List<Customer> reference) {
        Assert.assertEquals(reference.size(), customers.size());

        customers.sort(Comparator.comparing(Customer::getId));
        reference.sort(Comparator.comparing(Customer::getId));

        Assert.assertEquals(reference, customers);
    }

    @Test
    public void testFindByUserAndLevel() {
        final List<Customer> references = Arrays.asList(CUSTOMER_0, CUSTOMER_2);
        List<Customer> customers = this.repository.findByUser_Name(USER_NAME_0);

        assertCustomerListEquals(references, customers);

        customers = this.repository.findByUser_Name(FAKE_USER_NAME);

        Assert.assertTrue(customers.isEmpty());
    }

}
