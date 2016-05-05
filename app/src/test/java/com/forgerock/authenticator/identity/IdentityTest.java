/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 ForgeRock AS.
 */

package com.forgerock.authenticator.identity;

import com.forgerock.authenticator.mechanisms.MechanismCreationException;
import com.forgerock.authenticator.mechanisms.base.Mechanism;
import com.forgerock.authenticator.mechanisms.oath.Oath;
import com.forgerock.authenticator.storage.IdentityDatabase;
import com.forgerock.authenticator.storage.IdentityModel;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class IdentityTest {
    private IdentityModel model;
    private Identity identity;
    private IdentityDatabase identityDatabase;
    private Oath.OathBuilder MINIMUM_OATH_BUILDER;

    @Before
    public void setUp() throws MechanismCreationException {
        model = mock(IdentityModel.class);
        identityDatabase = mock(IdentityDatabase.class);
        given(model.getStorageSystem()).willReturn(identityDatabase);

        identity = Identity.builder().setIssuer("ForgeRock").setAccountName("test.user").build(model);
        MINIMUM_OATH_BUILDER = Oath.builder().setType("totp").setSecret("JMEZ2W7D462P3JYBDG2HV7PFBM").setMechanismUID("1");
    }

    @Test
    public void shouldEqualEquivalentIdentity() {
        Identity identityA = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);

        Identity identityB = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);

        assertEquals(identityA, identityB);
        assertEquals(identityA.compareTo(identityB), 0);
        assertEquals(identityB.compareTo(identityA), 0);
    }

    @Test
     public void shouldNotEqualDifferentIdentityWithIssuer() {
        Identity identityA = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);

        Identity identityB = Identity.builder()
                .setIssuer("ForgeRock1")
                .setAccountName("test.user")
                .build(model);

        assertFalse(identityA.equals(identityB));
        assertEquals(identityA.compareTo(identityB), -1);
        assertEquals(identityB.compareTo(identityA), 1);

    }

    @Test
    public void shouldNotEqualDifferentIdentityWithAccountName() {
        Identity identityA = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);

        Identity identityB = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user1")
                .build(model);

        assertFalse(identityA.equals(identityB));
        assertEquals(identityA.compareTo(identityB), -1);
        assertEquals(identityB.compareTo(identityA), 1);
    }

    @Test
    public void shouldHandleNullEquals() {
        Identity identityA = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);

        assertFalse(identityA.equals(null));
        assertEquals(identityA.compareTo(null), -1);
    }

    @Test
    public void saveShouldAddToDBIfNotStored() {
        Identity identity = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);
        identity.save();
        verify(identityDatabase).addIdentity(identity);
    }

    @Test
    public void saveShouldDoNothingIfStored() {
        Identity identity = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .setId(1)
                .build(model);
        identity.save();

        verifyZeroInteractions(identityDatabase);
    }

    @Test
    public void deleteShouldRemoveFromDBIfStored() {
        Identity identity = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .setId(1)
                .build(model);
        identity.delete();
        verify(identityDatabase).deleteIdentity(1);
    }

    @Test
    public void deleteShouldDoNothingIfNotStored() {
        Identity identity = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);
        identity.delete();

        verifyZeroInteractions(identityDatabase);
    }

    @Test
    public void canAddMechanism() throws Exception {
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(1l);
        Mechanism.PartialMechanismBuilder mechanismBuilder = MINIMUM_OATH_BUILDER;
        Mechanism mechanism = identity.addMechanism(mechanismBuilder);
        assertTrue(identity.getMechanisms().contains(mechanism));
    }

    @Test
    public void canRemoveMechanism() throws Exception {
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(1l);
        Mechanism.PartialMechanismBuilder mechanismBuilder = MINIMUM_OATH_BUILDER;
        Mechanism mechanism = identity.addMechanism(mechanismBuilder);

        identity.removeMechanism(mechanism);
        assertTrue(identity.getMechanisms().isEmpty());
        verify(model, times(1)).removeIdentity(identity);
    }

    @Test
    public void shouldGenerateCorrectOpaqueReference() {
        List<String> opaqueReference = identity.getOpaqueReference();
        assertEquals(opaqueReference.get(0), "ForgeRock:test.user");
    }

    @Test
    public void shouldConsumeCorrectOpaqueReference() {
        ArrayList<String> opaqueReference = new ArrayList<>();
        opaqueReference.add("ForgeRock:test.user");
        assertTrue(identity.consumeOpaqueReference(opaqueReference));
        assertEquals(opaqueReference.size(), 0);
    }

    @Test
    public void shouldNotConsumeIncorrectOpaqueReference() {
        ArrayList<String> opaqueReference = new ArrayList<>();
        opaqueReference.add("ForgeRock:test.user");
        Identity otherIdentity = Identity.builder().setIssuer("OtherIssuer").setAccountName("test.user").build(model);

        assertFalse(otherIdentity.consumeOpaqueReference(opaqueReference));
        assertEquals(opaqueReference.get(0), "ForgeRock:test.user");
    }

    @Test
    public void shouldValidateIfStoredAndMechanismsValidate() throws Exception {
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(1l);
        Mechanism.PartialMechanismBuilder mechanismBuilder = MINIMUM_OATH_BUILDER.setId(1);
        Identity identity = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .setId(1)
                .setMechanisms(Arrays.asList(mechanismBuilder))
                .build(model);

        assertTrue(identity.validate());
    }

    @Test
    public void shouldNotValidateIfNotStored() throws Exception {
        Identity identity = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .build(model);

        assertFalse(identity.validate());
    }

    @Test
    public void shouldNotValidateIfMechanismsDoNotValidate() throws Exception {
        given(identityDatabase.addMechanism(any(Mechanism.class))).willReturn(-1l);
        Mechanism.PartialMechanismBuilder mechanismBuilder = MINIMUM_OATH_BUILDER;
        Identity identity = Identity.builder()
                .setIssuer("ForgeRock")
                .setAccountName("test.user")
                .setId(1)
                .build(model);
        identity.addMechanism(mechanismBuilder);

        assertFalse(identity.validate());
    }

}
