/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.repository.integration;

import com.microsoft.azure.spring.data.cosmosdb.common.TestUtils;
import com.microsoft.azure.spring.data.cosmosdb.core.CosmosTemplate;
import com.microsoft.azure.spring.data.cosmosdb.domain.inheritance.Square;
import com.microsoft.azure.spring.data.cosmosdb.repository.TestRepositoryConfig;
import com.microsoft.azure.spring.data.cosmosdb.repository.repository.SquareRepository;
import com.microsoft.azure.spring.data.cosmosdb.repository.support.CosmosEntityInformation;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestRepositoryConfig.class)
public class SquareRepositoryIT {
    private Square square1 = new Square("id_1", 1, 1);
    private Square square2 = new Square("id_2", 2, 4);

    private static final CosmosEntityInformation<Square, String> entityInformation =
            new CosmosEntityInformation<>(Square.class);

    private static CosmosTemplate staticTemplate;
    private static boolean isSetupDone;

    @Autowired
    private CosmosTemplate template;

    @Autowired
    private SquareRepository repository;

    @Before
    public void setUp() {
        if (!isSetupDone) {
            staticTemplate = template;
            template.createContainerIfNotExists(entityInformation);
        }
        repository.save(square1);
        repository.save(square2);
        isSetupDone = true;
    }

    @After
    public void cleanup() {
        repository.deleteAll();
    }

    @AfterClass
    public static void afterClassCleanup() {
        staticTemplate.deleteContainer(entityInformation.getContainerName());
    }

    @Test
    public void testFindAll() {
        final List<Square> result = TestUtils.toList(repository.findAll());

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void testFindIncludeInheritedFields() {
        final Optional<Square> result = repository.findById(square1.getId());

        assertThat(result.get()).isNotNull();
        assertThat(result.get().getId().equals(square1.getId()));
        assertThat(result.get().getLength()).isEqualTo(square1.getLength());
        assertThat(result.get().getArea()).isEqualTo(square1.getArea());
    }
}
