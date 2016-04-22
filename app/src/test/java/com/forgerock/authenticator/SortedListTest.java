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

package com.forgerock.authenticator;

import com.forgerock.authenticator.model.SortedList;

import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;

public class SortedListTest {

    public final String[] elements = {"SECOND", "THIRD", "FIRST"};

    @Test
    public void shouldSortElementsAddedOneByOne() {
        SortedList<String> sortedList = new SortedList<>();

        for (String element : elements) {
            sortedList.add(element);
        }

        assertEquals(sortedList.get(0), "FIRST");
        assertEquals(sortedList.get(1), "SECOND");
        assertEquals(sortedList.get(2), "THIRD");
    }

    @Test
    public void shouldSortElementsAddedTogether() {
        SortedList<String> sortedList = new SortedList<>();

        sortedList.addAll(Arrays.asList(elements));

        assertEquals(sortedList.get(0), "FIRST");
        assertEquals(sortedList.get(1), "SECOND");
        assertEquals(sortedList.get(2), "THIRD");
    }
}
