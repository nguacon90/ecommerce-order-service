package com.vctek.orderservice.strategy.impl;

import com.vctek.orderservice.model.AbstractOrderEntryModel;
import com.vctek.orderservice.model.SubOrderEntryModel;
import com.vctek.orderservice.service.CartService;
import com.vctek.util.ComboType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class EntryMergeStrategyTest {
    private DefaultEntryMergeStrategy entryMergeStrategy;
    @Mock
    private CartService cartService;
    private List<AbstractOrderEntryModel> candidateEntries;
    @Mock
    private AbstractOrderEntryModel newEntry;
    @Mock
    private AbstractOrderEntryModel candidateEntry1;
    @Mock
    private AbstractOrderEntryModel candidateEntry2;
    @Mock
    private SubOrderEntryModel subEntry1;
    @Mock
    private SubOrderEntryModel subEntry2;
    @Mock
    private SubOrderEntryModel candidateSubEntry1;
    @Mock
    private SubOrderEntryModel candidateSubEntry2;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        candidateEntries = new ArrayList<>();
        entryMergeStrategy = new DefaultEntryMergeStrategy();
        entryMergeStrategy.setCartService(cartService);
        when(newEntry.isGiveAway()).thenReturn(false);
        when(cartService.isComboEntry(newEntry)).thenReturn(false);
        when(candidateEntry1.getId()).thenReturn(1l);
        when(candidateEntry1.getQuantity()).thenReturn(2l);
        when(candidateEntry2.getId()).thenReturn(2l);
        when(candidateEntry2.getQuantity()).thenReturn(2l);

        when(newEntry.getId()).thenReturn(3l);
        when(newEntry.getQuantity()).thenReturn(2l);
        candidateEntries.add(candidateEntry1);
        candidateEntries.add(candidateEntry2);
    }

    @Test
    public void emptyCandidateEntries() {
        candidateEntries = new ArrayList<>();
        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryIsGiveAway() {
        when(newEntry.isGiveAway()).thenReturn(true);
        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryNotCombo_NotExistedCandidateEntry() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(23l);

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryNotCombo_ExistedCandidateEntryButGiveAway() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(11l);
        when(candidateEntry2.isGiveAway()).thenReturn(true);

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryNotCombo_ExistedCandidateEntry() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(11l);

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertEquals(candidateEntry2, candidateEntry);
    }

    @Test
    public void newEntryCombo_ExistedComboCandidateEntryButDifferentComboType() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(newEntry.getComboType()).thenReturn(ComboType.FIXED_COMBO.toString());
        when(cartService.isComboEntry(newEntry)).thenReturn(true);
        when(newEntry.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(11l);
        when(candidateEntry2.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(cartService.isComboEntry(candidateEntry2)).thenReturn(true);
        when(candidateEntry2.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(candidateSubEntry1)));

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryCombo_ExistedComboCandidateEntryButNotTheSameProductSize() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(newEntry)).thenReturn(true);
        when(newEntry.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(candidateEntry2)).thenReturn(true);
        when(candidateEntry2.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(candidateSubEntry1)));

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryCombo_ExistedComboCandidateEntry_ButNotTheSameSubEntryProduct() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(newEntry)).thenReturn(true);
        when(subEntry1.getProductId()).thenReturn(1111l);
        when(subEntry1.getQuantity()).thenReturn(2);
        when(subEntry2.getProductId()).thenReturn(1122l);
        when(subEntry2.getQuantity()).thenReturn(2);
        when(newEntry.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(candidateEntry2)).thenReturn(true);
        when(candidateSubEntry1.getProductId()).thenReturn(1111l);
        when(candidateSubEntry1.getQuantity()).thenReturn(2);
        when(candidateSubEntry2.getProductId()).thenReturn(1133l);
        when(candidateSubEntry2.getQuantity()).thenReturn(2);
        when(candidateEntry2.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(candidateSubEntry1, candidateSubEntry2)));

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryCombo_ExistedComboCandidateEntry_TheSameSubEntryProduct_ButNotTheSameUnitQtyOfProduct() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(newEntry)).thenReturn(true);
        when(newEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(newEntry.getQuantity()).thenReturn(1l);
        when(subEntry1.getProductId()).thenReturn(1111l);
        when(subEntry1.getQuantity()).thenReturn(2);
        when(subEntry2.getProductId()).thenReturn(1122l);
        when(subEntry2.getQuantity()).thenReturn(2);
        when(newEntry.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(candidateEntry2)).thenReturn(true);
        when(candidateEntry2.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(candidateEntry2.getQuantity()).thenReturn(1l);
        when(candidateSubEntry1.getProductId()).thenReturn(1111l);
        when(candidateSubEntry1.getQuantity()).thenReturn(3);
        when(candidateSubEntry2.getProductId()).thenReturn(1122l);
        when(candidateSubEntry2.getQuantity()).thenReturn(1);
        when(candidateEntry2.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(candidateSubEntry1, candidateSubEntry2)));

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertNull(candidateEntry);
    }

    @Test
    public void newEntryCombo_ExistedComboCandidateEntry_TheSameSubEntryProduct_TheSameUnitQtyOfProduct() {
        when(newEntry.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(newEntry)).thenReturn(true);
        when(newEntry.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(newEntry.getQuantity()).thenReturn(1l);
        when(subEntry1.getProductId()).thenReturn(1111l);
        when(subEntry1.getQuantity()).thenReturn(2);
        when(subEntry2.getProductId()).thenReturn(1122l);
        when(subEntry2.getQuantity()).thenReturn(2);
        when(newEntry.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(subEntry1, subEntry2)));

        when(candidateEntry1.getProductId()).thenReturn(22l);
        when(candidateEntry2.getProductId()).thenReturn(11l);
        when(cartService.isComboEntry(candidateEntry2)).thenReturn(true);
        when(candidateEntry2.getComboType()).thenReturn(ComboType.ONE_GROUP.toString());
        when(candidateEntry2.getQuantity()).thenReturn(2l);
        when(candidateSubEntry1.getProductId()).thenReturn(1111l);
        when(candidateSubEntry1.getQuantity()).thenReturn(4);
        when(candidateSubEntry2.getProductId()).thenReturn(1122l);
        when(candidateSubEntry2.getQuantity()).thenReturn(4);
        when(candidateEntry2.getSubOrderEntries()).thenReturn(new HashSet<>(Arrays.asList(candidateSubEntry1, candidateSubEntry2)));

        AbstractOrderEntryModel candidateEntry = entryMergeStrategy.getEntryToMerge(candidateEntries, newEntry);
        assertEquals(candidateEntry2, candidateEntry);
    }
}
